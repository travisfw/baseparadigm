package org.baseparadigm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.HasCidScheme;
import org.baseparadigm.i.HasContentId;
import org.baseparadigm.i.Repo;
import org.baseparadigm.i.ResolvableId;
import org.baseparadigm.i.Stuffable;
import org.baseparadigm.i.Stuffed;
import org.baseparadigm.i.ToByteArray;

import static org.baseparadigm.Util.repoGet;

public class MapDatum implements SortedMap<ContentId, SetDatum>, ToByteArray, HasContentId, Stuffable, Cloneable {
    public SortedMap<ContentId, SetDatum> backingMap;
    public CidScheme cidScheme;
    public ResolvableId cachedId = null;
    
    // note how isMutable and initialized are used in constructors
    protected boolean isMutable = true;
    protected boolean initialized = false;
    
    public MapDatum(Stuffed datumId) {
        this.cidScheme = datumId.getCidScheme();
        assert cidScheme != null;
        isMutable = false;
        initialized = false;
        init(datumId);
        assert backingMap != null; 
        assert initialized == true;
    }
    
    /**
     * Mutable, for building; call buildFinish() to make immutable.
     * @param baseParadigm
     */
    public MapDatum(CidScheme scheme) {
        cidScheme = scheme;
        assert cidScheme != null;
        backingMap = new TreeMap<ContentId, SetDatum>();
        isMutable = true;
        initialized = true;
    }
    
    /**
     * Same as {@link MapDatum#MapDatum(CidScheme)}. A reference to the
     * {@link Repo} is not kept.
     * 
     * @param baseParadigm
     */
    public MapDatum(Repo repo) {
        cidScheme = repo.getCidScheme();
        assert cidScheme != null;
        backingMap = new TreeMap<ContentId, SetDatum>();
        isMutable = true;
        initialized = true;
    }
    
    /**
     * Make an uninitialized MapDatum; use init(Stuffed) next.
     */
    public MapDatum() {
        backingMap = null;
        isMutable = false;
        initialized = false;
    }
    
    /**
     * this constructor used by {@link MapDatum#clone()}
     * @param map The content to populate the map datum.
     * @param isMutable whether the resulting MapDatum is mutable
     */
    public MapDatum(CidScheme cids, SortedMap<ContentId, SetDatum> map, boolean isMutable) {
        assert (! (map instanceof HasCidScheme)) || ((MapDatum) map).cidScheme.equals(cids);
        cidScheme = cids;
        assert map.size() == 0 || map.keySet().iterator().next().getCidScheme().equals(cidScheme);
        if (map instanceof MapDatum)
            this.backingMap = ((MapDatum)map).backingMap;
        else
            this.backingMap = map;
        this.isMutable = isMutable;
        this.initialized = true;
    }

    /**
     * Similar to using the zero arg constructor and
     * {@link MapDatum#init(Stuffed)}, but specifies where to search for content
     * instead of bundling all necessary content in a {@link Stuffed}.
     * 
     * @param repo
     *            where to look up the given content id and nested content ids
     *            as well
     * @param cid
     *            this is the same thing {@link Stuffed#getTopId()} would return
     *            if you were initializing a MapDatum using a Stuffed.
     */
    public MapDatum(Repo repo, ContentId cid) {
        // TODO
        throw new Error("unimplemented");
    }

    /**
     * The ContentId for the string becomes the key for the given set of values. The previous set of values is discarded.
     * @return this
     */
    public MapDatum build(String key, SetDatum data) {
        assert data.cidScheme.equals(cidScheme);
        if (! isMutable) {
            MapDatum ret = (MapDatum) clone();
            ret.put(ret.cidScheme.keyFor(key.getBytes(Util.defaultCharset)), data);
            return ret;
        }
        put(cidScheme.keyFor(key.getBytes(Util.defaultCharset)), data);
        return this;
    }

    public MapDatum build(ContentId field, ContentId cid) {
        assert cid.getCidScheme().equals(cidScheme) && field.getCidScheme().equals(cidScheme);
        if (! isMutable) {
            MapDatum ret = (MapDatum) clone();
            ret.put(field, cid);
            return ret;
        }
        put(field, cid);
        return this;
    }

    /**
     * like {@link MapDatum#put(ContentId, SetDatum)}
     * @param cidKey the key for the content being put
     * @param values what to replace all values at the kiven key with
     * @return this if mutable, or a clone if not mutable
     */
    public MapDatum build(ContentId cidKey, SetDatum values) {
        assert cidKey.getCidScheme().equals(cidScheme) && values.getCidScheme().equals(cidScheme);
        if (! isMutable) {
            MapDatum ret = (MapDatum) clone();
            ret.put(cidKey, values);
            return ret;
        }
        put(cidKey, values);
        return this;
    }

    public MapDatum build(String fieldName, ContentId cid) {
        return build(cidScheme.keyFor(fieldName.getBytes(Util.defaultCharset)), cid);
    }

    public MapDatum build(CharSequence fieldName, CharSequence fieldValue) {
        return build(cidScheme.keyFor(fieldName.toString().getBytes(Util.defaultCharset))
                , fieldValue.toString().getBytes(Util.defaultCharset));
    }
    
    /**
     * The bytes parameter will have a {@link ContentId} generated for it and
     * inserted as one of the values.
     * 
     * @param key
     *            the key under which to store the value
     * @param bytes
     *            the content to store
     * @return
     */
    private MapDatum build(ContentId key, byte[] bytes) {
        return build(key, cidScheme.keyFor(bytes));
    }

    /**
     * put what the ContentId resolves to into this MapDatum, whether that
     *  means using the given ContentId, or resolving and reinserting into
     *  the Repo for this MapDatum.
     */
    public MapDatum build(MetadataFields field, ContentId cid) {
        if (cid.getCidScheme().equals(cidScheme))
            return build(field.name(), cid);
        else if (cid instanceof ResolvableId)
            return build(field.name(), cidScheme.keyFor(((ResolvableId)cid).resolve()));
        throw new IllegalArgumentException("the given content id neither matches the scheme nor can resolve to bytes");
    }

    /**
     * Assumes the elements of {@link TypeValues} are already inserted.
     */
    public MapDatum buildType(TypeValues tv) {
        return build(MetadataFields.TYPE
                , cidScheme.keyFor(
                        tv.name().getBytes(Util.defaultCharset))
                );
    }

    /**
     * Make the backing map immutable.
     * @return this
     */
    public MapDatum buildFinish() {
        if (isMutable ) {
            backingMap = Collections.unmodifiableSortedMap(backingMap);
            isMutable = false;
        }
        return this;
    }
    
    @Override
    public Object clone() {
        TreeMap<ContentId, SetDatum> newBackingMap = new TreeMap<ContentId, SetDatum>(backingMap);
        return new MapDatum(cidScheme, newBackingMap, isMutable);
    }

    @Override
    public void clear() { backingMap.clear(); }
    @Override
    public boolean containsKey(Object key) {
        assert key instanceof BigInteger;
        return containsKey((ContentId)key);
    }
    public boolean containsKey(ContentId key) {
        return backingMap.containsKey(key);
    }
    public boolean containsType(TypeValues typeValue) {
        return getField(MetadataFields.TYPE).contains(typeValue);
    }

    @Override
    public boolean containsValue(Object value) {
        return backingMap.containsValue(value);
    }
    @Override
    public Set<java.util.Map.Entry<ContentId, SetDatum>> entrySet() {
        return backingMap.entrySet();
    }
    
    /**
     * Get all the values associated with that key.
     */
    @Override
    public SetDatum get(Object key) {
        assert key instanceof ContentId;
        return backingMap.get(key);
    }

    /**
     * Same as get(ContentId) but with a MetadataField
     */
    public SetDatum getField(MetadataFields field) {
        // TODO optimize
        return get(cidScheme.keyFor(field.name().getBytes(Util.defaultCharset)));
    }
    
    
    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }
    @Override
    public Set<ContentId> keySet() {
        return backingMap.keySet();
    }
    
    /**
     * Replaces the given set with the existing set.
     */
    @Override
    public SetDatum put(ContentId key, SetDatum value) {
        assert value.getCidScheme().equals(cidScheme) && (value.size() == 0 ||
                value.iterator().next().getCidScheme().equals(cidScheme)) :
                    "CidScheme from incoming set should match this map's CidScheme";
        return backingMap.put(key, value);
    }
    
    /**
     * Inserts the given content id into the set located at the given key.
     */
    public SetDatum put(ContentId key, ContentId val) {
        assert isMutable;
        if (backingMap.containsKey(key)) {
            SetDatum prevVal = backingMap.get(key);
            return backingMap.put(key, prevVal.build(val));
        }
        return backingMap.put(key, new SetDatum(cidScheme).build(val));
    }
    
    @Override
    public void putAll(Map<? extends ContentId, ? extends SetDatum> arg0) {
        assert isMutable;
        backingMap.putAll(arg0);
    }
    @Override
    public SetDatum remove(Object key) {
        assert isMutable;
        return backingMap.remove(key);
    }
    @Override
    public int size() {
        return backingMap.size();
    }
    @Override
    public Collection<SetDatum> values() {
        return backingMap.values();
    }

    @Override
    public byte[] toByteArray() {
        byte[] ret = new byte[cidScheme.getKeyLength() *size() *2];
        int offset = 0;
        for (Map.Entry<ContentId, SetDatum> entry : entrySet()) {
            System.arraycopy(entry.getKey().toByteArray(), 0, ret, offset, cidScheme.getKeyLength());
            offset += cidScheme.getKeyLength();
            assert entry.getValue().size() >= 0 : "maybe I don't really have to assert this";
            SetDatum valSet;
            if (entry.getValue().size() > 0) {
                valSet = new SetDatum(entry.getValue());
            } else {
                valSet = new SetDatum(cidScheme).buildFinish();
            }
            byte[] src = cidScheme.keyFor(valSet.toByteArray()).toByteArray();
            assert src.length == cidScheme.getKeyLength() : "wrong length.\n src: "+ src.length +"\n repo: "+ cidScheme.getKeyLength();
            System.arraycopy( src, 0, ret, offset, cidScheme.getKeyLength());
            offset += cidScheme.getKeyLength();
        }
        return ret;
    }

    @Override
    public Comparator<? super ContentId> comparator() {
        return backingMap.comparator();
    }

    @Override
    public ContentId firstKey() {
        return backingMap.firstKey();
    }

    @Override
    public SortedMap<ContentId, SetDatum> headMap(ContentId endKey) {
        return backingMap.headMap(endKey);
    }

    @Override
    public ContentId lastKey() {
        return backingMap.lastKey();
    }

    @Override
    public SortedMap<ContentId, SetDatum> subMap(ContentId startKey,
            ContentId endKey) {
        return backingMap.subMap(startKey, endKey);
    }

    @Override
    public SortedMap<ContentId, SetDatum> tailMap(ContentId startKey) {
        return backingMap.tailMap(startKey);
    }

    @Override
    public ResolvableId getId() {
        assert (!isMutable) && initialized;
        if (cachedId == null || isMutable) {
            Repo r = Util.getDefaultRepo(cidScheme);
            if (r != null) {
                ContentId cid = r.put(toByteArray());
                if (cid instanceof ResolvableId)
                    cachedId = (ResolvableId)cid;
                else
                    cachedId = new ContentIdResolvable(r, cid.toByteArray());
            } else
                cachedId = new ContentIdResolvable(cidScheme, toByteArray());
        }
        return cachedId;
    }
    
    /**
     * Insert this MapDatum into the {@link Repo} but also
     * {@link Stuffable#stuff(Repo)} every value.
     * @see MapDatum#init(Stuffed)
     * @return something for {@link MapDatum#init(Stuffed)} to use
     * @throws NotInRepoException 
     */
    @Override // Stuffable
    public Stuffed stuff(Repo r, CidScheme cids) throws NotInRepoException {
        if(! r.getCidScheme().equals(cidScheme))
            throw new IllegalArgumentException("provide a repo containing content accessible via the content ids in this stuffable.");
        if (cachedId == null || isMutable) {
            cachedId = new ContentIdResolvable(cidScheme, this.toByteArray());
            assert (! Util.intensiveAssertions) || this.equals(new MapDatum(r, cachedId));
        }
        LinearVirtualRepo lvr = new LinearVirtualRepo(cids);
        for (java.util.Map.Entry<ContentId, SetDatum> entry : this.entrySet()) {
            lvr.put(repoGet(r, entry.getKey()));
            lvr.put(entry.getValue().toByteArray());
            for (ContentId cid : entry.getValue())
                lvr.put(repoGet(r, cid));
        }
        return new VirtualRepoBackedStuffed(lvr, cachedId);
    }
    
    @Override // Stuffable
    public void init(Stuffed stuffed) {
        assert !initialized;
        assert !isMutable : "zero arg constructor should have made this map datum immutable";
        
        SortedMap<ContentId, SetDatum> toFill = new TreeMap<ContentId, SetDatum>();
        cidScheme = stuffed.getCidScheme();
        byte[] thisMapDatumInBinary = stuffed.getTopId().resolve();
        assert thisMapDatumInBinary != null : "stuffed should contain all necessary data "+ stuffed;
        InputStream pairs = new ByteArrayInputStream(thisMapDatumInBinary);
        try {
            byte[] pair = new byte[cidScheme.getKeyLength() *2];
            for(int nbrRead = pairs.read(pair)
                    ;nbrRead == pair.length
                    ;nbrRead = pairs.read(pair)) {
                ContentId keyCid = new BasicContentId(cidScheme,
                        Arrays.copyOfRange(pair, 0, cidScheme.getKeyLength())
                        );
                ResolvableId valCid = new ContentIdResolvable(stuffed,
                        Arrays.copyOfRange(pair, cidScheme.getKeyLength(), pair.length)
                        );
                SetDatum values = new SetDatum();
                values.init(new VirtualRepoBackedStuffed(stuffed, valCid));
                toFill.put(keyCid, values);
            }
        } catch (IOException ioe) {
            assert false : "there is something drastically wrong if a ByteArrayInputStream throws an IOException";
        }

        backingMap = Collections.unmodifiableSortedMap(toFill);
        initialized = true;
    }

    @Override
    public int compareTo(HasContentId o) {
        return o.getId().compareTo(getId());
    }

    @Override
    public CidScheme getCidScheme() {
        return cidScheme;
    }

    public boolean isMutable() {
        return isMutable;
    }
}

package org.baseparadigm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.baseparadigm.i.HasContentId;
import org.baseparadigm.i.Repo;
import org.baseparadigm.i.ResolvableId;
import org.baseparadigm.i.Stuffable;
import org.baseparadigm.i.Stuffed;
import org.baseparadigm.i.ToByteArray;

import static org.baseparadigm.Util.repoGet;

/**
 * A map of maps. There are of course different ways to shard. This has indexes
 * in mind, which could be sharded by indexing function (eg isbn if the indexed
 * documents are books), or large indexes could simply be split into sections.
 * This datum is the entry place into such. In a lot of ways it is like {@link MapDatum}
 */
public class ShardedMapDatum implements SortedMap<ContentId, MapDatum>, ToByteArray, HasContentId, Stuffable, Cloneable {
    public SortedMap<ContentId, MapDatum> backingMap;
    public CidScheme cidScheme;
    
    // note how isMutable and initialized are used in constructors
    protected boolean isMutable = true;
    protected boolean initialized = false;
    private ContentIdResolvable cachedId;

    public ShardedMapDatum(CidScheme cids) {
        cidScheme = cids;
        backingMap = new TreeMap<>();
        isMutable = true;
        initialized = true;
    }
    
    public void buildFinish() {
        isMutable = false;
    }

    /**
     * Equivalent to get(indexFunction).get(indexValue).
     * 
     * @param indexFunction
     *            Which index to query.
     * 
     * @param indexValue
     *            What to query the index for.
     * 
     * @return Content for which applying the indexFunction would result in a
     *         set containing the indexValue.
     */
    public Set<ContentId> query(ContentId indexFunction, ContentId indexValue){
        MapDatum m = get(indexFunction);
        if (m == null) return null;
        return m.get(indexValue);
    }

    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        assert key instanceof ContentId;
        return backingMap.containsKey(key);
    }

    public boolean isIndexed(ContentId indexFunction, ContentId word) {
        return backingMap.containsKey(indexFunction) &&
                backingMap.get(indexFunction).containsKey(word);
    }

    /**
     * complying with the java {@link Map} interface makes this method less useful than it could be.
     */
    @Override
    public boolean containsValue(Object value) {
        assert value instanceof MapDatum;
        return backingMap.containsValue(value);
    }

    @Override
    public MapDatum get(Object key) {
        assert key instanceof ContentId;
        return backingMap.get(key);
    }

    @Override
    public MapDatum put(ContentId key, MapDatum value) {
        assert isMutable && initialized;
        return backingMap.put(key, value);
    }

    @Override
    public MapDatum remove(Object key) {
        assert isMutable && initialized;
        return backingMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends ContentId, ? extends MapDatum> m) {
        assert isMutable && initialized;
        backingMap.putAll(m);
    }

    @Override
    public void clear() {
        assert isMutable && initialized;
        backingMap.clear();
    }

    @Override
    public int compareTo(HasContentId o) {
        assert (!isMutable) && initialized;
        return getId().compareTo(o.getId());
    }

    @Override
    public CidScheme getCidScheme() {
        return cidScheme;
    }

    @Override
    public void init(Stuffed stuffed) {
        assert !initialized;
        assert !isMutable : "zero arg constructor should have made this index datum immutable";

        SortedMap<ContentId, MapDatum> toFill = new TreeMap<>();
        ResolvableId cid = stuffed.getTopId();
        CidScheme cidScheme = stuffed.getCidScheme();
        byte[] thisIndexDatumInBinary = cid.resolve();
        assert thisIndexDatumInBinary != null : "could not get content for:\n"+ cid;
        InputStream pairs = new ByteArrayInputStream(thisIndexDatumInBinary);
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
                MapDatum values = new MapDatum(stuffed, valCid);
                toFill.put(keyCid, values);
            }
        } catch (IOException ioe) {
            assert false : "there is something drastically wrong if a ByteArrayInputStream throws an IOException";
        }
        
        backingMap = Collections.unmodifiableSortedMap(toFill);
        initialized = true;
    }

    @Override
    public Stuffed stuff(Repo r, CidScheme cids) throws NotInRepoException {
        if(! r.getCidScheme().equals(cidScheme))
            throw new IllegalArgumentException("provide a repo containing content accessible via the content ids in this stuffable.");
        if (cachedId == null || isMutable) {
            cachedId = new ContentIdResolvable(cidScheme, this.toByteArray());
        }
        LinearVirtualRepo lvr = new LinearVirtualRepo(cids);
        for (Map.Entry<ContentId, MapDatum> entry : this.entrySet()) {
            lvr.put(repoGet(r, entry.getKey()));
            lvr.put(entry.getValue().toByteArray());
            for (Map.Entry<ContentId, SetDatum> ent2 : entry.getValue().entrySet()) {
                lvr.put(repoGet(r, ent2.getKey()));
                lvr.put(ent2.getValue().toByteArray());
                for (ContentId cid : ent2.getValue())
                    lvr.put(repoGet(r, cid));;
            }
        }
        return new VirtualRepoBackedStuffed(lvr, cachedId);
    }

    @Override
    public ResolvableId getId() {
        assert (!isMutable) && initialized;
        if (cachedId == null)
            cachedId = new ContentIdResolvable(cidScheme, toByteArray());
        return cachedId;
    }

    /**
     * A shallow serialization of this {@link ShardedMapDatum}
     */
    @Override
    public byte[] toByteArray() {
        final int keyLength = cidScheme.getKeyLength();
        final int increment = keyLength*2;
        byte[] ret = new byte[size() *increment];
        int arrPos = 0;
        for (java.util.Map.Entry<ContentId, MapDatum> i : backingMap.entrySet()) {
            System.arraycopy(i.getKey().toByteArray()
                    , 0, ret, arrPos, keyLength);
            System.arraycopy(i.getValue().getId().toByteArray()
                    , 0, ret, arrPos +keyLength, keyLength);
            arrPos+=increment;
        }
        assert arrPos == ret.length;
        return ret;
    }

    @Override
    public Comparator<? super ContentId> comparator() {
        assert initialized;
        return backingMap.comparator();
    }

    @Override
    public SortedMap<ContentId, MapDatum> subMap(ContentId fromKey,
            ContentId toKey) {
        assert initialized;
        return backingMap.subMap(fromKey, toKey);
    }

    @Override
    public SortedMap<ContentId, MapDatum> headMap(ContentId toKey) {
        assert initialized;
        return backingMap.headMap(toKey);
    }

    @Override
    public SortedMap<ContentId, MapDatum> tailMap(ContentId fromKey) {
        assert initialized;
        return backingMap.tailMap(fromKey);
    }

    @Override
    public ContentId firstKey() {
        assert initialized;
        return backingMap.firstKey();
    }

    @Override
    public ContentId lastKey() {
        assert initialized;
        return backingMap.lastKey();
    }

    @Override
    public Set<ContentId> keySet() {
        assert initialized;
        return backingMap.keySet();
    }

    @Override
    public Collection<MapDatum> values() {
        assert initialized;
        return backingMap.values();
    }

    @Override
    public Set<java.util.Map.Entry<ContentId, MapDatum>> entrySet() {
        assert initialized;
        return backingMap.entrySet();
    }

}

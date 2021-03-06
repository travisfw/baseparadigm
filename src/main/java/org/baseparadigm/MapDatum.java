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


public class MapDatum implements SortedMap<ContentId, SetDatum>, ToByteArray{
    public SortedMap<ContentId, SetDatum> backingMap;
    public Repo repo;
    private boolean modifyRepo = true;
    private boolean isMutable = true;
    public MapDatum(Repo repo, BigInteger datumId) {
        this.repo = repo;
        this.backingMap = Collections.unmodifiableSortedMap(toMap(repo, datumId));
    }
    
    /**
     * Mutable, for building; call buildFinish() to make immutable.
     * @param baseParadigm
     */
    public MapDatum(Repo baseParadigm) {
        repo = baseParadigm;
        backingMap = new TreeMap<ContentId, SetDatum>();
        isMutable = true;
    }

    
    /**
     * Immutable
     * @param value
     */
    public MapDatum(SortedMap<ContentId, SetDatum> map) {
        if (map.size() == 0)
            throw new IllegalStateException(
                    "MapDatum instances need to belong to a BaseParadigm instance, which cannot be inferred from an empty map.");
        repo = map.keySet().iterator().next().repo;
        backingMap = Collections.unmodifiableSortedMap(map);
        isMutable = false;
    }

    /**
     * Get the data for the content id and create a MapDatum from it.
     */
    public static MapDatum inflate(ContentId cid) {
        return new MapDatum(toMap(cid.repo, cid));
    }
    
    /**
     * builder pattern for method chaining
     */
    public MapDatum build(String key, int value) {
        if (! isMutable)
            throw new UnsupportedOperationException("this DatumMap is immutable");
        put(
                new ContentId(repo, key.getBytes(repo.getCharset()))
                , new BigInteger(""+ value).toByteArray()
                );
        return this;
    }

    /**
     * The ContentId for the string becomes the key for the given set of values. The previous set of values is discarded.
     * @return this
     */
    public MapDatum build(String key, SetDatum data) {
        if (! isMutable)
            throw new UnsupportedOperationException("this DatumMap is immutable");
        put(repo.put(key.getBytes(repo.getCharset())), data);
        return this;
    }

    /**
     * Adds the given item to the set at the ContentId for the key string.
     * @return this
     */
    public MapDatum build(String fieldName, byte[] item) {
        if (! isMutable)
            throw new UnsupportedOperationException("this DatumMap is immutable");
        put(repo.put(fieldName.getBytes(repo.getCharset())), item);
        return this;
    }

    public MapDatum build(String fieldName, ContentId cid) {
        if (! isMutable)
            throw new UnsupportedOperationException("this DatumMap is immutable");
        assert cid.repo == repo;
        put(repo.put(fieldName.getBytes(repo.getCharset())), cid);
        return this;
    }
    
    /**
     * build(fieldName, bytes.toByteArray())
     */
    public MapDatum build(String fieldName, ToByteArray bytes) {
        return build(fieldName, bytes.toByteArray());
    }

    /**
     * build(field.name(), string.getBytes())
     */
    public MapDatum build(MetadataFields field, String string) {
        return build(field.name(), string.getBytes());
    }

    /**
     * build(content.name(), bytes)
     */
    public MapDatum build(MetadataFields field, byte[] bytes) {
        return build(field.name(), bytes);
    }
    
    /**
     * build(field.name(), byteSize.toByteArray())
     */
    public MapDatum build(MetadataFields field, ToByteArray byteSize) {
        return build(field.name(), byteSize.toByteArray());
    }
    
    /**
     * put what the ContentId resolves to into this MapDatum, whether that
     *  means using the given ContentId, or resolving and reinserting into
     *  the Repo for this MapDatum.
     */
    public MapDatum build(MetadataFields field, ContentId cid) {
        if (cid.repo == repo)
            return build(field.name(), cid);
        else
            return build(field.name(), cid.resolve());
    }

    /**
     * like build(MetadataFields.TYPE, byte[])
     */
    public GraphDatum buildType(TypeValues tv) {
        return (GraphDatum) build(MetadataFields.TYPE, tv.name().getBytes(repo.getCharset()));
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
    

    /**
     * The datumId must reference a map of references to references to sets.
     * 
     * @param repo
     * Where to look up the datumId.
     * 
     * @param datumId
     * The id for the content of the map.
     */
    public static SortedMap<ContentId, SetDatum> toMap(Repo repo, BigInteger datumId) {
        SortedMap<ContentId, SetDatum> toFill = new TreeMap<ContentId, SetDatum>();
        InputStream pairs = new ByteArrayInputStream(repo.get(datumId));
        try {
            byte[] pair = new byte[repo.keyLength() *2];
            int nbrRead = pairs.read(pair);
            for(byte[] k, v;
                    nbrRead == pair.length;
                    nbrRead = pairs.read(pair)) {
                k = repo.get(Arrays.copyOfRange(pair, 0, repo.keyLength()));
                v = repo.get(Arrays.copyOfRange(pair, repo.keyLength(), pair.length));
                SetDatum setOfReferences = new SetDatum(repo, new BigInteger(v));
                toFill.put(new ContentId(repo, k), setOfReferences);
            }
        } catch (IOException ioe) {
            assert false : "there is something drastically wrong if a ByteArrayInputStream throws an IOException";
        }
        return toFill;
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
        return get(repo.idFor(field.name().getBytes(repo.getCharset())));
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
        return backingMap.put(key, value);
    }
    
    /**
     * Inserts the given byteArray into the backing repo (if modifyRepo is true) and inserts
     *  its content id into the set located at the given key.
     */
    public SetDatum put(ContentId key, byte[] byteArray) {
        return put(key
                , modifyRepo
                ? repo.put(byteArray)
                : repo.idFor(byteArray));
    }
    
    /**
     * Inserts the given content id into the set located at the given key.
     */
    public SetDatum put(ContentId key, ContentId val) {
        SetDatum replacing = get(key);
        if (replacing != null) {
            SetDatum merged = new SetDatum(replacing);
            merged.add(val);
            return put(key, merged);
        }
        replacing = new SetDatum(repo);
        replacing.add(val);
        return put(key, replacing);
    }
    
    /**
     * If set true, a DatumMap instance will insert content
     *  inserted into it into the BaseParadigm instance it contains.
     *  
     * @param willModifyRepo
     * whether to insert new content into BaseParadigm
     */
    public MapDatum setModifyRepo(boolean willModifyRepo) {
        modifyRepo = willModifyRepo;
        return this;
    }
    
    @Override
    public void putAll(Map<? extends ContentId, ? extends SetDatum> arg0) {
        backingMap.putAll(arg0);
    }
    @Override
    public SetDatum remove(Object key) {
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
        byte[] ret = new byte[repo.keyLength *size() *2];
        int offset = 0;
        for (Map.Entry<ContentId, SetDatum> entry : entrySet()) {
            System.arraycopy(entry.getKey().toByteArray(), 0, ret, offset, repo.keyLength);
            offset += repo.keyLength;
            byte[] src = repo.put(new SetDatum(entry.getValue()).toByteArray()).toByteArray();
            assert src.length == repo.keyLength;
            System.arraycopy( src, 0, ret, offset, repo.keyLength);
            offset += repo.keyLength;
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

    /**
     * Add an item to the metadata about the content this MapDatum is metadata about.
     * convenience for put(ContentId, byte[])
     */
    public SetDatum put(MetadataFields metadataField, byte[] byteArray) {
        return this.put(metadataField.name(), byteArray);
    }
    
    /**
     * like put(ContentId, byte[])
     */
    public SetDatum put(String fieldName, byte[] byteArray) {
        return put(repo.idFor(fieldName.getBytes(repo.getCharset())), byteArray);
    }

    public ContentId id = null;
    public ContentId getId() {
        if (id == null)
            id = repo.put(this);
        return id;
    }
}

package org.baseparadigm;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Repo implements Map<ContentId, byte[]>{
    /**
     * The default KEY_LENGTH determined by the COMMONS_ID_ALGORITHM.
     */
    public static final int COMMONS_KEY_LENGTH = 64;
    public static final String COMMONS_ID_ALGORITHM = "SHA-512";
    public static final Charset defaultCharset = Charset.forName("UTF8");
    public static Repo commons;
    private Map<ContentId, byte[]> map = new HashMap<ContentId, byte[]>();
    private MessageDigest md;
    
    /**
     * Subclasses should change this according to the way they create content ids.
     */
    public final int keyLength = COMMONS_KEY_LENGTH;
    
    private byte[] metaData = null;
    

    public Repo(Map<ContentId, byte[]> content) {
        map = content;
        init();
    }
    public Repo(Map.Entry<ContentId, byte[]>... content ) {
        init();
        for (Map.Entry<ContentId, byte[]> metaEntry : content)
            put(metaEntry);
    }

    public Repo() {
        init();
    }
    
    public static Repo commonsInstance() {
        if (commons == null)
            commons= new Repo();
        return commons;
    }
    
    private void init() {
        try {
            md = MessageDigest.getInstance(COMMONS_ID_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new Error("depending on "+ COMMONS_ID_ALGORITHM +" being available.", e);
        }
    }
    
    
    

    // in groovysh:
    // c = 0; d = java.security.MessageDigest.getInstance("SHA-512");
    // d.digest("OBJECTS".getBytes()).each{ b -> print("$b, "); if(++c % 8 == 0) println(); }
    
    // TODO for 0.1 set these in the constructor. if a different id scheme is used, these ids will be wrong.
    /**
     * The sha512 hash of "OBJECTS".
     * BigIntegers are good representations of ids because they are comparable and immutable.
     */
    public final ContentId OBJECTSid = new ContentId(this,
            new byte[]{ -66, 91, -126, 63, -122, 94, -15, 25, -108, -27, -71, -128, -68, -1, -5, 49, -99, -114, -119
            , -20, 108, -99, 117, -58, 5, -23, -5, -22, -10, 18, 33, -82, -43, -18, -82, 24, 94, 47, -30, -78, -26, -15
            , 94, -44, -120, 121, 67, -21, 52, 53, -128, -57, -35, 36, 112, 35, -45, 75, 117, -15, -87, 59, -14, -40 }
            );
    /**
     * The sha512 hash of "PREDICATE".
     */
    public final ContentId PREDICATESid = new ContentId(this,
            new byte[]{ -66, -72, 19, 113, -106, 84, -17, -22, 97
            , -92, 56, 30, 66, -71, 27, 61, 101, 31, -103, 42, -81, 89, -93, -97, 9, -86, -92, -54, 102, -44, -18, 127
            , -93, 105, -126, -33, 13, 86, 49, -122, 76, 107, 10, 79, -58, 110, -102, -3, -39, 111, 38, 35, -124, -9
            , -18, 118, -114, 77, 14, 61, 77, -42, -56, 80 });
    /**
     * The sha512 hash of "SUBJECT".
     */
    public final ContentId SUBJECTSid = new ContentId(this,
            new byte[]{ -42, -44, -12, 69, 11, 52, 123, 13, -108
            , 65, 39, 120, 22, -36, -109, 67, -57, 50, -106, 70, -91, 0, -107, -65, 66, -60, 85, -25, 83, -126, 33, 20
            , -41, 2, 40, 86, -36, 1, -104, 76, 46, 83, 4, -55, 56, -33, -19, -6, 69, 50, -44, -39, -105, -99, 118, 7
            , -64, -67, -91, 55, -93, 1, 100, 111 });
    /**
     * The sha512 hash of "AUTHORS".
     */
    public final ContentId AUTHORSid = new ContentId(this,
            new byte[]{ -85, -24, -99, 26, 22, 67, 114, -21, -59, 67
            , -20, -128, 106, -103, -110, 4, -16, 109, 41, -14, 42, -80, 19, 24, -58, -121, -64, 46, 117, 98, -111, 109
            , 51, -25, -66, 35, -126, -73, -41, -29, -67, 42, -18, -3, -43, 43, -23, 87, -97, 121, 53, 83, 98, 105, -84
            , -68, -1, 62, 7, 68, -99, -85, -97, 113 });
    /**
     * The sha512 hash of "ASSUMPTIONS".
     */
    public final ContentId ASSUMPTIONSid = new ContentId(this,
            new byte[]{ -121, 85, 89, 84, -62, -6, 10, -44, 42
            , 84, 66, 35, 6, -28, -83, -12, -69, -98, 92, -103, 74, 123, 34, 92, -11, -23, 63, 66, 98, -106, -98, -12
            , -108, -75, -46, 116, -120, -72, 92, 56, 124, 121, -52, -119, 118, -87, 104, -64, 70, -62, -101, -98, -13
            , 94, 109, 68, -93, -128, 83, 39, -7, -65, 50, -84 });
    /**
     * The sha512 hash of "PATTERNS".
     */
    public final ContentId PATTERNSid = new ContentId(this,
            new byte[]{
             -128, 22 , -51, 107, 60 , 13 , -87 , 95
            , 25 , 16 , 74 , 89 , -15, -84, -103, 9
            ,-13 , 95 , -85, 78 , 52 , 59 , -20 , -58
            ,-21, -125, 115, 53 , 65, -119, -38 , 14
            ,-100, -44, -26, -57, -99, -42, -80 , -94
            , 23 , -36, -47, -32, 13 , 80 , -66 , 30
            , 118, 78 , 112, -9, -108, 12 , -40 , 15
            , 27 , 51 , 53 , 109, 95, -102, -58 , 39
            });
    
    /**
     * add to backups to do things like persist to disk and keep stuff on other machines
     */
    public List<Repo> backups = new LinkedList<Repo>();

    /**
     * used for backup operations. set to null to disable backup.
     */
    public static Executor ex = new ThreadPoolExecutor(0, 2, 1, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

    
    @Override
    public void clear() {
        this.map.clear();
    }
    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }
    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }
    @Override
    public Set<java.util.Map.Entry<ContentId, byte[]>> entrySet() {
        return this.map.entrySet();
    }
    @Override
    public byte[] get(Object key) {
        assert key instanceof ContentId && ((ContentId)key).bp == this;
        return this.map.get(key);
    }
    
    /**
     * @return Metadata for this repository including things like KEY_LENGTH.
     */
    public byte[] getRepoMetadata() {
        if (this.metaData == null)
            this.metaData = new MapDatum(this).build("KEY_LENGTH", 64).toByteArray();
        return this.metaData;
    }
    
    public Set<byte[]> getAll(Iterable<ContentId> keys) {
        Set<byte[]> ret = new HashSet<byte[]>();
        for (ContentId k : keys)
            ret.add(get(k));
        return ret;
    }
    
    /**
     * The default is BaseParadigm.COMMONS_KEY_LENGTH.
     * @return
     * The key length.
     */
    public int keyLength() {
        return this.keyLength ;
    }
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    @Override
    public Set<ContentId> keySet() {
        return this.map.keySet();
    }
    
    
    /**
     * If you use this method, be sure that the key.equals(idFor(value)).
     * Unlike put(byte[] value), this put conforms to the Map interface method signature.
     */
    @Override
    public byte[] put(final ContentId key, final byte[] value) {
        assert key.equals(idFor(value));
        if (Repo.ex != null)
            Repo.ex.execute(new Runnable() {
                public void run() {
                    for (Repo b : backups)
                        b.put(value);
                }
            });
        return this.map.put(key, value);
    }
    /**
     * Convenience for put(ContentId key, byte[] value).
     */
    public byte[] put(java.util.Map.Entry<ContentId, byte[]> entry) {
        return put(entry.getKey(), entry.getValue());
    }
    /**
     * This put returns the key associated with the value inserted.
     * 
     * @param value
     * The byte array to store.
     * 
     * @return
     * The key that will retrieve the byte array stored, calculated using idFor().
     */
    public ContentId put(byte[] value) {
        ContentId id = idFor(value);
        put(id, value);
        return id;
    }
    /**
     * put(data.toByteArray())
     */
    public ContentId put(ToByteArray data) {
        return put(data.toByteArray());
    }
    
    
    /**
     * Subclasses override idFor.
     * 
     * @param value
     * Content to be retrieved later.
     * 
     * @return
     * The identifier to retrieve the content given.
     */
    public ContentId idFor(byte[] value) {
        return new ContentId(this, md.digest(value));
    }
    public ContentId idFor(ToByteArray value) {
        return idFor(value.toByteArray());
    }
    
    @Override
    public void putAll(Map<? extends ContentId, ? extends byte[]> arg0) {
        this.map.putAll(arg0);
    }
    
    /**
     * @return
     * A set of the keys which will retrieve the given values.
     */
    public Set<BigInteger> putAll(Iterable<byte[]> values){
        Set<BigInteger> ret = new HashSet<BigInteger>();
        for (byte[] v : values)
            ret.add(put(v));
        return ret;
    }
    @Override
    public byte[] remove(Object key) {
        return this.map.remove(key);
    }
    @Override
    public int size() {
        return this.map.size();
    }
    @Override
    public Collection<byte[]> values() {
        return this.map.values();
    }
    
    public ContentId idFor(SubjectPredicateObject key) {
        switch (key) {
        case SUBJECTS:
            return SUBJECTSid;
        case PREDICATES:
            return PREDICATESid;
        case OBJECTS:
            return OBJECTSid;
        case AUTHORS:
            return AUTHORSid;
        case ASSUMPTIONS:
            return ASSUMPTIONSid;
        case PATTERNS:
            return PATTERNSid;
        default:
            throw new Error("is there a new member of SubjectPredicateObject that isn't accounted for?");
        }
    }
    
}

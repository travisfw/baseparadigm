package org.baseparadigm.i;

public interface RepoStorage {

    /**
     * 
     * @param cid
     * @return the content requested or null if not found
     */
    byte[] get(ContentId cid);

    boolean containsKey(ContentId cid);

    void put(ContentId cid, byte[] value);
    
}

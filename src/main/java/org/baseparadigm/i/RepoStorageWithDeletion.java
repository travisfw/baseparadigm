package org.baseparadigm.i;

public interface RepoStorageWithDeletion extends RepoStorage {
    /**
     * Unlike the java.util.Map interface, remove does not return the value
     * stored because in the content addressable paradigm, the content should be
     * known.
     * 
     * @param cid
     *            identifier for the content to forget
     */
    void remove(ContentId cid);
}

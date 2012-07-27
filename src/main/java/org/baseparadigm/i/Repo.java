package org.baseparadigm.i;

/**
 * Repo lists a subset of the {@link java.util.Map} interface.
 * From CRUD (Create Read Update Delete), only C and R are necessary for a Repo.
 * Look at {@link RepoWithDeletion} to add support for D. U is not applicable to
 * content addressable storage.
 */
public interface Repo extends HasCidScheme {
    boolean containsKey(ContentId cid);
    /**
     * Maps almost directly to {@link RepoStorage#get(ContentId)}
     * @param cid
     * @return the content requested or null if not found
     */
    byte[] get(ContentId cid);
    ContentId put(byte[] value);
}

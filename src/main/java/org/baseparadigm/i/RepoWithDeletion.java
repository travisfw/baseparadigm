package org.baseparadigm.i;

/**
 * Adds delete functionality from java.util.Map to IRepo.
 * 
 */
public interface RepoWithDeletion extends Repo {

    /**
     * Free up space. The implementation is free to either actually delete the
     * content or just mark it to be removed when space is needed in the future.
     * In the latter case it could still be retrieved with the same ContentId
     * after deletion.
     * 
     * @param key for content to be deallocated
     */
    void remove(ContentId key);
    
}

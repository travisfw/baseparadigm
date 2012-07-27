package org.baseparadigm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.RepoStorage;
import org.baseparadigm.i.RepoStorageWithDeletion;
import org.baseparadigm.i.RepoWithBackups;
import org.baseparadigm.i.RepoWithDeletion;
import org.baseparadigm.i.ResolvableId;
import org.baseparadigm.i.ToByteArray;

/**
 * A Repo is a binding between a content id scheme and
 * one or more key value stores for binary data. It's the thing users talk about
 * putting stuff into.
 */
public class BasicRepo implements RepoWithDeletion, RepoWithBackups {
    private RepoStorage primaryStorage;
    private final CidScheme cidScheme;
    
    public BasicRepo() {
        primaryStorage = new MapStore();
        cidScheme = CommonsCidScheme.getInstance();
    }

    /**
     * Uses {@link CommonsCidScheme}
     */
    public BasicRepo(RepoStorage primary) {
        primaryStorage = primary;
        cidScheme = CommonsCidScheme.getInstance();
    }

    public BasicRepo(RepoStorage primary, CidScheme scheme) {
        primaryStorage = primary;
        cidScheme = scheme;
    }
    
    /**
     * add to backups to do things like persist to disk and keep stuff on other machines
     */
    public Collection<RepoStorage> copies = new HashSet<RepoStorage>();
    
    @Override
    public Collection<RepoStorage> getSecondaries() {return copies;}
    @Override
    public boolean addSecondary(RepoStorage rs) {return copies.add(rs);}

    /**
     * used for backup operations. set to null to disable backup.
     */
    public static Executor ex = new ThreadPoolExecutor(0, 2, 1, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    
    @Override
    public boolean containsKey(ContentId cid) {
        return this.primaryStorage.containsKey(cid);
    }
    
    @Override
    public byte[] get(ContentId cid) {
        assert cid.getCidScheme().equals(cidScheme);
        byte[] ret = primaryStorage.get(cid);
        if (ret == null) {
            for (RepoStorage i : copies)
                if ((ret = i.get(cid)) != null)
                    return ret;
        }
        return ret;
    }
    
    /**
     * Current implementation returns {@link ContentIdResolvable} so it's actually an {@link ResolvableId} (2012 04 08) 
     * 
     * @param value
     * The byte array to store.
     * 
     * @return
     * The key that will retrieve the byte array stored, calculated using idFor().
     */
    @Override
    public ContentId put(byte[] value) {
        final ResolvableId cid = new ContentIdResolvable(this, cidScheme.bytesFor(value));
        primaryStorage.put(cid, value);
        for (RepoStorage b : copies)
            b.put(cid, value);
        assert containsKey(cid);
        return cid;
    }

    /**
     * An asynchronous version of put(byte[])
     */
    public ContentId aput(final byte[] value) {
        final ResolvableId cid = new ContentIdResolvable(this, cidScheme.bytesFor(value));
        primaryStorage.put(cid, value);
        if (BasicRepo.ex != null && ! copies.isEmpty())
            BasicRepo.ex.execute(new Runnable() {
                public void run() {
                    for (RepoStorage b : copies)
                        b.put(cid, value);
                }
            });
        return cid;
    }
    
    /**
     * put(data.toByteArray())
     */
    public ContentId put(ToByteArray data) {
        return put(data.toByteArray());
    }
    
    /**
     * @return
     * A set of the keys which will retrieve the given values.
     */
    public Set<ContentId> putAll(Iterable<byte[]> values){
        Set<ContentId> ret = new HashSet<ContentId>();
        for (byte[] v : values)
            ret.add(put(v));
        return ret;
    }
    
    /**
     * @param key the key for the content to remove.
     * @return null
     */
    @Override
    public void remove(ContentId key) {
        if (primaryStorage instanceof RepoStorageWithDeletion)
            ((RepoStorageWithDeletion)primaryStorage).remove(key);
        else throw new IllegalStateException("the primaryStorage does not support IRepo#remove(IContentId)");
        for (RepoStorage b : copies)
            if (b instanceof RepoStorageWithDeletion)
                ((RepoStorageWithDeletion)b).remove(key);
    }
    
    public ContentId idFor(SubjectPredicateObject key) {
        // TODO optimize by hardcoding byte arrays similar to -r 763599d9fdea
        return cidScheme.keyFor(key.name().getBytes());
    }

    @Override
    public CidScheme getCidScheme() {
        return cidScheme;
    }
}

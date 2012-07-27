package org.baseparadigm;

import java.lang.ref.SoftReference;
import java.util.Arrays;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.Repo;
import org.baseparadigm.i.ResolvableId;

/**
 * Caches its content in a {@link SoftReference}
 */
public class ContentIdResolvable extends BasicContentId implements ResolvableId {

    protected final Repo repo;
    protected SoftReference<byte[]> value = null;
    protected final byte[] hardReference;
    
    /**
     * uses a {@link SoftReference} which will be refreshed on {@link ContentIdResolvable#resolve()}
     * @param repo a repo to query for the content referenced by the content id bytes
     * @param cidValue bytes to construct this content id (not the content itself)
     */
    public ContentIdResolvable(Repo repo, byte[] cidValue) {
        super(repo.getCidScheme(), cidValue);
        assert cidValue.length == repo.getCidScheme().getKeyLength();
        this.repo = repo;
        hardReference = null;
    }
    
    /**
     * Make a new id based on the given id but with a hard reference to the
     * content.
     * 
     * @param cid
     * @param content
     */
    public ContentIdResolvable(ContentId cid, byte[] content) {
        super(cid.getCidScheme(), cid.toByteArray());
        repo = null;
        value = null;
        hardReference = content;
        assert Arrays.equals(cid.toByteArray(), cidScheme.bytesFor(content));
    }
    
    /**
     * Make a new id based on the given id but with a hard reference to the
     * content.
     * 
     * @param cids
     * @param content
     */
    public ContentIdResolvable(CidScheme cids, byte[] content) {
        super(cids, cids.bytesFor(content));
        repo = null;
        value = null;
        hardReference = content;
    }

    @Override
    public Repo getRepo() {
        return repo;
    }

    @Override
    public byte[] resolve() {
        if (hardReference != null)
            return hardReference;
        if (value == null)
            return refreshResolve();
        byte[] ret = value.get();
        if (ret == null)
            return refreshResolve();
        return ret;
    }

    private byte[] refreshResolve() {
        byte[] ret = repo.get(this);
        if (ret == null) {
            value = null;
            return null;
        }
        value = new SoftReference<byte[]>(ret);
        return ret;
    }

}

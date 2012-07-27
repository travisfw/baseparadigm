package org.baseparadigm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.HasContentId;
import org.baseparadigm.i.Repo;
import org.baseparadigm.i.ResolvableId;
import org.baseparadigm.i.Stuffed;
import org.baseparadigm.i.SyncDeque;
import org.baseparadigm.i.VirtualRepo;

/**
 * A serializable {@link Repo} backed by a list of content not sorted by {@link ContentId}.
 * @author travis@traviswellman.com
 *
 */
public class LinearVirtualRepo implements VirtualRepo {

    protected final SyncDeque stuff;
    protected final CidScheme cidScheme;
    protected boolean initialized;
    protected boolean isMutable;
    
    public LinearVirtualRepo(CidScheme cids) {
        this.cidScheme = cids;
        this.stuff = new BasicSyncDeque(cids);
        this.initialized = true;
        this.isMutable = true;
    }
    
    /**
     * probably not efficient
     */
    @Override
    public boolean containsKey(ContentId cid) {
        return stuff.contains(cid);
    }

    /**
     * Does a linear search through a list of content.
     */
    @Override
    public byte[] get(ContentId cid) {
        // commented out because it would be incorrect to return content that was not in fact in this BasicVirtualRepo
        // this way, get(ContentId) may be used in stead of contains(ContentId). if it makes sense, this could change
//        if (cid instanceof ResolvableId)
//            return ((ResolvableId)cid).resolve();
        for (ResolvableId i : stuff)
            if (i.equals(cid))
                return i.resolve();
        return null;
    }

    /**
     * @return a {@link ResolvableId} that resolves to the byte array given
     */
    @Override
    public ContentId put(byte[] value) {
        assert value != null : "bytes added to the repo must not be null";
        if (! isMutable)
            throw new IllegalStateException("not mutable");
        ResolvableId cid = new ContentIdResolvable(cidScheme, value);
        stuff.push(cid);
        return cid;
    }

    @Override
    public CidScheme getCidScheme() {
        return cidScheme;
    }

    /**
     * Note that this id is not cheap to generate and may change.
     */
    @Override
    public ResolvableId getId() {
        return new ContentIdResolvable(cidScheme, toByteArray());
    }

    /**
     * for internal use by {@link LinearVirtualRepo#getId()} this method
     * synchronizes on internal state and takes a snapshot
     * 
     * @return a snapshot of the state of this repo
     */
    protected byte[] toByteArray() {
        byte[] cidsBytes = BigInteger.valueOf(cidScheme.schemeHash()).toByteArray();
        assert cidsBytes.length < 256 : "the length of the integer needs to be declared in the first byte";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // TODO obtain read lock on stuff
        try {
            bos.write(cidsBytes.length);
            bos.write(cidsBytes);
            for (ResolvableId i : stuff) {
                // content first, then id
                byte[] c = i.resolve();
                bos.write(BigInteger.valueOf(c.length).toByteArray());
                bos.write(c);
                bos.write(i.toByteArray());
                assert cidScheme.equals(i.getCidScheme());
            }
        } catch (IOException e) { assert false: "ByteArrayOutputStream should not throw an IOException under any condition"; }
        return bos.toByteArray();
    }

    @Override
    public int compareTo(HasContentId o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public void init(Stuffed stuffed) {
        if (initialized)
            throw new IllegalStateException("init must be called only once before initialization");
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Stuffed stuff(Repo repo, CidScheme cids) {
        if (!cids.equals(getCidScheme())) {
            LinearVirtualRepo lvr = new LinearVirtualRepo(cids);
            for (ResolvableId i : stuff)
                lvr.put(i.resolve());
            ContentId cid = repo.put(this.toByteArray());
            ResolvableId topId = new ContentIdResolvable(lvr, cid.toByteArray());
            return new VirtualRepoBackedStuffed(lvr, topId);
        }
        return new VirtualRepoBackedStuffed(this, getId());
    }

}

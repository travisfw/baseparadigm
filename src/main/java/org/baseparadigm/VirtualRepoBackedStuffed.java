package org.baseparadigm;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.HasContentId;
import org.baseparadigm.i.Repo;
import org.baseparadigm.i.ResolvableId;
import org.baseparadigm.i.Stuffed;
import org.baseparadigm.i.VirtualRepo;

public class VirtualRepoBackedStuffed implements Stuffed {

    protected final ResolvableId topId;
    protected final VirtualRepo vr;

    public VirtualRepoBackedStuffed(VirtualRepo vr, ResolvableId topId) {
        assert (! Util.intensiveAssertions) || vr.containsKey(topId);
        this.topId = topId;
        this.vr = vr;
    }

    @Override
    public boolean containsKey(ContentId cid) {
        return vr.containsKey(cid);
    }

    @Override
    public byte[] get(ContentId cid) {
        return vr.get(cid);
    }

    @Override
    public ContentId put(byte[] value) {
        throw new UnsupportedOperationException("A stuffed shouldn't be modified");
    }

    @Override
    public CidScheme getCidScheme() {
        return vr.getCidScheme();
    }

    @Override
    public ResolvableId getId() {
        return vr.getId();
    }

    @Override
    public int compareTo(HasContentId o) {
        return vr.compareTo(o);
    }

    @Override
    public void init(Stuffed stuffed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stuffed stuff(Repo repo, CidScheme cids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResolvableId getTopId() {
        return topId;
    }

}

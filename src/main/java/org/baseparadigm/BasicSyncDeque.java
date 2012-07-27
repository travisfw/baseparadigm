package org.baseparadigm;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ResolvableId;
import org.baseparadigm.i.SyncDeque;

/**
 * Basically a {@link LinkedList} with a {@link CidScheme}
 */
public class BasicSyncDeque extends LinkedBlockingDeque<ResolvableId> implements SyncDeque {

    private static final long serialVersionUID = 6960278741791235602L;
    protected final CidScheme cidScheme;
    
    public BasicSyncDeque(CidScheme cids) {
        this.cidScheme = cids;
    }

    @Override
    public CidScheme getCidScheme() {
        return this.cidScheme;
    }

    @Override
    public boolean add(ResolvableId cid) {
        assert cid.getCidScheme().equals(cidScheme);
        return super.add(cid);
    }

}

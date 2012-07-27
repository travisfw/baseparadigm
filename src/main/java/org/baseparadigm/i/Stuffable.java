package org.baseparadigm.i;

import org.baseparadigm.MapDatum;
import org.baseparadigm.NotInRepoException;

/**
 * Some structures may reference other structures whose data is not represented
 * in their {@link ToByteArray#toByteArray()} method ({@link MapDatum} for
 * instance); it is valuable for these to implement Stuffable.
 */
public interface Stuffable {

    /**
     * Initializes after using the zero arg constructor. If
     * the {@link Stuffed} is the same as was returned from
     * {@link Stuffable#stuff(Repo)} this should be guaranteed to work.
     */
    public void init(Stuffed stuffed);
    
    /**
     * Create a {@link Stuffed} using the given {@link Repo}. Stuffables may
     * contain references. After calling this method the content those
     * references point to should be accessible in the Stuffed. stuff(Repo) may
     * be an asynchronous operation as VirtualRepos may be backed by a stream;
     * which could help newStuffable.init(stuffableItem.stuff(repo)) complete
     * faster
     * 
     * @param cids
     *            The {@link CidScheme} of the resulting {@link Stuffed}
     * @param repo
     *            The Repo to provide the raw content to go into the Stuffed
     * @throws NotInRepoException
     *             if the repo given does not contain some content that is
     *             needed to create a Stuffed
     */
    public Stuffed stuff(Repo repo, CidScheme cids) throws NotInRepoException;
}

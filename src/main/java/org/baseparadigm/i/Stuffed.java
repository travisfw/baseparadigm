package org.baseparadigm.i;

/**
 * Stuffed instances return from {@link Stuffable#stuff(Repo, CidScheme)}. They
 * may be backed by a stream and so may block on access.
 * {@link Repo#put(byte[])} should not be called by anything other than the
 * {@link Stuffable} that constructed this, so may throw an
 * {@link UnsupportedOperationException} or {@link IllegalStateException}
 */
public interface Stuffed extends VirtualRepo {
    /**
     * @return id for the data {@link Stuffable#init(Stuffed)} to start with
     */
    public ResolvableId getTopId();
}

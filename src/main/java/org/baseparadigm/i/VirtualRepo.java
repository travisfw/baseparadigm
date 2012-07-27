package org.baseparadigm.i;

/**
 * A virtual repo is a repo that must take another Repo in its constructor.
 * {@link VirtualRepo#stuff(Repo)} should return itself.
 * {@link VirtualRepo#getId()} may block on io if the VirtualRepo
 * is streaming until the input stream closes.
 */
public interface VirtualRepo extends Repo, HasContentId, Stuffable {
}

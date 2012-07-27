package org.baseparadigm.i;

public interface HasContentId extends Comparable<HasContentId>, HasCidScheme {
    /**
     * The repo returned from the id returned may be a VirtualRepo in the case
     * where the HasContentId doesn't have a not-virtual repo to insert itself
     * into.
     * 
     * @return an id which resolves to the HasContentId it came from.
     */
    public ResolvableId getId();
}

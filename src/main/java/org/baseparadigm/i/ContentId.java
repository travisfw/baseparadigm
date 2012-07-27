package org.baseparadigm.i;

/**
 * An IContentId must implement toByteArray to return byte[] with
 * length == getCidScheme().getKeyLength()
 */
public interface ContentId extends ToByteArray, Comparable<ContentId>, HasCidScheme {

}

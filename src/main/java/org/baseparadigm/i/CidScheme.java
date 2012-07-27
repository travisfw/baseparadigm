package org.baseparadigm.i;

/**
 * An important thing about CidScheme that can't be specified in this interface
 * is the implementation of CidScheme#equals. equals should be true only if
 * identical bytes will be given identical content ids under this scheme.
 * 
 * @author travis@traviswellman.com
 */
public interface CidScheme extends ToByteArray {

    /**
     * A CidScheme creates content ids. It is not the responsibility of the
     * CidScheme to pair the value with the content id, so refrain from
     * returning a ResolvableId. This is because the pairing necessarily uses
     * more memory and the nature of the pairing should be managed separately.
     * 
     * @param value
     * @return a content id that can be used to retrieve the given content from
     *         a repository.
     */
    ContentId keyFor(byte[] value);

    /**
     * like {@link CidScheme#keyFor(byte[])} but without the {@link ContentId}
     * @param value
     * @return
     */
    byte[] bytesFor(byte[] value);

    /**
     * If two CidSchemes return the same scheme hash they are the same scheme.
     */
    long schemeHash();

    /**
     * Data structures (such as SetDatum and MapDatum) are constructed by
     * appending content ids without delimiters. These binary data structures
     * are parsed by counting bytes, which is why key length is a necessary
     * attribute of the content id scheme.
     * 
     * @return the length of a content id in this scheme
     */
    int getKeyLength();

//    /**
//     * The six edge keys may be hardcoded for optimization.
//     */
    // TODO IContentId keyFor(SubjectPredicateObject spo);
    // in groovysh:
    // c = 0; d = java.security.MessageDigest.getInstance("SHA-512");
    // d.digest("OBJECTS".getBytes()).each{ b -> print("$b, "); if(++c % 8 == 0) println(); }
}

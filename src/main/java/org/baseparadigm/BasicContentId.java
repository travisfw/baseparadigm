package org.baseparadigm;

import java.util.Arrays;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.StringyScheme;

/**
 * 
 * @author travis@traviswellman.com
 */
public class BasicContentId implements ContentId {

    /**
     * These bytes come from {@link CidScheme#bytesFor(byte[])}.
     * Similarly to how most days you probably refrain from hitting your
     * computer with a hammer, refrain from changing the bytes in this array
     * because things will stop working if you do so.
     */
    protected final byte[] bytes;
    protected final CidScheme cidScheme;
    @Override
    public CidScheme getCidScheme() {
        return cidScheme;
    }

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 492010517573211306L;

    /**
     * Similar to new BigInteger(bytes);
     * 
     * @param cidScheme
     *            The content id scheme that defines the key length.
     * 
     * @param bytes
     *            The integer number of the id in byte form. This array should
     *            have the length of the key length for the BaseParadigm given.
     */
    public BasicContentId(CidScheme cidScheme, byte[] bytes) {
        assert cidScheme != null;
        assert bytes != null;
        assert cidScheme.getKeyLength() == bytes.length : cidScheme.getKeyLength() +" != "+ bytes.length ;
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.cidScheme = cidScheme;
    }

    /**
     * Like BigInteger's toByteArray, but ensures that the length of the byte
     * array is correct.
     */
    @Override
    public byte[] toByteArray() {
        assert cidScheme.getKeyLength() == bytes.length :
            "Somehow you obtained an id of a different size than your repo supports. It is "+ bytes.length +" bytes. It should be "+ cidScheme.getKeyLength();
        return Arrays.copyOf(bytes, bytes.length);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 61;
        int result = 1;
        result = prime * result + Arrays.hashCode(bytes);
        result = prime * result
                + ((cidScheme == null) ? 0 : cidScheme.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (! (o instanceof ContentId)) return false;
        ContentId cid = (ContentId) o;
        return cid.getCidScheme().equals(this.cidScheme)
                && Arrays.equals(cid.toByteArray(), bytes);
    }

    @Override
    public BasicContentId clone() {
        return new BasicContentId(cidScheme, toByteArray());
    }

    @Override
    public int compareTo(ContentId o) {
        return Util.contentIdComparator.compare(this, o);
    }
    
    @Override
    public String toString() {
        if (cidScheme instanceof StringyScheme)
            return ((StringyScheme)cidScheme).toCharSequence(this).toString();
        return Util.toHex(bytes).toString();
    }
}

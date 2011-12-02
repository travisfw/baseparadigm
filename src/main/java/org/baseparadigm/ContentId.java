package org.baseparadigm;

import java.math.BigInteger;


/**
 * A BigInteger aware of the length of byte arrays.
 * 
 * @author travis@traviswellman.com
 *
 */
public class ContentId extends BigInteger implements ToByteArray {
    public final Repo repo;

    private static final long serialVersionUID = 492010517573211305L;

    /**
     * Similar to new BigInteger(bytes);
     * 
     * @param repo
     * The repo that defines the key length.
     * 
     * @param bytes
     * The integer number of the id in byte form.
     * This array should have the length of the key length for the BaseParadigm given.
     */
    public ContentId(Repo repo, byte[] bytes) {
        super(bytes);
        assert repo.keyLength == bytes.length;
        this.repo = repo;
    }

    public ContentId(Repo repo, BigInteger key) {
        super(key.toByteArray());
        this.repo = repo;
    }

    /**
     * Like BigInteger's toByteArray, but ensures that the length of the byte array is correct.
     */
    @Override
    public byte[] toByteArray() {
        byte[] ba = new byte[repo.keyLength];
        byte[] orig = super.toByteArray();
        if (ba.length < orig.length)
            throw new RuntimeException("Somehow you obtained an id bigger than your repo supports.");
        // ba.length > orig.length
        // the expected case where the random number is simply small and needs to be left padded.
        // there is a > 1/256 chance of this for every id
        System.arraycopy(orig, 0, ba, ba.length -orig.length, orig.length);
        assert super.equals(new BigInteger(ba));
        return ba;
    }

    /**
     * this.repo.get(this)
     */
    public byte[] resolve() {
        return repo.get(this);
    }
    
    @Override
    public boolean equals(Object o) {
        if (! (o instanceof ContentId))
            return o instanceof BigInteger
                    && o.equals(this);
        return o instanceof ContentId
                && ((ContentId)o).repo == this.repo
                && super.equals(o);
    }
    
    @Override
    public ContentId clone() {
        return new ContentId(repo, toByteArray());
    }
}

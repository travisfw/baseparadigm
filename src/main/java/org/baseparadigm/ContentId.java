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
     *            The repo that defines the key length.
     * 
     * @param bytes
     *            The integer number of the id in byte form. This array should
     *            have the length of the key length for the BaseParadigm given.
     */
    public ContentId(Repo repo, byte[] bytes) {
        super(bytes);
        assert repo.keyLength == bytes.length;
        this.repo = repo;
    }

    public ContentId(Repo repo, BigInteger key) {
        super(key.toByteArray());
        assert key.toByteArray().length <= repo.keyLength;
        this.repo = repo;
    }

    private static boolean allZeroes(byte[] ba) {
        for (byte b : ba)
            if (b != 0)
                return false;
        return true;
    }

    // static because the constructor uses it to call super
    // ie, the repo property on ContentID instances is not yet set
    private static byte[] pad(Repo repo, byte[] padBytes) {
        byte[] keyBytes = new byte[repo.keyLength];
        assert allZeroes(keyBytes) :
            "this java implementation fills new byte arrays with: "+ keyBytes[0];
        System.arraycopy(padBytes, 0, keyBytes, keyBytes.length
                - padBytes.length, padBytes.length);
        return keyBytes;
    }

    /**
     * Like BigInteger's toByteArray, but ensures that the length of the byte
     * array is correct.
     */
    @Override
    public byte[] toByteArray() {
        byte[] orig = super.toByteArray();
        if (repo.keyLength < orig.length)
            throw new RuntimeException(
                    "Somehow you obtained an id bigger than your repo supports.");
        // there is a > 1/256 chance of padding being necessary
        byte[] ba = pad(repo, orig);
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
        if (!(o instanceof ContentId))
            return o instanceof BigInteger && o.equals(this);
        return o instanceof ContentId && ((ContentId) o).repo == this.repo
                && super.equals(o);
    }

    @Override
    public ContentId clone() {
        return new ContentId(repo, toByteArray());
    }
}

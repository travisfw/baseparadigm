package org.baseparadigm;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.StringyScheme;


public class CommonsCidScheme implements StringyScheme, Serializable {
    private static final long serialVersionUID = 4699398483103310297L;
    public static final String ID_ALGORITHM = "SHA-512".intern();
    private static CommonsCidScheme instance = new CommonsCidScheme();
    public static CommonsCidScheme getInstance() { return instance; }
    @Override
    public int getKeyLength() {
        return 64;
    }
    private MessageDigest md;
    {
        try {
            md = MessageDigest.getInstance(ID_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new Error("depending on "+ ID_ALGORITHM +" being available.", e);
        }
    }
    
    /**
     * The empty set (and {@link CommonsCidScheme#emptyMap}) is immutable and
     * build methods will return new instances.
     */
    public static final SetDatum emptySet = new SetDatum(CommonsCidScheme.getInstance()).buildFinish();
    public static final MapDatum emptyMap = new MapDatum(CommonsCidScheme.getInstance()).buildFinish();
    
    @Override
    public ContentId keyFor(byte[] value) {
        final byte[] key = bytesFor(value);
        return new ContentId() {
            @Override
            public byte[] toByteArray() {
                return key;
            }
            @Override
            public int compareTo(ContentId o) {
                return Util.contentIdComparator.compare(this, o);
            }
            @Override
            public CidScheme getCidScheme() {
                return CommonsCidScheme.this;
            }
            @Override
            public String toString() {
                return CommonsCidScheme.this.toCharSequence(this).toString();
            }
        };
    }
    @Override
    public byte[] bytesFor(byte[] value) {
        return md.digest(value);
    }
    @Override
    public int hashCode() {
        return 53 + ID_ALGORITHM.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (! (obj instanceof CidScheme))
            return false;
        CidScheme other = (CidScheme) obj;
        return other.schemeHash() == schemeHash() &&
                ! (other instanceof NullCidScheme);
    }
    @Override
    public long schemeHash() {
        return serialVersionUID;
    }
    @Override
    public byte[] toByteArray() {
        return BigInteger.valueOf(schemeHash()).toByteArray();
    }
    @Override
    public CharSequence toCharSequence(ContentId contentId) {
        assert contentId.getCidScheme().equals(this) : "contentId must be from this scheme";
        return Util.toHex(contentId.toByteArray());
    }
    @Override
    public ContentId fromCharSequence(CharSequence stringyCid) {
        assert stringyCid.length() == getKeyLength()*2;
        return new BasicContentId(this, Util.fromHex(stringyCid));
    }
}

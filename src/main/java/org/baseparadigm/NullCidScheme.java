package org.baseparadigm;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;

public class NullCidScheme implements CidScheme{

    protected static CidScheme instance = null;
    
    public static CidScheme instance() {
        if (instance == null)
            instance = new NullCidScheme();
        return instance;
    }

    protected static byte[] byteRep = NullCidScheme.class.getName().getBytes(Util.defaultCharset);
    @Override
    public byte[] toByteArray() {
        return byteRep;
    }

    @Override
    public ContentId keyFor(byte[] value) {
        unsupported();
        return null;
    }

    @Override
    public byte[] bytesFor(byte[] value) {
        unsupported();
        return null;
    }
    
    private void unsupported() {
        throw new UnsupportedOperationException(NullCidScheme.class.getName() + " does not actually create content ids");
    }

    @Override
    public long schemeHash() {
        return 0;
    }

    @Override
    public int getKeyLength() {
        return 0;
    }

    /**
     * NullCidScheme equals nothing, including itself.
     */
    public boolean equals(Object o) {
        return false;
    }
}

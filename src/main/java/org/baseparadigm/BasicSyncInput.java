package org.baseparadigm;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ResolvableId;
import org.baseparadigm.i.SyncInput;

public class BasicSyncInput implements SyncInput {
    /**
     * The source for this input.
     */
    protected final InputStream in;
    /**
     * All incoming content should have addresses according to this scheme or an
     * error will be thrown and the stream will halt.
     */
    public final CidScheme cidScheme;

    public BasicSyncInput(InputStream is) throws IOException {
        this.in = is;
        cidScheme = Util.availableCidSchemes().get(readInteger());
    }

    /*
     * Reads one byte to determine how many bytes to read into a byte array to
     * construct a {@link BigInteger}
     * 
     * @return a 2048 bit signed integer
     * 
     * @throws IOException if reading from the {@link InputStream} throws an
     * exception or if the integer is incompletely read.
     */
    private BigInteger readInteger() throws IOException {
        int b = in.read();
        if (b == -1)
            return null;
        if (b == 0)
            return BigInteger.ZERO;
        byte[] buf = new byte[b];
        int nbrRead = in.read(buf);
        if (nbrRead != buf.length)
            throw new IOException(
                    "unable to read as many bytes as stated for this section");
        return new BigInteger(buf);
    }

    @Override
    public ResolvableId read() throws IOException {
        final byte[] nextContent = new byte[readInteger().intValue()];
        int nbrRead = in.read(nextContent);
        if (nbrRead != nextContent.length)
            throw new IOException(
                    "unable to read as many bytes as stated for this section");
        final byte[] cidBytes = new byte[cidScheme.getKeyLength()];
        nbrRead = in.read(cidBytes);
        if (nbrRead != cidBytes.length)
            throw new IOException(
                    "unable to read as many bytes as stated for this section");
        ResolvableId ret = new ContentIdResolvable(cidScheme, nextContent);
        if (!Arrays.equals(ret.toByteArray(), cidBytes))
            throw new IOException("mismatched content id");
        return ret;
    }

    @Override
    public CidScheme getCidScheme() {
        return cidScheme;
    }

    /**
     * calls {@link InputStream#close()}
     */
    @Override
    public void close() throws IOException {
        in.close();
    }
}

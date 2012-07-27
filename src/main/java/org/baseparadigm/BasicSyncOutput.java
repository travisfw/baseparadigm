package org.baseparadigm;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ResolvableId;
import org.baseparadigm.i.SyncOutput;

public class BasicSyncOutput implements SyncOutput {
    protected final OutputStream out;
    public final CidScheme cidScheme;

    /**
     * This constructor writes to the stream and so may block.
     * 
     * @param cids
     *            the CidScheme to initialize the stream
     * @param out
     *            where to write when calling
     *            {@link BasicSyncOutput#write(ResolvableId)}
     * @throws IOException
     *             if writing {@code cids} throws an IOException
     */
    public BasicSyncOutput(CidScheme cids, OutputStream out) throws IOException {
        this.out = out;
        this.cidScheme = cids;
        // First thing is to initialize the stream by writing the cid scheme
        byte[] cidsMark = BigInteger.valueOf(cids.schemeHash()).toByteArray();
        assert cidsMark.length < 256;
        this.out.write(cidsMark.length);
        this.out.write(cidsMark);
    }

    @Override
    public void write(ResolvableId cid) throws IOException {
        out.write(cid.resolve());
        out.write(cid.toByteArray());
    }

    @Override
    public CidScheme getCidScheme() {
        return cidScheme;
    }

    @Override
    public void close() throws IOException {
        out.flush();
        out.close();
    }
}

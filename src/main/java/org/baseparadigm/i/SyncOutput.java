package org.baseparadigm.i;

import java.io.Closeable;
import java.io.IOException;

public interface SyncOutput extends HasCidScheme, Closeable {
    void write(ResolvableId cid) throws IOException;
}

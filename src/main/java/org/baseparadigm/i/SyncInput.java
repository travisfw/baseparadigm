package org.baseparadigm.i;

import java.io.Closeable;
import java.io.IOException;

/**
 * Basically an input stream of content addressable content.
 */
public interface SyncInput extends HasCidScheme, Closeable {
    ResolvableId read() throws IOException;
}

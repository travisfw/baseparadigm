package org.baseparadigm.i;

import java.net.URI;


/**
 */
public interface CidNamer {
    public URI name(ContentId cid);
    public ContentId reverse(URI cid);
    public SchemeNamer getSchemeNamer();
}

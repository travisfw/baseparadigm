package org.baseparadigm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    public static byte[] drain(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int r = 0;
        for (byte[] buf = new byte[512];
                (r = is.read(buf)) >= 0;
                baos.write(buf, 0, r)) {
            if (r == 0)
                Thread.yield();
        }
        assert is.read() == -1;
        return baos.toByteArray();
    }
    
    /**
     * create a basic metadata document around raw content containing its type
     */
    public static MapDatum metaType(ContentId cid, TypeValues type) {
        return new MapDatum(cid.repo)
        .build(MetadataFields.CONTENT, cid)
        .build(MetadataFields.TYPE, type);
    }
}

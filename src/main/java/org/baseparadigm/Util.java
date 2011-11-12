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
}

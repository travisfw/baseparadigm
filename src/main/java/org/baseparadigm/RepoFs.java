package org.baseparadigm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class RepoFs extends Repo {
    public File storageDir;
    public File getStorageDir() {return storageDir;}
    
    public RepoFs(File storageDir) {
        super();
        if (! storageDir.mkdirs())
            if (! storageDir.exists())
                throw new RuntimeException("can't use "+ storageDir.getAbsolutePath());
        this.storageDir = storageDir;
    }
    
    public File fileName(ContentId key) {
        // add maxrange to remove negative values, then get the hex string
        String filename = key.add(maxRange).toString(32);
        File pdir = new File(storageDir, filename.substring(0, 3));
        pdir.mkdirs();
        return new File(pdir, filename);
    }
    
    @Override
    public byte[] put(final ContentId key, final byte[] value) {
        File dest = fileName(key);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(dest));
            bos.write(value);
            bos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null)
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    @Override
    public byte[] get(Object key) {
        assert key instanceof ContentId && ((ContentId)key).repo == this;
        InputStream is = null;
        try {
            is = new FileInputStream(fileName((ContentId) key));
            return Util.drain(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public byte[] getBase32(String key) {
	// subtract maxRange because base32 is unsigned
	return get(new ContentId(this,
		new BigInteger(key, 32).subtract(maxRange)));
    }
}

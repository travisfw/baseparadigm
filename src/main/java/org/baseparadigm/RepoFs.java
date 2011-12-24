package org.baseparadigm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RepoFs extends Repo {
    public File storageDir;
    private Namer namer;
    public File getStorageDir() {return storageDir;}
    
    public RepoFs(File storageDir, Namer n) {
        super();
        this.namer = n;
        if (! storageDir.mkdirs())
            if (! storageDir.exists())
                throw new RuntimeException("can't use "+ storageDir.getAbsolutePath());
        this.storageDir = storageDir;
    }
    
    public File fileName(ContentId key) {
        String filename = namer.name(key.toByteArray());
        assert filename.length() >= 3;
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
        ContentId cid;
        if (key instanceof String)
            cid = new ContentId(this, namer.reverse((String)key));
        else
            cid = (ContentId)key; // class cast exceptions here are good
        assert cid.repo == this;
        InputStream is = null;
        try {
            is = new FileInputStream(fileName(cid));
            return Util.drain(is);
        } catch (FileNotFoundException fnfe) {
            // per the contract for a Map when there is no mapping for the key
            return null;
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
    
    /**
     * A repo storing data in files needs to be told what to name the files.
     */
    public static interface Namer {
        public String name(byte[] cid);
        public byte[] reverse(String cid);
    }
}

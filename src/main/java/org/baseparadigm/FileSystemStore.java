package org.baseparadigm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.CidNamer;
import org.baseparadigm.i.RepoStorageWithDeletion;
import org.baseparadigm.i.SchemeNamer;

/**
 * An example subclass of Repo. Note that the Map method put(k, v) is overridden
 * because Repo implements IRepoMap. Other cases are possible where only IRepo
 * methods are of concern.
 */
public class FileSystemStore implements RepoStorageWithDeletion {
    public static final FSSSchemeNamer defaultSchemeNamer = new FSSSchemeNamer();
    public static class FSSSchemeNamer implements SchemeNamer {
        protected FSSSchemeNamer() {
            bind(CommonsCidScheme.getInstance(), "SHA512");
        }
        private Map<Object, Object> boundNames = new HashMap<>();
        @Override
        public CharSequence name(CidScheme cidsch) {
            CharSequence ret = (CharSequence) boundNames.get(cidsch);
            assert ret != null;
            return ret;
        }
        @Override
        public CidScheme reverse(CharSequence nam) {
            CidScheme ret = (CidScheme) boundNames.get(nam);
            assert ret != null;
            return ret;
        }
        public void bind(CidScheme cids, String name) {
            boundNames.put(name, cids);
            boundNames.put(cids, name);
        }
    }
    public static final FSSCidNamer defaultNamer = new FSSCidNamer();
    public static class FSSCidNamer implements CidNamer {
        protected FSSCidNamer() {};
        @Override
        public URI name(ContentId cid) {
            CharSequence a = getSchemeNamer().name(cid.getCidScheme());
            CharSequence b = Util.toHex(cid.toByteArray());
            try {
                return new URI(a.toString(), b.toString(), null);
            } catch (URISyntaxException e) {
                throw new Error(e);
            }
        }
        @Override
        public ContentId reverse(URI nam) {
            assert nam.getScheme() != null;
            CidScheme cidsch = getSchemeNamer().reverse(nam.getScheme());
            byte[] cidNbr = Util.fromHex(nam.getRawSchemeSpecificPart());
            return new BasicContentId(cidsch, cidNbr);
        }
        @Override
        public SchemeNamer getSchemeNamer() {
            return FileSystemStore.defaultSchemeNamer;
        }
    }
    public File storageDir;
    private final CidNamer namer;
    
    public File getStorageDir() {return storageDir;}
    
    public FileSystemStore(File storageDir, CidNamer n) {
        this.namer = n;
        this.storageDir = storageDir;
        validateStorageDir();
    }
    
    public FileSystemStore(CidNamer n) {
        this.namer = n;
        this.storageDir = getDefaultStorageDir();
        validateStorageDir();
    }
    
    public FileSystemStore(File storageDir) {
        this.namer = defaultNamer;
        this.storageDir = storageDir;
        validateStorageDir();
    }
    
    public FileSystemStore() {
        this.namer = defaultNamer;
        this.storageDir = getDefaultStorageDir();
        validateStorageDir();
    }
    
    private File getDefaultStorageDir() {
        return new File(System.getProperty("user.home") +File.separator +".baseparadigm");
    }

    private void validateStorageDir() {
        if (! storageDir.exists() && ! storageDir.mkdirs())
            throw new IllegalArgumentException("does not exist and can't create:\n"+ storageDir.getAbsolutePath());
        if ( ! storageDir.canRead() || ! storageDir.canWrite())
            throw new IllegalArgumentException("not readable or not writable:\n"+ storageDir.getAbsolutePath());
        if (! storageDir.isDirectory())
            throw new IllegalArgumentException("not a directory: "+ storageDir.getAbsolutePath());
    }

    public File fileName(ContentId key) {
        URI uri = namer.name(key);
        CharSequence filename = uri.getRawSchemeSpecificPart();
        assert filename.length() >= 3;
        File pdir = new File(storageDir,
                uri.getScheme() + File.separator +
                filename.subSequence(0, 3).toString()
                );
        pdir.mkdirs();
        return new File(pdir, filename.toString());
    }
    
    private static byte[] drainNoException(File f){
        try {
            return Util.drain(f);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }
    
    /**
     * always returns null;
     */
    @Override
    public void put(final ContentId key, final byte[] value) {
        File dest = fileName(key);
        if (dest.exists()) { // optimize for when the content is already there
            assert Arrays.equals(value, drainNoException(dest));
        }
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
    }

    @Override
    public byte[] get(ContentId cid) {
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
    
    @Override
    public boolean containsKey(ContentId key) {
        return fileName(key).exists();
    }
    
    @Override
    public void remove(ContentId key) {
        File f = fileName(key);
        if (! f .delete()) {
            // perhaps this should be a org.baseparadigm.ContentAccessException and be checked vs runtime?
            throw new RuntimeException("removal of content failed "+ key +" at file "+ f.getAbsolutePath());
        }
    }

    public byte[] get(String key) {
        try {
            return get(namer.reverse(new URI(key)));
        } catch (URISyntaxException e) {
            return null;
        }
    }
}

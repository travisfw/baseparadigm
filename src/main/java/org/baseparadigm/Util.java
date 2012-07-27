package org.baseparadigm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.Repo;
import org.baseparadigm.i.ToByteArray;

public class Util {
    
    public static final Charset defaultCharset = Charset.forName("UTF8");
    
    /**
     * If assertions are enabled, set this to true to enable the more rigorous
     * assertions at potentially great cost.
     */
    public static boolean intensiveAssertions = false;
    
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
        return new MapDatum(cid.getCidScheme())
        .build(MetadataFields.CONTENT, cid)
        .buildType(type);
    }

    public static byte[] drain(File f) throws IOException {
        return Util.drain(new BufferedInputStream(new FileInputStream(f)));
    }
    
    private static final String HEXES = "0123456789ABCDEF";
    public static CharSequence toHex( byte[] raw ) {
      if ( raw == null )
        return null;
      final StringBuilder hex = new StringBuilder( 2 * raw.length );
      for ( final byte b : raw ) {
        hex.append(HEXES.charAt((b & 0xF0) >> 4))
           .append(HEXES.charAt((b & 0x0F)));
      }
      return hex;
    }
    public static byte[] fromHex( CharSequence hex ) {
        if ( hex == null )
            return null;
        // http://stackoverflow.com/a/140861
        int len = hex.length();
        assert len %2 == 0;
        final byte[] ret = new byte[ len / 2 ];
        for ( int i = 0; i < len ; i += 2 ) {
            ret[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                + Character.digit(hex.charAt(i+1), 16));
        }
        return ret;
    }
    static {
        assert "ABCD098700FFE5".equals(toHex(fromHex("ABCD098700FFE5")).toString()) : "ABCD098700FFE5 does not equal "+ toHex(fromHex("ABCD098700FFE5"));
    }
    
    /**
     * Box a byte array in a ToByteArray.
     * tba(b).toByteArray() == b
     */
    public static ToByteArray tba(final byte[] b) {
        return new ToByteArray() {
            @Override
            public byte[] toByteArray() {
                return b;
            }
        };
    }

    public static Comparator<ContentId> contentIdComparator = new Comparator<ContentId>() {
        @Override
        public int compare(ContentId o1, ContentId o2) {
            assert o1.getCidScheme().equals(o2.getCidScheme());
            byte[] bytea = o1.toByteArray();
            byte[] cytea = o2.toByteArray();
            for (int i = 0;
                    i < bytea.length;
                    i++) {
                int unsigA = 0xFF & bytea[i];
                int unsigB = 0xFF & cytea[i];
                if (unsigA < unsigB)
                    return -1;
                else if (unsigA > unsigB)
                    return 1;
            }
            return 0;
        }
    };
    
    private static Map<CidScheme, Repo> defaultRepos = new HashMap<>();
    
    private static ThreadLocal<Map<CidScheme, Repo>> tlDefaultRepos = new ThreadLocal<Map<CidScheme, Repo>>() {
        @Override
        protected Map<CidScheme, Repo> initialValue() {
            return new HashMap<CidScheme, Repo>();
        }
    };

    public static byte[] toByteArray(BigInteger bi, int len) {
        byte[] keyBytes = new byte[len];
        java.util.Arrays.fill(keyBytes, (bi.signum() == -1
                ? (byte)0xFF // negative values get padded with 0xFFs
                : (byte)0x00 // positive values get padded with zeros
                ));
        byte[] bia = bi.toByteArray();
        System.arraycopy(bia, 0, keyBytes, keyBytes.length - bia.length, bia.length);
        return keyBytes;
    }

    /**
     * If there isn't a thread local default, this method synchronizes with setDefaultRepo.
     * @return the thread local default repo if one has been set or else the static default.
     */
    public static Repo getDefaultRepo(CidScheme cids) {
        Repo tl = tlDefaultRepos.get().get(cids);
        if (tl != null) return tl;
        synchronized(defaultRepos) {
            return defaultRepos.get(cids);
        }
    }
    /**
     * A useful utility for setting the default for content ids to resolve against. use unsetThreadLocalDefaultRepo(CidScheme) when done.
     */
    public static void setThreadLocalDefaultRepo(Repo repo) {
        tlDefaultRepos.get().put(repo.getCidScheme(), repo);
    }
    public static void unsetThreadLocalDefaultRepo(CidScheme cids) {
        tlDefaultRepos.get().remove(cids);
    }
    /**
     * this method is synchronized
     * @param repo
     */
    public static void setDefaultRepo(Repo repo) {
        synchronized (defaultRepos) {
            defaultRepos.put(repo.getCidScheme(), repo);
        }
    }
    public static void unsetDefaultRepo(CidScheme cids) {
        synchronized (defaultRepos) {
            defaultRepos.remove(cids);
        }
    }

    private static SortedMap<BigInteger, ? extends CidScheme> availableCidSchemes;
    public static Map<BigInteger, CidScheme> availableCidSchemes() {
        return Collections.unmodifiableSortedMap(availableCidSchemes);
    }
    
    /**
     * Same as {@link Repo#get(ContentId)} but it throws an exception if {@code get} returns null.
     * @param r the repo to retrieve from
     * @param cid the id for the content to retrieve
     * @return the content from r
     * @throws NotInRepoException if the content is not in the repo
     */
    public static byte[] repoGet(Repo r, ContentId cid) throws NotInRepoException {
        byte[] gotten = r.get(cid);
        if (gotten == null)
            throw new NotInRepoException(cid.toString());
        return gotten;
    }
}

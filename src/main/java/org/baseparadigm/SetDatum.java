package org.baseparadigm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.HasContentId;
import org.baseparadigm.i.Repo;
import org.baseparadigm.i.ResolvableId;
import org.baseparadigm.i.Stuffable;
import org.baseparadigm.i.Stuffed;
import org.baseparadigm.i.ToByteArray;

import static org.baseparadigm.Util.repoGet;

/**
 * DatumSet and DatumMap are core elements of the BaseParadigm data model.
 * They should both serialize and deserialize to/from a byte stream containing uniform length ContentIds.
 * 
 * For example, for a repo that has length two content ids, a file
 * containing four bytes could be deserialized into a set of two items.
 * 
 * @author travis@traviswellman.com
 *
 */
public class SetDatum implements SortedSet<ContentId>, ToByteArray, HasContentId, Stuffable, Cloneable {
    public static SetDatum empty = new SetDatum(NullCidScheme.instance()).buildFinish();
    public SortedSet<ContentId> backingSet = null;
    public CidScheme cidScheme;
    private ResolvableId cachedId = null;

    private boolean isMutable = false;
    private boolean initialized = false;
    
    /**
     * Constructs an immutable representation of what was previously stuffed.
     * @param stuffed from {@link SetDatum#stuff(Repo)}
     * @throws NotInRepoException 
     */
    public SetDatum(Stuffed stuffed) {
        this.cidScheme = stuffed.getCidScheme();
        isMutable = false;
        initialized = false;
        init(stuffed);
        assert backingSet != null;
        assert initialized == true;
    }
    
    /**
     * immutable set of content ids given the scheme.
     * @param cids
     * @param uris
     */
    public SetDatum(CidScheme cids, Set<URI> uris) {
        backingSet = new TreeSet<ContentId>();
        this.cidScheme = cids;
        for (URI uri : uris)
            backingSet.add(cids.keyFor(uri.toString().getBytes(Util.defaultCharset)));
        isMutable = false;
        backingSet = Collections.unmodifiableSortedSet(backingSet);
        initialized = true;
    }
    
    /**
     * Mutable
     * @param cids
     */
    public SetDatum(CidScheme cids) {
        this.cidScheme = cids;
        this.backingSet = new TreeSet<ContentId>();
        isMutable = true;
        initialized = true;
    }
    
    /**
     * Same as {@link SetDatum#SetDatum(CidScheme)} where the {@link CidScheme} is taken from the {@link Repo} given.
     * @param cidScheme
     */
    public SetDatum(Repo repo) {
        this.cidScheme = repo.getCidScheme();
        this.backingSet = new TreeSet<ContentId>();
        isMutable = true;
        initialized = true;
    }
    
    /**
     * Immutable
     * @param value
     */
    public SetDatum(SortedSet<ContentId> value) {
        // obviously this will throw an exception if the set is empty.
        // in that case another constructor should be used.
        cidScheme = value.iterator().next().getCidScheme();
        backingSet = Collections.unmodifiableSortedSet(value);
        isMutable = false;
        initialized = true;
    }
    
    /**
     * same as public SetDatum(SortedSet<ContentId> value) but is not necessarily mutable
     * @param value
     * the set of content to begin with
     * @param isMutable
     * whether to allow further mutation
     */
    public SetDatum(SortedSet<ContentId> value, boolean isMutable) {
        // obviously this will throw an exception if the set is empty.
        // in that case another constructor should be used.
        cidScheme = value.iterator().next().getCidScheme();
        this.isMutable = isMutable;
        backingSet = (isMutable
                ? new TreeSet<ContentId>(value)
                : Collections.unmodifiableSortedSet(new TreeSet<ContentId>(value)));
        initialized = true;
    }
    
    protected SetDatum(CidScheme cs, SortedSet<ContentId> value) {
        cidScheme = cs;
        backingSet = value;
        assert backingSet.size() == 0 || backingSet.iterator().next().getCidScheme().equals(cidScheme);
        isMutable = false;
        initialized = true;
    }
    
    public SetDatum() {
        backingSet = null;
        initialized = false;
        isMutable = false;
    }
    
    @Override
    public Object clone() {
        TreeSet<ContentId> newBackingSet = new TreeSet<ContentId>(backingSet);
        return new SetDatum(cidScheme, newBackingSet);
    }
    
    @Override // Stuffable
    public void init(Stuffed stuffed) {
        assert !initialized;
        assert !isMutable;
        ResolvableId cid = stuffed.getTopId();
        this.cidScheme = cid.getCidScheme();
        
        SortedSet<ContentId> toFill = new TreeSet<ContentId>();
        byte[] thisSetDatumInBinary = cid.resolve();
        assert thisSetDatumInBinary != null : "could not get content for:\n"+ cid;
        InputStream ids = new ByteArrayInputStream(thisSetDatumInBinary);
        try {
            byte[] val = new byte[cid.getCidScheme().getKeyLength()];
            for(int nbrRead = ids.read(val);
                    nbrRead == val.length;
                    nbrRead = ids.read(val)) {
                ContentId cidItem = new BasicContentId(cid.getCidScheme(), val);
                toFill.add(new ContentIdResolvable(cidItem, stuffed.get(cidItem)));
            }
            assert ids.read() == -1;
        } catch (IOException ioe) {
            assert false : "there is something drastically wrong if a ByteArrayInputStream throws an IOException";
        }
        backingSet = toFill;
        initialized = true;
    }
    

    /**
     * Put a URI into the set. If the set is not mutable a clone will be returned.
     * 
     * @param uri
     * this will be inserted into the backing repo and it's id will become one of the set.
     * 
     * @return
     * the instance called or a clone.
     */
    public SetDatum build(URI uri) {
        if (! isMutable) {
            SetDatum ret = (SetDatum) clone();
            ret.add( ret.cidScheme.keyFor(
                    uri.toString().getBytes(Util.defaultCharset)
                    ));
            return ret;
        }
        add( cidScheme.keyFor(
                uri.toString().getBytes(Util.defaultCharset)
                ));
        return this;
    }
    
    public SetDatum build(ContentId cid) {
        assert cidScheme.equals(cid.getCidScheme());
        if (! isMutable) {
            SetDatum ret = (SetDatum) clone();
            ret.add(cid);
            return ret;
        }
        add(cid);
        return this;
    }

    public SetDatum build(Set<? extends ContentId> data) {
        if (isMutable) {
            for (ContentId gd : data)
                build(gd);
            return this;
        }
        SetDatum ret = this;
        for (ContentId gd : data)
            ret = ret.build(gd);
        return ret;
    }

    @Override
    public boolean add(ContentId object) {
        assert object.getCidScheme().equals(getCidScheme());
        assert isMutable;
        return backingSet.add(object);
    }

    @Override
    public boolean addAll(Collection<? extends ContentId> arg0) {
        assert isMutable;
        return backingSet.addAll(arg0);
    }

    @Override
    public void clear() {
        assert isMutable;
        backingSet.clear();
    }

    @Override
    public boolean contains(Object object) {
        assert object instanceof HasContentId : "the object to compare must be a HasContentId. it is a "+ object.getClass().getCanonicalName();
        return backingSet.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        return backingSet.containsAll(arg0);
    }

    @Override
    public boolean isEmpty() {
        return backingSet.isEmpty();
    }

    @Override
    public Iterator<ContentId> iterator() {
        return backingSet.iterator();
    }

    @Override
    public boolean remove(Object object) {
        return backingSet.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        return backingSet.removeAll(arg0);
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        return backingSet.retainAll(arg0);
    }

    @Override
    public int size() {
        return backingSet.size();
    }

    @Override
    public Object[] toArray() {
        return backingSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return backingSet.toArray(array);
    }
    @Override
    public byte[] toByteArray() {
        if (isEmpty())
            return new byte[0];
        assert cidScheme != null;
        int keyLength = cidScheme.getKeyLength();
        byte[] ret = new byte[keyLength *size()];
        int offset = 0;
        for (ContentId i : this) {
//            assert i.getCidScheme().equals(getCidScheme());
            System.arraycopy(i.toByteArray(), 0, ret, offset, keyLength);
            offset += keyLength;
        }
        return ret;
    }

    @Override
    public Comparator<? super ContentId> comparator() {
        return backingSet.comparator();
    }

    @Override
    public ContentId first() {
        return backingSet.first();
    }

    @Override
    public SortedSet<ContentId> headSet(ContentId end) {
        return backingSet.headSet(end);
    }

    @Override
    public ContentId last() {
        return backingSet.last();
    }

    @Override
    public SortedSet<ContentId> subSet(ContentId start, ContentId end) {
        return backingSet.subSet(start, end);
    }

    @Override
    public SortedSet<ContentId> tailSet(ContentId start) {
        return backingSet.tailSet(start);
    }

    /**
     * make immutable
     */
    public SetDatum buildFinish() {
        isMutable  = false;
        return this;
    }

    @Override
    public int compareTo(HasContentId arg0) {
        return getId().compareTo(arg0.getId());
    }

    @Override
    public ResolvableId getId() {
        assert (!isMutable) && initialized;
        if (cachedId == null || isMutable) {
            Repo r = Util.getDefaultRepo(cidScheme);
            if (r != null) {
                ContentId cid = r.put(this.toByteArray());
                if (cid instanceof ResolvableId)
                    cachedId = (ResolvableId)cid;
                else
                    cachedId = new ContentIdResolvable(r, cid.toByteArray());
            } else
                cachedId = new ContentIdResolvable(cidScheme, toByteArray());
        }
        return cachedId;
    }
    
    @Override // Stuffable
    public Stuffed stuff(Repo r, CidScheme cids) throws NotInRepoException {
        if(! r.getCidScheme().equals(this.cidScheme))
            throw new IllegalArgumentException("provide a repo containing content accessible via the content ids in this stuffable.");
        if (cachedId == null || isMutable)
            cachedId = new ContentIdResolvable(getCidScheme(), this.toByteArray());
        LinearVirtualRepo lvr = new LinearVirtualRepo(cids);
        for (ContentId value : this)
            lvr.put(repoGet(r, value));
        return new VirtualRepoBackedStuffed(lvr, cachedId);
    }

    public boolean getIsMutable() {
        return isMutable;
    }

    @Override
    public CidScheme getCidScheme() {
        return cidScheme;
    }
}

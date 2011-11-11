package org.baseparadigm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
public class SetDatum implements SortedSet<ContentId>, ToByteArray{
    public static SetDatum empty = new SetDatum(Repo.commons).buildFinish();
    public SortedSet<ContentId> backingSet = null;
    public Repo bp = null;
    public boolean isMutable = false;
    
    /**
     * Constructs an immutable representation of the set given by the datumId.
     * @param repo
     * @param datumId
     * an id that should point to a set in the given repo.
     */
    public SetDatum(Repo repo, BigInteger datumId) {
        this.bp = repo;
        this.backingSet = Collections.unmodifiableSortedSet(toSet(repo, datumId));
        isMutable = false;
    }
    
    /**
     * Inserts the given URIs into the given repo and constructs a Set of ids for them; immutable.
     * @param repo
     * @param uris
     */
    public SetDatum(Repo repo, Set<URI> uris) {
        backingSet = new TreeSet<ContentId>();
        this.bp = repo;
        try {
            for (URI uri : uris)
                backingSet.add(repo.put(uri.toString().getBytes("UTF8")));
        } catch (UnsupportedEncodingException e) { throw new Error(e); }
        isMutable = false;
        backingSet = Collections.unmodifiableSortedSet(backingSet);
    }
    
    /**
     * Mutable
     * @param repo
     */
    public SetDatum(Repo repo) {
        this.bp = repo;
        this.backingSet = new TreeSet<ContentId>();
        isMutable = true;
    }
    
    /**
     * Immutable
     * @param value
     */
    public SetDatum(SortedSet<ContentId> value) {
        // obviously this will throw an exception if the set is empty.
        // in that case another constructor should be used.
        bp = value.iterator().next().bp;
        backingSet = Collections.unmodifiableSortedSet(value);
        isMutable = false;
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
        bp = value.iterator().next().bp;
        this.isMutable = isMutable;
        backingSet = (isMutable
                ? value
                : Collections.unmodifiableSortedSet(new TreeSet<ContentId>(value)));
    }


    /**
     * Put a URI into the set.
     * 
     * @param uri
     * this will be inserted into the backing repo and it's id will become one of the set.
     * 
     * @return
     * the instance called.
     */
    public SetDatum build(URI uri) {
        try {
            add( bp.put(
                    uri.toString().getBytes("UTF8")
                    ));
        } catch (UnsupportedEncodingException e) { throw new Error(e); }
        return this;
    }

    /**
     * Put anything that can be converted into a byte array into the set.
     */
    public SetDatum build(ToByteArray data) {
        return this.build(data.toByteArray());
    }

    public SetDatum build(byte[] byteArray) {
        add(bp.put(byteArray));
        return this;
    }

    public static SetDatum fromData(Repo bp2, Set<? extends ToByteArray> graphData) {
        SetDatum ret = new SetDatum(bp2);
        ret.addAll(graphData);
        return ret;
    }

    public void addAll(Set<? extends ToByteArray> data) {
        for (ToByteArray gd : data)
            build(gd);
    }

    /**
     * The datumId must reference a set of references.
     * 
     * @param repo
     * Where to look up the datumId.
     * 
     * @param datumId
     * The id for the content of the set.
     */
    public static SortedSet<ContentId> toSet(Repo repo, BigInteger datumId) {
        SortedSet<ContentId> toFill = new TreeSet<ContentId>();
        InputStream ids = new ByteArrayInputStream(repo.get(datumId));
        try {
            byte[] val = new byte[repo.keyLength()];
            for(int nbrRead = ids.read(val);
                    nbrRead == val.length;
                    nbrRead = ids.read(val)) {
                toFill.add(new ContentId(repo, val));
            }
            assert ids.read() == -1;
        } catch (IOException ioe) {
            assert false : "there is something drastically wrong if a ByteArrayInputStream throws an IOException";
        }
        return toFill;
    }

    /**
     * Get the data for the content id and create a SetDatum from it.
     */
    public static SetDatum inflate(ContentId cid) {
        return new SetDatum(toSet(cid.bp, cid));
    }

    @Override
    public boolean add(ContentId object) {
        return backingSet.add(object);
    }

    @Override
    public boolean addAll(Collection<? extends ContentId> arg0) {
        return backingSet.addAll(arg0);
    }

    @Override
    public void clear() {
        backingSet.clear();
    }

    @Override
    public boolean contains(Object object) {
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
        int keyLength;
        if (bp == null) {
            ContentId containedId = backingSet.iterator().next();
            if (containedId.bp == null)
                keyLength = containedId.toByteArray().length;
            else keyLength = containedId.bp.keyLength;
        } else keyLength = bp.keyLength;
        byte[] ret = new byte[keyLength *size()];
        int offset = 0;
        for (ContentId i : this) {
            assert i.bp.keyLength == keyLength;
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

}

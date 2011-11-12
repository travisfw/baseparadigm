package org.baseparadigm;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Graph {
    public Set<Repo> repos = new HashSet<Repo>();
    public Repo primaryRepo;

    public MapDatum subjIdx;
    public MapDatum predIdx;
    public MapDatum objeIdx;
    public MapDatum assuIdx;
    public MapDatum authIdx;
    public MapDatum pattIdx;

    /**
     * An index of indices.
     */
    public Map<SubjectPredicateObject, MapDatum> idx = new HashMap<SubjectPredicateObject, MapDatum>();

    public Graph(Repo r) {
        repos.add(r);
        primaryRepo = r;
        
        subjIdx = new MapDatum(r);
        predIdx = new MapDatum(r);
        objeIdx = new MapDatum(r);
        assuIdx = new MapDatum(r);
        authIdx = new MapDatum(r);
        pattIdx = new MapDatum(r);

        idx.put(SubjectPredicateObject.SUBJECTS,    subjIdx);
        idx.put(SubjectPredicateObject.PREDICATES,  predIdx);
        idx.put(SubjectPredicateObject.OBJECTS,     objeIdx);
        idx.put(SubjectPredicateObject.ASSUMPTIONS, assuIdx);
        idx.put(SubjectPredicateObject.AUTHORS,     authIdx);
        idx.put(SubjectPredicateObject.PATTERNS,    pattIdx);
        idx = Collections.unmodifiableMap(idx);
        
    }
    

    /**
     * Equivalent to idx.get(which).get(query) but probably faster.
     * 
     * @param which
     * Which index to query.
     * 
     * @param contentId
     * What to query the index for.
     * 
     * @return
     * Content that has been registered in the index to contain the query.
     */
    public Set<ContentId> query(SubjectPredicateObject which, ContentId contentId){
        switch (which) {
        case SUBJECTS:
            return nullToEmptySet(subjIdx.get(contentId));
        case PREDICATES:
            return nullToEmptySet(predIdx.get(contentId));
        case OBJECTS:
            return nullToEmptySet(objeIdx.get(contentId));
        case ASSUMPTIONS:
            return nullToEmptySet(assuIdx.get(contentId));
        case AUTHORS:
            return nullToEmptySet(authIdx.get(contentId));
        case PATTERNS:
            return nullToEmptySet(authIdx.get(contentId));
        default:
            assert which == null : "which is not null? which: "+ which.toString() +"\nThis code should only be reachable with a null which.";
            throw new NullPointerException("which must be an instance of the enumeration SubjectPredicateObject");
        }
    }

    /**
     * Index the graph datum for future queries, and also alert subscriptions for which their pattern matches.
     * @param toIndex
     */
    public void insert(GraphDatum toIndex) {
        assert someEqual(repos, toIndex.bp);
        ContentId theId = toIndex.id(); // id() is where toIndex gets stored in toIndex.bp
        for (Repo r : repos) // store in the rest of the repos too
            if (toIndex.bp != r) r.put(toIndex);
        // find possible subscriptions
        for (SubjectPredicateObject spoaap : SubjectPredicateObject.values() ) {
            // each value will become a keyword. well, not a word, but same concept.
            Set<ContentId> values = toIndex.get(spoaap);
            // one of the five indexes
            MapDatum oneIdx = idx.get(spoaap);
            for (ContentId keyword : values)
                oneIdx.put(keyword, theId);
        }
        // TODO publish indexed datum to subscriptions?
    }

    private static Set<ContentId> nullToEmptySet(Set<ContentId> s) {
        if (s == null) return new HashSet<ContentId>();
        return s;
    }
    
    
    /**
     * A set of graph data that are supersets of the given GraphDatum.
     * 
     * @param query
     * The constraints that the result set should match.
     * 
     * @return
     * Query results.
     */
    public Set<ContentId> query(GraphDatum query){
        assert someEqual(repos, query.bp);
        Set<ContentId> ret = null;
        for (Map.Entry<ContentId, SetDatum> kv : query.entrySet()) {
            SubjectPredicateObject k = spoaapFor(kv.getKey());
            for (ContentId v : kv.getValue()) {
                if (ret == null)
                    ret = query(k, v);
                else
                    ret.retainAll(query(k, v));
            }
        }
        return nullToEmptySet(ret);
    }
    
    private boolean someEqual(@SuppressWarnings("rawtypes") Iterable it, Object eq) {
        for (Object i : it)
            if (i == eq) return true;
        return false;
    }

    /**
     * Convenience method for query(kv.getKey(), kv.getValue()).
     */
    public Set<ContentId> query(Entry<SubjectPredicateObject, ContentId> kv) {
        return query(kv.getKey(), kv.getValue());
    }

    /**
     * Union of queries: one for each of SubjectPredicateObject.
     * @param id
     * The key content that would be contained in each query result.
     */
    public Set<ContentId> queryAll(ContentId id) {
        Set<ContentId> ret = new HashSet<ContentId>();
        for (SubjectPredicateObject i : SubjectPredicateObject.values()) {
            ret.addAll(query(i, id));
        }
        return ret;
    }

    
    /**
     * If the key is one of the ids for spoaap, returns the appropriate spoaap, otherwise throws an IllegalArgumentException;
     */
    protected SubjectPredicateObject spoaapFor(ContentId key) {
        assert someEqual(repos, key.bp);
        if (key.equals(primaryRepo.SUBJECTSid))
            return SubjectPredicateObject.SUBJECTS;
        if (key.equals(primaryRepo.PREDICATESid))
            return SubjectPredicateObject.PREDICATES;
        if (key.equals(primaryRepo.OBJECTSid))
            return SubjectPredicateObject.OBJECTS;
        if (key.equals(primaryRepo.AUTHORSid))
            return SubjectPredicateObject.AUTHORS;
        if (key.equals(primaryRepo.ASSUMPTIONSid))
            return SubjectPredicateObject.ASSUMPTIONS;
        if (key.equals(primaryRepo.PATTERNSid))
            return SubjectPredicateObject.PATTERNS;
        throw new IllegalArgumentException("key is not a SubjectPredicateObject");
    }
    
}

package org.baseparadigm;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;


/**
 * A GraphDatum provides access to the fields enumerated in SubjectPredicateObject.
 * 
 */
public class GraphDatum extends MapDatum {
    /**
     * Construct an immutable GraphDatum.
     * 
     * @param repo
     * A BaseParadigm repo to back this GraphDatum.
     * 
     * @param datumId
     * This needs to be a complete MapDatum already contained in the given repo.
     */
    public GraphDatum(Repo repo, BigInteger datumId) {
        super(repo, datumId);
        assertContainsKeys();
    }
    
    /**
     * Same as public GraphDatum(BaseParadigm repo, BigInteger datumId)
     * but the ContentId specifies the repo.
     * 
     * @param datumId
     * This needs to be a complete MapDatum.
     */
    public GraphDatum(ContentId datumId) {
        super(datumId.repo, datumId);
        assertContainsKeys();
    }
    
    // with assertions disabled this is a no-op
    private void assertContainsKeys() {
        assert containsKey(repo.OBJECTSid);
        assert containsKey(repo.PREDICATESid);
        assert containsKey(repo.SUBJECTSid);
        assert containsKey(repo.ASSUMPTIONSid);
        assert containsKey(repo.AUTHORSid);
        assert containsKey(repo.PATTERNSid);
    }
    
    /**
     * A mutable GraphDatum; call buildFinish() to make immutable.
     */
    public GraphDatum(Repo repo) {
        super(repo);
    }
    
    /**
     * like MapDatum.put(ContentId, SetDatum)
     */
    public GraphDatum build(SubjectPredicateObject key, SetDatum data) {
        super.put(repo.idFor(key), data);
        return this;
    }
    
    /**
     * Add the ContentId to the set of values at this key. Does not replace.
     * 
     * @return this
     */
    public GraphDatum build(SubjectPredicateObject key, ContentId datum) {
        put(repo.idFor(key), datum);
        return this;
    }
    
    /**
     * Put the byte array into the set of values at this key. Does not replace.
     * 
     * @return this
     */
    public GraphDatum build(SubjectPredicateObject key, byte[] datum) {
        put(repo.idFor(key), repo.put(datum));
        return this;
    }
    
    public SetDatum get(SubjectPredicateObject which) {
        switch (which) {
        case SUBJECTS:
            return get(repo.SUBJECTSid);
        case PREDICATES:
            return get(repo.PREDICATESid);
        case OBJECTS:
            return get(repo.OBJECTSid);
        case AUTHORS:
            return get(repo.AUTHORSid);
        case ASSUMPTIONS:
            return get(repo.ASSUMPTIONSid);
        case PATTERNS:
            return get(repo.PATTERNSid);
        default:
            throw new Error("serious bug. all SubjectPredicateObject enumeration values not covered");
        }
    }

    /**
     * @param g
     * A GraphDatum that could be a subset of this one.
     * 
     * @return
     * true if each aaspo contains all of the ids the respective aaspo of the given graphdatum contains.
     */
    public boolean isSupersetOf(GraphDatum g) {
        for (SubjectPredicateObject i : SubjectPredicateObject.values())
            if (! get(i).containsAll(g.get(i)))
                return false;
        return true;
    }

    /**
     * @param negatives
     * a set of graph data that each could be a subset of this
     * 
     * @return
     * true if any of the given set are a subset of this GraphDatum.
     */
    public boolean isSupersetOfAny(Set<GraphDatum> negatives) {
        for (GraphDatum g : negatives)
            if (isSupersetOf(g))
                return true;
        return false;
    }

    /**
     * Convenience for getting the PATTERNS field and turning each into a GraphData manually.
     */
    public Set<Pattern> getPatterns() {
        Set<Pattern> ret = new HashSet<Pattern>();
        for (ContentId cid : get(repo.PATTERNSid))
            ret.add(PatternInflator.inflat(cid));
        return ret;
    }
    
    
}

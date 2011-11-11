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
        super(datumId.bp, datumId);
        assertContainsKeys();
    }
    
    // with assertions disabled this is a no-op
    private void assertContainsKeys() {
        assert containsKey(bp.OBJECTSid);
        assert containsKey(bp.PREDICATESid);
        assert containsKey(bp.SUBJECTSid);
        assert containsKey(bp.ASSUMPTIONSid);
        assert containsKey(bp.AUTHORSid);
        assert containsKey(bp.PATTERNSid);
    }
    
    /**
     * A mutable GraphDatum; call buildFinish() to make immutable.
     */
    public GraphDatum(Repo bp) {
        super(bp);
    }
    
    /**
     * like MapDatum.put(ContentId, SetDatum)
     */
    public GraphDatum build(SubjectPredicateObject key, SetDatum data) {
        super.put(bp.idFor(key), data);
        return this;
    }
    
    /**
     * Add the ContentId to the set of values at this key. Does not replace.
     * 
     * @return this
     */
    public GraphDatum build(SubjectPredicateObject key, ContentId datum) {
        put(bp.idFor(key), datum);
        return this;
    }
    
    /**
     * Put the byte array into the set of values at this key. Does not replace.
     * 
     * @return this
     */
    public GraphDatum build(SubjectPredicateObject key, byte[] datum) {
        put(bp.idFor(key), bp.put(datum));
        return this;
    }
    
    public SetDatum get(SubjectPredicateObject which) {
        switch (which) {
        case SUBJECTS:
            return get(bp.SUBJECTSid);
        case PREDICATES:
            return get(bp.PREDICATESid);
        case OBJECTS:
            return get(bp.OBJECTSid);
        case AUTHORS:
            return get(bp.AUTHORSid);
        case ASSUMPTIONS:
            return get(bp.ASSUMPTIONSid);
        case PATTERNS:
            return get(bp.PATTERNSid);
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
        for (ContentId cid : get(bp.PATTERNSid))
            ret.add(PatternInflator.inflat(cid));
        return ret;
    }

    /**
     * Checks that the GraphDatum matches somewhere in the pattern.
     */
    public boolean isPieceOfPattern(GraphData pattern) {
        throw new Error("unimplemented");
    }
    
    
}

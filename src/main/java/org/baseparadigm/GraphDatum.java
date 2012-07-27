package org.baseparadigm;

import java.util.Set;
import java.util.TreeMap;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.Repo;
import org.baseparadigm.i.Stuffed;


/**
 * A GraphDatum is a {@link MapDatum} that provides access to the fields
 * enumerated in {@link SubjectPredicateObject} more efficiently. Being a
 * MapDatum, a GraphDatum (aka "graph edge" or "edge document") may contain
 * fields other than the six: SUBJECTS, PREDICATES, OBJECTS, AUTHORS,
 * ASSUMPTIONS, PATTERNS. One such field which will commonly be present is
 * SIGNATURES which will contain nodes verifying the authorship of the values of
 * AUTHORS.
 * 
 */
public class GraphDatum extends MapDatum {
    /**
     * Construct an immutable GraphDatum.
     * 
     * @param datumId reference to a serialized graph edge
     */
    public GraphDatum(Repo r, ContentId datumId) {
        super(r, datumId);
    }
    
    /**
     * A mutable GraphDatum; call buildFinish() to make immutable.
     */
    public GraphDatum(CidScheme scheme) {
        super(scheme);
    }
    
    public GraphDatum(CidScheme cids, TreeMap<ContentId, SetDatum> newBackingMap, boolean isMutable) {
        super(cids, newBackingMap, isMutable);
    }
    
    /**
     * if you're initializing using a {@link Stuffed}
     */
    public GraphDatum(Stuffed stuffed) {
        super();
        init(stuffed);
    }
    
    /**
     * like {@link MapDatum#build(ContentId, SetDatum)}
     */
    public GraphDatum build(SubjectPredicateObject key, SetDatum data) {
        return (GraphDatum) super.build(cidScheme.keyFor(key.name().getBytes(Util.defaultCharset)), data);
    }
    /**
     * Add the ContentId to the set of values at this key. Does not replace.
     * 
     * @return this
     */
    public GraphDatum build(SubjectPredicateObject key, ContentId datum) {
        return (GraphDatum) super.build(cidScheme.keyFor(key.name().getBytes(Util.defaultCharset)), datum);
    }
    
    public SetDatum get(SubjectPredicateObject which) {
        return get(cidScheme.keyFor(which.name().getBytes(Util.defaultCharset)));
        // optimization to be considered
//        switch (which) {
//        case SUBJECTS:
//            return get(repo.SUBJECTSid);
//        case PREDICATES:
//            return get(repo.PREDICATESid);
//        case OBJECTS:
//            return get(repo.OBJECTSid);
//        case AUTHORS:
//            return get(repo.AUTHORSid);
//        case ASSUMPTIONS:
//            return get(repo.ASSUMPTIONSid);
//        case PATTERNS:
//            return get(repo.PATTERNSid);
//        default:
//            throw new Error("serious bug. all SubjectPredicateObject enumeration values not covered");
//        }
    }

    /**
     * @param g
     *            A GraphDatum that could be a subset of this one.
     * 
     * @return true if each spoaap contains all of the ids the respective spoaap
     *         of the given graphdatum contains.
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
    
    @Override
    public Object clone() {
        TreeMap<ContentId, SetDatum> newBackingMap = new TreeMap<ContentId, SetDatum>(backingMap);
        return new GraphDatum(cidScheme, newBackingMap, isMutable);
    }
}

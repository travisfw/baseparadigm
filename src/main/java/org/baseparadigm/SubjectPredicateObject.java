package org.baseparadigm;


/**
 * SubjectPredicateObject enumerates the fields of a GraphDatum.
 * Every value of a field is a set (SetDatum) of metadata blobs (MapDatum)
 *  whose <i>content</i> field contains what is described as the meaning of
 *  each field in SubjectPredicateObject.
 * Obviously GraphData are not just triples.
 * So a bppath to the first positives from a GraphDatum woulb be something like this:
 * "gdObject/PATTERNS/0/content/positives"
 * @author travis@traviswellman.com
 *
 */
public enum SubjectPredicateObject {
    /**
     * Active (viewed) content.
     */
    SUBJECTS(),
    
    /**
     * Links defining structure for data.
     */
    PREDICATES(),
    
    /**
     * Content in the context of the predicate.
     */
    OBJECTS(),
    
    /**
     * Entities claiming authorship this edge.
     * Look up signatures in the graph to verify the claim. Plain signatures indicate
     *  nothing more than having looked at it which, for the authors listed, implies
     *  that the claim is true.
     */
    AUTHORS(),
    
    /**
     * Content that is necessary for this edge to be useful.
     *  This could also easily be called DEPENDENCIES, INCLUDES, or ATTACHMENTS.
     */
    ASSUMPTIONS(),
    
    /**
     * Queries which this edge would be returned within a request for.
     */
    PATTERNS();
}

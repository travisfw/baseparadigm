package org.baseparadigm.i;

import java.util.Iterator;

import org.baseparadigm.GraphData;
import org.baseparadigm.GraphDatum;

/** 
 * @author travis@traviswellman.com
 *
 */
public interface Pattern extends Stuffable {
    
    /**
     * A Pattern partially matches a graph when no conditions for
     *  not matching are true and some conditions for matching are true.
     */
    public boolean isPartialMatch(GraphData g);
    
    /**
     * Like isPartialMatch(GraphData) without having to create a
     *  GraphData containing only one GraphDatum.
     */
    public boolean isPartialMatch(GraphDatum g);
    
    /**
     * A Pattern completely matches a graph when all conditions for
     *  matching are true and no conditions for not matching are true.
     */
    public boolean isCompleteMatch(GraphData g);
    
    /**
     * An exhaustive nonoverlapping list of matches all of which
     *  will be entirely a partial match.
     * These matches are greedy like with completeMatch, but
     *  <em>may</em> each not be complete.
     * The iterator may not do any matching until hasNext or
     *  next is called, so it could be wasteful to drain the
     *  whole iterator if all matches will not be used.
     */
    public Iterator<GraphData> partialMatch(GraphData g);
    
    /**
     * Returns GraphData for which this.isCompleteMatch(gd) would
     *  return true.
     *  Guidelines for implementations: be greedy.
     *  IE, favor the biggest possible match.
     *  Matches should not overlap. IE, do not provide all permutations.
     *  Ideally, do not do any work until hasNext or next is called.
     * @return
     *  a list of complete matches
     */
    public Iterator<GraphData> completeMatch(GraphData g);
    
}

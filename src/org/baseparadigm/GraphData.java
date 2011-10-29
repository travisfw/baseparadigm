package org.baseparadigm;

import java.util.Iterator;
import java.util.SortedSet;


/**
 * Wraps a SetDatum, but ensures each member is a GraphDatum. Also provides an iterator over the GraphDatum type instead of ContentId.
 *  
 * @author travis@traviswellman.com
 */
public class GraphData implements Iterable<GraphDatum> {
    public static GraphData empty = new GraphData(SetDatum.empty);
    public SetDatum data;
    public GraphData(SetDatum value) {
        this.data = value;
        assert ! value.isMutable;
        assert assertAllGraphData(value);
    }
    public GraphData(ContentId cid) {
        data = SetDatum.inflate(cid);
        assert assertAllGraphData(data);
    }
    
    private boolean assertAllGraphData(SortedSet<ContentId> graphData) {
        for (ContentId ci : graphData) {
            new GraphDatum(ci);
        }
        // the constructor does the assertion
        return true;
    }
    
    public Iterator<GraphDatum> iterator() {
        final Iterator<ContentId> sit = data.iterator();
        return new Iterator<GraphDatum>() {
            @Override
            public boolean hasNext() {
                return sit.hasNext();
            }
            @Override
            public GraphDatum next() {
                return new GraphDatum(sit.next());
            }
            @Override
            public void remove() {
                sit.remove();
            }
        };
    }
}

package org.baseparadigm;

import java.util.Iterator;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.ResolvableId;


/**
 * Wraps a SetDatum, but ensures each member is a GraphDatum. Also provides an iterator over the GraphDatum type instead of ContentId.
 *  
 * @author travis@traviswellman.com
 */
public class GraphData implements Iterable<GraphDatum> {
    public static GraphData empty = new GraphData(SetDatum.empty);
    public final SetDatum data;
    public final CidScheme cidScheme;
    public GraphData(SetDatum value) {
        this.data = value;
        this.cidScheme = value.getCidScheme();
        assert assertAllGraphData(value);
    }
    
    public GraphData build(GraphDatum g) {
        if (data.getIsMutable()) {
            data.build(data.cidScheme.keyFor(g.toByteArray()));
            return this;
        }
        data.buildFinish();
        return new GraphData(data.build(data.cidScheme.keyFor(g.toByteArray())));
    }
    
    private boolean assertAllGraphData(SetDatum graphData) {
        for (ContentId cid : graphData) {
            if (cid instanceof ResolvableId)
                new GraphDatum(((ResolvableId)cid).getRepo(), cid);
            else
                new GraphDatum(Util.getDefaultRepo(cidScheme), cid);
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
                ContentId n = sit.next();
                if (n instanceof ResolvableId)
                    return new GraphDatum(((ResolvableId)n).getRepo(), n);
                else
                    return new GraphDatum(Util.getDefaultRepo(cidScheme), n);
            }
            @Override
            public void remove() {
                sit.remove();
            }
        };
    }
}

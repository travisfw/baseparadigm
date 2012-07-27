package org.baseparadigm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.Stuffed;

/**
 * A graph is six indexes into a set of edges, one for each edge field.
 */
public class Graph extends MapDatum {

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
    private GraphData graphData = GraphData.empty;

    public Graph(CidScheme r) {
        super(r);
        
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
     * Index the graph datum for future queries, and also alert subscriptions for which their pattern matches.
     * @param toIndex
     * @return true if the GraphDatum was added
     */
    public boolean add(GraphDatum toIndex) {
        assert cidScheme.equals(toIndex.cidScheme);
        if (graphData.data.contains(toIndex)) return true;
        ContentId theId = toIndex.getId(); // id() is where toIndex gets stored in toIndex.repo
        for (SubjectPredicateObject spoaap : SubjectPredicateObject.values() ) {
            // each value will become a keyword. well, not a word, but same concept.
            Set<ContentId> values = toIndex.get(spoaap);
            // one of the five indexes
            MapDatum oneIdx = idx.get(spoaap);
            for (ContentId keyword : values)
                oneIdx.put(keyword, theId);
        }
        // TODO publish indexed datum to subscriptions?
        graphData = graphData.build(toIndex);
        return true;
    }
    
    public boolean add(Stuffed stuffed) {
        return add(new GraphDatum(stuffed, stuffed.getTopId()));
    }
    
    public boolean addAll(Collection<Stuffed> c) {
        for (Stuffed stuffed : c)
            if (! add(new GraphDatum(stuffed, stuffed.getTopId())))
                return false;
        return true;
    }
    
}

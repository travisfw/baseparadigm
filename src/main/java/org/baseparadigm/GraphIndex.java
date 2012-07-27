package org.baseparadigm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.baseparadigm.i.CidScheme;
import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.ResolvableId;

/**
 * A graph index is six indexes into a set of edges, one for each edge field.
 */
public class GraphIndex extends ShardedMapDatum {
    public SortedMap<ContentId, SetDatum> backingMap;
    public CidScheme cidScheme;
    
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

    public GraphIndex(CidScheme cids) {
        super(cids);
        subjIdx = new MapDatum(cids);
        predIdx = new MapDatum(cids);
        objeIdx = new MapDatum(cids);
        assuIdx = new MapDatum(cids);
        authIdx = new MapDatum(cids);
        pattIdx = new MapDatum(cids);

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
     * @return true if the GraphDatum was added
     */
    public boolean add(GraphDatum toIndex) {
        assert isMutable;
        assert cidScheme.equals(toIndex.cidScheme);
        if (graphData.data.contains(toIndex)) return true;
        ContentId edgeId = toIndex.getId(); // id() is where toIndex gets stored in toIndex.repo
        for (SubjectPredicateObject spoaap : SubjectPredicateObject.values() ) {
            // each value will become a keyword. well, not a word, but same concept.
            Set<ContentId> values = toIndex.get(spoaap);
            // one of the five indexes
            MapDatum oneIdx = idx.get(spoaap);
            for (ContentId nodeCid : values) {
                ResolvableId resolvableNodeCid;
                if (nodeCid instanceof ResolvableId)
                    resolvableNodeCid = (ResolvableId)nodeCid;
                else if (Util.getDefaultRepo(cidScheme) != null)
                    resolvableNodeCid = new ContentIdResolvable(Util.getDefaultRepo(cidScheme), nodeCid.toByteArray());
                else
                    throw new IllegalStateException("can't parse "+ toIndex +" without a repo. use Util.setDefaultRepo(Repo)");
                
                for (ContentId keyword : new MapDatum(resolvableNodeCid.getRepo(), resolvableNodeCid).getField(MetadataFields.CONTENT))
                    oneIdx.put(keyword, edgeId);
            }
        }
        graphData = graphData.build(toIndex);
        return true;
    }
    
    public boolean add(ResolvableId cid) {
        return add(new GraphDatum(cid.getRepo(), cid));
    }
    
    public boolean addAll(Collection<? extends ResolvableId> c) {
        for (ResolvableId cid : c)
            if (! add(new GraphDatum(cid.getRepo(), cid)))
                return false;
        return true;
    }

    private static Set<ContentId> nullToEmptySet(Set<ContentId> s) {
        if (s == null) return new HashSet<ContentId>();
        return s;
    }
    
}

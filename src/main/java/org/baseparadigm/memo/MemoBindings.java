package org.baseparadigm.memo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.baseparadigm.GraphDatum;

/**
 * Where every value is an edge, ie a GraphDatum.
 * 
 * @contributor travis@traviswellman.com
 */
public class MemoBindings implements Bindings {
    protected final Map<String, Object> backingMap = new HashMap<String, Object>();
    
    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return backingMap.containsValue(value);
    }

    @Override
    public void clear() {
        backingMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return backingMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return backingMap.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return backingMap.entrySet();
    }

    @Override
    public Object put(String name, Object value) {
        assert value instanceof GraphDatum;
        return backingMap.put(name, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> toMerge) {
        assert allGraphData(toMerge.values());
        backingMap.putAll(toMerge);
    }
    
    private boolean allGraphData(Iterable<? extends Object> ite) {
        for (Object obj : ite)
            if (! (obj instanceof GraphDatum))
                return false;
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        return backingMap.containsKey(key);
    }

    @Override
    public Object get(Object key) {
        assert backingMap.get(key) instanceof GraphDatum;
        return backingMap.get(key);
    }

    @Override
    public Object remove(Object key) {
        return backingMap.remove(key);
    }

}

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
    protected Map<String, GraphDatum> backingMap = new HashMap<String, GraphDatum>();
    
    @Override
    public int size() {
        return backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Set<String> keySet() {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Collection<Object> values() {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Object put(String name, Object value) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> toMerge) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Object get(Object key) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

    @Override
    public Object remove(Object key) {
        // TODO Auto-generated method stub
        throw new Error("unimplemented");
    }

}

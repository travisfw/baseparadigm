package org.baseparadigm;

import java.util.HashMap;
import java.util.Map;

import org.baseparadigm.i.ContentId;
import org.baseparadigm.i.RepoStorageWithDeletion;

/**
 * A basic in-memory RepoStorage.
 */
public class MapStore implements RepoStorageWithDeletion {
    
    public Map<ContentId, byte[]> map;

    public Map<ContentId, byte[]> getMap() { return map; }
    public void setMap(Map<ContentId, byte[]> map) { this.map = map; }

    public MapStore(Map<ContentId, byte[]> m) {
        this.setMap(m);
    }
    public MapStore() {
        this.setMap(new HashMap<ContentId, byte[]>());
    }
    
    /**
     * always returns null;
     */
    @Override
    public void put(final ContentId key, final byte[] value) {
        map.put(key, value);
    }

    @Override
    public byte[] get(ContentId cid) {
        return map.get(cid);
    }
    
    @Override
    public boolean containsKey(ContentId key) {
        return map.containsKey(key);
    }
    
    @Override
    public void remove(ContentId key) {
        map.remove(key);
    }
}

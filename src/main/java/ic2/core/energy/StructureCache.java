// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy;

import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class StructureCache
{
    private static final int maxSize = 32;
    Map<Key, Data> entries;
    int hits;
    int misses;
    
    StructureCache() {
        this.entries = new HashMap<Key, Data>();
        this.hits = 0;
        this.misses = 0;
    }
    
    Data get(final Set<Integer> activeSources, final Set<Integer> activeSinks) {
        final Key key = new Key(activeSources, activeSinks);
        Data ret = this.entries.get(key);
        if (ret == null) {
            ret = new Data();
            this.add(key, ret);
            ++this.misses;
        }
        else {
            ++this.hits;
        }
        final Data data = ret;
        ++data.queries;
        return ret;
    }
    
    void clear() {
        this.entries.clear();
    }
    
    int size() {
        return this.entries.size();
    }
    
    private void add(final Key key, final Data data) {
        int min = Integer.MAX_VALUE;
        Key minKey = null;
        if (this.entries.size() >= 32) {
            for (final Map.Entry<Key, Data> entry : this.entries.entrySet()) {
                if (entry.getValue().queries < min) {
                    min = entry.getValue().queries;
                    minKey = entry.getKey();
                }
            }
            this.entries.remove(minKey);
        }
        this.entries.put(new Key(key), data);
    }
    
    static class Key
    {
        final Set<Integer> activeSources;
        final Set<Integer> activeSinks;
        final int hashCode;
        
        Key(final Set<Integer> activeSources1, final Set<Integer> activeSinks1) {
            this.activeSources = activeSources1;
            this.activeSinks = activeSinks1;
            this.hashCode = this.activeSources.hashCode() * 31 + this.activeSinks.hashCode();
        }
        
        Key(final Key key) {
            this.activeSources = new HashSet<Integer>(key.activeSources);
            this.activeSinks = new HashSet<Integer>(key.activeSinks);
            this.hashCode = key.hashCode;
        }
        
        @Override
        public int hashCode() {
            return this.hashCode;
        }
        
        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Key)) {
                return false;
            }
            final Key key = (Key)o;
            return key.activeSources.equals(this.activeSources) && key.activeSinks.equals(this.activeSinks);
        }
    }
    
    static class Data
    {
        boolean isInitialized;
        Map<Integer, Node> optimizedNodes;
        List<Node> activeNodes;
        DenseMatrix64F networkMatrix;
        DenseMatrix64F sourceMatrix;
        DenseMatrix64F resultMatrix;
        LinearSolver<DenseMatrix64F> solver;
        int queries;
        
        Data() {
            this.isInitialized = false;
            this.queries = 0;
        }
    }
}

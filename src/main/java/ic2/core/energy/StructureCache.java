package ic2.core.energy;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StructureCache {
  private static final int maxSize = 32;
  
  Data get(Set<Integer> activeSources, Set<Integer> activeSinks) {
    Key key = new Key(activeSources, activeSinks);
    Data ret = this.entries.get(key);
    if (ret == null) {
      ret = new Data();
      add(key, ret);
      this.misses++;
    } else {
      this.hits++;
    } 
    ret.queries++;
    return ret;
  }
  
  void clear() {
    this.entries.clear();
  }
  
  int size() {
    return this.entries.size();
  }
  
  private void add(Key key, Data data) {
    int min = Integer.MAX_VALUE;
    Key minKey = null;
    if (this.entries.size() >= 32) {
      for (Map.Entry<Key, Data> entry : this.entries.entrySet()) {
        if (((Data)entry.getValue()).queries < min) {
          min = ((Data)entry.getValue()).queries;
          minKey = entry.getKey();
        } 
      } 
      this.entries.remove(minKey);
    } 
    this.entries.put(new Key(key), data);
  }
  
  Map<Key, Data> entries = new HashMap<>();
  
  int hits = 0;
  
  int misses = 0;
  
  static class Key {
    final Set<Integer> activeSources;
    
    final Set<Integer> activeSinks;
    
    final int hashCode;
    
    Key(Set<Integer> activeSources1, Set<Integer> activeSinks1) {
      this.activeSources = activeSources1;
      this.activeSinks = activeSinks1;
      this.hashCode = this.activeSources.hashCode() * 31 + this.activeSinks.hashCode();
    }
    
    Key(Key key) {
      this.activeSources = new HashSet<>(key.activeSources);
      this.activeSinks = new HashSet<>(key.activeSinks);
      this.hashCode = key.hashCode;
    }
    
    public int hashCode() {
      return this.hashCode;
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof Key))
        return false; 
      Key key = (Key)o;
      return (key.activeSources.equals(this.activeSources) && key.activeSinks.equals(this.activeSinks));
    }
  }
  
  static class Data {
    boolean isInitialized = false;
    
    Map<Integer, Node> optimizedNodes;
    
    List<Node> activeNodes;
    
    DenseMatrix64F networkMatrix;
    
    DenseMatrix64F sourceMatrix;
    
    DenseMatrix64F resultMatrix;
    
    LinearSolver<DenseMatrix64F> solver;
    
    int queries = 0;
  }
}

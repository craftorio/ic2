// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tuple
{
    public static <K, V> List<T2<K, V>> fromMap(final Map<K, V> map) {
        final List<T2<K, V>> ret = new ArrayList<T2<K, V>>(map.size());
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            ret.add(new T2<K, V>(entry.getKey(), entry.getValue()));
        }
        return ret;
    }
    
    public static class T2<TA, TB>
    {
        public TA a;
        public TB b;
        
        public T2(final TA a, final TB b) {
            this.a = a;
            this.b = b;
        }
    }
    
    public static class T3<TA, TB, TC>
    {
        public TA a;
        public TB b;
        public TC c;
        
        public T3(final TA a, final TB b, final TC c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}

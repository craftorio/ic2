package ic2.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Tuple {
   public static <K, V> List<Tuple.T2<K, V>> fromMap(Map<K, V> map) {
      List<Tuple.T2<K, V>> ret = new ArrayList<>(map.size());

      for (Entry<K, V> entry : map.entrySet()) {
         ret.add(new Tuple.T2<>(entry.getKey(), entry.getValue()));
      }

      return ret;
   }

   public static class T2<TA, TB> {
      public TA a;
      public TB b;

      public T2(TA a, TB b) {
         this.a = a;
         this.b = b;
      }
   }

   public static class T3<TA, TB, TC> {
      public TA a;
      public TB b;
      public TC c;

      public T3(TA a, TB b, TC c) {
         this.a = a;
         this.b = b;
         this.c = c;
      }
   }
}

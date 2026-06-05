package ic2.core.block.state;

public class UnlistedEnumProperty<V extends Enum<V>> extends UnlistedProperty<V> {
   public UnlistedEnumProperty(String name, Class<V> cls) {
      super(name, cls);
   }

   public String valueToString(V value) {
      return value.name();
   }
}

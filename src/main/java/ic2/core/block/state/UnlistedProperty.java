package ic2.core.block.state;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedProperty<T> implements IUnlistedProperty<T> {
   private final String name;
   private final Class<T> cls;

   public UnlistedProperty(String name, Class<T> cls) {
      this.name = name;
      this.cls = cls;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public boolean isValid(T value) {
      return value == null || this.cls.isInstance(value);
   }

   @Override
   public Class<T> getType() {
      return this.cls;
   }

   @Override
   public String valueToString(T value) {
      return value.toString();
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "{name=" + this.name + ", cls=" + this.cls.getName() + "}";
   }
}

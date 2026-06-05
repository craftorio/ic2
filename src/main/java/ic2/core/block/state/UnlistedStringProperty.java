package ic2.core.block.state;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedStringProperty implements IUnlistedProperty<String> {
   private final String name;

   public UnlistedStringProperty(String name) {
      this.name = name;
   }

   @Override
   public String getName() {
      return this.name;
   }

   public boolean isValid(String value) {
      return true;
   }

   @Override
   public Class<String> getType() {
      return String.class;
   }

   public String valueToString(String value) {
      return value;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "{name=" + this.name + "}";
   }
}

package ic2.core.block.state;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedIntegerProperty implements IUnlistedProperty<Integer> {
  private final String name;
  
  public UnlistedIntegerProperty(String name) {
    this.name = name;
  }
  
  public String getName() {
    return this.name;
  }
  
  public boolean isValid(Integer value) {
    return true;
  }
  
  public Class<Integer> getType() {
    return Integer.class;
  }
  
  public String valueToString(Integer value) {
    return value.toString();
  }
  
  public String toString() {
    return getClass().getSimpleName() + "{name=" + this.name + "}";
  }
}

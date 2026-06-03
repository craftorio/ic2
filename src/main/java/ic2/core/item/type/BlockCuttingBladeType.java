package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;

@NotClassic
public enum BlockCuttingBladeType implements IIdProvider {
  iron(0),
  steel(1),
  diamond(2);
  
  private final int id;
  
  BlockCuttingBladeType(int id) {
    this.id = id;
  }
  
  public String getName() {
    return name();
  }
  
  public int getId() {
    return this.id;
  }
}

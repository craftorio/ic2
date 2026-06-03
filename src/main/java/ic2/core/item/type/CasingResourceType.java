package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;

@NotClassic
public enum CasingResourceType implements IIdProvider {
  bronze(0),
  copper(1),
  gold(2),
  iron(3),
  lead(4),
  steel(5),
  tin(6);
  
  private final int id;
  
  CasingResourceType(int id) {
    this.id = id;
  }
  
  public String getName() {
    return name();
  }
  
  public int getId() {
    return this.id;
  }
}

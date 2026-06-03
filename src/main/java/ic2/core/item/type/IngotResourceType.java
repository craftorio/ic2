package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;
import ic2.core.profile.NotExperimental;

public enum IngotResourceType implements IIdProvider {
  alloy(0),
  bronze(1),
  copper(2),
  lead(3),
  silver(4),
  steel(5),
  tin(6),
  refined_iron(7),
  uranium(8);
  
  private final int id;
  
  IngotResourceType(int id) {
    this.id = id;
  }
  
  public String getName() {
    return name();
  }
  
  public int getId() {
    return this.id;
  }
}

package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;

public enum MiscResourceType implements IIdProvider {
  ashes(0),
  iridium_ore(1),
  iridium_shard(2),
  matter(3),
  resin(4),
  slag(5),
  iodine(6),
  water_sheet(7),
  lava_sheet(8);
  
  private final int id;
  
  MiscResourceType(int id) {
    this.id = id;
  }
  
  public String getName() {
    return name();
  }
  
  public int getId() {
    return this.id;
  }
}

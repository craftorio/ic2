package ic2.core.ref;

import ic2.core.block.state.IIdProvider;

public enum Materials implements IIdProvider {
  brass(16757760, true),
  bronze(16744448, true),
  copper(16737280, false),
  gold(16776990, true),
  iridium(15658734, false),
  iron(13158600, false),
  lead(9200780, false),
  silver(14474495, true),
  steel(8421504, false),
  tin(14474460, false),
  zinc(16445680, true);
  
  private final int color;
  
  private final boolean shiny;
  
  Materials(int color, boolean shiny) {
    this.color = color;
    this.shiny = shiny;
  }
  
  public String getName() {
    return name();
  }
  
  public int getId() {
    return ordinal();
  }
  
  public int getColor() {
    return this.color;
  }
  
  public String getModelName() {
    return this.shiny ? "shiny" : "normal";
  }
}

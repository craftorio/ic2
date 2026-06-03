package ic2.core.block.transport.items;

import ic2.core.block.state.IIdProvider;
import java.util.HashMap;
import java.util.Map;

public enum PipeSize implements IIdProvider {
  tiny(1, 0.25F, 0.16666667F),
  small(4, 0.375F, 0.33333334F),
  medium(16, 0.5F, 1.0F),
  large(64, 0.625F, 2.0F);
  
  public final int maxStackSize;
  
  public final float thickness;
  
  public final float multiplier;
  
  public static final PipeSize[] values;
  
  private static final Map<String, PipeSize> nameMap;
  
  PipeSize(int maxStackSize, float thickness, float multiplier) {
    this.maxStackSize = maxStackSize;
    this.thickness = thickness;
    this.multiplier = multiplier;
  }
  
  public String getName() {
    return name();
  }
  
  public int getId() {
    return ordinal();
  }
  
  public static PipeSize get(String name) {
    return nameMap.get(name);
  }
  
  static {
    values = values();
    nameMap = new HashMap<>();
    for (PipeSize type : values)
      nameMap.put(type.getName(), type); 
  }
}

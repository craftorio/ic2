package ic2.core.block.transport.items;

import ic2.core.block.state.IIdProvider;
import java.util.HashMap;
import java.util.Map;

public enum PipeType implements IIdProvider {
  bronze(2400, 174, 81, 17),
  steel(4800, 128, 128, 128);
  
  public final int transferRate;
  
  public final int red;
  
  public final int green;
  
  public final int blue;
  
  public static final PipeType[] values;
  
  private static final Map<String, PipeType> nameMap;
  
  PipeType(int transferRate, int red, int green, int blue) {
    this.transferRate = transferRate;
    this.red = red;
    this.green = green;
    this.blue = blue;
  }
  
  public String getName(PipeSize size) {
    StringBuilder ret = new StringBuilder(getName());
    ret.append("_pipe");
    if (size != null) {
      ret.append('_');
      ret.append(size.name());
    } 
    return ret.toString();
  }
  
  public String getName() {
    return name();
  }
  
  public int getId() {
    return ordinal();
  }
  
  public static PipeType get(String name) {
    return nameMap.get(name);
  }
  
  static {
    values = values();
    nameMap = new HashMap<>();
    for (PipeType type : values)
      nameMap.put(type.getName(), type); 
  }
}

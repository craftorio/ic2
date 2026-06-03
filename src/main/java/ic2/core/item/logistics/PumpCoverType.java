package ic2.core.item.logistics;

import ic2.core.block.state.IIdProvider;

public enum PumpCoverType implements IIdProvider {
  pump_lv(640, 65280),
  pump_mv(2560, 16776960);
  
  public final int transferRate;
  
  public final int color;
  
  PumpCoverType(int transferRate, int color) {
    this.transferRate = transferRate;
    this.color = color;
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
    return "pump";
  }
}

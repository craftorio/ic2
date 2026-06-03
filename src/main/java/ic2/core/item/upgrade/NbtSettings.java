package ic2.core.item.upgrade;

import java.util.Locale;

public enum NbtSettings {
  IGNORED, FUZZY, EXACT;
  
  public static final NbtSettings[] VALUES;
  
  final String name;
  
  NbtSettings() {
    this.name = "ic2.upgrade.advancedGUI." + name().toLowerCase(Locale.ENGLISH);
  }
  
  public boolean enabled() {
    return (this != IGNORED);
  }
  
  public byte getForNBT() {
    return (byte)ordinal();
  }
  
  public static NbtSettings getFromNBT(byte type) {
    return VALUES[type];
  }
  
  static {
    VALUES = values();
  }
}

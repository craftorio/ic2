package ic2.core.item.upgrade;

import java.util.Locale;

public enum ComparisonType {
  IGNORED, DIRECT, COMPARISON, RANGE;
  
  public static final ComparisonType[] VALUES;
  
  final String name;
  
  ComparisonType() {
    this.name = "ic2.upgrade.advancedGUI." + name().toLowerCase(Locale.ENGLISH);
  }
  
  public boolean enabled() {
    return (this != IGNORED);
  }
  
  public boolean ignoreFilters() {
    return (this == IGNORED || this == DIRECT);
  }
  
  public byte getForNBT() {
    return (byte)ordinal();
  }
  
  public static ComparisonType getFromNBT(byte type) {
    return VALUES[type];
  }
  
  static {
    VALUES = values();
  }
}

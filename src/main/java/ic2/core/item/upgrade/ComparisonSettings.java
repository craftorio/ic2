package ic2.core.item.upgrade;

import java.util.Locale;

public enum ComparisonSettings {
  LESS_OR_EQUAL("<=") {
    public boolean compare(int value, int comparison) {
      return (value <= comparison);
    }
  },
  LESS("<") {
    public boolean compare(int value, int comparison) {
      return (value < comparison);
    }
  },
  GREATER(">") {
    public boolean compare(int value, int comparison) {
      return (value > comparison);
    }
  },
  GREATER_OR_EQUAL(">=") {
    public boolean compare(int value, int comparison) {
      return (value >= comparison);
    }
  };
  
  final String symbol;
  
  final String name;
  
  public static final ComparisonSettings DEFAULT;
  
  public static final ComparisonSettings[] VALUES;
  
  ComparisonSettings(String symbol) {
    this.name = "ic2.upgrade.advancedGUI." + name().toLowerCase(Locale.ENGLISH);
    this.symbol = symbol;
  }
  
  public byte getForNBT() {
    return (byte)ordinal();
  }
  
  public static ComparisonSettings getFromNBT(byte type) {
    return VALUES[type];
  }
  
  static {
    DEFAULT = LESS;
    VALUES = values();
  }
  
  public abstract boolean compare(int paramInt1, int paramInt2);
}

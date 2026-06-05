package ic2.core.item.upgrade;

import java.util.Locale;

public enum ComparisonSettings {
   LESS_OR_EQUAL("<=") {
      @Override
      public boolean compare(int value, int comparison) {
         return value <= comparison;
      }
   },
   LESS("<") {
      @Override
      public boolean compare(int value, int comparison) {
         return value < comparison;
      }
   },
   GREATER(">") {
      @Override
      public boolean compare(int value, int comparison) {
         return value > comparison;
      }
   },
   GREATER_OR_EQUAL(">=") {
      @Override
      public boolean compare(int value, int comparison) {
         return value >= comparison;
      }
   };

   final String symbol;
   final String name = "ic2.upgrade.advancedGUI." + this.name().toLowerCase(Locale.ENGLISH);
   public static final ComparisonSettings DEFAULT = LESS;
   public static final ComparisonSettings[] VALUES = values();

   ComparisonSettings(String symbol) {
      this.symbol = symbol;
   }

   public abstract boolean compare(int var1, int var2);

   public byte getForNBT() {
      return (byte)this.ordinal();
   }

   public static ComparisonSettings getFromNBT(byte type) {
      return VALUES[type];
   }
}

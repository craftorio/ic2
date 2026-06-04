// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.upgrade;

import java.util.Locale;

public enum ComparisonSettings
{
    LESS_OR_EQUAL("<=") {
        @Override
        public boolean compare(final int value, final int comparison) {
            return value <= comparison;
        }
    }, 
    LESS("<") {
        @Override
        public boolean compare(final int value, final int comparison) {
            return value < comparison;
        }
    }, 
    GREATER(">") {
        @Override
        public boolean compare(final int value, final int comparison) {
            return value > comparison;
        }
    }, 
    GREATER_OR_EQUAL(">=") {
        @Override
        public boolean compare(final int value, final int comparison) {
            return value >= comparison;
        }
    };
    
    final String symbol;
    final String name;
    public static final ComparisonSettings DEFAULT;
    public static final ComparisonSettings[] VALUES;
    
    private ComparisonSettings(final String symbol) {
        this.name = "ic2.upgrade.advancedGUI." + this.name().toLowerCase(Locale.ENGLISH);
        this.symbol = symbol;
    }
    
    public abstract boolean compare(final int p0, final int p1);
    
    public byte getForNBT() {
        return (byte)this.ordinal();
    }
    
    public static ComparisonSettings getFromNBT(final byte type) {
        return ComparisonSettings.VALUES[type];
    }
    
    static {
        DEFAULT = ComparisonSettings.LESS;
        VALUES = values();
    }
}

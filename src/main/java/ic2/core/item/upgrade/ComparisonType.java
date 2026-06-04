// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.upgrade;

import java.util.Locale;

public enum ComparisonType
{
    IGNORED, 
    DIRECT, 
    COMPARISON, 
    RANGE;
    
    final String name;
    public static final ComparisonType[] VALUES;
    
    private ComparisonType() {
        this.name = "ic2.upgrade.advancedGUI." + this.name().toLowerCase(Locale.ENGLISH);
    }
    
    public boolean enabled() {
        return this != ComparisonType.IGNORED;
    }
    
    public boolean ignoreFilters() {
        return this == ComparisonType.IGNORED || this == ComparisonType.DIRECT;
    }
    
    public byte getForNBT() {
        return (byte)this.ordinal();
    }
    
    public static ComparisonType getFromNBT(final byte type) {
        return ComparisonType.VALUES[type];
    }
    
    static {
        VALUES = values();
    }
}

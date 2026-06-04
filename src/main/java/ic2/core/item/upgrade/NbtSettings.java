// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.upgrade;

import java.util.Locale;

public enum NbtSettings
{
    IGNORED, 
    FUZZY, 
    EXACT;
    
    final String name;
    public static final NbtSettings[] VALUES;
    
    private NbtSettings() {
        this.name = "ic2.upgrade.advancedGUI." + this.name().toLowerCase(Locale.ENGLISH);
    }
    
    public boolean enabled() {
        return this != NbtSettings.IGNORED;
    }
    
    public byte getForNBT() {
        return (byte)this.ordinal();
    }
    
    public static NbtSettings getFromNBT(final byte type) {
        return NbtSettings.VALUES[type];
    }
    
    static {
        VALUES = values();
    }
}

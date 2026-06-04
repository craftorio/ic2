// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

public enum HudMode
{
    DISABLED("ic2.hud.disabled"), 
    BASIC("ic2.hud.basic"), 
    EXTENDED("ic2.hud.extended"), 
    ADVANCED("ic2.hud.advanced");
    
    private final String translationKey;
    private static final HudMode[] VALUES;
    
    private HudMode(final String key) {
        this.translationKey = key;
    }
    
    public boolean shouldDisplay() {
        return this != HudMode.DISABLED;
    }
    
    public boolean hasTooltip() {
        return this == HudMode.EXTENDED || this == HudMode.ADVANCED;
    }
    
    public String getTranslationKey() {
        return this.translationKey;
    }
    
    public int getID() {
        return this.ordinal();
    }
    
    public static HudMode getFromID(final int ID) {
        return HudMode.VALUES[ID % HudMode.VALUES.length];
    }
    
    public static int getMaxMode() {
        return HudMode.VALUES.length - 1;
    }
    
    static {
        VALUES = values();
    }
}

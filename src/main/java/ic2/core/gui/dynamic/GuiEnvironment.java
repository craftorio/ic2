// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui.dynamic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum GuiEnvironment
{
    GAME, 
    JEI;
    
    private static final Map<String, GuiEnvironment> map;
    public final String name;
    
    private GuiEnvironment() {
        this.name = this.name().toLowerCase(Locale.ENGLISH);
    }
    
    public static GuiEnvironment get(final String name) {
        return GuiEnvironment.map.get(name);
    }
    
    private static Map<String, GuiEnvironment> getMap() {
        final GuiEnvironment[] values = values();
        final Map<String, GuiEnvironment> ret = new HashMap<String, GuiEnvironment>(values.length);
        for (final GuiEnvironment value : values) {
            ret.put(value.name, value);
        }
        return ret;
    }
    
    static {
        map = getMap();
    }
}

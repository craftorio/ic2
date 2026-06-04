// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.util.EnumMap;
import net.minecraft.item.EnumDyeColor;
import java.util.Map;
import ic2.core.block.state.IIdProvider;

public enum Ic2Color implements IIdProvider
{
    black(EnumDyeColor.BLACK, "dyeBlack"), 
    blue(EnumDyeColor.BLUE, "dyeBlue"), 
    brown(EnumDyeColor.BROWN, "dyeBrown"), 
    cyan(EnumDyeColor.CYAN, "dyeCyan"), 
    gray(EnumDyeColor.GRAY, "dyeGray"), 
    green(EnumDyeColor.GREEN, "dyeGreen"), 
    light_blue(EnumDyeColor.LIGHT_BLUE, "dyeLightBlue"), 
    light_gray(EnumDyeColor.SILVER, "dyeLightGray"), 
    lime(EnumDyeColor.LIME, "dyeLime"), 
    magenta(EnumDyeColor.MAGENTA, "dyeMagenta"), 
    orange(EnumDyeColor.ORANGE, "dyeOrange"), 
    pink(EnumDyeColor.PINK, "dyePink"), 
    purple(EnumDyeColor.PURPLE, "dyePurple"), 
    red(EnumDyeColor.RED, "dyeRed"), 
    white(EnumDyeColor.WHITE, "dyeWhite"), 
    yellow(EnumDyeColor.YELLOW, "dyeYellow");
    
    public static final Ic2Color[] values;
    private static final Map<EnumDyeColor, Ic2Color> mcColorMap;
    public final EnumDyeColor mcColor;
    public final String oreDictDyeName;
    
    private Ic2Color(final EnumDyeColor mcColor, final String oreDictDyeName) {
        this.mcColor = mcColor;
        this.oreDictDyeName = oreDictDyeName;
    }
    
    @Override
    public String getName() {
        return this.name();
    }
    
    @Override
    public int getId() {
        return this.ordinal();
    }
    
    public static Ic2Color get(final EnumDyeColor mcColor) {
        return Ic2Color.mcColorMap.get(mcColor);
    }
    
    static {
        values = values();
        mcColorMap = new EnumMap<EnumDyeColor, Ic2Color>(EnumDyeColor.class);
        for (final Ic2Color color : Ic2Color.values) {
            Ic2Color.mcColorMap.put(color.mcColor, color);
        }
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.info;

import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;

public class Info
{
    public static IInfoProvider itemInfo;
    public static Object ic2ModInstance;
    public static DamageSource DMG_ELECTRIC;
    public static DamageSource DMG_NUKE_EXPLOSION;
    public static DamageSource DMG_RADIATION;
    public static Potion POTION_RADIATION;
    public static String MOD_ID;
    private static Boolean ic2Available;
    
    public static boolean isIc2Available() {
        if (Info.ic2Available != null) {
            return Info.ic2Available;
        }
        final boolean loaded = Loader.isModLoaded(Info.MOD_ID);
        if (Loader.instance().hasReachedState(LoaderState.CONSTRUCTING)) {
            Info.ic2Available = loaded;
        }
        return loaded;
    }
    
    static {
        Info.MOD_ID = "ic2";
        Info.ic2Available = null;
    }
}

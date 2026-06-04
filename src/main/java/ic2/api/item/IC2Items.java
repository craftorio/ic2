// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraftforge.fml.common.ModContainer;
import ic2.api.info.Info;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.item.ItemStack;

public final class IC2Items
{
    private static IItemAPI instance;
    
    public static ItemStack getItem(final String name, final String variant) {
        if (IC2Items.instance == null) {
            return null;
        }
        return IC2Items.instance.getItemStack(name, variant);
    }
    
    public static ItemStack getItem(final String name) {
        return getItem(name, null);
    }
    
    public static IItemAPI getItemAPI() {
        return IC2Items.instance;
    }
    
    public static void setInstance(final IItemAPI api) {
        final ModContainer mc = Loader.instance().activeModContainer();
        if (mc == null || !Info.MOD_ID.equals(mc.getModId())) {
            throw new IllegalAccessError("invoked from " + mc);
        }
        IC2Items.instance = api;
    }
}

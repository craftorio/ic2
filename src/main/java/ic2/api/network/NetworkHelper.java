// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.network;

import net.minecraftforge.fml.common.ModContainer;
import ic2.api.info.Info;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.tileentity.TileEntity;

public final class NetworkHelper
{
    private static INetworkManager serverInstance;
    private static INetworkManager clientInstance;
    
    public static void updateTileEntityField(final TileEntity te, final String field) {
        getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).updateTileEntityField(te, field);
    }
    
    public static void initiateTileEntityEvent(final TileEntity te, final int event, final boolean limitRange) {
        getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateTileEntityEvent(te, event, limitRange);
    }
    
    public static void initiateItemEvent(final EntityPlayer player, final ItemStack stack, final int event, final boolean limitRange) {
        getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateItemEvent(player, stack, event, limitRange);
    }
    
    public static void sendInitialData(final TileEntity te) {
        getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).sendInitialData(te);
    }
    
    public static void initiateClientTileEntityEvent(final TileEntity te, final int event) {
        getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateClientTileEntityEvent(te, event);
    }
    
    public static void initiateClientItemEvent(final ItemStack stack, final int event) {
        getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateClientItemEvent(stack, event);
    }
    
    public static INetworkManager getNetworkManager(final Side side) {
        if (side.isClient()) {
            return NetworkHelper.clientInstance;
        }
        return NetworkHelper.serverInstance;
    }
    
    public static void setInstance(final INetworkManager server, final INetworkManager client) {
        final ModContainer mc = Loader.instance().activeModContainer();
        if (mc == null || !Info.MOD_ID.equals(mc.getModId())) {
            throw new IllegalAccessError();
        }
        NetworkHelper.serverInstance = server;
        NetworkHelper.clientInstance = client;
    }
}

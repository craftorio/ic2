package ic2.api.network;

import ic2.api.info.Info;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;

public final class NetworkHelper {
   private static INetworkManager serverInstance;
   private static INetworkManager clientInstance;

   public static void updateTileEntityField(TileEntity te, String field) {
      getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).updateTileEntityField(te, field);
   }

   public static void initiateTileEntityEvent(TileEntity te, int event, boolean limitRange) {
      getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateTileEntityEvent(te, event, limitRange);
   }

   public static void initiateItemEvent(EntityPlayer player, ItemStack stack, int event, boolean limitRange) {
      getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateItemEvent(player, stack, event, limitRange);
   }

   public static void sendInitialData(TileEntity te) {
      getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).sendInitialData(te);
   }

   public static void initiateClientTileEntityEvent(TileEntity te, int event) {
      getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateClientTileEntityEvent(te, event);
   }

   public static void initiateClientItemEvent(ItemStack stack, int event) {
      getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateClientItemEvent(stack, event);
   }

   public static INetworkManager getNetworkManager(Side side) {
      return side.isClient() ? clientInstance : serverInstance;
   }

   public static void setInstance(INetworkManager server, INetworkManager client) {
      ModContainer mc = Loader.instance().activeModContainer();
      if (mc != null && Info.MOD_ID.equals(mc.getModId())) {
         serverInstance = server;
         clientInstance = client;
      } else {
         throw new IllegalAccessError();
      }
   }
}

package ic2.api.item;

import ic2.api.info.Info;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class IC2Items {
   private static IItemAPI instance;

   public static ItemStack getItem(String name, String variant) {
      return instance == null ? null : instance.getItemStack(name, variant);
   }

   public static ItemStack getItem(String name) {
      return getItem(name, null);
   }

   public static IItemAPI getItemAPI() {
      return instance;
   }

   public static void setInstance(IItemAPI api) {
      ModContainer mc = Loader.instance().activeModContainer();
      if (mc != null && Info.MOD_ID.equals(mc.getModId())) {
         instance = api;
      } else {
         throw new IllegalAccessError("invoked from " + mc);
      }
   }
}

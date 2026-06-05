package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.core.init.Localization;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class ElectricItemTooltipHandler {
   public ElectricItemTooltipHandler() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   public void drawTooltips(ItemTooltipEvent event) {
      ItemStack stack = event.getItemStack();
      if (stack != null && ElectricItem.manager.getMaxCharge(stack) > 0.0) {
         String tooltip = ElectricItem.manager.getToolTip(stack);
         if (tooltip != null && !tooltip.trim().isEmpty()) {
            event.getToolTip().add(tooltip);
            if (Keyboard.isKeyDown(42)) {
               event.getToolTip().add(Localization.translate("ic2.item.tooltip.PowerTier", ElectricItem.manager.getTier(stack)));
            }
         }
      }
   }
}

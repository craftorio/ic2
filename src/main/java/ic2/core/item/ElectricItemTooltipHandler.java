// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import ic2.core.init.Localization;
import org.lwjgl.input.Keyboard;
import ic2.api.item.ElectricItem;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ElectricItemTooltipHandler
{
    public ElectricItemTooltipHandler() {
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    @SubscribeEvent
    public void drawTooltips(final ItemTooltipEvent event) {
        final ItemStack stack = event.getItemStack();
        if (stack != null && ElectricItem.manager.getMaxCharge(stack) > 0.0) {
            final String tooltip = ElectricItem.manager.getToolTip(stack);
            if (tooltip != null && !tooltip.trim().isEmpty()) {
                event.getToolTip().add(tooltip);
                if (Keyboard.isKeyDown(42)) {
                    event.getToolTip().add(Localization.translate("ic2.item.tooltip.PowerTier", ElectricItem.manager.getTier(stack)));
                }
            }
        }
    }
}

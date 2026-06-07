package ic2.core.item;

import ic2.api.item.ElectricItem;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ElectricItemTooltipHandler
{
	public static void addTooltip(ItemStack stack, List<Component> out)
	{
		if (stack != null && ElectricItem.manager.getMaxCharge(stack) > 0.0)
		{
			String tooltip = ElectricItem.manager.getToolTip(stack);
			if (tooltip != null && !tooltip.trim().isEmpty())
			{
				out.add(Component.m_237113_(tooltip));
				if (Screen.m_96638_())
				{
					out.add(Component.m_237110_("ic2.item.tooltip.PowerTier", new Object[] { ElectricItem.manager.getTier(stack) }).m_130940_(ChatFormatting.GRAY));
				}
			}
		}
	}
}

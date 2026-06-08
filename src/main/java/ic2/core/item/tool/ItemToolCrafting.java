package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.api.item.IItemHudInfo;
import ic2.core.init.Localization;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemToolCrafting extends Item implements IBoxable, IItemHudInfo
{
	public static final String TOOLTIP_USES_LEFT = "ic2.tooltip.tool.uses_left";

	public ItemToolCrafting(Properties settings)
	{
		super(settings);
	}

	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		tooltip.add(Component.translatable("ic2.tooltip.tool.uses_left", new Object[] { getRemainingUses(stack) }));
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add(Localization.translate("ic2.tooltip.tool.uses_left", getRemainingUses(stack)).formatted(ChatFormatting.GRAY));
		return info;
	}

	protected static int getRemainingUses(ItemStack stack)
	{
		return stack.getMaxDamage() - stack.getDamageValue();
	}
}

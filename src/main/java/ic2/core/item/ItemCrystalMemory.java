package ic2.core.item;

import ic2.core.init.Localization;
import ic2.core.profile.NotClassic;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.uu.UuIndex;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemCrystalMemory extends Item
{
	public static final String TOOLTIP_ITEM = "item.ic2.crystal_memory.tooltip.item";
	public static final String TOOLTIP_UU_MATTER = "item.ic2.crystal_memory.tooltip.uu_matter";
	public static final String TOOLTIP_ENERGY = "item.ic2.crystal_memory.tooltip.energy";
	public static final String TOOLTIP_EMPTY = "item.ic2.crystal_memory.tooltip.empty";

	public ItemCrystalMemory(Properties settings)
	{
		super(settings);
	}

	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		ItemStack recorded = this.readItemStack(stack);
		if (!StackUtil.isEmpty(recorded))
		{
			tooltip.add(
				Component.literal(Localization.translate("item.ic2.crystal_memory.tooltip.item") + " " + recorded.getHoverName()).withStyle(ChatFormatting.GRAY)
			);
			tooltip.add(
				Component.literal(
						Localization.translate("item.ic2.crystal_memory.tooltip.uu_matter") + " " + Util.toSiString(UuIndex.instance.getInBuckets(recorded), 4) + "B"
					)
					.withStyle(ChatFormatting.GRAY)
			);
		} else
		{
			tooltip.add(Component.translatable("item.ic2.crystal_memory.tooltip.empty").withStyle(ChatFormatting.GRAY));
		}
	}

	public ItemStack readItemStack(ItemStack stack)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		CompoundTag contentTag = nbt.getCompound("Pattern");
		return ItemStack.of(contentTag);
	}

	public void writecontentsTag(ItemStack stack, ItemStack recorded)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		CompoundTag contentTag = new CompoundTag();
		recorded.save(contentTag);
		nbt.put("Pattern", contentTag);
	}
}

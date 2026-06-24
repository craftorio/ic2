package ic2.core.item.tool;

import ic2.core.IHasGui;
import ic2.core.item.IHandHeldInventory;
import ic2.core.util.StackUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemMiningFilterCard extends Item implements IHandHeldInventory
{
	public ItemMiningFilterCard(Properties settings)
	{
		super(settings);
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide)
		{
			this.getInventory(player, hand, stack).openManagedItem(player, hand, null);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public IHasGui getInventory(Player player, InteractionHand hand, ItemStack stack)
	{
		return new HandHeldMiningFilter(player, hand, stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		CompoundTag nbt = stack.getTag();
		if (nbt != null)
		{
			boolean isBlacklist = !nbt.contains("blacklist") || nbt.getBoolean("blacklist");
			ListTag items = nbt.getList("Items", 10);
			int count = items.size();
			tooltip.add(Component.translatable(isBlacklist ? "ic2.MiningFilter.gui.mode.blacklist" : "ic2.MiningFilter.gui.mode.whitelist")
				.withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("ic2.MiningFilter.tooltip.entries", count)
				.withStyle(ChatFormatting.GRAY));
		}
	}
}

package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.profile.NotClassic;
import ic2.core.item.tool.GuiToolbox;
import ic2.core.util.StackUtil;

import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@NotClassic
public class ItemBatteryChargeHotbar extends ItemBattery implements IBoxable
{
	public ItemBatteryChargeHotbar(Properties settings, double maxCharge, double transferLimit, int tier)
	{
		super(settings, maxCharge, transferLimit, tier);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context)
	{
		super.appendHoverText(stack, world, tooltip, context);
		Mode mode = getMode(stack);
		tooltip.add(Component.translatable("ic2.tooltip.mode",
			Component.translatable("ic2.tooltip.mode." + mode.name().toLowerCase(Locale.ENGLISH))));
		if (world != null && world.isClientSide)
		{
			showBoxableTip(tooltip, mode);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void showBoxableTip(List<Component> tooltip, Mode mode)
	{
		if (Minecraft.getInstance().screen instanceof GuiToolbox)
		{
			tooltip.add(Component.translatable("ic2.tooltip.mode.boxable")
				.withStyle(mode.enabled ? ChatFormatting.RED : ChatFormatting.GREEN));
		}
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, Level world, @NotNull Entity entity, int itemSlot, boolean isSelected)
	{
		Mode mode = getMode(stack);
		if (!world.isClientSide && entity instanceof Player player
			&& world.getGameTime() % 10L < this.getTier(stack) && mode.enabled)
		{
			double limit = this.getTransferLimit(stack);
			int tier = this.getTier(stack);

			for (int i = 0; i < 9 && limit > 0.0; i++)
			{
				ItemStack toCharge = player.getInventory().items.get(i);
				if (!toCharge.isEmpty()
					&& (mode != Mode.NOT_IN_HAND || i != player.getInventory().selected)
					&& !(toCharge.getItem() instanceof ItemBatteryChargeHotbar))
				{
					double charge = ElectricItem.manager.charge(toCharge, limit, tier, false, true);
					charge = ElectricItem.manager.discharge(stack, charge, tier, true, false, false);
					ElectricItem.manager.charge(toCharge, charge, tier, true, false);
					limit -= charge;
				}
			}
		}
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (world.isClientSide)
		{
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		}

		Mode mode = getMode(stack);
		mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
		setMode(stack, mode);
		IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode",
			"ic2.tooltip.mode." + mode.name().toLowerCase(Locale.ENGLISH));
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	public void setMode(ItemStack stack, Mode mode)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		nbt.putByte("mode", (byte) mode.ordinal());
	}

	public Mode getMode(ItemStack stack)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		if (!nbt.contains("mode")) return Mode.ENABLED;
		int mode = nbt.getByte("mode");
		if (mode < 0 || mode >= Mode.values().length) return Mode.ENABLED;
		return Mode.values()[mode];
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack stack)
	{
		return getMode(stack) == Mode.DISABLED;
	}

	public enum Mode
	{
		ENABLED(true),
		DISABLED(false),
		NOT_IN_HAND(true);

		final boolean enabled;

		Mode(boolean enabled)
		{
			this.enabled = enabled;
		}
	}
}

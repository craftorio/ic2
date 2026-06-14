package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.item.ElectricItemTooltipHandler;
import ic2.core.ref.Ic2ArmorMaterials;
import ic2.core.util.KeyboardClient;
import ic2.core.util.StackUtil;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemArmorNightVisionGoggles extends ItemArmorUtility implements IElectricItem, IItemHudInfo
{
	public ItemArmorNightVisionGoggles(Properties settings)
	{
		super(Ic2ArmorMaterials.NIGHT_VISION_GOGGLES, settings, EquipmentSlot.HEAD);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return false;
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return 200000.0;
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return 1;
	}

	@Override
	public double getTransferLimit(ItemStack stack)
	{
		return 200.0;
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add(ElectricItem.manager.getToolTip(stack));
		return info;
	}

	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if (entity instanceof Player player)
		{
			if (slot == this.getEquipmentSlot().getIndex())
			{
				CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
				boolean active = nbtData.getBoolean("active");
				byte toggleTimer = nbtData.getByte("toggle_timer");
				if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0)
				{
					toggleTimer = 10;
					active = !active;
					if (IC2.sideProxy.isSimulating())
					{
						nbtData.putBoolean("active", active);
						if (active)
						{
							IC2.sideProxy.messagePlayer(player, "ic2.night_vision.mode.enabled");
						} else
						{
							IC2.sideProxy.messagePlayer(player, "ic2.night_vision.mode.disabled");
						}
					}
				}

				if (IC2.sideProxy.isSimulating() && toggleTimer > 0)
				{
					nbtData.putByte("toggle_timer", --toggleTimer);
				}

				if (active && IC2.sideProxy.isSimulating() && ElectricItem.manager.use(stack, 1.0, player))
				{
					int skylight = player.getCommandSenderWorld().getMaxLocalRawBrightness(BlockPos.containing(player.position()));
					if (skylight > 8)
					{
						player.removeEffect(MobEffects.NIGHT_VISION);
						player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, true));
					}
					else
					{
						player.removeEffect(MobEffects.BLINDNESS);
						player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
					}

				}
			}
		}
	}

	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag context)
	{
		ElectricItemTooltipHandler.addTooltip(stack, tooltip);
		tooltip.add(Component.translatable("item.ic2.tooltip.night_vision.toggle", KeyboardClient.altKey.getKey().getDisplayName(), KeyboardClient.modeSwitchKey.getKey().getDisplayName()));
	}

	public boolean isValidRepairItem(@NotNull ItemStack par1ItemStack, @NotNull ItemStack par2ItemStack)
	{
		return false;
	}
}

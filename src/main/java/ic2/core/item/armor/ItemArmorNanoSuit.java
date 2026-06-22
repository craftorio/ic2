package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IItemHudProvider;
import ic2.core.IC2;
import ic2.core.util.KeyboardClient;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemArmorNanoSuit extends ItemArmorElectric implements IItemHudProvider
{
	public static final int[] CHARGED_PROTECTION = new int[] { 3, 6, 8, 3 };

	public ItemArmorNanoSuit(ArmorMaterial material, EquipmentSlot slot, Properties settings)
	{
		super(material, slot, settings, 1000000.0, 1600.0, 3);
	}

	static void getNightVisionOrNot(@NotNull ItemStack stack, Player player, CompoundTag nbtData, byte toggleTimer, boolean isNightVisionEnabled)
	{
		if (IC2.sideProxy.isSimulating() && toggleTimer > 0)
		{
			nbtData.putByte("toggle_timer", --toggleTimer);
		}

		if (isNightVisionEnabled && IC2.sideProxy.isSimulating() && ElectricItem.manager.use(stack, 1.0, player))
		{
			int skylight = player.getCommandSenderWorld().getMaxLocalRawBrightness(BlockPos.containing(player.position()));
			affectPlayer(player, skylight);

		}
	}

	static void affectPlayer(Player player, int skylight)
	{
		if (skylight > 8)
		{
			player.removeEffect(MobEffects.NIGHT_VISION);
			player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, true));
		} else
		{
			player.removeEffect(MobEffects.BLINDNESS);
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
		}
	}

	@Override
	public int getEnergyPerDamage()
	{
		return 5000;
	}

	@Override
	public double getDamageAbsorptionRatio()
	{
		return 0.9;
	}

	public boolean absorbFall(ItemStack stack, float distance)
	{
		int fallDamage = Math.max((int) distance - 3, 0);
		if (fallDamage >= 8)
		{
			return false;
		}

		double energyCost = this.getEnergyPerDamage() * fallDamage;
		if (energyCost > ElectricItem.manager.getCharge(stack))
		{
			return false;
		}

		ElectricItem.manager.discharge(stack, energyCost, Integer.MAX_VALUE, true, false, false);
		return true;
	}

	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if (entity instanceof Player player)
		{
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
			byte toggleTimer = nbtData.getByte("toggle_timer");
			boolean ret = false;
			if (slot == EquipmentSlot.HEAD.getIndex())
			{
				boolean isNightVisionEnabled = nbtData.getBoolean("night_vision");
				short hubmode = nbtData.getShort("hud_mode");
				if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0)
				{
					toggleTimer = 10;
					isNightVisionEnabled = !isNightVisionEnabled;
					if (IC2.sideProxy.isSimulating())
					{
						nbtData.putBoolean("night_vision", isNightVisionEnabled);
						if (isNightVisionEnabled)
						{
							IC2.sideProxy.messagePlayer(player, "ic2.night_vision.mode.enabled");
						} else
						{
							IC2.sideProxy.messagePlayer(player, "ic2.night_vision.mode.disabled");
						}
					}
				}

				if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isHudModeKeyDown(player) && toggleTimer == 0)
				{
					toggleTimer = 10;
					if (hubmode == HudMode.getMaxMode())
					{
						hubmode = 0;
					} else
					{
						hubmode++;
					}

					if (IC2.sideProxy.isSimulating())
					{
						nbtData.putShort("hud_mode", hubmode);
						IC2.sideProxy.messagePlayer(player, Component.translatable(HudMode.getFromID(hubmode).getTranslationKey()).getString());
					}
				}

				getNightVisionOrNot(stack, player, nbtData, toggleTimer, isNightVisionEnabled);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context)
	{
		super.appendHoverText(stack, world, tooltip, context);
		if (this.getEquipmentSlot() == EquipmentSlot.HEAD)
		{
			tooltip.add(Component.translatable("item.ic2.tooltip.night_vision.toggle", KeyboardClient.altKey.getKey().getDisplayName(), KeyboardClient.modeSwitchKey.getKey().getDisplayName()));
		}
	}

	public @NotNull Rarity getRarity(@NotNull ItemStack stack)
	{
		return Rarity.UNCOMMON;
	}

	@Override
	public boolean doesProvideHUD(ItemStack stack)
	{
		return this.getEquipmentSlot() == EquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0.0;
	}

	@Override
	public HudMode getHudMode(ItemStack stack)
	{
		return HudMode.getFromID(StackUtil.getOrCreateNbtData(stack).getShort("hud_mode"));
	}
}

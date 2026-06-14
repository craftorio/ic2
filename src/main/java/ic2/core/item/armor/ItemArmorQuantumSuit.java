package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IHazmatLike;
import ic2.api.item.IItemHudProvider;
import ic2.core.IC2;
import ic2.core.Ic2Potion;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.item.ItemTinCan;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;
import ic2.core.util.KeyboardClient;
import ic2.core.util.StackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ItemArmorQuantumSuit extends ItemArmorElectric implements IJetpack, IHazmatLike, IItemHudProvider, DyeableLeatherItem
{
	public static final int[] CHARGED_PROTECTION = new int[] { 3, 6, 8, 3 };
	protected static final Map<MobEffect, Integer> potionRemovalCost = new IdentityHashMap<>();
	private float jumpCharge;

	static
	{
		potionRemovalCost.put(MobEffects.POISON, 10000);
		potionRemovalCost.put(Ic2Potion.radiation, 10000);
		potionRemovalCost.put(MobEffects.WITHER, 25000);
	}

	public ItemArmorQuantumSuit(ArmorMaterial material, EquipmentSlot armorType, Properties settings)
	{
		super(material, armorType, settings, 1.0E7, 12000.0, 4);
	}

	@Override
	public int getEnergyPerDamage()
	{
		return 20000;
	}

	@Override
	public double getDamageAbsorptionRatio()
	{
		return switch (this.getEquipmentSlot())
		{
			case HEAD, FEET -> 0.15;
			case CHEST -> 0.48;
			case LEGS -> 0.3;
			default -> 0.0;
		};
	}

	public boolean hasCustomColor(@NotNull ItemStack stack)
	{
		return this.getColor(stack) != -1;
	}

	public void clearColor(@NotNull ItemStack stack)
	{
		CompoundTag nbt = this.getDisplayNbt(stack, false);
		if (nbt != null && nbt.contains("color", 3))
		{
			nbt.remove("color");
			if (nbt.isEmpty())
			{
				assert stack.getTag() != null;
				stack.getTag().remove("display");
			}
		}
	}

	public int getColor(@NotNull ItemStack stack)
	{
		CompoundTag nbt = this.getDisplayNbt(stack, false);
		return nbt != null && nbt.contains("color", 3) ? nbt.getInt("color") : -1;
	}

	public void setColor(@NotNull ItemStack stack, int color)
	{
		CompoundTag nbt = this.getDisplayNbt(stack, true);
		assert nbt != null;
		nbt.putInt("color", color);
	}

	private CompoundTag getDisplayNbt(ItemStack stack, boolean create)
	{
		CompoundTag nbt = stack.getTag();
		if (nbt == null)
		{
			if (!create)
			{
				return null;
			}

			nbt = new CompoundTag();
			stack.setTag(nbt);
		}

		CompoundTag ret;
		if (!nbt.contains("display", 10))
		{
			if (!create)
			{
				return null;
			}

			ret = new CompoundTag();
			nbt.put("display", ret);
		} else
		{
			ret = nbt.getCompound("display");
		}

		return ret;
	}

	@Override
	public boolean addsProtection(LivingEntity entity, EquipmentSlot slot, ItemStack stack)
	{
		return ElectricItem.manager.getCharge(stack) > 0.0;
	}

	public boolean absorbFall(ItemStack stack, LivingEntity entity, float distance)
	{
		int fallDamage = Math.max((int) distance - 10, 0);
		double energyCost = this.getEnergyPerDamage() * fallDamage;
		if (energyCost > ElectricItem.manager.getCharge(stack))
		{
			return false;
		}

		ElectricItem.manager.discharge(stack, energyCost, Integer.MAX_VALUE, true, false, false);
		return true;
	}

	@Override
	public @NotNull Rarity getRarity(@NotNull ItemStack stack)
	{
		return Rarity.RARE;
	}

	@Override
	public int getEnchantmentValue()
	{
		return 0;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context)
	{
		super.appendHoverText(stack, world, tooltip, context);
		if (this.getEquipmentSlot() == EquipmentSlot.HEAD)
		{
			tooltip.add(Component.translatable("item.ic2.tooltip.night_vision.toggle", KeyboardClient.altKey.getKey().getDisplayName(), KeyboardClient.modeSwitchKey.getKey().getDisplayName()));
		}
		if (this.getEquipmentSlot() == EquipmentSlot.CHEST)
		{
			tooltip.add(Component.translatable("item.ic2.tooltip.jetpack.toggle", Minecraft.getInstance().options.keyJump.getKey().getDisplayName(), KeyboardClient.modeSwitchKey.getKey().getDisplayName()));
		}
		
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof Player player))
		{
			return;
		}

		if (player.getItemBySlot(this.getEquipmentSlot()) != stack)
		{
			return;
		}

		CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
		byte toggleTimer = nbtData.getByte("toggle_timer");
		boolean ret = false;
		EquipmentSlot equipSlot = this.getEquipmentSlot();

		if (equipSlot == EquipmentSlot.HEAD)
		{
			int air = player.getAirSupply();
			if (ElectricItem.manager.canUse(stack, 1000.0) && air < player.getMaxAirSupply())
			{
				player.setAirSupply(air + 200);
				ElectricItem.manager.use(stack, 1000.0, player);
				ret = true;
			} else if (air <= 0)
			{
				IC2.achievements.issueAchievement(player, "starveWithQHelmet");
			}

			if (ElectricItem.manager.canUse(stack, 1000.0) && player.getFoodData().needsFood())
			{
				int foodSlot = -1;

				for (int i = 0; i < player.getInventory().items.size(); i++)
				{
					ItemStack invStack = player.getInventory().items.get(i);
					if (!StackUtil.isEmpty(invStack) && invStack.getItem() == Ic2Items.FILLED_TIN_CAN)
					{
						foodSlot = i;
						break;
					}
				}

				if (foodSlot > -1)
				{
					ItemStack foodStack = player.getInventory().items.get(foodSlot);
					ItemTinCan can = (ItemTinCan) foodStack.getItem();
					InteractionResultHolder<ItemStack> result = can.onEaten(player, foodStack);
					player.getInventory().items.set(foodSlot, result.getObject());

					if (result.getResult() == InteractionResult.SUCCESS)
					{
						ElectricItem.manager.use(stack, 1000.0, player);
					}

					ret = true;
				}
			} else if (player.getFoodData().getFoodLevel() <= 0)
			{
				IC2.achievements.issueAchievement(player, "starveWithQHelmet");
			}

			for (MobEffectInstance effect : new LinkedList<>(player.getActiveEffects()))
			{
				MobEffect potion = effect.getEffect();
				Integer cost = potionRemovalCost.get(potion);
				if (cost != null)
				{
					cost = cost * (effect.getAmplifier() + 1);
					if (ElectricItem.manager.canUse(stack, cost))
					{
						ElectricItem.manager.use(stack, cost, player);
						player.removeEffect(potion);
					}
				}
			}

			boolean Nightvision = nbtData.getBoolean("night_vision");
			short hudmode = nbtData.getShort("hud_mode");
			if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0)
			{
				toggleTimer = 10;
				Nightvision = !Nightvision;
				if (IC2.sideProxy.isSimulating())
				{
					nbtData.putBoolean("night_vision", Nightvision);
					if (Nightvision)
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
				if (hudmode == HudMode.getMaxMode())
				{
					hudmode = 0;
				} else
				{
					hudmode++;
				}

				if (IC2.sideProxy.isSimulating())
				{
					nbtData.putShort("hud_mode", hudmode);
					IC2.sideProxy.messagePlayer(player, Localization.translate(HudMode.getFromID(hudmode).getTranslationKey()));
				}
			}

			if (IC2.sideProxy.isSimulating() && toggleTimer > 0)
			{
				nbtData.putByte("toggle_timer", --toggleTimer);
			}

			if (Nightvision && IC2.sideProxy.isSimulating() && ElectricItem.manager.use(stack, 1.0, player))
			{
				BlockPos pos = BlockPos.containing(player.position());
				int skylight = player.getCommandSenderWorld().getMaxLocalRawBrightness(pos);
				if (skylight > 8)
				{
					player.removeEffect(MobEffects.NIGHT_VISION);
					player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, true));
				} else
				{
					player.removeEffect(MobEffects.BLINDNESS);
					player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
				}

				ret = true;
			}

		} else if (equipSlot == EquipmentSlot.CHEST)
		{
			player.clearFire();
		} else if (equipSlot == EquipmentSlot.LEGS)
		{
			boolean enableQuantumSpeedOnSprint;
			if (IC2.sideProxy.isRendering())
			{
				enableQuantumSpeedOnSprint = ConfigUtil.getBool(MainConfig.get(), "misc/quantumSpeedOnSprint");
			} else
			{
				enableQuantumSpeedOnSprint = true;
			}

			if (ElectricItem.manager.canUse(stack, 1000.0)
				&& (player.onGround() || player.isInWater())
				&& IC2.keyboard.isForwardKeyDown(player)
				&& (enableQuantumSpeedOnSprint && player.isSprinting() || !enableQuantumSpeedOnSprint && IC2.keyboard.isBoostKeyDown(player)))
			{
				byte speedTicker = nbtData.getByte("speed_ticker");
				if (++speedTicker >= 10)
				{
					speedTicker = 0;
					ElectricItem.manager.use(stack, 1000.0, player);
					ret = true;
				}

				nbtData.putByte("speed_ticker", speedTicker);
				float speed = 0.22F;
				if (player.isInWater())
				{
					speed = 0.1F;
					if (IC2.keyboard.isJumpKeyDown(player))
					{
						Vec3 motion = player.getDeltaMovement();
						player.setDeltaMovement(motion.x, motion.y + 0.1F, motion.z);
					}
				}

				float yawRad = player.getYRot() * (float) Math.PI / 180.0F;
				Vec3 motion = player.getDeltaMovement();
				player.setDeltaMovement(motion.x + (-Math.sin(yawRad) * speed), motion.y, motion.z + (Math.cos(yawRad) * speed));
			}

		} else if (equipSlot == EquipmentSlot.FEET)
		{
			if (IC2.sideProxy.isSimulating())
			{
				boolean wasOnGround = !nbtData.contains("on_ground") || nbtData.getBoolean("on_ground");
				if (wasOnGround && !player.onGround() && IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isBoostKeyDown(player))
				{
					ElectricItem.manager.use(stack, 4000.0, player);
					ret = true;
				}

				if (player.onGround() != wasOnGround)
				{
					nbtData.putBoolean("on_ground", player.onGround());
				}
			} else
			{
				if (ElectricItem.manager.canUse(stack, 4000.0) && player.onGround())
				{
					this.jumpCharge = 1.0F;
				}

				if (player.getDeltaMovement().y >= 0.0 && this.jumpCharge > 0.0F && !player.isInWater())
				{
					if (IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isBoostKeyDown(player))
					{
						if (this.jumpCharge == 1.0F)
						{
							Vec3 motion = player.getDeltaMovement();
							player.setDeltaMovement(motion.x * 3.5, motion.y, motion.z * 3.5);
						}

						Vec3 motion = player.getDeltaMovement();
						player.setDeltaMovement(motion.x, motion.y + this.jumpCharge * 0.3F, motion.z);
						this.jumpCharge = (float) (this.jumpCharge * 0.75);
					} else if (this.jumpCharge < 1.0F)
					{
						this.jumpCharge = 0.0F;
					}
				}
			}

		}

		if (ret)
		{
			player.inventoryMenu.broadcastChanges();
		}
	}

	@Override
	public void drainEnergy(ItemStack pack, int amount)
	{
		ElectricItem.manager.discharge(pack, amount + 6, Integer.MAX_VALUE, true, false, false);
	}

	@Override
	public float getPower(ItemStack stack)
	{
		return 1.0F;
	}

	@Override
	public float getDropPercentage(ItemStack stack)
	{
		return 0.05F;
	}

	@Override
	public double getChargeLevel(ItemStack stack)
	{
		return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
	}

	@Override
	public boolean isJetpackActive(ItemStack stack)
	{
		return true;
	}

	@Override
	public float getHoverMultiplier(ItemStack stack, boolean upwards)
	{
		return 0.1F;
	}

	@Override
	public float getWorldHeightDivisor(ItemStack stack)
	{
		return 0.9F;
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

	public static boolean isWearingFullSet(Player player)
	{
		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			if (slot.getType() == EquipmentSlot.Type.ARMOR)
			{
				ItemStack armor = player.getItemBySlot(slot);
				if (!(armor.getItem() instanceof ItemArmorQuantumSuit))
				{
					return false;
				}
			}
		}

		return true;
	}

}

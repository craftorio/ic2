package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.sound.item.ISwingSoundItem;
import ic2.core.IC2;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.slot.ArmorSlot;
import ic2.core.util.StackUtil;
import ic2.forge.NanoSaberCapabilities;

import java.util.Collections;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractItemNanoSaber extends ItemElectricTool implements ISwingSoundItem
{
	private static final int SYNC_ACTIVE_BASE = 0x100;

	private int soundTicker = 0;

	public AbstractItemNanoSaber(Properties settings)
	{
		super(settings, 10, Tiers.DIAMOND, Collections.emptyList());
		this.maxCharge = 160000;
		this.transferLimit = 500;
		this.tier = 3;
	}

	public static boolean isActive(ItemStack stack)
	{
		return NanoSaberCapabilities.isActive(stack);
	}

	private static void setActive(ItemStack stack, boolean active)
	{
		NanoSaberCapabilities.setActive(stack, active);
	}

	private static void syncActiveState(Player player, ItemStack stack, boolean active)
	{
		int slot = findSlot(player, stack);
		if (slot < 0)
		{
			return;
		}

		IC2.network.get(true).initiateItemEvent(player, stack, SYNC_ACTIVE_BASE + slot * 2 + (active ? 1 : 0), true);
	}

	private static int findSlot(Player player, ItemStack stack)
	{
		for (int i = 0; i < player.getInventory().getContainerSize(); i++)
		{
			ItemStack invStack = player.getInventory().getItem(i);
			if (invStack == stack || ItemStack.isSameItemSameTags(invStack, stack))
			{
				return i;
			}
		}

		return -1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if (slotChanged) return true;
		return !ItemStack.isSameItem(oldStack, newStack);
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack)
	{
		return false;
	}

	@Override
	public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity entity)
	{
		if (!super.consumeEnergy(stack, amount, entity))
		{
			if (isActive(stack))
			{
				setActive(stack, false);
				NanoSaberCapabilities.setEnergyTick(stack, 0);
				if (entity instanceof Player player)
				{
					syncActiveState(player, stack, false);
				}
			}

			return false;
		} else
		{
			return true;
		}
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		if (isActive(stack))
		{
			this.soundTicker++;
			if (IC2.sideProxy.isRendering() && this.soundTicker % 4 == 0)
			{
				Entity entity = stack.getEntityRepresentation();
				if (entity != null)
				{
					entity.playSound(this.getRandomSwingSound(), 1.0F, 1.0F);
				}
			}

			return state.getBlock() == Blocks.COBWEB ? 50.0F : 4.0F;
		} else
		{
			return 1.0F;
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity source)
	{
		if (!isActive(stack))
		{
			return true;
		}

		if (IC2.sideProxy.isSimulating())
		{
			this.consumeEnergy(stack, 400.0, source);
			if (!(source instanceof ServerPlayer) || !(target instanceof Player) || ((ServerPlayer) source).canHarmPlayer((Player) target))
			{
				for (EquipmentSlot slot : ArmorSlot.getAll())
				{
					if (!ElectricItem.manager.canUse(stack, 2000.0))
					{
						break;
					}

					ItemStack armor = target.getItemBySlot(slot);
					double amount = 0.0;
					if (armor.getItem() instanceof ItemArmorNanoSuit)
					{
						amount = 48000.0;
					} else if (armor.getItem() instanceof ItemArmorQuantumSuit)
					{
						amount = 300000.0;
					}

					if (amount > 0.0)
					{
						this.consumeEnergy(armor, amount, null);
						if (!ElectricItem.manager.canUse(armor, 1.0))
						{
							target.setItemSlot(slot, null);
						}

						this.consumeEnergy(stack, 2000.0, source);
					}
				}
			}
		}

		return true;
	}

	public SoundEvent getRandomSwingSound()
	{
		return switch (IC2.random.nextInt(3))
		{
			case 1 -> Ic2SoundEvents.ITEM_NANOSABER_SWING2;
			case 2 -> Ic2SoundEvents.ITEM_NANOSABER_SWING3;
			default -> Ic2SoundEvents.ITEM_NANOSABER_SWING1;
		};
	}

	public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player miner)
	{
		return !miner.isCreative() && super.canAttackBlock(state, world, pos, miner);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context)
	{
		return super.useOn(context);
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (isActive(stack) || ElectricItem.manager.canUse(stack, 16.0))
		{
			toggleActive(stack, player);
			return InteractionResultHolder.consume(stack);
		}

		return world.isClientSide ? InteractionResultHolder.consume(stack) : super.use(world, player, hand);
	}

	private void toggleActive(ItemStack stack, Player player)
	{
		if (isActive(stack))
		{
			setActive(stack, false);
			NanoSaberCapabilities.setEnergyTick(stack, 0);
		} else if (ElectricItem.manager.canUse(stack, 16.0))
		{
			setActive(stack, true);
			NanoSaberCapabilities.setEnergyTick(stack, 0);
		} else
		{
			return;
		}

		if (!player.level().isClientSide)
		{
			syncActiveState(player, stack, isActive(stack));
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean par5)
	{
		super.inventoryTick(stack, world, entity, slot, par5 && isActive(stack));
		if (!isActive(stack))
		{
			return;
		}

		if (!IC2.sideProxy.isSimulating())
		{
			return;
		}

		int energyTick = NanoSaberCapabilities.getEnergyTick(stack) + 1;
		NanoSaberCapabilities.setEnergyTick(stack, energyTick);

		if (energyTick % 16 == 0 && entity instanceof ServerPlayer)
		{
			if (slot < 9)
			{
				this.consumeEnergy(stack, 64.0, (Player) entity);
			} else if (energyTick % 64 == 0)
			{
				this.consumeEnergy(stack, 16.0, (Player) entity);
			}
		}
	}

	public float getActiveData(ItemStack stack, Level world)
	{
		return (!isActive(stack) || world == null) ? 0f : 1f;
	}

	public @NotNull Rarity getRarity(@NotNull ItemStack stack)
	{
		return Rarity.UNCOMMON;
	}

	@Override
	protected SoundEvent getIdleSound(LivingEntity player, ItemStack stack)
	{
		return Ic2SoundEvents.ITEM_NANOSABER_IDLE;
	}

	@Override
	protected SoundEvent getStartSound(LivingEntity player, ItemStack stack)
	{
		return Ic2SoundEvents.ITEM_NANOSABER_POWER_UP;
	}

	@Override
	public SoundEvent getSwingSound(LivingEntity entity, InteractionHand hand)
	{
		return isActive(entity.getItemInHand(hand)) ? this.getRandomSwingSound() : null;
	}

	@Override
	public void onNetworkEvent(ItemStack stack, Player player, int event)
	{
		if (event >= SYNC_ACTIVE_BASE)
		{
			int delta = event - SYNC_ACTIVE_BASE;
			boolean active = (delta & 1) == 1;
			int slot = delta >> 1;
			if (slot < player.getInventory().getContainerSize())
			{
				ItemStack invStack = player.getInventory().getItem(slot);
				if (invStack.getItem() instanceof AbstractItemNanoSaber)
				{
					setActive(invStack, active);
					if (!active)
					{
						NanoSaberCapabilities.setEnergyTick(invStack, 0);
					}
				}
			}

			return;
		}

		super.onNetworkEvent(stack, player, event);
	}
}
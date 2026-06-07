package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.sound.item.ISwingSoundItem;
import ic2.core.IC2;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.slot.ArmorSlot;
import ic2.core.util.StackUtil;

import java.util.Collections;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractItemNanoSaber extends ItemElectricTool implements ISwingSoundItem
{
	public static int ticker = 0;
	private int soundTicker = 0;
	public static final int ANIMATION_FRAME = 4;

	public AbstractItemNanoSaber(Properties settings)
	{
		super(settings, 10, Tiers.DIAMOND, Collections.emptyList());
		this.maxCharge = 160000;
		this.transferLimit = 500;
		this.tier = 3;
	}

	@Override
	public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity entity)
	{
		if (!super.consumeEnergy(stack, amount, entity))
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			setActive(nbt, false);
			return false;
		} else
		{
			return true;
		}
	}

	@Override
	public float m_8102_(ItemStack stack, BlockState state)
	{
		if (isActive(stack))
		{
			this.soundTicker++;
			if (IC2.sideProxy.isRendering() && this.soundTicker % 4 == 0)
			{
				Entity entity = stack.m_41609_();
				if (entity != null)
				{
					entity.m_5496_(this.getRandomSwingSound(), 1.0F, 1.0F);
				}
			}

			return state.getBlock() == Blocks.f_50033_ ? 50.0F : 4.0F;
		} else
		{
			return 1.0F;
		}
	}

	@Override
	public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity source)
	{
		if (!isActive(stack))
		{
			return true;
		}

		if (IC2.sideProxy.isSimulating())
		{
			this.consumeEnergy(stack, 400.0, source);
			if (!(source instanceof ServerPlayer) || !(target instanceof Player) || ((ServerPlayer) source).m_7099_((Player) target))
			{
				for (EquipmentSlot slot : ArmorSlot.getAll())
				{
					if (!ElectricItem.manager.canUse(stack, 2000.0))
					{
						break;
					}

					ItemStack armor = target.m_6844_(slot);
					if (armor != null)
					{
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
								target.m_8061_(slot, null);
							}

							this.consumeEnergy(stack, 2000.0, source);
						}
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

	public boolean m_6777_(BlockState state, Level world, BlockPos pos, Player miner)
	{
		return miner.m_7500_() ? false : super.m_6777_(state, world, pos, miner);
	}

	@Override
	public InteractionResult m_6225_(UseOnContext context)
	{
		return super.m_6225_(context);
	}

	@Override
	public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (world.isClientSide)
		{
			return new InteractionResultHolder(InteractionResult.PASS, stack);
		} else
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			if (isActive(nbt))
			{
				setActive(nbt, false);
				return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
			} else if (ElectricItem.manager.canUse(stack, 16.0))
			{
				setActive(nbt, true);
				return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
			} else
			{
				return super.m_7203_(world, player, hand);
			}
		}
	}

	@Override
	public void m_6883_(ItemStack stack, Level world, Entity entity, int slot, boolean par5)
	{
		super.m_6883_(stack, world, entity, slot, par5 && isActive(stack));
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		if (!isActive(nbt))
		{
			ticker = 0;
		} else
		{
			ticker++;
			if (ticker % 16 == 0 && entity instanceof ServerPlayer)
			{
				if (slot < 9)
				{
					this.consumeEnergy(stack, 64.0, (Player) entity);
				} else if (ticker % 64 == 0)
				{
					this.consumeEnergy(stack, 16.0, (Player) entity);
				}
			}
		}
	}

	public float getActiveData()
	{
		return ticker > 0 ? (float) (Math.floor(ticker / 20.0 * 4.0) % 10.0 + 1.0) / 10.0F : 0.0F;
	}

	public Rarity m_41460_(ItemStack stack)
	{
		return Rarity.UNCOMMON;
	}

	public static boolean isActive(ItemStack stack)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
		return isActive(nbt);
	}

	public static boolean isActive(CompoundTag nbt)
	{
		return nbt.getBoolean("active");
	}

	private static void setActive(CompoundTag nbt, boolean active)
	{
		nbt.putBoolean("active", active);
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
		return this.getRandomSwingSound();
	}
}

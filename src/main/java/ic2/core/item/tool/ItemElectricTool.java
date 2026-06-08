package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.api.network.INetworkItemEventListener;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.ElectricItemTooltipHandler;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.sound.Sound;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class ItemElectricTool extends DiggerItem implements IElectricItem, INetworkItemEventListener, IItemHudInfo
{
	public double operationEnergyCost;
	private final Collection<TagKey<Block>> effectiveBlocks;
	public int maxCharge;
	public int transferLimit;
	public int tier;
	protected Sound idleSound;
	protected Sound startSound;
	protected Sound stopSound;
	protected boolean wasEquipped;

	protected ItemElectricTool(Properties settings, int operationEnergyCost)
	{
		this(settings, operationEnergyCost, Tiers.IRON, Collections.emptyList());
	}

	protected ItemElectricTool(Properties settings, int operationEnergyCost, Tier material, Collection<TagKey<Block>> effectiveBlocks)
	{
		this(settings, 2.0F, -3.0F, operationEnergyCost, material, effectiveBlocks);
	}

	private ItemElectricTool(
		Properties settings, float attackDamage, float attackSpeed, int operationEnergyCost, Tier material, Collection<TagKey<Block>> effectiveBlocks
	)
	{
		super(attackDamage, attackSpeed, material, effectiveBlocks.isEmpty() ? Ic2BlockTags.EMPTY : effectiveBlocks.iterator().next(), settings);
		this.operationEnergyCost = operationEnergyCost;
		this.effectiveBlocks = effectiveBlocks;
	}

	public static boolean consumeEnergy(ItemStack stack, double amount, int tier, LivingEntity entity)
	{
		if (!(stack.getItem() instanceof IElectricItem))
		{
			return false;
		}

		if (!ElectricItem.manager.canUse(stack, amount))
		{
			return false;
		}

		boolean isConsumed;
		if (entity == null)
		{
			isConsumed = Util.isSimilar(ElectricItem.manager.discharge(stack, amount, tier, true, false, false), amount);
		} else
		{
			isConsumed = ElectricItem.manager.use(stack, amount, entity);
		}

		if (ElectricItem.manager.getCharge(stack) <= 0.0 && entity instanceof Player player)
		{
			IC2.network.get(true).initiateItemEvent(player, stack, 0, true);
		}

		return isConsumed;
	}

	@Override
	public void onNetworkEvent(ItemStack stack, Player player, int event)
	{
		player.playSound(this.getShutdownSound(), 1.0F, 1.0F);
	}

	public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity entity)
	{
		return consumeEnergy(stack, amount, this.tier, entity);
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		List<String> info = new LinkedList<>();
		info.add(ElectricItem.manager.getToolTip(stack));
		info.add(Localization.translate("ic2.item.tooltip.PowerTier", this.tier));
		return info;
	}

	public InteractionResult useOn(UseOnContext context)
	{
		ElectricItem.manager.use(context.getItemInHand(), 0.0, context.getPlayer());
		return super.useOn(context);
	}

	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ElectricItem.manager.use(StackUtil.get(player, hand), 0.0, player);
		return super.use(world, player, hand);
	}

	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		return this.isEffective(state) && ElectricItem.manager.canUse(stack, this.operationEnergyCost) ? this.speed : 1.0F;
	}

	public boolean isCorrectToolForDrops(BlockState state)
	{
		int level = this.getTier().getLevel();
		return (level >= 3 || !state.is(BlockTags.NEEDS_DIAMOND_TOOL))
			&& (level >= 2 || !state.is(BlockTags.NEEDS_IRON_TOOL))
			&& (level >= 1 || !state.is(BlockTags.NEEDS_STONE_TOOL))
			? this.isEffective(state)
			: false;
	}

	private boolean isEffective(BlockState state)
	{
		for (TagKey<Block> tag : this.effectiveBlocks)
		{
			if (state.is(tag))
			{
				return true;
			}
		}

		return false;
	}

	public boolean hurtEnemy(ItemStack itemstack, LivingEntity entityliving, LivingEntity entityliving1)
	{
		return true;
	}

	public int getEnchantmentValue()
	{
		return 0;
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return false;
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return this.maxCharge;
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return this.tier;
	}

	@Override
	public double getTransferLimit(ItemStack stack)
	{
		return this.transferLimit;
	}

	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity user)
	{
		if (state.getDestroySpeed(world, pos) != 0.0F)
		{
			this.consumeEnergy(stack, this.operationEnergyCost, user);
		}

		return true;
	}

	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems)
	{
		if (true)
		{
			ElectricItemManager.addChargeVariants(this, subItems);
		}
	}

	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context)
	{
		ElectricItemTooltipHandler.addTooltip(stack, tooltip);
	}

	protected ItemStack getItemStack(double charge)
	{
		ItemStack ret = new ItemStack(this);
		ElectricItem.manager.charge(ret, charge, Integer.MAX_VALUE, true, false);
		return ret;
	}

	public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int i, boolean flag)
	{
		boolean isEquipped = flag && entity instanceof LivingEntity;
		if (IC2.sideProxy.isRendering())
		{
			if (isEquipped && !this.wasEquipped)
			{
				this.initSound((LivingEntity) entity, itemstack);
				if (this.idleSound != null)
				{
					this.idleSound.play();
				}

				if (this.startSound != null)
				{
					this.startSound.playOnce();
				}
			} else if (!isEquipped && this.idleSound != null && entity instanceof LivingEntity theEntity)
			{
				ItemStack stack = theEntity.getItemBySlot(EquipmentSlot.MAINHAND);
				if (stack == null || stack.getItem() != this || stack == itemstack)
				{
					if (this.stopSound != null)
					{
						this.stopSound.playOnce();
					}

					this.clearSound(theEntity);
				}
			}

			this.wasEquipped = isEquipped;
		}
	}

	protected void initSound(LivingEntity entity, ItemStack stack)
	{
		SoundEvent idleEvent;
		if (this.idleSound == null && (idleEvent = this.getIdleSound(entity, stack)) != null)
		{
			this.idleSound = IC2.soundManager.createSound(entity, idleEvent, SoundSource.PLAYERS, entity, 1.0F, 1.0F);
		}

		SoundEvent startEvent;
		if (this.startSound == null && (startEvent = this.getStartSound(entity, stack)) != null)
		{
			this.stopSound = IC2.soundManager.createSound(entity, startEvent, SoundSource.PLAYERS, entity, 1.0F, 1.0F);
		}

		SoundEvent stopEvent;
		if (this.stopSound == null && (stopEvent = this.getStopSound(entity, stack)) != null)
		{
			this.stopSound = IC2.soundManager.createSound(entity, stopEvent, SoundSource.PLAYERS, entity, 1.0F, 1.0F);
		}
	}

	protected void clearSound(LivingEntity entity)
	{
		if (this.idleSound != null)
		{
			IC2.soundManager.removeSound(entity, this.idleSound);
			this.idleSound = null;
		}

		if (this.startSound != null)
		{
			IC2.soundManager.removeSound(entity, this.startSound);
			this.startSound = null;
		}

		if (this.stopSound != null)
		{
			IC2.soundManager.removeSound(entity, this.stopSound);
			this.stopSound = null;
		}
	}

	public boolean onDroppedByPlayer(ItemStack stack, Player player)
	{
		this.clearSound(player);
		return true;
	}

	protected SoundEvent getIdleSound(LivingEntity player, ItemStack stack)
	{
		return null;
	}

	protected SoundEvent getStopSound(LivingEntity player, ItemStack stack)
	{
		return null;
	}

	protected SoundEvent getStartSound(LivingEntity player, ItemStack stack)
	{
		return null;
	}

	public SoundEvent getShutdownSound()
	{
		return Ic2SoundEvents.ITEM_ELECTRIC_SHUTDOWN;
	}

	public boolean isBarVisible(ItemStack stack)
	{
		return true;
	}

	public int getBarWidth(ItemStack stack)
	{
		return (int) Math.round(ElectricItem.manager.getChargeLevel(stack) * 13.0);
	}

	public int getBarColor(ItemStack stack)
	{
		return Mth.hsvToRgb((float) (ElectricItem.manager.getChargeLevel(stack) / 3.0), 1.0F, 1.0F);
	}

	public boolean canUse(ItemStack stack)
	{
		return ElectricItem.manager.canUse(stack, this.operationEnergyCost);
	}
}

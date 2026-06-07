package ic2.core.item.tool;

import ic2.api.item.BlockBreakableItem;
import ic2.api.item.IEntityAttackableItem;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.ref.Ic2ToolMaterials;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Collections;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import org.jetbrains.annotations.Nullable;

public class ItemElectricToolChainsaw extends ItemElectricTool implements IHitSoundOverride, BlockBreakableItem, IEntityAttackableItem
{
	public ItemElectricToolChainsaw(Properties settings)
	{
		super(settings, 100, Ic2ToolMaterials.CHAINSAW, Collections.singletonList(BlockTags.f_144280_));
		this.maxCharge = 30000;
		this.transferLimit = 100;
		this.tier = 1;
	}

	private boolean isShearMode(ItemStack stack)
	{
		return !StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear");
	}

	@Override
	public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand)
	{
		if (world.isClientSide)
		{
			return super.m_7203_(world, player, hand);
		}

		if (IC2.keyboard.isModeSwitchKeyDown(player))
		{
			CompoundTag compoundTag = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
			if (compoundTag.getBoolean("disableShear"))
			{
				compoundTag.putBoolean("disableShear", false);
				IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.normal");
			} else
			{
				compoundTag.putBoolean("disableShear", true);
				IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.noShear");
			}
		}

		return super.m_7203_(world, player, hand);
	}

	@Override
	public boolean m_8096_(BlockState state)
	{
		return super.m_8096_(state) || state.m_60713_(Blocks.f_50033_) || Util.canShear(state);
	}

	@Override
	public float m_8102_(ItemStack stack, BlockState state)
	{
		return !this.canUse(stack) || !state.m_204336_(BlockTags.f_144280_) && !state.m_60713_(Blocks.f_50033_) && !Util.canShear(state) ? 1.0F : this.f_40980_;
	}

	@Override
	public boolean onAttackEntity(Player player, Entity target)
	{
		ItemStack itemstack = player.m_21205_();
		if (this.consumeEnergy(itemstack, this.operationEnergyCost, player))
		{
			this.playUsingSound(player);
		}

		return true;
	}

	private void handleVanillaBlockBreakLogic(Player player, Level world, BlockPos pos, BlockState state)
	{
		world.m_5898_(player, 2001, pos, Block.m_49956_(state));
		if (state.m_204336_(BlockTags.f_13088_))
		{
			PiglinAi.m_34873_(player, false);
		}

		world.m_220407_(GameEvent.f_157794_, pos, Context.m_223719_(player, state));
	}

	@Override
	public InteractionResult onBlockStartBreak(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction)
	{
		BlockState state = world.getBlockState(pos);
		ItemStack stack = player.m_21120_(hand);
		if (!this.isShearMode(stack) || !Util.canShear(state))
		{
			return InteractionResult.PASS;
		} else if (this.consumeEnergy(stack, this.operationEnergyCost, player))
		{
			this.handleVanillaBlockBreakLogic(player, world, pos, state);
			StackUtil.dropAsEntity(world, pos, new ItemStack(state.getBlock().m_5456_()));
			world.m_7731_(pos, Blocks.f_50016_.defaultBlockState(), 11);
			this.playUsingSound(player);
			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	public InteractionResult m_6880_(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand)
	{
		if (entity instanceof Shearable shearable
			&& !StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear")
			&& this.consumeEnergy(stack, this.operationEnergyCost, user)
			&& shearable.m_6220_())
		{
			shearable.m_5851_(SoundSource.PLAYERS);
			this.playUsingSound(user);
			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	public void playUsingSound(LivingEntity user)
	{
		if (user.m_9236_().isClientSide)
		{
			user.m_5496_(this.getToolUsingSound(), 1.0F, 1.0F);
		}
	}

	public SoundEvent getToolUsingSound()
	{
		return IC2.random.m_188499_() ? Ic2SoundEvents.ITEM_CHAINSAW_USE1 : Ic2SoundEvents.ITEM_CHAINSAW_USE2;
	}

	@Override
	public SoundEvent getHitSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		return this.getToolUsingSound();
	}

	@Override
	public SoundEvent getBreakSoundForBlock(LocalPlayer player, Level world, BlockPos pos, ItemStack stack)
	{
		return null;
	}

	@Override
	protected SoundEvent getIdleSound(LivingEntity player, ItemStack stack)
	{
		return Ic2SoundEvents.ITEM_CHAINSAW_IDLE;
	}

	@Override
	protected SoundEvent getStopSound(LivingEntity player, ItemStack stack)
	{
		return Ic2SoundEvents.ITEM_CHAINSAW_STOP;
	}

	@Override
	public boolean beforeBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
		return true;
	}

	@Override
	public void afterBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
	}
}

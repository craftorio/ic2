package ic2.core.item.tool;

import ic2.api.item.BlockBreakableItem;
import ic2.api.item.IEntityAttackableItem;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.ref.Ic2ToolMaterials;
import ic2.core.util.KeyboardClient;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraftforge.common.IForgeShearable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemElectricToolChainsaw extends ItemElectricTool implements IHitSoundOverride, BlockBreakableItem, IEntityAttackableItem
{
	public ItemElectricToolChainsaw(Properties settings)
	{
		super(settings, 100, Ic2ToolMaterials.CHAINSAW, Collections.singletonList(BlockTags.MINEABLE_WITH_AXE));
		this.maxCharge = 30000;
		this.transferLimit = 100;
		this.tier = 1;
	}

	private boolean isShearMode(ItemStack stack)
	{
		return !StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear");
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag par4)
	{
		super.appendHoverText(stack, world, list, par4);
		list.add(Component.translatable("item.ic2.tooltip.mode.switch", KeyboardClient.modeSwitchKey.getKey().getDisplayName(), Minecraft.getInstance().options.keyUse.getKey().getDisplayName()));
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		if (world.isClientSide)
		{
			return super.use(world, player, hand);
		}

		if (IC2.keyboard.isModeSwitchKeyDown(player))
		{
			CompoundTag compoundTag = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
			if (compoundTag.getBoolean("disableShear"))
			{
				compoundTag.putBoolean("disableShear", false);
				IC2.sideProxy.messagePlayer(player, Component.translatable("item.ic2.mining_laser.tooltip.mode", Component.translatable("item.ic2.mining_laser.tooltip.mode.normal")));
			} else
			{
				compoundTag.putBoolean("disableShear", true);
				IC2.sideProxy.messagePlayer(player, Component.translatable("item.ic2.mining_laser.tooltip.mode", Component.translatable("item.ic2.mining_laser.tooltip.mode.no_shear")));
			}
		}

		return super.use(world, player, hand);
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState state)
	{
		return super.isCorrectToolForDrops(state) || state.is(Blocks.COBWEB) || Util.canShear(state);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		return !this.canUse(stack) || !state.is(BlockTags.MINEABLE_WITH_AXE) && !state.is(Blocks.COBWEB) && !Util.canShear(state) ? 1.0F : this.speed;
	}

	@Override
	public boolean onAttackEntity(Player player, Entity target)
	{
		ItemStack itemstack = player.getMainHandItem();
		if (this.consumeEnergy(itemstack, this.operationEnergyCost, player))
		{
			this.playUsingSound(player);
		}

		return true;
	}

	private void handleVanillaBlockBreakLogic(Player player, Level world, BlockPos pos, BlockState state)
	{
		world.levelEvent(player, 2001, pos, Block.getId(state));
		if (state.is(BlockTags.GUARDED_BY_PIGLINS))
		{
			PiglinAi.angerNearbyPiglins(player, false);
		}

		world.gameEvent(GameEvent.BLOCK_DESTROY, pos, Context.of(player, state));
	}

	@Override
	public InteractionResult onBlockStartBreak(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction)
	{
		BlockState state = world.getBlockState(pos);
		ItemStack stack = player.getItemInHand(hand);
		if (!this.isShearMode(stack) || !Util.canShear(state))
		{
			return InteractionResult.PASS;
		} else if (this.consumeEnergy(stack, this.operationEnergyCost, player))
		{
			this.handleVanillaBlockBreakLogic(player, world, pos, state);
			StackUtil.dropAsEntity(world, pos, new ItemStack(state.getBlock().asItem()));
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
			this.playUsingSound(player);
			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player user, @NotNull LivingEntity entity, @NotNull InteractionHand hand)
	{
		if (!StackUtil.getOrCreateNbtData(stack).getBoolean("disableShear"))
		{
			if (entity instanceof IForgeShearable shearable)
			{
				Level level = entity.level();
				BlockPos pos = entity.blockPosition();

				if (shearable.isShearable(stack, level, pos))
				{
					if (this.consumeEnergy(stack, this.operationEnergyCost, user))
					{
						List<ItemStack> drops = shearable.onSheared(user, stack, level, pos, 0);

						for (ItemStack drop : drops)
						{
							if (!drop.isEmpty())
							{
								entity.spawnAtLocation(drop);
							}
						}
						this.playUsingSound(user);
						return InteractionResult.SUCCESS;
					}
				}
			}
		}
		return InteractionResult.PASS;
	}

	public void playUsingSound(LivingEntity user)
	{
		if (user.level().isClientSide)
		{
			user.playSound(this.getToolUsingSound(), 1.0F, 1.0F);
		}
	}

	public SoundEvent getToolUsingSound()
	{
		return IC2.random.nextBoolean() ? Ic2SoundEvents.ITEM_CHAINSAW_USE1 : Ic2SoundEvents.ITEM_CHAINSAW_USE2;
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

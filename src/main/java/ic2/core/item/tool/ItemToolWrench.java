package ic2.core.item.tool;

import ic2.api.item.BlockBreakableItem;
import ic2.api.item.IBoxable;
import ic2.api.tile.IWrenchAble;
import ic2.core.IC2;
import ic2.core.init.IC2Config;
import ic2.core.item.PriorityUsableItem;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemToolWrench extends Item implements PriorityUsableItem, IBoxable, BlockBreakableItem
{
	public ItemToolWrench(Properties settings)
	{
		super(settings);
	}

	public static int onWrenchUse(Player player, UseOnContext context, boolean removeBlock)
	{
		WrenchResult result = wrenchBlock(context.getLevel(), context.getClickedPos(), context.getClickedFace(), player, removeBlock);
		if (result != WrenchResult.Nothing)
		{
			if (!context.getLevel().isClientSide)
			{
				return result == WrenchResult.Rotated ? 1 : 10;
			}

			player.playSound(Ic2SoundEvents.ITEM_WRENCH_USE, 1.0F, 1.0F);
			return -2;
		}

		return -1;
	}

	public static WrenchResult wrenchBlock(Level world, BlockPos pos, Direction side, Player player, boolean remove)
	{
		BlockState state = world.getBlockState(pos);
		if (state.isAir())
		{
			return WrenchResult.Nothing;
		}

		Block block = state.getBlock();
		if (block instanceof IWrenchAble wrenchAble)
		{
			return wrenchAbleBlock(world, pos, side, player, remove, state, wrenchAble);
		}

		return wrenchVanillaBlock(world, pos, side, player, state);
	}

	private static WrenchResult wrenchAbleBlock(Level world, BlockPos pos, Direction side, Player player, boolean remove, BlockState state, IWrenchAble wrenchAble)
	{
		Direction currentFacing = wrenchAble.getFacing(world, pos);
		Direction newFacing;

		if (IC2.keyboard.isAltKeyDown(player))
		{
			// Alt key: rotate facing around the clicked side's axis
			Axis axis = side.getAxis();
			newFacing = isAltRotationClockwise(side, player)
				? currentFacing.getClockWise(axis)
				: currentFacing.getCounterClockWise(axis);
		} else
		{
			// Normal: face the clicked side; Shift: face the opposite (back toward player)
			newFacing = player.isShiftKeyDown() ? side.getOpposite() : side;
		}

		// If the facing would change, try to rotate
		if (newFacing != currentFacing && wrenchAble.setFacing(world, pos, newFacing, player))
		{
			return WrenchResult.Rotated;
		}

		// Rotation didn't happen (same facing or rejected) — try to remove instead
		if (remove && wrenchAble.wrenchCanRemove(world, pos, player))
		{
			return removeBlockWithWrench(world, pos, state, player, wrenchAble);
		}

		return WrenchResult.Nothing;
	}

	private static WrenchResult wrenchVanillaBlock(Level world, BlockPos pos, Direction side, Player player, BlockState state)
	{
		// Alt key: rotate around the clicked side's axis
		if (IC2.keyboard.isAltKeyDown(player))
		{
			Rotation rotation = isAltRotationClockwise(side, player) ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
			BlockState newState = IC2.envProxy.rotate(state, world, pos, rotation);
			if (newState != state)
			{
				world.setBlockAndUpdate(pos, newState);
				return WrenchResult.Rotated;
			}

			return WrenchResult.Nothing;
		}

		// On horizontal faces: try to rotate the block's facing
		if (side.getAxis().isHorizontal())
		{
			Property<?> property = state.getBlock().getStateDefinition().getProperty("facing");
			if (property != null && property.getValueClass() == Direction.class)
			{
				Direction facing = (Direction) state.getValue(property);
				Direction newFacing = player.isShiftKeyDown() ? side.getOpposite() : side;
				if (facing.getAxis().isHorizontal() && facing != newFacing && property.getPossibleValues().contains(newFacing))
				{
					Rotation rotation = getHorizontalRotation(facing, newFacing);
					BlockState newState = IC2.envProxy.rotate(state, world, pos, rotation);
					if (newState != state)
					{
						world.setBlockAndUpdate(pos, newState);
						return WrenchResult.Rotated;
					}
				}
			}
		}

		return WrenchResult.Nothing;
	}

	private static Rotation getHorizontalRotation(Direction from, Direction to)
	{
		if (from.getOpposite() == to) return Rotation.CLOCKWISE_180;
		if (from.getClockWise(Axis.Y) == to) return Rotation.CLOCKWISE_90;
		return Rotation.COUNTERCLOCKWISE_90;
	}

	private static boolean isAltRotationClockwise(Direction sideHit, Player player)
	{
		return sideHit.getAxisDirection() == AxisDirection.POSITIVE != player.isShiftKeyDown();
	}

	private static String getTeName(BlockEntity te)
	{
		return te != null ? ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(te.getType()).toString() : "none";
	}

	private static WrenchResult removeBlockWithWrench(Level world, BlockPos pos, BlockState state, Player player, IWrenchAble wrenchAble)
	{
		if (world.isClientSide)
		{
			return WrenchResult.Removed;
		}

		if (player.blockActionRestricted(world, pos, ((ServerPlayer) player).gameMode.getGameModeForPlayer()))
		{
			return WrenchResult.Nothing;
		}

		Block block = state.getBlock();
		BlockEntity te = world.getBlockEntity(pos);

		if (IC2Config.protection.wrenchLogging.get())
		{
			String playerName = player.getGameProfile().getName() + "/" + player.getGameProfile().getId();
			IC2.log.info(LogCategory.PlayerActivity,
				"Player %s used a wrench to remove the block %s (te %s) at %s.",
				playerName, state, getTeName(te), Util.formatPosition(world, pos));
		}

		block.playerWillDestroy(world, pos, state, player);
		if (world.removeBlock(pos, false))
		{
			block.destroy(world, pos, state);
		}

		List<ItemStack> drops = wrenchAble.getWrenchDrops(world, pos, state, te, player, 0);
		if (drops != null && !drops.isEmpty())
		{
			for (ItemStack stack : drops)
			{
				StackUtil.dropAsEntity(world, pos, stack);
			}
		} else if (IC2Config.debug.logEmptyWrenchDrops.get())
		{
			IC2.log.warn(LogCategory.General,
				"The block %s (te %s) at %s didn't yield any wrench drops.",
				state, getTeName(te), Util.formatPosition(world, pos));
		}

		if (!player.getAbilities().instabuild)
		{
			state.spawnAfterBreak((ServerLevel) world, pos, player.getUseItem(), false);
		}

		return WrenchResult.Removed;
	}

	// === Left-click (mining) behavior ===

	/**
	 * Called when the player starts breaking a block. We let the normal mining
	 * proceed (with the speed from {@link #getDestroySpeed}) and intercept at
	 * {@link #beforeBlockBreak} to swap in wrench drops.
	 */
	@Override
	public InteractionResult onBlockStartBreak(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction)
	{
		return InteractionResult.PASS;
	}

	/**
	 * Called right before the block is about to be removed by vanilla mining.
	 * For IWrenchAble blocks: we cancel the vanilla break and do our own removal
	 * so the block drops the machine itself (not the machine casing).
	 */
	@Override
	public boolean beforeBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
		if (state.getBlock() instanceof IWrenchAble wrenchAble && wrenchAble.wrenchCanRemove(world, pos, player))
		{
			removeBlockWithWrench(world, pos, state, player, wrenchAble);
			player.getMainHandItem().hurtAndBreak(10, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
			return false;
		}

		return true;
	}

	@Override
	public void afterBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity)
	{
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		if (state.getBlock() instanceof IWrenchAble)
		{
			return 6.0F;
		}

		return super.getDestroySpeed(stack, state);
	}

	// === Item behavior ===

	public boolean canTakeDamage()
	{
		return true;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		if (!this.canTakeDamage())
		{
			return InteractionResult.FAIL;
		}

		Player player = context.getPlayer();
		if (player == null)
		{
			return InteractionResult.PASS;
		}

		int useResult = onWrenchUse(player, context, this.canTakeDamage());
		return switch (useResult)
		{
			case -2 -> InteractionResult.PASS;
			case -1 -> InteractionResult.FAIL;
			default ->
			{
				this.damage(stack, useResult, player, context.getHand());
				yield InteractionResult.SUCCESS;
			}
		};
	}

	public void damage(ItemStack is, int damage, Player player, InteractionHand hand)
	{
		is.hurtAndBreak(damage, player, p -> p.broadcastBreakEvent(hand));
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair)
	{
		return repair.is(Ic2ItemTags.BRONZE_INGOTS);
	}

	public enum WrenchResult
	{
		Rotated,
		Removed,
		Nothing
	}
}

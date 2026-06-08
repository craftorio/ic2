package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.item.PriorityUsableItem;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ItemToolWrench extends Item implements PriorityUsableItem, IBoxable
{
	private static final boolean logEmptyWrenchDrops = ConfigUtil.getBool(MainConfig.get(), "debug/logEmptyWrenchDrops");
	public static final int wrenchUseFailed = -1;
	public static final int wrenchUsePass = -2;

	public ItemToolWrench(Properties settings)
	{
		super(settings);
	}

	public boolean canTakeDamage(ItemStack stack, int amount)
	{
		return true;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		if (!this.canTakeDamage(stack, 1))
		{
			return InteractionResult.FAIL;
		}

		Player player = context.getPlayer();
		if (player == null)
		{
			return InteractionResult.PASS;
		}

		int useResult = onWrenchUse(player, stack, context, this.canTakeDamage(stack, 10));
		switch (useResult)
		{
			case -2:
				return InteractionResult.PASS;
			case -1:
				return InteractionResult.FAIL;
			default:
				this.damage(stack, useResult, player, context.getHand());
				return InteractionResult.SUCCESS;
		}
	}

	public static int onWrenchUse(Player player, ItemStack stack, UseOnContext context, boolean removeBlock)
	{
		ItemToolWrench.WrenchResult result = wrenchBlock(context.getLevel(), context.getClickedPos(), context.getClickedFace(), player, removeBlock);
		if (result != ItemToolWrench.WrenchResult.Nothing)
		{
			if (!context.getLevel().isClientSide)
			{
				return result == ItemToolWrench.WrenchResult.Rotated ? 1 : 10;
			}

			player.playSound(Ic2SoundEvents.ITEM_WRENCH_USE, 1.0F, 1.0F);
			return -2;
		} else
		{
			return -1;
		}
	}

	public static ItemToolWrench.WrenchResult wrenchBlock(Level world, BlockPos pos, Direction side, Player player, boolean remove)
	{
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (state.isAir())
		{
			return ItemToolWrench.WrenchResult.Nothing;
		}

		if (block instanceof IWrenchable wrenchable)
		{
			Direction currentFacing = wrenchable.getFacing(world, pos);
			Direction newFacing = currentFacing;
			if (IC2.keyboard.isAltKeyDown(player))
			{
				Axis axis = side.getAxis();
				if (isAltRotationClockwise(side, player))
				{
					newFacing = newFacing.getClockWise(axis);
				} else
				{
					newFacing = newFacing.getCounterClockWise(axis);
				}
			} else
			{
				newFacing = getNewFacing(side, player);
			}

			if (newFacing != currentFacing && wrenchable.setFacing(world, pos, newFacing, player))
			{
				return ItemToolWrench.WrenchResult.Rotated;
			}

			if (remove && wrenchable.wrenchCanRemove(world, pos, player))
			{
				if (world.isClientSide)
				{
					return ItemToolWrench.WrenchResult.Removed;
				}

				if (player.blockActionRestricted(world, pos, ((ServerPlayer) player).gameMode.getGameModeForPlayer()))
				{
					return ItemToolWrench.WrenchResult.Nothing;
				}

				BlockEntity te = world.getBlockEntity(pos);
				if (ConfigUtil.getBool(MainConfig.get(), "protection/wrenchLogging"))
				{
					String playerName = player.getGameProfile().getName() + "/" + player.getGameProfile().getId();
					IC2.log
						.info(
							LogCategory.PlayerActivity,
							"Player %s used a wrench to remove the block %s (te %s) at %s.",
							playerName,
							state,
							getTeName(te),
							Util.formatPosition(world, pos)
						);
				}

				block.playerWillDestroy(world, pos, state, player);
				if (world.removeBlock(pos, false))
				{
					block.destroy(world, pos, state);
				}

				List<ItemStack> drops = wrenchable.getWrenchDrops(world, pos, state, te, player, 0);
				if (drops != null && !drops.isEmpty())
				{
					for (ItemStack stack : drops)
					{
						StackUtil.dropAsEntity(world, pos, stack);
					}
				} else if (logEmptyWrenchDrops)
				{
					IC2.log
						.warn(LogCategory.General, "The block %s (te %s) at %s didn't yield any wrench drops.", state, getTeName(te), Util.formatPosition(world, pos));
				}

				if (!player.getAbilities().instabuild)
				{
					state.spawnAfterBreak((ServerLevel) world, pos, player.getUseItem(), false);
				}

				return ItemToolWrench.WrenchResult.Removed;
			}
		} else
		{
			Rotation rotation = null;
			if (IC2.keyboard.isAltKeyDown(player))
			{
				if (isAltRotationClockwise(side, player))
				{
					rotation = Rotation.CLOCKWISE_90;
				} else
				{
					rotation = Rotation.COUNTERCLOCKWISE_90;
				}
			} else if (side.getAxis().isHorizontal())
			{
				Property<?> property = state.getBlock().getStateDefinition().getProperty("facing");
				Direction facing;
				Direction newFacing;
				if (property != null
					&& property.getValueClass() == Direction.class
					&& (facing = (Direction) state.getValue(property)) != null
					&& facing.getAxis().isHorizontal()
					&& (newFacing = getNewFacing(side, player)) != facing
					&& property.getPossibleValues().contains(newFacing))
				{
					if (facing.getOpposite() == newFacing)
					{
						rotation = Rotation.CLOCKWISE_180;
					} else if (facing.getClockWise(Axis.Y) == newFacing)
					{
						rotation = Rotation.CLOCKWISE_90;
					} else
					{
						rotation = Rotation.COUNTERCLOCKWISE_90;
					}
				}
			}

			BlockState newState;
			if (rotation != null && (newState = IC2.envProxy.rotate(state, world, pos, rotation)) != state)
			{
				world.setBlockAndUpdate(pos, newState);
				return ItemToolWrench.WrenchResult.Rotated;
			}
		}

		return ItemToolWrench.WrenchResult.Nothing;
	}

	private static boolean isAltRotationClockwise(Direction sideHit, Player player)
	{
		return sideHit.getAxisDirection() == AxisDirection.POSITIVE != player.isShiftKeyDown();
	}

	private static Direction getNewFacing(Direction sideHit, Player player)
	{
		return player.isShiftKeyDown() ? sideHit.getOpposite() : sideHit;
	}

	private static String getTeName(BlockEntity te)
	{
		return te != null ? BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(te.getType()).toString() : "none";
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

	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair)
	{
		return repair != null && repair.is(Ic2ItemTags.BRONZE_INGOTS);
	}

	private enum WrenchResult
	{
		Rotated,
		Removed,
		Nothing;
	}
}

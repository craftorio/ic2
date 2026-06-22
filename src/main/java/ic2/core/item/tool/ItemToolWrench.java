package ic2.core.item.tool;

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

public class ItemToolWrench extends Item implements PriorityUsableItem, IBoxable
{
	public ItemToolWrench(Properties settings)
	{
		super(settings);
	}

	public static int onWrenchUse(Player player, UseOnContext context, boolean removeBlock)
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

		if (block instanceof IWrenchAble wrenchAble)
		{
			Direction currentFacing = wrenchAble.getFacing(world, pos);
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

			if (newFacing != currentFacing && wrenchAble.setFacing(world, pos, newFacing, player))
			{
				return ItemToolWrench.WrenchResult.Rotated;
			}

			if (remove && wrenchAble.wrenchCanRemove(world, pos, player))
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
				if (IC2Config.protection.wrenchLogging.get())
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

				List<ItemStack> drops = wrenchAble.getWrenchDrops(world, pos, state, te, player, 0);
				if (drops != null && !drops.isEmpty())
				{
					for (ItemStack stack : drops)
					{
						StackUtil.dropAsEntity(world, pos, stack);
					}
				} else if (IC2Config.debug.logEmptyWrenchDrops.get())
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
				Direction facing = (Direction) state.getValue(property);
				Direction newFacing;
				if (property.getValueClass() == Direction.class && facing.getAxis().isHorizontal() && (newFacing = getNewFacing(side, player)) != facing && property.getPossibleValues().contains(newFacing))
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
		return te != null ? ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(te.getType()).toString() : "none";
	}

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

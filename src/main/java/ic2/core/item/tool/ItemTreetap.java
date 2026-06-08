package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.core.block.misc.RubberLogBlock;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2GameEvents;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

public class ItemTreetap extends Item implements IBoxable
{
	public ItemTreetap(Properties settings)
	{
		super(settings);
	}

	public InteractionResult useOn(UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block == Ic2Blocks.RUBBER_LOG)
		{
			Player player = context.getPlayer();
			if (attemptExtract(player, world, pos, context.getClickedFace(), state, null, false))
			{
				StackUtil.damage(player, context.getHand(), StackUtil.anyStack, 1);
				return InteractionResult.SUCCESS;
			} else
			{
				return InteractionResult.FAIL;
			}
		} else
		{
			return InteractionResult.PASS;
		}
	}

	public static boolean attemptExtract(Player player, Level world, BlockPos pos, Direction side, BlockState state, List<ItemStack> stacks, boolean isElectric)
	{
		assert state.getBlock() == Ic2Blocks.RUBBER_LOG;
		RubberLogBlock.RubberWoodState rwState = (RubberLogBlock.RubberWoodState) state.getValue(RubberLogBlock.stateProperty);
		if (rwState.isPlain() || rwState.facing != side)
		{
			return false;
		}

		if (rwState.wet)
		{
			if (!world.isClientSide)
			{
				world.setBlockAndUpdate(pos, (BlockState) state.setValue(RubberLogBlock.stateProperty, rwState.getDry()));
				if (stacks != null)
				{
					stacks.add(StackUtil.copyWithSize(new ItemStack(Ic2Items.RESIN), world.random.nextInt(3) + 1));
				} else
				{
					ejectResin(world, pos, side, world.random.nextInt(3) + 1);
				}
			}

			triggerToolUseEvent(world, pos, player, state, isElectric);
			return true;
		} else
		{
			boolean ret = false;
			if (!world.isClientSide)
			{
				if (world.random.nextInt(5) == 0)
				{
					world.setBlockAndUpdate(pos, (BlockState) state.setValue(RubberLogBlock.stateProperty, RubberLogBlock.RubberWoodState.plain));
					triggerToolUseEvent(world, pos, player, state, isElectric);
					ret = true;
				}

				if (world.random.nextInt(5) == 0)
				{
					ejectResin(world, pos, side, 1);
					if (stacks != null)
					{
						stacks.add(new ItemStack(Ic2Items.RESIN));
					} else
					{
						ejectResin(world, pos, side, 1);
					}

					triggerToolUseEvent(world, pos, player, state, isElectric);
					ret = true;
				}
			}

			return ret;
		}
	}

	private static void triggerToolUseEvent(Level world, BlockPos pos, Player player, BlockState state, boolean isElectric)
	{
		player.playNotifySound(getToolUseSound(isElectric), SoundSource.PLAYERS, 1.0F, 1.0F);
		world.gameEvent(GameEvent.BLOCK_CHANGE, pos, Context.of(player, state));
		world.gameEvent(Ic2GameEvents.TOOL_USE, pos, Context.of(player, null));
	}

	private static void ejectResin(Level world, BlockPos pos, Direction side, int quantity)
	{
		double ejectBias = 0.3;
		double ejectX = pos.getX() + 0.5 + side.getStepX() * 0.3;
		double ejectY = pos.getY() + 0.5 + side.getStepY() * 0.3;
		double ejectZ = pos.getZ() + 0.5 + side.getStepZ() * 0.3;

		for (int i = 0; i < quantity; i++)
		{
			ItemEntity entityitem = new ItemEntity(world, ejectX, ejectY, ejectZ, new ItemStack(Ic2Items.RESIN));
			entityitem.setDefaultPickUpDelay();
			world.addFreshEntity(entityitem);
		}
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	public static SoundEvent getToolUseSound(boolean isElectric)
	{
		return isElectric ? Ic2SoundEvents.ITEM_TREETAP_ELECTRIC_USE : Ic2SoundEvents.ITEM_TREETAP_USE;
	}
}

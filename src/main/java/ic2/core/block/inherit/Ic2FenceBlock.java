package ic2.core.block.inherit;

import ic2.api.item.ItemWrapper;
import ic2.core.IC2;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class Ic2FenceBlock extends FenceBlock
{
	public static final Map<Direction, BooleanProperty> connectProperties = getConnectProperties();
	public final boolean canBoost;

	public Ic2FenceBlock(Properties settings, boolean canBoost)
	{
		super(settings);
		this.canBoost = canBoost;
	}

	private static TileEntityMagnetizer getMagnetizer(BlockGetter world, BlockPos pos, Direction side, BlockState state, boolean checkPower)
	{
		if (state.getBlock() != Ic2Blocks.MAGNETIZER)
		{
			return null;
		}

		if (world.getBlockEntity(pos) instanceof TileEntityMagnetizer ret)
		{
			if (side != null && !side.getOpposite().equals(ret.getFacing()))
			{
				return null;
			}

			if (!checkPower || ret.canBoost())
			{
				return ret;
			}
		}

		return null;
	}

	public static boolean hasMetalShoes(Player player)
	{
		ItemStack shoes = (ItemStack) player.getInventory().armor.get(0);
		Item item = shoes.getItem();
		return item == Items.IRON_BOOTS || item == Items.GOLDEN_BOOTS || item == Items.CHAINMAIL_BOOTS || ItemWrapper.isMetalArmor(shoes, player);
	}

	private static Map<Direction, BooleanProperty> getConnectProperties()
	{
		return CrossCollisionBlock.PROPERTY_BY_DIRECTION;
	}

	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		return this.getActualState((BlockState) this.defaultBlockState().setValue(WATERLOGGED, fluidState.is(Fluids.WATER)), ctx.getLevel(), ctx.getClickedPos());
	}

	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
	{
		if ((Boolean) state.getValue(WATERLOGGED))
		{
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}

		return this.getActualState(state, world, pos);
	}

	public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos)
	{
		boolean isPole = true;
		boolean magnetizerConnected = false;
		BlockState ret = state;

		for (Direction facing : Util.HORIZONTAL_DIRS)
		{
			BlockState neighborState = world.getBlockState(pos.relative(facing));
			if (this.isFence(neighborState))
			{
				isPole = false;
				if (magnetizerConnected)
				{
					break;
				}

				ret = (BlockState) ret.setValue((Property) connectProperties.get(facing), true);
			} else if (isPole && getMagnetizer(world, pos.relative(facing), facing, world.getBlockState(pos.relative(facing)), false) != null)
			{
				magnetizerConnected = true;
				ret = (BlockState) ret.setValue((Property) connectProperties.get(facing), true);
			} else
			{
				ret = (BlockState) ret.setValue((Property) connectProperties.get(facing), false);
			}
		}

		if (!isPole && magnetizerConnected)
		{
			ret = state;

			for (Direction facing : Util.HORIZONTAL_DIRS)
			{
				BlockState neighborState = world.getBlockState(pos.relative(facing));
				if (this.isFence(neighborState))
				{
					ret = (BlockState) ret.setValue((Property) connectProperties.get(facing), true);
				} else
				{
					ret = (BlockState) ret.setValue((Property) connectProperties.get(facing), false);
				}
			}
		}

		return ret;
	}

	private boolean isFence(BlockState state)
	{
		return state.is(BlockTags.FENCES) && !state.is(BlockTags.WOODEN_FENCES);
	}

	public void entityInside(BlockState state, Level world, BlockPos pos, Entity rawEntity)
	{
		if (rawEntity instanceof Player player)
		{
			boolean powered = this.isPowered(world, pos);
			boolean metalShoes = hasMetalShoes(player);
			boolean descending = player.isShiftKeyDown();
			Vec3 velocity = player.getDeltaMovement();
			boolean slow = velocity.y >= -0.25 || velocity.y < 1.6;
			if (slow)
			{
				player.fallDistance = 0.0F;
			}

			if (!powered)
			{
				if (descending && !slow && metalShoes)
				{
					player.setDeltaMovement(velocity.multiply(1.0, 0.9, 1.0));
				}
			} else if (descending)
			{
				if (!slow)
				{
					player.setDeltaMovement(velocity.multiply(1.0, 0.8, 1.0));
				}
			} else
			{
				player.setDeltaMovement(velocity.add(0.0, 0.075, 0.0));
				velocity = player.getDeltaMovement();
				if (velocity.y() > 0.0)
				{
					player.setDeltaMovement(velocity.multiply(1.0, 1.03, 1.0));
				}

				double maxSpeed = IC2.keyboard.isAltKeyDown(player) ? 0.1 : (metalShoes ? 1.5 : 0.5);
				velocity = player.getDeltaMovement();
				player.setDeltaMovement(velocity.x, Math.min(velocity.y(), maxSpeed), velocity.z);
			}

			if (!world.isClientSide)
			{
				for (TileEntityMagnetizer magnetizer : this.getMagnetizers(world, pos, false))
				{
					IC2.network.get(true).updateTileEntityField(magnetizer, "energy");
				}
			}
		}
	}

	private boolean isPowered(Level world, BlockPos start)
	{
		if (!this.canBoost)
		{
			return false;
		}

		List<TileEntityMagnetizer> magnetizers = this.getMagnetizers(world, start, true);
		if (magnetizers.isEmpty())
		{
			return false;
		}

		double multiplier = 1.0 / magnetizers.size();

		for (TileEntityMagnetizer magnetizer : magnetizers)
		{
			magnetizer.boost(multiplier);
		}

		return true;
	}

	private List<TileEntityMagnetizer> getMagnetizers(BlockGetter world, BlockPos start, boolean checkPower)
	{
		int maxRange = 20;
		List<TileEntityMagnetizer> ret = new ArrayList<>();
		BlockPos center = new BlockPos(start);
		new BlockPos(0, 0, 0);

		for (Direction facing : Util.HORIZONTAL_DIRS)
		{
			BlockPos nPos = center.offset(0, 0, 0).relative(facing);
			BlockState state = world.getBlockState(nPos);
			if (this.isFence(state))
			{
				return Collections.emptyList();
			}

			TileEntityMagnetizer te;
			if ((te = getMagnetizer(world, nPos, facing, state, checkPower)) != null)
			{
				ret.add(te);
			}
		}

		if (!ret.isEmpty())
		{
			return ret;
		}

		int minDir = 0;
		int maxDir = 2;

		for (int dy = 1; dy <= 20; dy++)
		{
			boolean abort = false;
			int dir = minDir;

			label79:
			while (dir < maxDir)
			{
				int offset = dir * 2 - 1;
				center = center.atY(start.getY() + offset * dy);
				BlockState centerState = world.getBlockState(center);
				if (centerState.getBlock() instanceof Ic2FenceBlock && ((Ic2FenceBlock) centerState.getBlock()).canBoost)
				{
					int oldSize = ret.size();
					Direction[] var16 = Util.HORIZONTAL_DIRS;
					int var17 = var16.length;
					int var18 = 0;

					while (true)
					{
						if (var18 < var17)
						{
							Direction facing = var16[var18];
							BlockPos nPos = center.offset(0, 0, 0).relative(facing);
							BlockState state = world.getBlockState(nPos);
							if (!this.isFence(state))
							{
								TileEntityMagnetizer te;
								if ((te = getMagnetizer(world, nPos, facing, state, checkPower)) != null)
								{
									abort = true;
									ret.add(te);
								}

								var18++;
								continue;
							}

							if (dir == 0)
							{
								minDir = 1;
							} else
							{
								maxDir = 1;
							}

							if (minDir == maxDir)
							{
								abort = true;
							}

							while (ret.size() > oldSize)
							{
								ret.remove(ret.size() - 1);
							}
						}

						dir++;
						continue label79;
					}
				}

				if (dir == 0)
				{
					minDir = 1;
				} else
				{
					maxDir = 1;
				}

				if (minDir == maxDir)
				{
					abort = true;
				}
				break;
			}

			if (abort)
			{
				break;
			}
		}

		return ret;
	}
}

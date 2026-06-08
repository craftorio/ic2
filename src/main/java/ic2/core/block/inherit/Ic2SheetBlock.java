package ic2.core.block.inherit;

import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class Ic2SheetBlock extends Block
{
	private static final VoxelShape aabb = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
	private static final Direction[] positiveHorizontalFacings = new Direction[] { Direction.EAST, Direction.SOUTH };

	public Ic2SheetBlock(Properties settings)
	{
		super(settings);
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		Level world = ctx.getLevel();
		if (world == null)
		{
			return null;
		}

		BlockPos pos = ctx.getClickedPos();
		BlockState state = this.defaultBlockState();
		return this.isValidPosition(world, pos, state) ? state : null;
	}

	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return aabb;
	}

	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		if (state.getBlock() == Ic2Blocks.RESIN_SHEET)
		{
			return Shapes.empty();
		} else
		{
			return !(state.getBlock() == Ic2Blocks.WOOL_SHEET && context instanceof EntityCollisionContext entityContext)
				|| !(entityContext.getEntity() instanceof Player)
				|| !entityContext.getEntity().isShiftKeyDown()
				&& !(entityContext.getEntity().getY() < pos.getY() + 0.125 - entityContext.getEntity().maxUpStep)
				? aabb
				: Shapes.empty();
		}
	}

	private boolean isValidPosition(Level world, BlockPos pos, BlockState state)
	{
		if (state.getBlock() == Ic2Blocks.RESIN_SHEET)
		{
			return this.isNormalCubeBelow(world, pos);
		}

		if (state.getBlock() != Ic2Blocks.RUBBER_SHEET)
		{
			return state.getBlock() == Ic2Blocks.WOOL_SHEET;
		}

		for (Direction facing : Util.HORIZONTAL_DIRS)
		{
			state = world.getBlockState(pos.relative(facing));
			if (state == Ic2Blocks.RUBBER_SHEET.defaultBlockState() || state.getBlock().isCollisionShapeFullBlock(state, world, pos))
			{
				return true;
			}
		}

		return this.isNormalCubeBelow(world, pos);
	}

	private boolean isNormalCubeBelow(Level world, BlockPos pos)
	{
		pos = pos.below();
		BlockState state = world.getBlockState(pos);
		return state.getBlock().isCollisionShapeFullBlock(state, world, pos);
	}

	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
	{
		if (!this.isValidPosition(world, pos, state))
		{
			Block.dropResources(state, world, pos, null);
			world.removeBlock(pos, false);
		}
	}

	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
	{
		if (state.getBlock() == Ic2Blocks.RESIN_SHEET)
		{
			entity.fallDistance = (float) (entity.fallDistance * 0.75);
			entity.setDeltaMovement(entity.getDeltaMovement().x() * 0.6, entity.getDeltaMovement().y() * 0.85, entity.getDeltaMovement().z() * 0.6);
		} else if (state.getBlock() == Ic2Blocks.RUBBER_SHEET)
		{
			if (!world.isEmptyBlock(pos.below()))
			{
				return;
			}

			if (entity instanceof LivingEntity && !canSupportWeight(world, pos))
			{
				world.levelEvent(2001, pos, Block.getId(state));
				world.removeBlock(pos, false);
				return;
			}

			if (entity.getDeltaMovement().y() <= -0.4)
			{
				entity.fallDistance = 0.0F;
				entity.setDeltaMovement(entity.getDeltaMovement().x() * 1.1, entity.getDeltaMovement().y(), entity.getDeltaMovement().z() * 1.1);
				if (entity instanceof LivingEntity)
				{
					if (entity instanceof Player && IC2.keyboard.isJumpKeyDown((Player) entity))
					{
						entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -1.3, entity.getDeltaMovement().z());
					} else if (entity instanceof Player && entity.isShiftKeyDown())
					{
						entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -0.1, entity.getDeltaMovement().z());
					} else
					{
						entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -0.8, entity.getDeltaMovement().z());
					}
				} else
				{
					entity.setDeltaMovement(entity.getDeltaMovement().x(), entity.getDeltaMovement().y() * -0.8, entity.getDeltaMovement().z());
				}
			} else if (state.getBlock() == Ic2Blocks.WOOL_SHEET)
			{
				entity.fallDistance = (float) (entity.fallDistance * 0.95);
			}
		}
	}

	private static boolean canSupportWeight(Level world, BlockPos pos)
	{
		int maxRange = 16;
		MutableBlockPos cPos = new MutableBlockPos();

		for (Direction axis : positiveHorizontalFacings)
		{
			for (int dir = -1; dir <= 1; dir += 2)
			{
				cPos.set(pos);
				boolean supported = false;

				for (int i = 0; i < 16; i++)
				{
					cPos.move(axis, dir);
					BlockState state = world.getBlockState(cPos);
					if (state.getBlock().isCollisionShapeFullBlock(state, world, cPos))
					{
						supported = true;
						break;
					}

					if (state != Ic2Blocks.RUBBER_SHEET.defaultBlockState())
					{
						break;
					}

					cPos.move(Direction.DOWN);
					BlockState baseState = world.getBlockState(cPos);
					if (baseState.getBlock().isCollisionShapeFullBlock(baseState, world, cPos))
					{
						supported = true;
						break;
					}

					cPos.move(Direction.UP);
				}

				if (!supported)
				{
					break;
				}

				if (dir == 1)
				{
					return true;
				}
			}
		}

		return false;
	}
}

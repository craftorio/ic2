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
	private static final VoxelShape aabb = Shapes.m_83048_(0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
	private static final Direction[] positiveHorizontalFacings = new Direction[] { Direction.EAST, Direction.SOUTH };

	public Ic2SheetBlock(Properties settings)
	{
		super(settings);
	}

	@Nullable
	public BlockState m_5573_(BlockPlaceContext ctx)
	{
		Level world = ctx.m_43725_();
		if (world == null)
		{
			return null;
		}

		BlockPos pos = ctx.m_8083_();
		BlockState state = this.defaultBlockState();
		return this.isValidPosition(world, pos, state) ? state : null;
	}

	public VoxelShape m_5940_(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return aabb;
	}

	public VoxelShape m_5939_(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		if (state.getBlock() == Ic2Blocks.RESIN_SHEET)
		{
			return Shapes.m_83040_();
		} else
		{
			return !(state.getBlock() == Ic2Blocks.WOOL_SHEET && context instanceof EntityCollisionContext entityContext)
				|| !(entityContext.m_193113_() instanceof Player)
				|| !entityContext.m_193113_().m_6144_()
				&& !(entityContext.m_193113_().getY() < pos.getY() + 0.125 - entityContext.m_193113_().f_19793_)
				? aabb
				: Shapes.m_83040_();
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
			if (state == Ic2Blocks.RUBBER_SHEET.defaultBlockState() || state.getBlock().m_180643_(state, world, pos))
			{
				return true;
			}
		}

		return this.isNormalCubeBelow(world, pos);
	}

	private boolean isNormalCubeBelow(Level world, BlockPos pos)
	{
		pos = pos.m_7495_();
		BlockState state = world.getBlockState(pos);
		return state.getBlock().m_180643_(state, world, pos);
	}

	public void m_6861_(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
	{
		if (!this.isValidPosition(world, pos, state))
		{
			Block.m_49892_(state, world, pos, null);
			world.removeBlock(pos, false);
		}
	}

	public void m_7892_(BlockState state, Level world, BlockPos pos, Entity entity)
	{
		if (state.getBlock() == Ic2Blocks.RESIN_SHEET)
		{
			entity.f_19789_ = (float) (entity.f_19789_ * 0.75);
			entity.m_20334_(entity.m_20184_().m_7096_() * 0.6, entity.m_20184_().m_7098_() * 0.85, entity.m_20184_().m_7094_() * 0.6);
		} else if (state.getBlock() == Ic2Blocks.RUBBER_SHEET)
		{
			if (!world.m_46859_(pos.m_7495_()))
			{
				return;
			}

			if (entity instanceof LivingEntity && !canSupportWeight(world, pos))
			{
				world.m_46796_(2001, pos, Block.m_49956_(state));
				world.removeBlock(pos, false);
				return;
			}

			if (entity.m_20184_().m_7098_() <= -0.4)
			{
				entity.f_19789_ = 0.0F;
				entity.m_20334_(entity.m_20184_().m_7096_() * 1.1, entity.m_20184_().m_7098_(), entity.m_20184_().m_7094_() * 1.1);
				if (entity instanceof LivingEntity)
				{
					if (entity instanceof Player && IC2.keyboard.isJumpKeyDown((Player) entity))
					{
						entity.m_20334_(entity.m_20184_().m_7096_(), entity.m_20184_().m_7098_() * -1.3, entity.m_20184_().m_7094_());
					} else if (entity instanceof Player && entity.m_6144_())
					{
						entity.m_20334_(entity.m_20184_().m_7096_(), entity.m_20184_().m_7098_() * -0.1, entity.m_20184_().m_7094_());
					} else
					{
						entity.m_20334_(entity.m_20184_().m_7096_(), entity.m_20184_().m_7098_() * -0.8, entity.m_20184_().m_7094_());
					}
				} else
				{
					entity.m_20334_(entity.m_20184_().m_7096_(), entity.m_20184_().m_7098_() * -0.8, entity.m_20184_().m_7094_());
				}
			} else if (state.getBlock() == Ic2Blocks.WOOL_SHEET)
			{
				entity.f_19789_ = (float) (entity.f_19789_ * 0.95);
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
				cPos.m_122190_(pos);
				boolean supported = false;

				for (int i = 0; i < 16; i++)
				{
					cPos.m_122175_(axis, dir);
					BlockState state = world.getBlockState(cPos);
					if (state.getBlock().m_180643_(state, world, cPos))
					{
						supported = true;
						break;
					}

					if (state != Ic2Blocks.RUBBER_SHEET.defaultBlockState())
					{
						break;
					}

					cPos.m_122173_(Direction.DOWN);
					BlockState baseState = world.getBlockState(cPos);
					if (baseState.getBlock().m_180643_(baseState, world, cPos))
					{
						supported = true;
						break;
					}

					cPos.m_122173_(Direction.UP);
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

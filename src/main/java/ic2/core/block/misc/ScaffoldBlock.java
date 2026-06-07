package ic2.core.block.misc;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2EntityTags;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaffoldBlock extends Block
{
	private static final IRecipeInput stickInput = Recipes.inputFactory.forItem(Items.f_42398_);
	private static final Direction[] supportedFacings = new Direction[] { Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };
	private static final double border = 0.03125;
	private static final VoxelShape aabb = Shapes.m_83048_(0.03125, 0.0, 0.03125, 0.96875, 1.0, 0.96875);
	private final int maxDistance;

	public ScaffoldBlock(Properties settings, int maxDistance)
	{
		super(settings);
		this.maxDistance = maxDistance;
	}

	public void m_7892_(BlockState state, Level world, BlockPos pos, Entity rawEntity)
	{
		if (rawEntity instanceof LivingEntity entity)
		{
			entity.f_19789_ = 0.0F;
			double limit = 0.15;
			Vec3 velocity = entity.m_20184_();
			double velocityX = Util.limit(velocity.m_7096_(), -limit, limit);
			double velocityZ = Util.limit(velocity.m_7094_(), -limit, limit);
			entity.m_20334_(velocityX, velocity.m_7098_(), velocityZ);
			if (entity.m_6144_() && entity instanceof Player)
			{
				if (entity.m_20069_())
				{
					entity.m_20334_(velocityX, 0.02, velocityZ);
				} else
				{
					entity.m_20334_(velocityX, 0.08, velocityZ);
				}
			} else if (entity.f_19862_ && entity.m_6095_().m_204039_(Ic2EntityTags.SCAFFOLD_CLIMBABLE))
			{
				entity.m_20334_(velocityX, 0.2, velocityZ);
			} else
			{
				entity.m_20334_(velocityX, -0.07, velocityZ);
			}
		}
	}

	public VoxelShape m_5939_(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return aabb;
	}

	public VoxelShape m_5940_(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return Shapes.block();
	}

	public VoxelShape m_7947_(BlockState state, BlockGetter world, BlockPos pos)
	{
		return Shapes.block();
	}

	public InteractionResult m_6227_(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if (player.m_6144_())
		{
			return InteractionResult.PASS;
		}

		ItemStack stack = player.m_21120_(hand);
		if (StackUtil.isEmpty(stack))
		{
			return InteractionResult.PASS;
		}

		int stickCount = 2;
		int fenceCount = 1;
		Block block = state.getBlock();
		Block newBlock;
		if (block == Ic2Blocks.WOODEN_SCAFFOLD)
		{
			if (!stickInput.matches(stack) || StackUtil.getSize(stack) < 2)
			{
				return InteractionResult.PASS;
			}

			newBlock = Ic2Blocks.REINFORCED_WOODEN_SCAFFOLD;
		} else
		{
			if (block != Ic2Blocks.IRON_SCAFFOLD)
			{
				return InteractionResult.PASS;
			}

			if (!StackUtil.checkItemEquality(stack, new ItemStack(Ic2Blocks.IRON_FENCE)) || StackUtil.getSize(stack) < 1)
			{
				return InteractionResult.PASS;
			}

			newBlock = Ic2Blocks.REINFORCED_IRON_SCAFFOLD;
		}

		if (!this.isPillar(world, pos))
		{
			return InteractionResult.PASS;
		}

		if (block == Ic2Blocks.WOODEN_SCAFFOLD)
		{
			StackUtil.consumeOrError(player, hand, StackUtil.recipeInput(stickInput), 2);
		} else
		{
			StackUtil.consumeOrError(player, hand, StackUtil.sameStack(new ItemStack(Ic2Blocks.IRON_FENCE)), 1);
		}

		world.setBlockAndUpdate(pos, newBlock.defaultBlockState());
		return InteractionResult.SUCCESS;
	}

	public void m_6256_(BlockState state, Level world, BlockPos pos, Player player)
	{
		InteractionHand hand = InteractionHand.MAIN_HAND;
		ItemStack stack = player.m_21120_(hand);
		if (!StackUtil.isEmpty(stack))
		{
			if (StackUtil.checkItemEquality(stack, Item.m_41439_(this)))
			{
				while (world.getBlockState(pos).getBlock() == this)
				{
					pos = pos.m_7494_();
				}

				if (this.m_7898_(this.defaultBlockState(), world, pos) && pos.getY() < IC2.getWorldMaxHeight(world))
				{
					boolean isCreative = player.m_150110_().f_35937_;
					ItemStack prev = isCreative ? StackUtil.copy(stack) : null;
					stack.m_41661_(
						new BlockPlaceContext(
							player,
							hand,
							stack,
							new BlockHitResult(
								new Vec3(0.5, 1.0, 0.5).m_82520_(pos.m_7495_().getX(), pos.m_7495_().getY(), pos.m_7495_().getZ()),
								Direction.UP,
								pos.m_7495_(),
								true
							)
						)
					);
					if (!isCreative)
					{
						StackUtil.clearEmpty(player, hand);
					} else
					{
						StackUtil.set(player, hand, prev);
					}
				}
			}
		}
	}

	public boolean m_7898_(BlockState state, LevelReader world, BlockPos pos)
	{
		return super.m_7898_(state, world, pos) && this.hasSupport(world, pos, this);
	}

	public void m_6861_(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
	{
		this.checkSupport(world, pos);
	}

	public void m_213898_(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
	{
		if (random.nextInt(8) == 0)
		{
			this.checkSupport(world, pos);
		}
	}

	private boolean isPillar(Level world, BlockPos pos)
	{
		while (world.getBlockState(pos).getBlock() == this)
		{
			pos = pos.m_7495_();
		}

		return world.getBlockState(pos).m_60659_(world, pos, Direction.UP, SupportType.FULL);
	}

	private boolean hasSupport(BlockGetter world, BlockPos start, ScaffoldBlock block)
	{
		return this.calculateSupport(world, start, block).get(start).strength >= 0;
	}

	private void checkSupport(Level world, BlockPos start)
	{
		Block block = world.getBlockState(start).getBlock();
		if (block instanceof ScaffoldBlock)
		{
			Map<BlockPos, ScaffoldBlock.Support> results = this.calculateSupport(world, start, (ScaffoldBlock) block);
			boolean droppedAny = false;

			for (ScaffoldBlock.Support support : results.values())
			{
				if (support.strength < 0)
				{
					world.m_7731_(support.pos, Blocks.f_50016_.defaultBlockState(), 2);
					Block.m_49950_(support.block.defaultBlockState(), world, support.pos);
					droppedAny = true;
				}
			}

			if (droppedAny)
			{
				for (ScaffoldBlock.Support support : results.values())
				{
					if (support.strength < 0)
					{
						world.m_6289_(support.pos, this);
					}
				}
			}
		}
	}

	private Map<BlockPos, ScaffoldBlock.Support> calculateSupport(BlockGetter world, BlockPos start, ScaffoldBlock startBlock)
	{
		Map<BlockPos, ScaffoldBlock.Support> results = new HashMap<>();
		Queue<ScaffoldBlock.Support> queue = new ArrayDeque<>();
		Set<BlockPos> groundSupports = new HashSet<>();
		ScaffoldBlock.Support support = new ScaffoldBlock.Support(start, startBlock, -1);
		results.put(start, support);
		queue.add(support);

		while ((support = queue.poll()) != null)
		{
			for (Direction dir : Util.ALL_DIRS)
			{
				BlockPos pos = support.pos.relative(dir);
				if (!results.containsKey(pos))
				{
					BlockState state = world.getBlockState(pos);
					Block block = state.getBlock();
					if (block instanceof ScaffoldBlock)
					{
						ScaffoldBlock.Support cSupport = new ScaffoldBlock.Support(pos, (ScaffoldBlock) block, -1);
						results.put(pos, cSupport);
						queue.add(cSupport);
					} else if (block.m_180643_(state, world, pos))
					{
						groundSupports.add(pos);
					}
				}
			}
		}

		for (BlockPos groundPos : groundSupports)
		{
			BlockPos pos = groundPos.m_7494_();
			int propagatedStrength = 0;

			while (true)
			{
				support = results.get(pos);
				if (support == null)
				{
					break;
				}

				int strength;
				if (support.block.maxDistance >= propagatedStrength)
				{
					strength = support.block.maxDistance;
					propagatedStrength = strength - 1;
				} else
				{
					strength = propagatedStrength--;
				}

				if (support.strength < strength)
				{
					support.strength = strength;

					for (Direction dir : Util.HORIZONTAL_DIRS)
					{
						BlockPos nPos = pos.relative(dir);
						ScaffoldBlock.Support nSupport = results.get(nPos);
						if (nSupport != null && nSupport.strength < strength)
						{
							nSupport.strength = strength - 1;
							queue.add(nSupport);
						}
					}
				}

				pos = pos.m_7494_();
			}
		}

		while ((support = queue.poll()) != null)
		{
			for (Direction dir : supportedFacings)
			{
				BlockPos pos = support.pos.relative(dir);
				ScaffoldBlock.Support nSupport = results.get(pos);
				if (nSupport != null && nSupport.strength < support.strength)
				{
					nSupport.strength = support.strength - 1;
					if (nSupport.strength > 0)
					{
						queue.add(nSupport);
					}
				}
			}
		}

		return results;
	}

	private static class Support
	{
		final BlockPos pos;
		final ScaffoldBlock block;
		int strength;

		Support(BlockPos pos, ScaffoldBlock block, int strength)
		{
			this.pos = pos;
			this.block = block;
			this.strength = strength;
		}
	}
}

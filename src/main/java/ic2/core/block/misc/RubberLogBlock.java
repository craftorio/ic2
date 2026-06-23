package ic2.core.block.misc;

import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import ic2.core.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class RubberLogBlock extends RotatedPillarBlock
{
	public static final EnumProperty<RubberLogBlock.RubberWoodState> stateProperty = EnumProperty.create("state", RubberLogBlock.RubberWoodState.class);

	public RubberLogBlock(Properties settings)
	{
		super(settings);
		this.registerDefaultState(this.defaultBlockState().setValue(stateProperty, RubberWoodState.plain));
	}

	protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(stateProperty);
	}

	public @NotNull BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		return this.defaultBlockState().setValue(AXIS, ctx.getClickedFace().getAxis()).setValue(stateProperty, RubberWoodState.plain);
	}

	public void randomTick(@NotNull BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, RandomSource random)
	{
		if (random.nextInt(7) == 0)
		{
			RubberLogBlock.RubberWoodState rwState = state.getValue(stateProperty);
			if (!rwState.canRegenerate())
			{
				return;
			}

			world.setBlockAndUpdate(pos, state.setValue(stateProperty, rwState.getWet()));
		}
	}

	public PushReaction getPistonPushReaction(BlockState state)
	{
		Axis axis = state.getValue(AXIS);
		return axis != Axis.X && axis != Axis.Y && axis != Axis.Z ? PushReaction.BLOCK : PushReaction.NORMAL;
	}

	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit)
	{
     RandomSource rng = RandomSource.create();
		ItemStack mainHandItem = player.getMainHandItem();
		if (!(mainHandItem.getItem() instanceof AxeItem))
		{
			return super.use(state, world, pos, player, hand, hit);
		}

		WorldUtil.strip(state, world, pos, player, mainHandItem, Ic2Blocks.STRIPPED_RUBBER_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
		RubberLogBlock.RubberWoodState resinState = state.getValue(stateProperty);
		if (resinState == RubberLogBlock.RubberWoodState.wet_north || resinState == RubberLogBlock.RubberWoodState.wet_south || resinState == RubberLogBlock.RubberWoodState.wet_west || resinState == RubberLogBlock.RubberWoodState.wet_east)
		{
			this.dropResin(world, pos, rng.nextInt(2) + 1);
		}

		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	private void dropResin(Level world, BlockPos pos, int amount)
	{
		for (int i = 0; i < amount; i++)
		{
			ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Ic2Items.RESIN));
			entity.setDefaultPickUpDelay();
			world.addFreshEntity(entity);
		}
	}

	public enum RubberWoodState implements StringRepresentable
	{
		plain(null, false),
		dry_north(Direction.NORTH, false),
		dry_south(Direction.SOUTH, false),
		dry_west(Direction.WEST, false),
		dry_east(Direction.EAST, false),
		wet_north(Direction.NORTH, true),
		wet_south(Direction.SOUTH, true),
		wet_west(Direction.WEST, true),
		wet_east(Direction.EAST, true);

		private static final RubberLogBlock.RubberWoodState[] values = values();
		public final Direction facing;
		public final boolean wet;

		RubberWoodState(Direction facing, boolean wet)
		{
			this.facing = facing;
			this.wet = wet;
		}

		public @NotNull String getSerializedName()
		{
			return this.name();
		}

		public boolean isPlain()
		{
			return this.facing == null;
		}

		public boolean canRegenerate()
		{
			return !this.isPlain() && !this.wet;
		}

		public RubberLogBlock.RubberWoodState getWet()
		{
			if (this.isPlain())
			{
				return null;
			} else
			{
				return this.wet ? this : values[this.ordinal() + 4];
			}
		}

		public RubberLogBlock.RubberWoodState getDry()
		{
			return !this.isPlain() && this.wet ? values[this.ordinal() - 4] : this;
		}
	}
}

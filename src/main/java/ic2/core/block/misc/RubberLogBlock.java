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
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public class RubberLogBlock extends RotatedPillarBlock
{
	public static final EnumProperty<RubberLogBlock.RubberWoodState> stateProperty = EnumProperty.m_61587_("state", RubberLogBlock.RubberWoodState.class);

	public RubberLogBlock(Properties settings)
	{
		super(settings);
		this.m_49959_((BlockState) this.defaultBlockState().setValue(stateProperty, RubberLogBlock.RubberWoodState.plain));
	}

	protected void m_7926_(Builder<Block, BlockState> builder)
	{
		super.m_7926_(builder);
		builder.m_61104_(new Property[] { stateProperty });
	}

	public BlockState m_5573_(BlockPlaceContext ctx)
	{
		return (BlockState) ((BlockState) this.defaultBlockState().setValue(f_55923_, ctx.m_43719_().m_122434_()))
			.setValue(stateProperty, RubberLogBlock.RubberWoodState.plain);
	}

	public void m_213898_(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
	{
		if (random.nextInt(7) == 0)
		{
			RubberLogBlock.RubberWoodState rwState = (RubberLogBlock.RubberWoodState) state.getValue(stateProperty);
			if (!rwState.canRegenerate())
			{
				return;
			}

			world.setBlockAndUpdate(pos, (BlockState) state.setValue(stateProperty, rwState.getWet()));
		}
	}

	public PushReaction m_5537_(BlockState state)
	{
		Axis axis = (Axis) state.getValue(f_55923_);
		return axis != Axis.X && axis != Axis.Y && axis != Axis.Z ? PushReaction.BLOCK : PushReaction.NORMAL;
	}

	public InteractionResult m_6227_(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		ItemStack mainHandItem = player.m_21205_();
		if (!(mainHandItem.getItem() instanceof AxeItem))
		{
			return super.m_6227_(state, world, pos, player, hand, hit);
		}

		WorldUtil.strip(
			state,
			world,
			pos,
			player,
			mainHandItem,
			(BlockState) Ic2Blocks.STRIPPED_RUBBER_LOG.defaultBlockState().setValue(RotatedPillarBlock.f_55923_, (Axis) state.getValue(RotatedPillarBlock.f_55923_))
		);
		RubberLogBlock.RubberWoodState resinState = (RubberLogBlock.RubberWoodState) state.getValue(stateProperty);
		if (resinState == RubberLogBlock.RubberWoodState.wet_north
			|| resinState == RubberLogBlock.RubberWoodState.wet_south
			|| resinState == RubberLogBlock.RubberWoodState.wet_west
			|| resinState == RubberLogBlock.RubberWoodState.wet_east)
		{
			this.dropResin(world, pos, world.random.nextInt(2) + 1);
		}

		return InteractionResult.m_19078_(world.isClientSide);
	}

	private void dropResin(Level world, BlockPos pos, int amount)
	{
		for (int i = 0; i < amount; i++)
		{
			ItemEntity entityitem = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Ic2Items.RESIN));
			entityitem.m_32060_();
			world.addFreshEntity(entityitem);
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

		public final Direction facing;
		public final boolean wet;
		private static final RubberLogBlock.RubberWoodState[] values = values();

		RubberWoodState(Direction facing, boolean wet)
		{
			this.facing = facing;
			this.wet = wet;
		}

		public String m_7912_()
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

		public static RubberLogBlock.RubberWoodState getWet(Direction facing)
		{
			switch (facing)
			{
				case NORTH:
					return wet_north;
				case SOUTH:
					return wet_south;
				case WEST:
					return wet_west;
				case EAST:
					return wet_east;
				default:
					throw new IllegalArgumentException("incompatible facing: " + facing);
			}
		}
	}
}

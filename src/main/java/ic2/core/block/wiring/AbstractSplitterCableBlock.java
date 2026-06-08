package ic2.core.block.wiring;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class AbstractSplitterCableBlock extends AbstractCableBlock
{
	public static final BooleanProperty active = BooleanProperty.create("active");

	protected AbstractSplitterCableBlock(Properties settings, CableType type, int insulation)
	{
		super(settings, type, insulation);
		this.registerDefaultState((BlockState) this.defaultBlockState().setValue(active, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(new Property[] { active });
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		return (BlockState) super.getStateForPlacement(ctx).setValue(active, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
	}

	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
	{
		if (!world.isClientSide)
		{
			boolean newActive = world.hasNeighborSignal(pos);
			if ((Boolean) state.getValue(active) != newActive)
			{
				state = (BlockState) state.setValue(active, newActive);
				world.setBlockAndUpdate(pos, state);
				if (newActive)
				{
					this.addToEnet(state, world, pos, true);
				} else
				{
					this.removeFromEnet(state, world, pos);
				}
			}
		}
	}

	@Override
	protected void addToEnet(BlockState state, Level world, BlockPos pos, boolean checkConflicting)
	{
		if ((Boolean) state.getValue(active))
		{
			super.addToEnet(state, world, pos, checkConflicting);
		}
	}
}

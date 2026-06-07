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
	public static final BooleanProperty active = BooleanProperty.m_61465_("active");

	protected AbstractSplitterCableBlock(Properties settings, CableType type, int insulation)
	{
		super(settings, type, insulation);
		this.m_49959_((BlockState) this.defaultBlockState().setValue(active, false));
	}

	@Override
	protected void m_7926_(Builder<Block, BlockState> builder)
	{
		super.m_7926_(builder);
		builder.m_61104_(new Property[] { active });
	}

	@Override
	public BlockState m_5573_(BlockPlaceContext ctx)
	{
		return (BlockState) super.m_5573_(ctx).setValue(active, ctx.m_43725_().m_46753_(ctx.m_8083_()));
	}

	public void m_6861_(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
	{
		if (!world.isClientSide)
		{
			boolean newActive = world.m_46753_(pos);
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

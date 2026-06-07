package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class AbstractDetectorCableBlock extends AbstractCableBlock
{
	public static final BooleanProperty active = BooleanProperty.m_61465_("active");
	private static final int tickRate = 32;

	protected AbstractDetectorCableBlock(Properties settings)
	{
		super(settings, CableType.detector, 0);
		this.m_49959_((BlockState) this.defaultBlockState().setValue(active, false));
	}

	@Override
	protected void m_7926_(Builder<Block, BlockState> builder)
	{
		super.m_7926_(builder);
		builder.m_61104_(new Property[] { active });
	}

	@Override
	public void m_6807_(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
	{
		super.m_6807_(state, world, pos, oldState, notify);
		world.m_186460_(pos, this, world.m_213780_().nextInt(32));
	}

	public void m_213897_(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
	{
		world.m_186460_(pos, this, 32);
		IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
		if (tile != null)
		{
			NodeStats stats = EnergyNet.instance.getNodeStats(tile);
			if (stats != null)
			{
				boolean newActive = stats.getEnergyIn() > 0.0;
				if (newActive != (Boolean) state.getValue(active))
				{
					world.setBlockAndUpdate(pos, (BlockState) state.setValue(active, newActive));
				} else if (newActive)
				{
					world.m_46717_(pos, this);
				}
			}
		}
	}

	public boolean m_7899_(BlockState state)
	{
		return true;
	}

	public int m_6378_(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
	{
		return state.getValue(active) ? 15 : 0;
	}

	public boolean m_7278_(BlockState state)
	{
		return true;
	}

	public int m_6782_(BlockState state, Level world, BlockPos pos)
	{
		if (!(Boolean) state.getValue(active))
		{
			return 0;
		}

		IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
		return tile == null ? 0 : (int) Util.map(EnergyNet.instance.getNodeStats(tile).getEnergyIn() / this.type.capacity, 1.0, 15.0);
	}
}

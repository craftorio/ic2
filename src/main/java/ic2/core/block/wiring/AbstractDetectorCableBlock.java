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
	public static final BooleanProperty active = BooleanProperty.create("active");
	private static final int tickRate = 32;

	protected AbstractDetectorCableBlock(Properties settings)
	{
		super(settings, CableType.detector, 0);
		this.registerDefaultState((BlockState) this.defaultBlockState().setValue(active, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(new Property[] { active });
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
	{
		super.onPlace(state, world, pos, oldState, notify);
		world.scheduleTick(pos, this, world.getRandom().nextInt(32));
	}

	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
	{
		world.scheduleTick(pos, this, 32);
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
					world.updateNeighbourForOutputSignal(pos, this);
				}
			}
		}
	}

	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction)
	{
		return state.getValue(active) ? 15 : 0;
	}

	public boolean hasAnalogOutputSignal(BlockState state)
	{
		return true;
	}

	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
	{
		if (!(Boolean) state.getValue(active))
		{
			return 0;
		}

		IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
		return tile == null ? 0 : (int) Util.map(EnergyNet.instance.getNodeStats(tile).getEnergyIn() / this.type.capacity, 1.0, 15.0);
	}
}

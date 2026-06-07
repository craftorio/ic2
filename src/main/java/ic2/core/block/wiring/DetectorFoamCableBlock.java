package ic2.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class DetectorFoamCableBlock extends AbstractDetectorCableBlock
{
	public static DetectorFoamCableBlock create(Properties settings)
	{
		prepareCreate(CableType.detector, 0);
		return new DetectorFoamCableBlock(settings);
	}

	protected DetectorFoamCableBlock(Properties settings)
	{
		super(settings);
	}

	@Override
	public boolean isFoam()
	{
		return true;
	}

	@Override
	public boolean isHardFoam(BlockState state)
	{
		return ((CableFoam) state.getValue(foamProperty)).isHard();
	}
}

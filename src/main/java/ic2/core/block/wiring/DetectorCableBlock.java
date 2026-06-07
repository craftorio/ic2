package ic2.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class DetectorCableBlock extends AbstractDetectorCableBlock
{
	private final DetectorFoamCableBlock foamCableBlock;

	public static DetectorCableBlock create(Properties settings, DetectorFoamCableBlock foamCableBlock)
	{
		prepareCreate(CableType.detector, 0);
		return new DetectorCableBlock(settings, foamCableBlock);
	}

	protected DetectorCableBlock(Properties settings, DetectorFoamCableBlock foamCableBlock)
	{
		super(settings);
		this.foamCableBlock = foamCableBlock;
	}

	@Override
	public boolean isFoam()
	{
		return false;
	}

	@Override
	public boolean isHardFoam(BlockState state)
	{
		return false;
	}

	public DetectorFoamCableBlock getFoamCableBlock()
	{
		return this.foamCableBlock;
	}
}

package ic2.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class CableBlock extends AbstractCableBlock
{
	private final FoamCableBlock foamCableBlock;

	protected CableBlock(Properties settings, CableType type, int insulation, FoamCableBlock foamCableBlock)
	{
		super(settings, type, insulation);
		this.foamCableBlock = foamCableBlock;
	}

	public static CableBlock create(Properties settings, CableType type, int insulation, FoamCableBlock foamCableBlock)
	{
		prepareCreate(type, insulation);
		return new CableBlock(settings, type, insulation, foamCableBlock);
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

	public FoamCableBlock getFoamCableBlock()
	{
		return this.foamCableBlock;
	}
}

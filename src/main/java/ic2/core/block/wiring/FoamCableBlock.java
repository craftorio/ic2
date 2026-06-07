package ic2.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class FoamCableBlock extends AbstractCableBlock
{
	public static final CableFoam DEFAULT_FOAM = CableFoam.SOFT;

	public static FoamCableBlock create(Properties settings, CableType type, int insulation)
	{
		prepareCreate(type, insulation);
		return new FoamCableBlock(settings, type, insulation);
	}

	protected FoamCableBlock(Properties settings, CableType type, int insulation)
	{
		super(settings, type, insulation);
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

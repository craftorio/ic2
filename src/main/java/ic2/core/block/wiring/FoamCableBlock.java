package ic2.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;

public class FoamCableBlock extends AbstractCableBlock
{
	public static final CableFoam DEFAULT_FOAM = CableFoam.SOFT;

	protected FoamCableBlock(Properties settings, CableType type, int insulation)
	{
		super(settings, type, insulation);
	}

	public static FoamCableBlock create(Properties settings, CableType type, int insulation)
	{
		prepareCreate(type, insulation);
		return new FoamCableBlock(settings, type, insulation);
	}

	@Override
	public boolean isFoam()
	{
		return true;
	}

	@Override
	public boolean isHardFoam(BlockState state)
	{
		return state.getValue(foamProperty).isHard();
	}
}

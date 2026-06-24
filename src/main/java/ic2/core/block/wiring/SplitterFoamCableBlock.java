package ic2.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;

public class SplitterFoamCableBlock extends AbstractSplitterCableBlock
{
	protected SplitterFoamCableBlock(Properties settings)
	{
		super(settings);
	}

	public static SplitterFoamCableBlock create(Properties settings)
	{
		prepareCreate(CableType.splitter, 0);
		return new SplitterFoamCableBlock(settings);
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

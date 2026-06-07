package ic2.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class SplitterCableBlock extends AbstractSplitterCableBlock
{
	private final SplitterFoamCableBlock foamCableBlock;

	public static SplitterCableBlock create(Properties settings, SplitterFoamCableBlock foamCableBlock)
	{
		prepareCreate(CableType.splitter, 0);
		return new SplitterCableBlock(settings, foamCableBlock);
	}

	protected SplitterCableBlock(Properties settings, SplitterFoamCableBlock foamCableBlock)
	{
		super(settings, CableType.splitter, 0);
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

	public SplitterFoamCableBlock getFoamCableBlock()
	{
		return this.foamCableBlock;
	}
}

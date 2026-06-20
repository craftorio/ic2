package ic2.core.block.wiring;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SplitterFoamCableBlock extends AbstractSplitterCableBlock
{
    public static final com.mojang.serialization.MapCodec<SplitterFoamCableBlock> CODEC = simpleCodec(SplitterFoamCableBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<SplitterFoamCableBlock> codec() {
        return CODEC;
    }

	public static SplitterFoamCableBlock create(Properties settings)
	{
		prepareCreate(CableType.splitter, 0);
		return new SplitterFoamCableBlock(settings);
	}

	protected SplitterFoamCableBlock(Properties settings)
	{
		super(settings, CableType.splitter, 0);
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

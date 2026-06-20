package ic2.core.block.inherit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class Ic2GlassBlock extends TransparentBlock
{
    public static final com.mojang.serialization.MapCodec<Ic2GlassBlock> CODEC = simpleCodec(Ic2GlassBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.TransparentBlock> codec() {
        return CODEC;
    }

	public Ic2GlassBlock(Properties settings)
	{
		super(settings);
	}

	public VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos)
	{
		return Shapes.empty();
	}
}

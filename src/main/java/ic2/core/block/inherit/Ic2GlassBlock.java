package ic2.core.block.inherit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Ic2GlassBlock extends AbstractGlassBlock
{
	public Ic2GlassBlock(Properties settings)
	{
		super(settings);
	}

	public VoxelShape m_7947_(BlockState state, BlockGetter world, BlockPos pos)
	{
		return Shapes.m_83040_();
	}
}

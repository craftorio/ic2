package ic2.core.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MiningPipeBlock extends Block
{
	private static final VoxelShape SHAPE = Shapes.box(0.375, 0.0, 0.375, 0.625, 1.0, 0.625);

	public MiningPipeBlock(Properties settings)
	{
		super(settings);
	}

	public boolean useShapeForLightOcclusion(BlockState state)
	{
		return true;
	}

	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}
}

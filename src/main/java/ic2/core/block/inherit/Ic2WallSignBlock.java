package ic2.core.block.inherit;

import ic2.core.block.tileentity.Ic2SignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.WoodType;

public class Ic2WallSignBlock extends WallSignBlock
{
	public Ic2WallSignBlock(Properties settings, WoodType signType)
	{
		super(settings, signType);
	}

	public BlockEntity m_142194_(BlockPos pos, BlockState state)
	{
		return new Ic2SignBlockEntity(pos, state);
	}
}

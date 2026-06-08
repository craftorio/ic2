package ic2.core.block.tileentity;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class Ic2SignBlockEntity extends SignBlockEntity
{
	public Ic2SignBlockEntity(BlockPos pos, BlockState state)
	{
		super(pos, state);
	}

	public BlockEntityType<?> getType()
	{
		return Ic2BlockEntities.SIGN;
	}
}

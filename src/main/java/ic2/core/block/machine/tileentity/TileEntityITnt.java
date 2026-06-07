package ic2.core.block.machine.tileentity;

import ic2.api.entity.block.ExplosiveEntity;
import ic2.core.entity.block.ITntEntity;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityITnt extends TileEntityExplosive
{
	public TileEntityITnt(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.ITNT, pos, state);
	}

	@Override
	protected boolean explodeOnRemoval()
	{
		return true;
	}

	@Override
	protected ExplosiveEntity getEntity(LivingEntity igniter)
	{
		return new ITntEntity(this.getLevel(), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5);
	}
}

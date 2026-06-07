package ic2.core.entity.block;

import ic2.api.entity.block.ExplosiveEntity;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Entities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ITntEntity extends ExplosiveEntity
{
	public ITntEntity(Level world, double x, double y, double z)
	{
		super(Ic2Entities.ITNT, world, x, y, z, 60, 5.5F, 0.9F, 0.3F, Ic2Blocks.ITNT.defaultBlockState(), 0);
	}

	public ITntEntity(EntityType<? extends ITntEntity> type, Level world)
	{
		this(world, 0.0, 0.0, 0.0);
	}
}

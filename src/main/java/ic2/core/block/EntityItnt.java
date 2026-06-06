package ic2.core.block;

import ic2.core.ref.BlockName;
import ic2.core.ref.TeBlock;
import net.minecraft.world.World;

public class EntityItnt extends EntityIC2Explosive
{
	public EntityItnt(World world, double x, double y, double z)
	{
		super(world, x, y, z, 60, 5.5F, 0.9F, 0.3F, BlockName.te.getBlockState(TeBlock.itnt), 0);
	}

	public EntityItnt(World world)
	{
		this(world, 0.0, 0.0, 0.0);
	}
}

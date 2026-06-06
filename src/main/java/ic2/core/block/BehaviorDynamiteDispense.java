package ic2.core.block;

import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BehaviorDynamiteDispense extends BehaviorProjectileDispense
{
	private final boolean sticky;

	public BehaviorDynamiteDispense(boolean sticky)
	{
		this.sticky = sticky;
	}

	protected IProjectile getProjectileEntity(World world, IPosition pos, ItemStack stack)
	{
		return this.sticky
			? new EntityStickyDynamite(world, pos.getX(), pos.getY(), pos.getZ())
			: new EntityDynamite(world, pos.getX(), pos.getY(), pos.getZ());
	}
}

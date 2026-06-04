// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.dispenser.IPosition;
import net.minecraft.world.World;
import net.minecraft.dispenser.BehaviorProjectileDispense;

public class BehaviorDynamiteDispense extends BehaviorProjectileDispense
{
    private final boolean sticky;
    
    public BehaviorDynamiteDispense(final boolean sticky) {
        this.sticky = sticky;
    }
    
    protected IProjectile getProjectileEntity(final World world, final IPosition pos, final ItemStack stack) {
        if (this.sticky) {
            return (IProjectile)new EntityStickyDynamite(world, pos.getX(), pos.getY(), pos.getZ());
        }
        return (IProjectile)new EntityDynamite(world, pos.getX(), pos.getY(), pos.getZ());
    }
}

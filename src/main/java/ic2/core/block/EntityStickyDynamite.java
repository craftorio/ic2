// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityStickyDynamite extends EntityDynamite
{
    public EntityStickyDynamite(final World world) {
        super(world);
        this.sticky = true;
    }
    
    public EntityStickyDynamite(final World world, final EntityLivingBase entityliving) {
        super(world, entityliving);
        this.sticky = true;
    }
    
    public EntityStickyDynamite(final World world, final double x, final double y, final double z) {
        super(world, x, y, z);
        this.sticky = true;
    }
}

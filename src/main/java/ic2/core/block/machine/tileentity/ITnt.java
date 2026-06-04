// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.EntityItnt;
import ic2.core.block.EntityIC2Explosive;
import net.minecraft.entity.EntityLivingBase;

public class ITnt extends Explosive
{
    @Override
    protected boolean explodeOnRemoval() {
        return true;
    }
    
    @Override
    protected EntityIC2Explosive getEntity(final EntityLivingBase igniter) {
        return new EntityItnt(this.getWorld(), this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5);
    }
}

package ic2.core.block.machine.tileentity;

import ic2.core.block.EntityIC2Explosive;
import ic2.core.block.EntityItnt;
import net.minecraft.entity.EntityLivingBase;

public class ITnt extends Explosive {
  protected boolean explodeOnRemoval() {
    return true;
  }
  
  protected EntityIC2Explosive getEntity(EntityLivingBase igniter) {
    return (EntityIC2Explosive)new EntityItnt(getWorld(), this.field_174879_c.getX() + 0.5D, this.field_174879_c.getY() + 0.5D, this.field_174879_c.getZ() + 0.5D);
  }
}

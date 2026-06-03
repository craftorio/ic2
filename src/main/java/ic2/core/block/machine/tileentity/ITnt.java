package ic2.core.block.machine.tileentity;

import ic2.core.block.EntityIC2Explosive;
import ic2.core.block.EntityItnt;
import net.minecraft.entity.EntityLivingBase;

public class ITnt extends Explosive {
  protected boolean explodeOnRemoval() {
    return true;
  }
  
  protected EntityIC2Explosive getEntity(EntityLivingBase igniter) {
    return (EntityIC2Explosive)new EntityItnt(func_145831_w(), this.field_174879_c.func_177958_n() + 0.5D, this.field_174879_c.func_177956_o() + 0.5D, this.field_174879_c.func_177952_p() + 0.5D);
  }
}

package ic2.core.block;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityStickyDynamite extends EntityDynamite {
   public EntityStickyDynamite(World world) {
      super(world);
      this.sticky = true;
   }

   public EntityStickyDynamite(World world, EntityLivingBase entityliving) {
      super(world, entityliving);
      this.sticky = true;
   }

   public EntityStickyDynamite(World world, double x, double y, double z) {
      super(world, x, y, z);
      this.sticky = true;
   }
}

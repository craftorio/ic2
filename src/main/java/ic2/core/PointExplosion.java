package ic2.core;

import ic2.api.event.ExplosionEvent;
import ic2.core.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class PointExplosion extends Explosion {
   private final World world;
   private final Entity entity;
   private final float dropRate;
   private final int entityDamage;
   private float explosionSize;

   public PointExplosion(World world1, Entity entity, EntityLivingBase exploder, double x, double y, double z, float power, float dropRate1, int entityDamage1) {
      super(world1, exploder, x, y, z, power, true, true);
      this.world = world1;
      this.entity = entity;
      this.dropRate = dropRate1;
      this.entityDamage = entityDamage1;
      this.explosionSize = power;
   }

   public void doExplosionA() {
      double explosionX = this.getPosition().x;
      double explosionY = this.getPosition().y;
      double explosionZ = this.getPosition().z;
      ExplosionEvent event = new ExplosionEvent(this.world, this.entity, this.getPosition(), this.explosionSize, this.getExplosivePlacedBy(), 0, 1.0);
      if (!MinecraftForge.EVENT_BUS.post(event)) {
         for (int x = Util.roundToNegInf(explosionX) - 1; x <= Util.roundToNegInf(explosionX) + 1; x++) {
            for (int y = Util.roundToNegInf(explosionY) - 1; y <= Util.roundToNegInf(explosionY) + 1; y++) {
               for (int z = Util.roundToNegInf(explosionZ) - 1; z <= Util.roundToNegInf(explosionZ) + 1; z++) {
                  BlockPos pos = new BlockPos(x, y, z);
                  IBlockState block = this.world.getBlockState(pos);
                  if (block.getBlock().getExplosionResistance(this.world, pos, this.getExplosivePlacedBy(), this) < this.explosionSize * 10.0F) {
                     this.getAffectedBlockPositions().add(pos);
                  }
               }
            }
         }

         for (Entity entity : this.world
            .getEntitiesWithinAABBExcludingEntity(
               this.getExplosivePlacedBy(),
               new AxisAlignedBB(explosionX - 2.0, explosionY - 2.0, explosionZ - 2.0, explosionX + 2.0, explosionY + 2.0, explosionZ + 2.0)
            )) {
            entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), this.entityDamage);
         }

         this.explosionSize = 1.0F / this.dropRate;
      }
   }
}

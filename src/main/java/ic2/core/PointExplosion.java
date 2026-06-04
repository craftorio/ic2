package ic2.core;

import ic2.api.event.ExplosionEvent;
import ic2.core.util.Util;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PointExplosion extends Explosion {
  private final World world;
  
  private final Entity entity;
  
  private final float dropRate;
  
  private final int entityDamage;
  
  private float explosionSize;
  
  public PointExplosion(World world1, Entity entity, EntityLivingBase exploder, double x, double y, double z, float power, float dropRate1, int entityDamage1) {
    super(world1, (Entity)exploder, x, y, z, power, true, true);
    this.world = world1;
    this.entity = entity;
    this.dropRate = dropRate1;
    this.entityDamage = entityDamage1;
    this.explosionSize = power;
  }
  
  public void func_77278_a() {
    double explosionX = (getPosition()).field_72450_a;
    double explosionY = (getPosition()).field_72448_b;
    double explosionZ = (getPosition()).field_72449_c;
    ExplosionEvent event = new ExplosionEvent(this.world, this.entity, getPosition(), this.explosionSize, func_94613_c(), 0, 1.0D);
    if (MinecraftForge.EVENT_BUS.post((Event)event))
      return; 
    for (int x = Util.roundToNegInf(explosionX) - 1; x <= Util.roundToNegInf(explosionX) + 1; x++) {
      for (int y = Util.roundToNegInf(explosionY) - 1; y <= Util.roundToNegInf(explosionY) + 1; y++) {
        for (int z = Util.roundToNegInf(explosionZ) - 1; z <= Util.roundToNegInf(explosionZ) + 1; z++) {
          BlockPos pos = new BlockPos(x, y, z);
          IBlockState block = this.world.getBlockState(pos);
          if (block.getBlock().getExplosionResistance(this.world, pos, (Entity)func_94613_c(), this) < this.explosionSize * 10.0F)
            func_180343_e().add(pos); 
        } 
      } 
    } 
    List<Entity> entitiesInRange = this.world.func_72839_b((Entity)func_94613_c(), new AxisAlignedBB(explosionX - 2.0D, explosionY - 2.0D, explosionZ - 2.0D, explosionX + 2.0D, explosionY + 2.0D, explosionZ + 2.0D));
    for (Entity entity : entitiesInRange)
      entity.func_70097_a(DamageSource.func_94539_a(this), this.entityDamage); 
    this.explosionSize = 1.0F / this.dropRate;
  }
}

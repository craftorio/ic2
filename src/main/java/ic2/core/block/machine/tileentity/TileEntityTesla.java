package ic2.core.block.machine.tileentity;

import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.item.armor.ItemArmorHazmat;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class TileEntityTesla extends TileEntityBlock {
  protected final Redstone redstone = (Redstone)addComponent((TileEntityComponent)new Redstone(this));
  
  protected final Energy energy = (Energy)addComponent((TileEntityComponent)Energy.asBasicSink(this, 10000.0D, 2));
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (!this.redstone.hasRedstoneInput())
      return; 
    if (this.energy.useEnergy(1.0D) && ++this.ticker % 32 == 0) {
      int damage = (int)this.energy.getEnergy() / 400;
      if (damage > 0 && shock(damage))
        this.energy.useEnergy((damage * 400)); 
    } 
  }
  
  protected boolean shock(int damage) {
    int r = 4;
    World world = getWorld();
    List<EntityLivingBase> entities = world.func_72872_a(EntityLivingBase.class, new AxisAlignedBB((this.pos
          .getX() - 4), (this.pos.getY() - 4), (this.pos.getZ() - 4), (this.pos
          .getX() + 4 + 1), (this.pos.getY() + 4 + 1), (this.pos.getZ() + 4 + 1)));
    for (EntityLivingBase entity : entities) {
      if (ItemArmorHazmat.hasCompleteHazmat(entity))
        continue; 
      if (entity.func_70097_a((DamageSource)IC2DamageSource.electricity, damage)) {
        if (world instanceof WorldServer) {
          WorldServer worldServer = (WorldServer)world;
          Random rnd = world.rand;
          for (int i = 0; i < damage; i++)
            worldServer.func_180505_a(EnumParticleTypes.REDSTONE, true, entity.posX + rnd
                
                .nextFloat() - 0.5D, entity.posY + (rnd
                .nextFloat() * 2.0F) - 1.0D, entity.posZ + rnd
                .nextFloat() - 0.5D, 0, 0.1D, 0.1D, 1.0D, 1.0D, new int[0]); 
        } 
        return true;
      } 
    } 
    return false;
  }
  
  private int ticker = IC2.random.nextInt(32);
}

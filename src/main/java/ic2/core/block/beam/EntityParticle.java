package ic2.core.block.beam;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class EntityParticle extends Entity {
  private static final double initialVelocity = 0.5D;
  
  private static final double slowdown = 0.99D;
  
  public EntityParticle(World world) {
    super(world);
    this.field_70145_X = true;
  }
  
  public EntityParticle(TileEmitter emitter) {
    this(emitter.func_145831_w());
    EnumFacing dir = emitter.getFacing();
    double x = emitter.func_174877_v().func_177958_n() + 0.5D + dir.func_82601_c() * 0.5D;
    double y = emitter.func_174877_v().func_177956_o() + 0.5D + dir.func_96559_d() * 0.5D;
    double z = emitter.func_174877_v().func_177952_p() + 0.5D + dir.func_82599_e() * 0.5D;
    func_70107_b(x, y, z);
    this.field_70159_w = dir.func_82601_c() * 0.5D;
    this.field_70181_x = dir.func_96559_d() * 0.5D;
    this.field_70179_y = dir.func_82599_e() * 0.5D;
    func_70105_a(0.2F, 0.2F);
  }
  
  protected void func_70088_a() {}
  
  protected void func_70037_a(NBTTagCompound nbttagcompound) {}
  
  protected void func_70014_b(NBTTagCompound nbttagcompound) {}
  
  public void func_70071_h_() {
    this.field_70169_q = this.field_70165_t;
    this.field_70167_r = this.field_70163_u;
    this.field_70166_s = this.field_70161_v;
    func_70091_d(MoverType.SELF, this.field_70159_w, this.field_70181_x, this.field_70179_y);
    this.field_70159_w *= 0.99D;
    this.field_70181_x *= 0.99D;
    this.field_70179_y *= 0.99D;
    if (this.field_70159_w * this.field_70159_w + this.field_70181_x * this.field_70181_x + this.field_70179_y * this.field_70179_y < 1.0E-4D)
      func_70106_y(); 
  }
}

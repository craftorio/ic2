package ic2.core.item.tool;

import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.util.Quaternion;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Vector3;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

public class EntityParticle extends Entity implements IThrowableEntity {
  private double coreSize;
  
  private double influenceSize;
  
  private int lifeTime;
  
  private Entity owner;
  
  private Vector3[] radialTestVectors;
  
  public EntityParticle(World world) {
    super(world);
    this.noClip = true;
    this.lifeTime = 6000;
  }
  
  public EntityParticle(World world, EntityLivingBase owner1, float speed, double coreSize1, double influenceSize1) {
    this(world);
    this.coreSize = coreSize1;
    this.influenceSize = influenceSize1;
    this.owner = (Entity)owner1;
    Vector3 eyePos = Util.getEyePosition(this.owner);
    setPosition(eyePos.x, eyePos.y, eyePos.z);
    Vector3 motion = new Vector3(owner1.func_70040_Z());
    Vector3 ortho = motion.copy().cross(Vector3.UP).scaleTo(influenceSize1);
    double stepAngle = Math.atan(0.5D / influenceSize1) * 2.0D;
    int steps = (int)Math.ceil(6.283185307179586D / stepAngle);
    Quaternion q = (new Quaternion()).setFromAxisAngle(motion, stepAngle);
    this.radialTestVectors = new Vector3[steps];
    this.radialTestVectors[0] = ortho.copy();
    for (int i = 1; i < steps; i++) {
      q.rotate(ortho);
      this.radialTestVectors[i] = ortho.copy();
    } 
    motion.scale(speed);
    this.motionX = motion.x;
    this.motionY = motion.y;
    this.motionZ = motion.z;
  }
  
  protected void entityInit() {}
  
  protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {}
  
  protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {}
  
  public Entity getThrower() {
    return this.owner;
  }
  
  public void setThrower(Entity entity) {
    this.owner = entity;
  }
  
  public void onUpdate() {
    this.prevPosX = this.posX;
    this.prevPosY = this.posY;
    this.prevPosZ = this.posZ;
    this.posX += this.motionX;
    this.posY += this.motionY;
    this.posZ += this.motionZ;
    Vector3 start = new Vector3(this.prevPosX, this.prevPosY, this.prevPosZ);
    Vector3 end = new Vector3(this.posX, this.posY, this.posZ);
    World world = getEntityWorld();
    RayTraceResult hit = world.func_72901_a(start.toVec3(), end.toVec3(), true);
    if (hit != null) {
      end.set(hit.field_72307_f);
      this.posX = hit.field_72307_f.field_72450_a;
      this.posY = hit.field_72307_f.field_72448_b;
      this.posZ = hit.field_72307_f.field_72449_c;
    } 
    List<Entity> entitiesToCheck = world.func_72839_b(this, (new AxisAlignedBB(this.prevPosX, this.prevPosY, this.prevPosZ, this.posX, this.posY, this.posZ)).func_186662_g(this.influenceSize));
    List<RayTraceResult> entitiesInfluences = new ArrayList<>();
    double minDistanceSq = start.distanceSquared(end);
    for (Entity entity : entitiesToCheck) {
      if (entity != this.owner && entity.func_70067_L()) {
        RayTraceResult entityInfluence = entity.func_174813_aQ().func_186662_g(this.influenceSize).func_72327_a(start.toVec3(), end.toVec3());
        if (entityInfluence != null) {
          entitiesInfluences.add(entityInfluence);
          RayTraceResult entityHit = entity.func_174813_aQ().func_186662_g(this.coreSize).func_72327_a(start.toVec3(), end.toVec3());
          if (entityHit != null) {
            double distanceSq = start.distanceSquared(entityHit.field_72307_f);
            if (distanceSq < minDistanceSq) {
              hit = entityHit;
              minDistanceSq = distanceSq;
            } 
          } 
        } 
      } 
    } 
    double maxInfluenceDistance = Math.sqrt(minDistanceSq) + this.influenceSize;
    for (RayTraceResult entityInfluence : entitiesInfluences) {
      if (start.distance(entityInfluence.field_72307_f) <= maxInfluenceDistance)
        onInfluence(entityInfluence); 
    } 
    if (this.radialTestVectors != null) {
      Vector3 vForward = end.copy().sub(start);
      double len = vForward.length();
      vForward.scale(1.0D / len);
      Vector3 origin = new Vector3(start);
      Vector3 tmp = new Vector3();
      for (int d = 0; d < len; d++) {
        for (Vector3 radialTestVector : this.radialTestVectors) {
          origin.copy(tmp).add(radialTestVector);
          RayTraceResult influence = world.func_72901_a(origin.toVec3(), tmp.toVec3(), true);
          if (influence != null)
            onInfluence(influence); 
        } 
        origin.add(vForward);
      } 
    } 
    if (hit != null) {
      onImpact(hit);
      setDead();
    } else {
      this.lifeTime--;
      if (this.lifeTime <= 0)
        setDead(); 
    } 
  }
  
  protected void onImpact(RayTraceResult hit) {
    if (!IC2.platform.isSimulating())
      return; 
    System.out.println("hit " + hit.typeOfHit + " " + hit.field_72307_f + " sim=" + IC2.platform.isSimulating());
    if (hit.typeOfHit != RayTraceResult.Type.BLOCK || IC2.platform.isSimulating());
    ExplosionIC2 explosion = new ExplosionIC2(getEntityWorld(), this.owner, hit.field_72307_f.field_72450_a, hit.field_72307_f.field_72448_b, hit.field_72307_f.field_72449_c, 18.0F, 0.95F, ExplosionIC2.Type.Heat);
    explosion.doExplosion();
  }
  
  protected void onInfluence(RayTraceResult hit) {
    if (!IC2.platform.isSimulating())
      return; 
    System.out.println("influenced " + hit.typeOfHit + " " + hit.field_72307_f + " sim=" + IC2.platform.isSimulating());
    if (hit.typeOfHit == RayTraceResult.Type.BLOCK && IC2.platform.isSimulating()) {
      World world = getEntityWorld();
      IBlockState state = world.getBlockState(hit.getBlockPos());
      Block block = state.getBlock();
      if (block == Blocks.field_150355_j || block == Blocks.field_150358_i) {
        world.func_175698_g(hit.getBlockPos());
      } else {
        List<ItemStack> drops = StackUtil.getDrops((IBlockAccess)world, hit.getBlockPos(), state, null, 0, true);
        if (drops.size() == 1 && StackUtil.getSize(drops.get(0)) == 1) {
          ItemStack existing = drops.get(0);
          ItemStack smelted = FurnaceRecipes.func_77602_a().func_151395_a(existing);
          if (smelted != null && smelted.getItem() instanceof ItemBlock) {
            world.func_175656_a(hit.getBlockPos(), ((ItemBlock)smelted.getItem()).func_179223_d().getDefaultState());
          } else if (block.isFlammable((IBlockAccess)world, hit.getBlockPos(), hit.field_178784_b)) {
            world.func_175656_a(hit.getBlockPos().func_177972_a(hit.field_178784_b.func_176734_d()), Blocks.field_150480_ab.getDefaultState());
          } 
        } 
      } 
    } 
  }
}

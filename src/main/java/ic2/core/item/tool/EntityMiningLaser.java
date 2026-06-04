package ic2.core.item.tool;

import ic2.api.event.LaserEvent;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.Ic2Player;
import ic2.core.block.MaterialIC2TNT;
import ic2.core.ref.BlockName;
import ic2.core.util.StackUtil;
import ic2.core.util.Vector3;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

public class EntityMiningLaser extends Entity implements IThrowableEntity {
  public float range;
  
  public float power;
  
  public int blockBreaks;
  
  public boolean explosive;
  
  public static final double laserSpeed = 1.0D;
  
  public EntityLivingBase owner;
  
  public boolean headingSet;
  
  public boolean smelt;
  
  private int ticksInAir;
  
  public EntityMiningLaser(World world) {
    super(world);
    this.range = 0.0F;
    this.power = 0.0F;
    this.blockBreaks = 0;
    this.explosive = false;
    this.headingSet = false;
    this.smelt = false;
    this.ticksInAir = 0;
    setSize(0.8F, 0.8F);
  }
  
  public EntityMiningLaser(World world, Vector3 start, Vector3 dir, EntityLivingBase owner, float range, float power, int blockBreaks, boolean explosive) {
    super(world);
    this.range = 0.0F;
    this.power = 0.0F;
    this.blockBreaks = 0;
    this.explosive = false;
    this.headingSet = false;
    this.smelt = false;
    this.ticksInAir = 0;
    this.owner = owner;
    setSize(0.8F, 0.8F);
    setPosition(start.x, start.y, start.z);
    setLaserHeading(dir.x, dir.y, dir.z, 1.0D);
    this.range = range;
    this.power = power;
    this.blockBreaks = blockBreaks;
    this.explosive = explosive;
  }
  
  protected void entityInit() {}
  
  public void setLaserHeading(double motionX, double motionY, double motionZ, double speed) {
    double currentSpeed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
    this.motionX = motionX / currentSpeed * speed;
    this.motionY = motionY / currentSpeed * speed;
    this.motionZ = motionZ / currentSpeed * speed;
    this.field_70126_B = this.rotationYaw = (float)Math.toDegrees(Math.atan2(motionX, motionZ));
    this.field_70127_C = this.rotationPitch = (float)Math.toDegrees(Math.atan2(motionY, Math.sqrt(motionX * motionX + motionZ * motionZ)));
    this.headingSet = true;
  }
  
  public void func_70016_h(double motionX, double motionY, double motionZ) {
    setLaserHeading(motionX, motionY, motionZ, 1.0D);
  }
  
  public void onUpdate() {
    super.onUpdate();
    if (IC2.platform.isSimulating() && (this.range < 1.0F || this.power <= 0.0F || this.blockBreaks <= 0)) {
      if (this.explosive)
        explode(); 
      setDead();
      return;
    } 
    this.ticksInAir++;
    Vec3d oldPosition = new Vec3d(this.posX, this.posY, this.posZ);
    Vec3d newPosition = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
    World world = getEntityWorld();
    RayTraceResult result = world.rayTraceBlocks(oldPosition, newPosition, false, true, false);
    oldPosition = new Vec3d(this.posX, this.posY, this.posZ);
    if (result != null) {
      newPosition = new Vec3d(result.field_72307_f.field_72450_a, result.field_72307_f.field_72448_b, result.field_72307_f.field_72449_c);
    } else {
      newPosition = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
    } 
    Entity hitEntity = null;
    List<Entity> list = world.func_72839_b(this, func_174813_aQ().func_72321_a(this.motionX, this.motionY, this.motionZ).func_186662_g(1.0D));
    double distance = 0.0D;
    for (Entity entity : list) {
      if (!entity.func_70067_L() || (entity == this.owner && this.ticksInAir < 5))
        continue; 
      AxisAlignedBB hitBox = entity.func_174813_aQ().func_186662_g(0.3D);
      RayTraceResult intercept = hitBox.func_72327_a(oldPosition, newPosition);
      if (intercept == null)
        continue; 
      double newDistance = oldPosition.func_72438_d(intercept.field_72307_f);
      if (newDistance < distance || distance == 0.0D) {
        hitEntity = entity;
        distance = newDistance;
      } 
    } 
    RayTraceResult blockHit = result;
    if (hitEntity != null)
      result = new RayTraceResult(hitEntity); 
    if (result != null && result.typeOfHit != RayTraceResult.Type.MISS && !world.isRemote) {
      if (this.explosive) {
        explode();
        setDead();
        return;
      } 
      switch (result.typeOfHit) {
        case ENTITY:
          if (hitEntity(result.field_72308_g))
            break; 
          if (blockHit == null) {
            this.power -= 0.5F;
            break;
          } 
          result = blockHit;
        case BLOCK:
          if (!hitBlock(result.getBlockPos(), result.field_178784_b))
            this.power -= 0.5F; 
          break;
        default:
          throw new RuntimeException("invalid hit type: " + result.typeOfHit);
      } 
    } else {
      this.power -= 0.5F;
    } 
    setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
    this.range = (float)(this.range - Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ));
    if (func_70090_H())
      setDead(); 
  }
  
  private void explode() {
    World world = getEntityWorld();
    LaserEvent.LaserExplodesEvent event = new LaserEvent.LaserExplodesEvent(world, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, 5.0F, 0.85F, 0.55F);
    if (MinecraftForge.EVENT_BUS.post((Event)event)) {
      setDead();
      return;
    } 
    copyDataFromEvent((LaserEvent)event);
    ExplosionIC2 explosion = new ExplosionIC2(world, this, this.posX, this.posY, this.posZ, event.explosionPower, event.explosionDropRate);
    explosion.doExplosion();
  }
  
  private boolean hitEntity(Entity entity) {
    LaserEvent.LaserHitsEntityEvent event = new LaserEvent.LaserHitsEntityEvent(getEntityWorld(), this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, entity);
    if (MinecraftForge.EVENT_BUS.post((Event)event)) {
      if (event.passThrough)
        return false; 
      setDead();
      return true;
    } 
    copyDataFromEvent((LaserEvent)event);
    entity = event.hitEntity;
    int damage = (int)this.power;
    if (damage > 0) {
      entity.func_70015_d(damage * (this.smelt ? 2 : 1));
      if (entity.func_70097_a((new EntityDamageSourceIndirect("arrow", this, (Entity)this.owner)).func_76349_b(), damage) && ((this.owner instanceof EntityPlayer && entity instanceof EntityDragon && ((EntityDragon)entity).func_110143_aJ() <= 0.0F) || (entity instanceof MultiPartEntityPart && ((MultiPartEntityPart)entity).field_70259_a instanceof EntityDragon && ((EntityLivingBase)((MultiPartEntityPart)entity).field_70259_a).func_110143_aJ() <= 0.0F)))
        IC2.achievements.issueAchievement((EntityPlayer)this.owner, "killDragonMiningLaser"); 
    } 
    setDead();
    return true;
  }
  
  private boolean hitBlock(BlockPos pos, EnumFacing side) {
    World world = getEntityWorld();
    LaserEvent.LaserHitsBlockEvent event = new LaserEvent.LaserHitsBlockEvent(world, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, pos, side, 0.9F, true, true);
    if (MinecraftForge.EVENT_BUS.post((Event)event)) {
      setDead();
      return true;
    } 
    copyDataFromEvent((LaserEvent)event);
    IBlockState state = world.getBlockState(event.pos);
    Block block = state.getBlock();
    EntityPlayer playerOwner = (this.owner instanceof EntityPlayer) ? (EntityPlayer)this.owner : Ic2Player.get(world);
    if (MinecraftForge.EVENT_BUS.post((Event)new BlockEvent.BreakEvent(world, pos, state, playerOwner))) {
      setDead();
      return true;
    } 
    if (block.isAir(state, (IBlockAccess)world, event.pos) || block == Blocks.field_150359_w || block == Blocks.field_150410_aZ || block == BlockName.glass.getInstance())
      return false; 
    if (world.isRemote)
      return true; 
    float hardness = state.func_185887_b(world, event.pos);
    if (hardness < 0.0F) {
      setDead();
      return true;
    } 
    this.power -= hardness / 1.5F;
    if (this.power < 0.0F)
      return true; 
    List<ItemStack> replacements = new ArrayList<>();
    if (state.getMaterial() == Material.field_151590_u || state.getMaterial() == MaterialIC2TNT.instance) {
      block.func_180652_a(world, event.pos, new Explosion(world, this, event.pos.getX() + 0.5D, event.pos.getY() + 0.5D, event.pos.getZ() + 0.5D, 1.0F, false, true));
    } else if (this.smelt) {
      if (state.getMaterial() == Material.field_151575_d) {
        event.dropBlock = false;
      } else {
        for (ItemStack isa : StackUtil.getDrops((IBlockAccess)world, event.pos, state, block, 0)) {
          ItemStack is = FurnaceRecipes.func_77602_a().func_151395_a(isa);
          if (!StackUtil.isEmpty(is))
            replacements.add(is); 
        } 
        event.dropBlock = replacements.isEmpty();
      } 
    } 
    if (event.removeBlock) {
      if (event.dropBlock)
        block.func_180653_a(world, event.pos, state, event.dropChance, 0); 
      world.func_175698_g(event.pos);
      for (ItemStack replacement : replacements) {
        if (!StackUtil.placeBlock(replacement, world, event.pos))
          StackUtil.dropAsEntity(world, event.pos, replacement); 
        this.power = 0.0F;
      } 
      if (world.rand.nextInt(10) == 0 && state.getMaterial().func_76217_h())
        world.func_175656_a(event.pos, Blocks.field_150480_ab.getDefaultState()); 
    } 
    this.blockBreaks--;
    return true;
  }
  
  public void writeEntityToNBT(NBTTagCompound nbttagcompound) {}
  
  public void readEntityFromNBT(NBTTagCompound nbttagcompound) {}
  
  void copyDataFromEvent(LaserEvent event) {
    this.owner = event.owner;
    this.range = event.range;
    this.power = event.power;
    this.blockBreaks = event.blockBreaks;
    this.explosive = event.explosive;
    this.smelt = event.smelt;
  }
  
  public Entity getThrower() {
    return (Entity)this.owner;
  }
  
  public void setThrower(Entity entity) {
    if (entity instanceof EntityLivingBase)
      this.owner = (EntityLivingBase)entity; 
  }
}

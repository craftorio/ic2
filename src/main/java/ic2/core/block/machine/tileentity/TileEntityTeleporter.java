package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.tile.IEnergyStorage;
import ic2.core.IC2;
import ic2.core.audio.AudioPosition;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.init.MainConfig;
import ic2.core.network.NetworkManager;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTeleporter extends TileEntityBlock implements INetworkTileEntityEventListener {
  protected final ComparatorEmitter comparator = (ComparatorEmitter)addComponent((TileEntityComponent)new ComparatorEmitter(this));
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    if (nbt.hasKey("targetX"))
      this.target = new BlockPos(nbt.getInteger("targetX"), nbt.getInteger("targetY"), nbt.getInteger("targetZ")); 
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    if (this.target != null) {
      nbt.setInteger("targetX", this.target.getX());
      nbt.setInteger("targetY", this.target.getY());
      nbt.setInteger("targetZ", this.target.getZ());
    } 
    return nbt;
  }
  
  protected void onUnloaded() {
    if (IC2.platform.isRendering() && this.audioSource != null) {
      IC2.audioManager.removeSources(this);
      this.audioSource = null;
    } 
    super.onUnloaded();
  }
  
  protected void onLoaded() {
    super.onLoaded();
    updateComparatorLevel();
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean coolingDown = (this.cooldown > 0);
    if (coolingDown) {
      this.cooldown--;
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "cooldown");
    } 
    World world = getWorld();
    if (world.isBlockPowered(this.pos) && this.target != null) {
      List<Entity> entitiesNearby;
      setActive(true);
      if (coolingDown) {
        entitiesNearby = Collections.emptyList();
      } else {
        entitiesNearby = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB((this.pos
              .getX() - 1), this.pos.getY(), (this.pos.getZ() - 1), (this.pos.getX() + 2), (this.pos.getY() + 3), (this.pos.getZ() + 2)));
      } 
      if (!entitiesNearby.isEmpty() && verifyTarget()) {
        double minDistanceSquared = Double.MAX_VALUE;
        Entity closestEntity = null;
        for (Entity entity : entitiesNearby) {
          if (entity.getRidingEntity() != null)
            continue; 
          double distSquared = this.pos.distanceSqToCenter(entity.posX, entity.posY, entity.posZ);
          if (distSquared < minDistanceSquared) {
            minDistanceSquared = distSquared;
            closestEntity = entity;
          } 
        } 
        assert closestEntity != null;
        teleport(closestEntity, Math.sqrt(this.pos.distanceSq((Vec3i)this.target)));
      } else if (++this.targetCheckTicker % 1024 == 0) {
        verifyTarget();
      } 
    } else {
      setActive(false);
    } 
  }
  
  private boolean verifyTarget() {
    if (getWorld().getTileEntity(this.target) instanceof TileEntityTeleporter)
      return true; 
    this.target = null;
    updateComparatorLevel();
    setActive(false);
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (getActive())
      if (this.cooldown > 0) {
        spawnGreenParticles(2, this.pos);
      } else {
        spawnBlueParticles(2, this.pos);
      }  
  }
  
  private void updateComparatorLevel() {
    int targetLevel = (this.target != null) ? 15 : 0;
    this.comparator.setLevel(targetLevel);
  }
  
  public void teleport(Entity user, double distance) {
    int weight = getWeightOf(user);
    if (weight == 0)
      return; 
    int energyCost = (int)(weight * Math.pow(distance + 10.0D, 0.7D) * 5.0D);
    if (energyCost > getAvailableEnergy())
      return; 
    consumeEnergy(energyCost);
    if (user instanceof EntityPlayerMP) {
      ((EntityPlayerMP)user).setPositionAndUpdate(this.target.getX() + 0.5D, this.target.getY() + 1.5D + user.getYOffset(), this.target.getZ() + 0.5D);
    } else {
      user.setPositionAndRotation(this.target.getX() + 0.5D, this.target.getY() + 1.5D + user.getYOffset(), this.target.getZ() + 0.5D, user.rotationYaw, user.rotationPitch);
    } 
    TileEntity te = getWorld().getTileEntity(this.target);
    assert te instanceof TileEntityTeleporter;
    ((TileEntityTeleporter)te).onTeleportTo(this, user);
    ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this, 0, true);
    if (user instanceof EntityPlayer && distance >= 1000.0D)
      IC2.achievements.issueAchievement((EntityPlayer)user, "teleportFarAway"); 
  }
  
  public void spawnBlueParticles(int n, BlockPos pos) {
    spawnParticles(n, pos, -1, 0, 1);
  }
  
  public void spawnGreenParticles(int n, BlockPos pos) {
    spawnParticles(n, pos, -1, 1, 0);
  }
  
  private void spawnParticles(int n, BlockPos pos, int red, int green, int blue) {
    World world = getWorld();
    Random rnd = world.rand;
    for (int i = 0; i < n; i++) {
      world.spawnParticle(EnumParticleTypes.REDSTONE, (pos
          .getX() + rnd.nextFloat()), ((pos
          .getY() + 1) + rnd.nextFloat()), (pos
          .getZ() + rnd.nextFloat()), red, green, blue, new int[0]);
      world.spawnParticle(EnumParticleTypes.REDSTONE, (pos
          .getX() + rnd.nextFloat()), ((pos
          .getY() + 2) + rnd.nextFloat()), (pos
          .getZ() + rnd.nextFloat()), red, green, blue, new int[0]);
    } 
  }
  
  public void consumeEnergy(int energy) {
    World world = getWorld();
    List<IEnergyStorage> energySources = new LinkedList<>();
    for (EnumFacing dir : EnumFacing.VALUES) {
      TileEntity target = world.getTileEntity(this.pos.offset(dir));
      if (target instanceof IEnergyStorage) {
        IEnergyStorage energySource = (IEnergyStorage)target;
        if (energySource.isTeleporterCompatible(dir.getOpposite()) && energySource.getStored() > 0)
          energySources.add(energySource); 
      } 
    } 
    while (energy > 0) {
      int drain = (energy + energySources.size() - 1) / energySources.size();
      for (Iterator<IEnergyStorage> it = energySources.iterator(); it.hasNext(); ) {
        IEnergyStorage energySource = it.next();
        if (drain > energy)
          drain = energy; 
        if (energySource.getStored() <= drain) {
          energy -= energySource.getStored();
          energySource.setStored(0);
          it.remove();
          continue;
        } 
        energy -= drain;
        energySource.addEnergy(-drain);
      } 
    } 
  }
  
  public int getAvailableEnergy() {
    World world = getWorld();
    int energy = 0;
    for (EnumFacing dir : EnumFacing.VALUES) {
      TileEntity target = world.getTileEntity(this.pos.offset(dir));
      if (target instanceof IEnergyStorage) {
        IEnergyStorage storage = (IEnergyStorage)target;
        if (storage.isTeleporterCompatible(dir.getOpposite()))
          energy += storage.getStored(); 
      } 
    } 
    return energy;
  }
  
  public int getWeightOf(Entity user) {
    boolean teleporterUseInventoryWeight = ConfigUtil.getBool(MainConfig.get(), "balance/teleporterUseInventoryWeight");
    int weight = 0;
    if (user instanceof EntityItem) {
      ItemStack is = ((EntityItem)user).getItem();
      weight += 100 * StackUtil.getSize(is) / is.getMaxStackSize();
    } else if (user instanceof net.minecraft.entity.passive.EntityAnimal || user instanceof net.minecraft.entity.item.EntityMinecart || user instanceof net.minecraft.entity.item.EntityBoat) {
      weight += 100;
    } else if (user instanceof EntityPlayer) {
      weight += 1000;
      if (teleporterUseInventoryWeight)
        for (ItemStack stack : ((EntityPlayer)user).inventory.mainInventory)
          weight += getStackCost(stack);  
    } else if (user instanceof net.minecraft.entity.monster.EntityGhast) {
      weight += 2500;
    } else if (user instanceof net.minecraft.entity.boss.EntityWither) {
      weight += 5000;
    } else if (user instanceof net.minecraft.entity.boss.EntityDragon) {
      weight += 10000;
    } else if (user instanceof net.minecraft.entity.EntityCreature) {
      weight += 500;
    } 
    if (teleporterUseInventoryWeight && user instanceof EntityLivingBase) {
      EntityLivingBase living = (EntityLivingBase)user;
      for (ItemStack stack : living.getEquipmentAndArmor())
        weight += getStackCost(stack); 
      if (user instanceof EntityPlayer) {
        ItemStack stack = living.getHeldItemMainhand();
        weight -= getStackCost(stack);
      } 
    } 
    for (Entity passenger : user.getPassengers())
      weight += getWeightOf(passenger); 
    return weight;
  }
  
  private static int getStackCost(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return 0; 
    return 100 * StackUtil.getSize(stack) / stack.getMaxStackSize();
  }
  
  private void onTeleportTo(TileEntityTeleporter from, Entity entity) {
    this.cooldown = 20;
  }
  
  protected boolean canEntityDestroy(Entity entity) {
    return (!(entity instanceof net.minecraft.entity.boss.EntityDragon) && !(entity instanceof net.minecraft.entity.boss.EntityWither));
  }
  
  public boolean hasTarget() {
    return (this.target != null);
  }
  
  public BlockPos getTarget() {
    return this.target;
  }
  
  public void setTarget(BlockPos pos) {
    this.target = pos;
    updateComparatorLevel();
    ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "target");
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("target");
    return ret;
  }
  
  public void onNetworkUpdate(String field) {
    if (field.equals("active")) {
      if (this.audioSource == null)
        this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/Teleporter/TeleChargedLoop.ogg", true, false, IC2.audioManager
            
            .getDefaultVolume()); 
      if (getActive()) {
        if (this.audioSource != null)
          this.audioSource.play(); 
      } else if (this.audioSource != null) {
        this.audioSource.stop();
      } 
    } 
    super.onNetworkUpdate(field);
  }
  
  public void onNetworkEvent(int event) {
    switch (event) {
      case 0:
        IC2.audioManager.playOnce(this, "Machines/Teleporter/TeleUse.ogg");
        IC2.audioManager.playOnce(new AudioPosition(getWorld(), this.pos), "Machines/Teleporter/TeleUse.ogg");
        spawnBlueParticles(20, this.pos);
        spawnBlueParticles(20, this.target);
        return;
    } 
    IC2.platform.displayError("An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID " + event + ", tile entity below)\nT: " + this + " (" + this.pos + ")", new Object[0]);
  }
  
  private AudioSource audioSource = null;
  
  private int targetCheckTicker = IC2.random.nextInt(1024);
  
  private int cooldown = 0;
  
  private BlockPos target;
  
  private static final int EventTeleport = 0;
}

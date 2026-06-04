// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.audio.AudioPosition;
import ic2.core.audio.PositionSpec;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
import ic2.core.util.StackUtil;
import net.minecraft.entity.item.EntityItem;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraft.util.EnumFacing;
import ic2.api.tile.IEnergyStorage;
import java.util.LinkedList;
import java.util.Random;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.Entity;
import java.util.Collections;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.IC2;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.audio.AudioSource;
import net.minecraft.util.math.BlockPos;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.block.TileEntityBlock;

public class TileEntityTeleporter extends TileEntityBlock implements INetworkTileEntityEventListener
{
    private BlockPos target;
    private AudioSource audioSource;
    private int targetCheckTicker;
    private int cooldown;
    protected final ComparatorEmitter comparator;
    private static final int EventTeleport = 0;
    
    public TileEntityTeleporter() {
        this.audioSource = null;
        this.targetCheckTicker = IC2.random.nextInt(1024);
        this.cooldown = 0;
        this.comparator = this.addComponent(new ComparatorEmitter(this));
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("targetX")) {
            this.target = new BlockPos(nbt.getInteger("targetX"), nbt.getInteger("targetY"), nbt.getInteger("targetZ"));
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (this.target != null) {
            nbt.setInteger("targetX", this.target.getX());
            nbt.setInteger("targetY", this.target.getY());
            nbt.setInteger("targetZ", this.target.getZ());
        }
        return nbt;
    }
    
    @Override
    protected void onUnloaded() {
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }
        super.onUnloaded();
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateComparatorLevel();
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        final boolean coolingDown = this.cooldown > 0;
        if (coolingDown) {
            --this.cooldown;
            IC2.network.get(true).updateTileEntityField(this, "cooldown");
        }
        final World world = this.getWorld();
        if (world.isBlockPowered(this.pos) && this.target != null) {
            this.setActive(true);
            List<Entity> entitiesNearby;
            if (coolingDown) {
                entitiesNearby = Collections.emptyList();
            }
            else {
                entitiesNearby = world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB((double)(this.pos.getX() - 1), (double)this.pos.getY(), (double)(this.pos.getZ() - 1), (double)(this.pos.getX() + 2), (double)(this.pos.getY() + 3), (double)(this.pos.getZ() + 2)));
            }
            if (!entitiesNearby.isEmpty() && this.verifyTarget()) {
                double minDistanceSquared = Double.MAX_VALUE;
                Entity closestEntity = null;
                for (final Entity entity : entitiesNearby) {
                    if (entity.getRidingEntity() != null) {
                        continue;
                    }
                    final double distSquared = this.pos.distanceSqToCenter(entity.posX, entity.posY, entity.posZ);
                    if (distSquared >= minDistanceSquared) {
                        continue;
                    }
                    minDistanceSquared = distSquared;
                    closestEntity = entity;
                }
                assert closestEntity != null;
                this.teleport(closestEntity, Math.sqrt(this.pos.distanceSq((Vec3i)this.target)));
            }
            else if (++this.targetCheckTicker % 1024 == 0) {
                this.verifyTarget();
            }
        }
        else {
            this.setActive(false);
        }
    }
    
    private boolean verifyTarget() {
        if (this.getWorld().getTileEntity(this.target) instanceof TileEntityTeleporter) {
            return true;
        }
        this.target = null;
        this.updateComparatorLevel();
        this.setActive(false);
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    protected void updateEntityClient() {
        super.updateEntityClient();
        if (this.getActive()) {
            if (this.cooldown > 0) {
                this.spawnGreenParticles(2, this.pos);
            }
            else {
                this.spawnBlueParticles(2, this.pos);
            }
        }
    }
    
    private void updateComparatorLevel() {
        final int targetLevel = (this.target != null) ? 15 : 0;
        this.comparator.setLevel(targetLevel);
    }
    
    public void teleport(final Entity user, final double distance) {
        final int weight = this.getWeightOf(user);
        if (weight == 0) {
            return;
        }
        final int energyCost = (int)(weight * Math.pow(distance + 10.0, 0.7) * 5.0);
        if (energyCost > this.getAvailableEnergy()) {
            return;
        }
        this.consumeEnergy(energyCost);
        if (user instanceof EntityPlayerMP) {
            ((EntityPlayerMP)user).setPositionAndUpdate(this.target.getX() + 0.5, this.target.getY() + 1.5 + user.getYOffset(), this.target.getZ() + 0.5);
        }
        else {
            user.setPositionAndRotation(this.target.getX() + 0.5, this.target.getY() + 1.5 + user.getYOffset(), this.target.getZ() + 0.5, user.rotationYaw, user.rotationPitch);
        }
        final TileEntity te = this.getWorld().getTileEntity(this.target);
        assert te instanceof TileEntityTeleporter;
        ((TileEntityTeleporter)te).onTeleportTo(this, user);
        IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
        if (user instanceof EntityPlayer && distance >= 1000.0) {
            IC2.achievements.issueAchievement((EntityPlayer)user, "teleportFarAway");
        }
    }
    
    public void spawnBlueParticles(final int n, final BlockPos pos) {
        this.spawnParticles(n, pos, -1, 0, 1);
    }
    
    public void spawnGreenParticles(final int n, final BlockPos pos) {
        this.spawnParticles(n, pos, -1, 1, 0);
    }
    
    private void spawnParticles(final int n, final BlockPos pos, final int red, final int green, final int blue) {
        final World world = this.getWorld();
        final Random rnd = world.rand;
        for (int i = 0; i < n; ++i) {
            world.spawnParticle(EnumParticleTypes.REDSTONE, (double)(pos.getX() + rnd.nextFloat()), (double)(pos.getY() + 1 + rnd.nextFloat()), (double)(pos.getZ() + rnd.nextFloat()), (double)red, (double)green, (double)blue, new int[0]);
            world.spawnParticle(EnumParticleTypes.REDSTONE, (double)(pos.getX() + rnd.nextFloat()), (double)(pos.getY() + 2 + rnd.nextFloat()), (double)(pos.getZ() + rnd.nextFloat()), (double)red, (double)green, (double)blue, new int[0]);
        }
    }
    
    public void consumeEnergy(int energy) {
        final World world = this.getWorld();
        final List<IEnergyStorage> energySources = new LinkedList<IEnergyStorage>();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity target = world.getTileEntity(this.pos.offset(dir));
            if (target instanceof IEnergyStorage) {
                final IEnergyStorage energySource = (IEnergyStorage)target;
                if (energySource.isTeleporterCompatible(dir.getOpposite()) && energySource.getStored() > 0) {
                    energySources.add(energySource);
                }
            }
        }
        while (energy > 0) {
            int drain = (energy + energySources.size() - 1) / energySources.size();
            final Iterator<IEnergyStorage> it = energySources.iterator();
            while (it.hasNext()) {
                final IEnergyStorage energySource2 = it.next();
                if (drain > energy) {
                    drain = energy;
                }
                if (energySource2.getStored() <= drain) {
                    energy -= energySource2.getStored();
                    energySource2.setStored(0);
                    it.remove();
                }
                else {
                    energy -= drain;
                    energySource2.addEnergy(-drain);
                }
            }
        }
    }
    
    public int getAvailableEnergy() {
        final World world = this.getWorld();
        int energy = 0;
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity target = world.getTileEntity(this.pos.offset(dir));
            if (target instanceof IEnergyStorage) {
                final IEnergyStorage storage = (IEnergyStorage)target;
                if (storage.isTeleporterCompatible(dir.getOpposite())) {
                    energy += storage.getStored();
                }
            }
        }
        return energy;
    }
    
    public int getWeightOf(final Entity user) {
        final boolean teleporterUseInventoryWeight = ConfigUtil.getBool(MainConfig.get(), "balance/teleporterUseInventoryWeight");
        int weight = 0;
        if (user instanceof EntityItem) {
            final ItemStack is = ((EntityItem)user).getItem();
            weight += 100 * StackUtil.getSize(is) / is.getMaxStackSize();
        }
        else if (user instanceof EntityAnimal || user instanceof EntityMinecart || user instanceof EntityBoat) {
            weight += 100;
        }
        else if (user instanceof EntityPlayer) {
            weight += 1000;
            if (teleporterUseInventoryWeight) {
                for (final ItemStack stack : ((EntityPlayer)user).inventory.mainInventory) {
                    weight += getStackCost(stack);
                }
            }
        }
        else if (user instanceof EntityGhast) {
            weight += 2500;
        }
        else if (user instanceof EntityWither) {
            weight += 5000;
        }
        else if (user instanceof EntityDragon) {
            weight += 10000;
        }
        else if (user instanceof EntityCreature) {
            weight += 500;
        }
        if (teleporterUseInventoryWeight && user instanceof EntityLivingBase) {
            final EntityLivingBase living = (EntityLivingBase)user;
            for (final ItemStack stack2 : living.getEquipmentAndArmor()) {
                weight += getStackCost(stack2);
            }
            if (user instanceof EntityPlayer) {
                final ItemStack stack = living.getHeldItemMainhand();
                weight -= getStackCost(stack);
            }
        }
        for (final Entity passenger : user.getPassengers()) {
            weight += this.getWeightOf(passenger);
        }
        return weight;
    }
    
    private static int getStackCost(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return 0;
        }
        return 100 * StackUtil.getSize(stack) / stack.getMaxStackSize();
    }
    
    private void onTeleportTo(final TileEntityTeleporter from, final Entity entity) {
        this.cooldown = 20;
    }
    
    @Override
    protected boolean canEntityDestroy(final Entity entity) {
        return !(entity instanceof EntityDragon) && !(entity instanceof EntityWither);
    }
    
    public boolean hasTarget() {
        return this.target != null;
    }
    
    public BlockPos getTarget() {
        return this.target;
    }
    
    public void setTarget(final BlockPos pos) {
        this.target = pos;
        this.updateComparatorLevel();
        IC2.network.get(true).updateTileEntityField(this, "target");
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("target");
        return ret;
    }
    
    @Override
    public void onNetworkUpdate(final String field) {
        if (field.equals("active")) {
            if (this.audioSource == null) {
                this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/Teleporter/TeleChargedLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());
            }
            if (this.getActive()) {
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
            }
            else if (this.audioSource != null) {
                this.audioSource.stop();
            }
        }
        super.onNetworkUpdate(field);
    }
    
    @Override
    public void onNetworkEvent(final int event) {
        switch (event) {
            case 0: {
                IC2.audioManager.playOnce(this, "Machines/Teleporter/TeleUse.ogg");
                IC2.audioManager.playOnce(new AudioPosition(this.getWorld(), this.pos), "Machines/Teleporter/TeleUse.ogg");
                this.spawnBlueParticles(20, this.pos);
                this.spawnBlueParticles(20, this.target);
                break;
            }
            default: {
                IC2.platform.displayError("An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID " + event + ", tile entity below)\nT: " + this + " (" + this.pos + ")", new Object[0]);
                break;
            }
        }
    }
}

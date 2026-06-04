// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.crafting.FurnaceRecipes;
import ic2.core.util.StackUtil;
import net.minecraft.world.Explosion;
import ic2.core.block.MaterialIC2TNT;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import ic2.core.ref.BlockName;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.event.world.BlockEvent;
import ic2.core.Ic2Player;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntityDamageSourceIndirect;
import ic2.core.ExplosionIC2;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import ic2.api.event.LaserEvent;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import ic2.core.IC2;
import ic2.core.util.Vector3;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraft.entity.Entity;

public class EntityMiningLaser extends Entity implements IThrowableEntity
{
    public float range;
    public float power;
    public int blockBreaks;
    public boolean explosive;
    public static final double laserSpeed = 1.0;
    public EntityLivingBase owner;
    public boolean headingSet;
    public boolean smelt;
    private int ticksInAir;
    
    public EntityMiningLaser(final World world) {
        super(world);
        this.range = 0.0f;
        this.power = 0.0f;
        this.blockBreaks = 0;
        this.explosive = false;
        this.headingSet = false;
        this.smelt = false;
        this.ticksInAir = 0;
        this.setSize(0.8f, 0.8f);
    }
    
    public EntityMiningLaser(final World world, final Vector3 start, final Vector3 dir, final EntityLivingBase owner, final float range, final float power, final int blockBreaks, final boolean explosive) {
        super(world);
        this.range = 0.0f;
        this.power = 0.0f;
        this.blockBreaks = 0;
        this.explosive = false;
        this.headingSet = false;
        this.smelt = false;
        this.ticksInAir = 0;
        this.owner = owner;
        this.setSize(0.8f, 0.8f);
        this.setPosition(start.x, start.y, start.z);
        this.setLaserHeading(dir.x, dir.y, dir.z, 1.0);
        this.range = range;
        this.power = power;
        this.blockBreaks = blockBreaks;
        this.explosive = explosive;
    }
    
    protected void entityInit() {
    }
    
    public void setLaserHeading(final double motionX, final double motionY, final double motionZ, final double speed) {
        final double currentSpeed = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        this.motionX = motionX / currentSpeed * speed;
        this.motionY = motionY / currentSpeed * speed;
        this.motionZ = motionZ / currentSpeed * speed;
        final float n = (float)Math.toDegrees(Math.atan2(motionX, motionZ));
        this.rotationYaw = n;
        this.prevRotationYaw = n;
        final float n2 = (float)Math.toDegrees(Math.atan2(motionY, Math.sqrt(motionX * motionX + motionZ * motionZ)));
        this.rotationPitch = n2;
        this.prevRotationPitch = n2;
        this.headingSet = true;
    }
    
    public void setVelocity(final double motionX, final double motionY, final double motionZ) {
        this.setLaserHeading(motionX, motionY, motionZ, 1.0);
    }
    
    public void onUpdate() {
        super.onUpdate();
        if (IC2.platform.isSimulating() && (this.range < 1.0f || this.power <= 0.0f || this.blockBreaks <= 0)) {
            if (this.explosive) {
                this.explode();
            }
            this.setDead();
            return;
        }
        ++this.ticksInAir;
        Vec3d oldPosition = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d newPosition = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        final World world = this.getEntityWorld();
        RayTraceResult result = world.rayTraceBlocks(oldPosition, newPosition, false, true, false);
        oldPosition = new Vec3d(this.posX, this.posY, this.posZ);
        if (result != null) {
            newPosition = new Vec3d(result.hitVec.x, result.hitVec.y, result.hitVec.z);
        }
        else {
            newPosition = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        }
        Entity hitEntity = null;
        final List<Entity> list = world.getEntitiesWithinAABBExcludingEntity((Entity)this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0));
        double distance = 0.0;
        for (final Entity entity : list) {
            if (entity.canBeCollidedWith()) {
                if (entity == this.owner && this.ticksInAir < 5) {
                    continue;
                }
                final AxisAlignedBB hitBox = entity.getEntityBoundingBox().grow(0.3);
                final RayTraceResult intercept = hitBox.calculateIntercept(oldPosition, newPosition);
                if (intercept == null) {
                    continue;
                }
                final double newDistance = oldPosition.distanceTo(intercept.hitVec);
                if (newDistance >= distance && distance != 0.0) {
                    continue;
                }
                hitEntity = entity;
                distance = newDistance;
            }
        }
        final RayTraceResult blockHit = result;
        if (hitEntity != null) {
            result = new RayTraceResult(hitEntity);
        }
        if (result != null && result.typeOfHit != RayTraceResult.Type.MISS && !world.isRemote) {
            if (this.explosive) {
                this.explode();
                this.setDead();
                return;
            }
            switch (result.typeOfHit) {
                case ENTITY: {
                    if (this.hitEntity(result.entityHit)) {
                        break;
                    }
                    if (blockHit == null) {
                        this.power -= 0.5f;
                        break;
                    }
                    result = blockHit;
                }
                case BLOCK: {
                    if (!this.hitBlock(result.getBlockPos(), result.sideHit)) {
                        this.power -= 0.5f;
                        break;
                    }
                    break;
                }
                default: {
                    throw new RuntimeException("invalid hit type: " + result.typeOfHit);
                }
            }
        }
        else {
            this.power -= 0.5f;
        }
        this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        this.range -= (float)Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        if (this.isInWater()) {
            this.setDead();
        }
    }
    
    private void explode() {
        final World world = this.getEntityWorld();
        final LaserEvent.LaserExplodesEvent event = new LaserEvent.LaserExplodesEvent(world, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, 5.0f, 0.85f, 0.55f);
        if (MinecraftForge.EVENT_BUS.post((Event)event)) {
            this.setDead();
            return;
        }
        this.copyDataFromEvent(event);
        final ExplosionIC2 explosion = new ExplosionIC2(world, this, this.posX, this.posY, this.posZ, event.explosionPower, event.explosionDropRate);
        explosion.doExplosion();
    }
    
    private boolean hitEntity(Entity entity) {
        final LaserEvent.LaserHitsEntityEvent event = new LaserEvent.LaserHitsEntityEvent(this.getEntityWorld(), this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, entity);
        if (!MinecraftForge.EVENT_BUS.post((Event)event)) {
            this.copyDataFromEvent(event);
            entity = event.hitEntity;
            final int damage = (int)this.power;
            if (damage > 0) {
                entity.setFire(damage * (this.smelt ? 2 : 1));
                if (entity.attackEntityFrom(new EntityDamageSourceIndirect("arrow", (Entity)this, (Entity)this.owner).setProjectile(), (float)damage) && ((this.owner instanceof EntityPlayer && entity instanceof EntityDragon && ((EntityDragon)entity).getHealth() <= 0.0f) || (entity instanceof MultiPartEntityPart && ((MultiPartEntityPart)entity).parent instanceof EntityDragon && ((EntityLivingBase)((MultiPartEntityPart)entity).parent).getHealth() <= 0.0f))) {
                    IC2.achievements.issueAchievement((EntityPlayer)this.owner, "killDragonMiningLaser");
                }
            }
            this.setDead();
            return true;
        }
        if (event.passThrough) {
            return false;
        }
        this.setDead();
        return true;
    }
    
    private boolean hitBlock(final BlockPos pos, final EnumFacing side) {
        final World world = this.getEntityWorld();
        final LaserEvent.LaserHitsBlockEvent event = new LaserEvent.LaserHitsBlockEvent(world, this, this.owner, this.range, this.power, this.blockBreaks, this.explosive, this.smelt, pos, side, 0.9f, true, true);
        if (MinecraftForge.EVENT_BUS.post((Event)event)) {
            this.setDead();
            return true;
        }
        this.copyDataFromEvent(event);
        final IBlockState state = world.getBlockState(event.pos);
        final Block block = state.getBlock();
        final EntityPlayer playerOwner = (this.owner instanceof EntityPlayer) ? this.owner : Ic2Player.get(world);
        if (MinecraftForge.EVENT_BUS.post((Event)new BlockEvent.BreakEvent(world, pos, state, playerOwner))) {
            this.setDead();
            return true;
        }
        if (block.isAir(state, (IBlockAccess)world, event.pos) || block == Blocks.GLASS || block == Blocks.GLASS_PANE || block == BlockName.glass.getInstance()) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        final float hardness = state.getBlockHardness(world, event.pos);
        if (hardness < 0.0f) {
            this.setDead();
            return true;
        }
        this.power -= hardness / 1.5f;
        if (this.power < 0.0f) {
            return true;
        }
        final List<ItemStack> replacements = new ArrayList<ItemStack>();
        if (state.getMaterial() == Material.TNT || state.getMaterial() == MaterialIC2TNT.instance) {
            block.onBlockDestroyedByExplosion(world, event.pos, new Explosion(world, (Entity)this, event.pos.getX() + 0.5, event.pos.getY() + 0.5, event.pos.getZ() + 0.5, 1.0f, false, true));
        }
        else if (this.smelt) {
            if (state.getMaterial() == Material.WOOD) {
                event.dropBlock = false;
            }
            else {
                for (final ItemStack isa : StackUtil.getDrops((IBlockAccess)world, event.pos, state, block, 0)) {
                    final ItemStack is = FurnaceRecipes.instance().getSmeltingResult(isa);
                    if (!StackUtil.isEmpty(is)) {
                        replacements.add(is);
                    }
                }
                event.dropBlock = replacements.isEmpty();
            }
        }
        if (event.removeBlock) {
            if (event.dropBlock) {
                block.dropBlockAsItemWithChance(world, event.pos, state, event.dropChance, 0);
            }
            world.setBlockToAir(event.pos);
            for (final ItemStack replacement : replacements) {
                if (!StackUtil.placeBlock(replacement, world, event.pos)) {
                    StackUtil.dropAsEntity(world, event.pos, replacement);
                }
                this.power = 0.0f;
            }
            if (world.rand.nextInt(10) == 0 && state.getMaterial().getCanBurn()) {
                world.setBlockState(event.pos, Blocks.FIRE.getDefaultState());
            }
        }
        --this.blockBreaks;
        return true;
    }
    
    public void writeEntityToNBT(final NBTTagCompound nbttagcompound) {
    }
    
    public void readEntityFromNBT(final NBTTagCompound nbttagcompound) {
    }
    
    void copyDataFromEvent(final LaserEvent event) {
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
    
    public void setThrower(final Entity entity) {
        if (entity instanceof EntityLivingBase) {
            this.owner = (EntityLivingBase)entity;
        }
    }
}

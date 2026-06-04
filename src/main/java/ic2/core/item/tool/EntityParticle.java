// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.StackUtil;
import net.minecraft.init.Blocks;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.RayTraceResult;
import java.util.ArrayList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.Quaternion;
import ic2.core.util.Util;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import ic2.core.util.Vector3;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraft.entity.Entity;

public class EntityParticle extends Entity implements IThrowableEntity
{
    private double coreSize;
    private double influenceSize;
    private int lifeTime;
    private Entity owner;
    private Vector3[] radialTestVectors;
    
    public EntityParticle(final World world) {
        super(world);
        this.noClip = true;
        this.lifeTime = 6000;
    }
    
    public EntityParticle(final World world, final EntityLivingBase owner1, final float speed, final double coreSize1, final double influenceSize1) {
        this(world);
        this.coreSize = coreSize1;
        this.influenceSize = influenceSize1;
        this.owner = (Entity)owner1;
        final Vector3 eyePos = Util.getEyePosition(this.owner);
        this.setPosition(eyePos.x, eyePos.y, eyePos.z);
        final Vector3 motion = new Vector3(owner1.getLookVec());
        final Vector3 ortho = motion.copy().cross(Vector3.UP).scaleTo(influenceSize1);
        final double stepAngle = Math.atan(0.5 / influenceSize1) * 2.0;
        final int steps = (int)Math.ceil(6.283185307179586 / stepAngle);
        final Quaternion q = new Quaternion().setFromAxisAngle(motion, stepAngle);
        (this.radialTestVectors = new Vector3[steps])[0] = ortho.copy();
        for (int i = 1; i < steps; ++i) {
            q.rotate(ortho);
            this.radialTestVectors[i] = ortho.copy();
        }
        motion.scale(speed);
        this.motionX = motion.x;
        this.motionY = motion.y;
        this.motionZ = motion.z;
    }
    
    protected void entityInit() {
    }
    
    protected void readEntityFromNBT(final NBTTagCompound nbttagcompound) {
    }
    
    protected void writeEntityToNBT(final NBTTagCompound nbttagcompound) {
    }
    
    public Entity getThrower() {
        return this.owner;
    }
    
    public void setThrower(final Entity entity) {
        this.owner = entity;
    }
    
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        final Vector3 start = new Vector3(this.prevPosX, this.prevPosY, this.prevPosZ);
        final Vector3 end = new Vector3(this.posX, this.posY, this.posZ);
        final World world = this.getEntityWorld();
        RayTraceResult hit = world.rayTraceBlocks(start.toVec3(), end.toVec3(), true);
        if (hit != null) {
            end.set(hit.hitVec);
            this.posX = hit.hitVec.x;
            this.posY = hit.hitVec.y;
            this.posZ = hit.hitVec.z;
        }
        final List<Entity> entitiesToCheck = world.getEntitiesWithinAABBExcludingEntity((Entity)this, new AxisAlignedBB(this.prevPosX, this.prevPosY, this.prevPosZ, this.posX, this.posY, this.posZ).grow(this.influenceSize));
        final List<RayTraceResult> entitiesInfluences = new ArrayList<RayTraceResult>();
        double minDistanceSq = start.distanceSquared(end);
        for (final Entity entity : entitiesToCheck) {
            if (entity != this.owner && entity.canBeCollidedWith()) {
                final RayTraceResult entityInfluence = entity.getEntityBoundingBox().grow(this.influenceSize).calculateIntercept(start.toVec3(), end.toVec3());
                if (entityInfluence == null) {
                    continue;
                }
                entitiesInfluences.add(entityInfluence);
                final RayTraceResult entityHit = entity.getEntityBoundingBox().grow(this.coreSize).calculateIntercept(start.toVec3(), end.toVec3());
                if (entityHit == null) {
                    continue;
                }
                final double distanceSq = start.distanceSquared(entityHit.hitVec);
                if (distanceSq >= minDistanceSq) {
                    continue;
                }
                hit = entityHit;
                minDistanceSq = distanceSq;
            }
        }
        final double maxInfluenceDistance = Math.sqrt(minDistanceSq) + this.influenceSize;
        for (final RayTraceResult entityInfluence2 : entitiesInfluences) {
            if (start.distance(entityInfluence2.hitVec) <= maxInfluenceDistance) {
                this.onInfluence(entityInfluence2);
            }
        }
        if (this.radialTestVectors != null) {
            final Vector3 vForward = end.copy().sub(start);
            final double len = vForward.length();
            vForward.scale(1.0 / len);
            final Vector3 origin = new Vector3(start);
            final Vector3 tmp = new Vector3();
            for (int d = 0; d < len; ++d) {
                for (final Vector3 radialTestVector : this.radialTestVectors) {
                    origin.copy(tmp).add(radialTestVector);
                    final RayTraceResult influence = world.rayTraceBlocks(origin.toVec3(), tmp.toVec3(), true);
                    if (influence != null) {
                        this.onInfluence(influence);
                    }
                }
                origin.add(vForward);
            }
        }
        if (hit != null) {
            this.onImpact(hit);
            this.setDead();
        }
        else {
            --this.lifeTime;
            if (this.lifeTime <= 0) {
                this.setDead();
            }
        }
    }
    
    protected void onImpact(final RayTraceResult hit) {
        if (!IC2.platform.isSimulating()) {
            return;
        }
        System.out.println("hit " + hit.typeOfHit + " " + hit.hitVec + " sim=" + IC2.platform.isSimulating());
        if (hit.typeOfHit != RayTraceResult.Type.BLOCK || IC2.platform.isSimulating()) {}
        final ExplosionIC2 explosion = new ExplosionIC2(this.getEntityWorld(), this.owner, hit.hitVec.x, hit.hitVec.y, hit.hitVec.z, 18.0f, 0.95f, ExplosionIC2.Type.Heat);
        explosion.doExplosion();
    }
    
    protected void onInfluence(final RayTraceResult hit) {
        if (!IC2.platform.isSimulating()) {
            return;
        }
        System.out.println("influenced " + hit.typeOfHit + " " + hit.hitVec + " sim=" + IC2.platform.isSimulating());
        if (hit.typeOfHit == RayTraceResult.Type.BLOCK && IC2.platform.isSimulating()) {
            final World world = this.getEntityWorld();
            final IBlockState state = world.getBlockState(hit.getBlockPos());
            final Block block = state.getBlock();
            if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                world.setBlockToAir(hit.getBlockPos());
            }
            else {
                final List<ItemStack> drops = StackUtil.getDrops((IBlockAccess)world, hit.getBlockPos(), state, null, 0, true);
                if (drops.size() == 1 && StackUtil.getSize(drops.get(0)) == 1) {
                    final ItemStack existing = drops.get(0);
                    final ItemStack smelted = FurnaceRecipes.instance().getSmeltingResult(existing);
                    if (smelted != null && smelted.getItem() instanceof ItemBlock) {
                        world.setBlockState(hit.getBlockPos(), ((ItemBlock)smelted.getItem()).getBlock().getDefaultState());
                    }
                    else if (block.isFlammable((IBlockAccess)world, hit.getBlockPos(), hit.sideHit)) {
                        world.setBlockState(hit.getBlockPos().offset(hit.sideHit.getOpposite()), Blocks.FIRE.getDefaultState());
                    }
                }
            }
        }
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.init.Items;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.SoundEvent;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import java.lang.reflect.Array;
import net.minecraft.entity.MoverType;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.core.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.entity.item.EntityBoat;

public abstract class EntityIC2Boat extends EntityBoat
{
    private static Method method_tickLerp;
    private static Field field_paddlePositions;
    private static Field field_previousStatus;
    private static Field field_status;
    private static Field field_outOfControlTicks;
    private static Field field_momentum;
    private static Field field_lastYd;
    private static Field field_waterLevel;
    private static Field field_boatGlide;
    private static Field field_deltaRotation;
    private static Field field_rightInputDown;
    private static Field field_leftInputDown;
    private static Field field_forwardInputDown;
    private static Field field_backInputDown;
    
    public static void init() {
        EntityIC2Boat.method_tickLerp = getMethod("tickLerp", "tickLerp", (Class<?>[])new Class[0]);
        EntityIC2Boat.field_paddlePositions = getField("paddlePositions", "paddlePositions");
        EntityIC2Boat.field_previousStatus = getField("previousStatus", "previousStatus");
        EntityIC2Boat.field_status = getField("status", "status");
        EntityIC2Boat.field_outOfControlTicks = getField("outOfControlTicks", "outOfControlTicks");
        EntityIC2Boat.field_momentum = getField("momentum", "momentum");
        EntityIC2Boat.field_lastYd = getField("lastYd", "lastYd");
        EntityIC2Boat.field_waterLevel = getField("waterLevel", "waterLevel");
        EntityIC2Boat.field_boatGlide = getField("boatGlide", "boatGlide");
        EntityIC2Boat.field_deltaRotation = getField("deltaRotation", "deltaRotation");
        EntityIC2Boat.field_rightInputDown = getField("rightInputDown", "rightInputDown");
        EntityIC2Boat.field_leftInputDown = getField("leftInputDown", "leftInputDown");
        EntityIC2Boat.field_forwardInputDown = getField("forwardInputDown", "forwardInputDown");
        EntityIC2Boat.field_backInputDown = getField("backInputDown", "backInputDown");
    }
    
    private static Field getField(final String deobfName, final String srgName) {
        return ReflectionUtil.getField(EntityBoat.class, srgName, deobfName);
    }
    
    private static Method getMethod(final String deobfName, final String srgName, final Class<?>... parameterTypes) {
        return ReflectionUtil.getMethod(EntityBoat.class, new String[] { srgName, deobfName }, parameterTypes);
    }
    
    public EntityIC2Boat(final World world) {
        super(world);
    }
    
    public void onUpdate() {
        final World world = this.getEntityWorld();
        try {
            EntityIC2Boat.field_previousStatus.set(this, EntityIC2Boat.field_status.get(this));
            final EntityBoat.Status status = this.getBoatStatus();
            EntityIC2Boat.field_status.set(this, status);
            if (status != EntityBoat.Status.UNDER_WATER && status != EntityBoat.Status.UNDER_FLOWING_WATER) {
                EntityIC2Boat.field_outOfControlTicks.setFloat(this, 0.0f);
            }
            else {
                EntityIC2Boat.field_outOfControlTicks.setFloat(this, EntityIC2Boat.field_outOfControlTicks.getFloat(this) + 1.0f);
            }
            if (!world.isRemote && EntityIC2Boat.field_outOfControlTicks.getFloat(this) >= 60.0f) {
                this.removePassengers();
            }
            if (this.getTimeSinceHit() > 0) {
                this.setTimeSinceHit(this.getTimeSinceHit() - 1);
            }
            if (this.getDamageTaken() > 0.0f) {
                this.setDamageTaken(this.getDamageTaken() - 1.0f);
            }
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.doEntityUpdate(world);
            EntityIC2Boat.method_tickLerp.invoke(this, new Object[0]);
            if (this.canPassengerSteer()) {
                if (this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof EntityPlayer)) {
                    this.setPaddleState(false, false);
                }
                this.updateMotion();
                if (world.isRemote) {
                    this.controlBoat();
                    world.sendPacketToServer((Packet)new CPacketSteerBoat(this.getPaddleState(0), this.getPaddleState(1)));
                }
                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            }
            else {
                this.motionX = 0.0;
                this.motionY = 0.0;
                this.motionZ = 0.0;
            }
            for (int i = 0; i <= 1; ++i) {
                if (this.getPaddleState(i)) {
                    final double paddlePosition = Array.getFloat(EntityIC2Boat.field_paddlePositions.get(this), i);
                    if (!this.isSilent() && paddlePosition % 6.283185307179586 <= 0.7853981633974483 && (paddlePosition + 0.4) % 6.283185307179586 >= 0.7853981633974483) {
                        final SoundEvent soundevent = this.getPaddleSound();
                        if (soundevent != null) {
                            final Vec3d look = this.getLook(1.0f);
                            world.playSound((EntityPlayer)null, this.posX + ((i == 1) ? (-look.z) : look.z), this.posY, this.posZ + ((i == 1) ? look.x : (-look.x)), soundevent, this.getSoundCategory(), 1.0f, 0.8f + 0.4f * this.rand.nextFloat());
                        }
                    }
                    Array.setFloat(EntityIC2Boat.field_paddlePositions.get(this), i, (float)(paddlePosition + 0.04));
                }
                else {
                    Array.setFloat(EntityIC2Boat.field_paddlePositions.get(this), i, 0.0f);
                }
            }
        }
        catch (final Exception e) {
            throw new RuntimeException("Error reflecting boat in update", e);
        }
        this.doBlockCollisions();
        final List<Entity> list = world.getEntitiesInAABBexcluding((Entity)this, this.getEntityBoundingBox().grow(0.2, -0.01, 0.2), EntitySelectors.getTeamCollisionPredicate((Entity)this));
        if (!list.isEmpty()) {
            final boolean flag = !world.isRemote && !(this.getControllingPassenger() instanceof EntityPlayer);
            for (final Entity entity : list) {
                if (!entity.isPassenger((Entity)this)) {
                    if (flag && this.getPassengers().size() < 2 && !entity.isRiding() && entity.width < this.width && entity instanceof EntityLivingBase && !(entity instanceof EntityWaterMob) && !(entity instanceof EntityPlayer)) {
                        entity.startRiding((Entity)this);
                    }
                    else {
                        this.applyEntityCollision(entity);
                    }
                }
            }
        }
    }
    
    private void doEntityUpdate(final World world) {
        if (!world.isRemote) {
            this.setFlag(6, this.isGlowing());
        }
        this.onEntityUpdate();
    }
    
    private void updateMotion() {
        double generalHeightChangingValue = this.hasNoGravity() ? 0.0 : -0.04;
        double heightChange = 0.0;
        float momentum = 0.05f;
        try {
            final EntityBoat.Status status = (EntityBoat.Status)EntityIC2Boat.field_status.get(this);
            if (EntityIC2Boat.field_previousStatus.get(this) == EntityBoat.Status.IN_AIR && status != EntityBoat.Status.IN_AIR && status != EntityBoat.Status.ON_LAND) {
                EntityIC2Boat.field_waterLevel.setDouble(this, this.getEntityBoundingBox().minY + this.height);
                this.setPosition(this.posX, this.getWaterLevelAbove() - this.height + 0.101, this.posZ);
                this.motionY = 0.0;
                EntityIC2Boat.field_lastYd.setDouble(this, 0.0);
                EntityIC2Boat.field_status.set(this, EntityBoat.Status.IN_WATER);
            }
            else {
                switch (status) {
                    case IN_AIR: {
                        momentum = 0.9f;
                        break;
                    }
                    case IN_WATER: {
                        heightChange = (EntityIC2Boat.field_waterLevel.getDouble(this) - this.getEntityBoundingBox().minY) / this.height;
                        momentum = 0.9f;
                        break;
                    }
                    case ON_LAND: {
                        momentum = EntityIC2Boat.field_boatGlide.getFloat(this);
                        if (this.getControllingPassenger() instanceof EntityPlayer) {
                            EntityIC2Boat.field_boatGlide.setFloat(this, momentum / 2.0f);
                            break;
                        }
                        break;
                    }
                    case UNDER_FLOWING_WATER: {
                        generalHeightChangingValue = -7.0E-4;
                        momentum = 0.9f;
                        break;
                    }
                    case UNDER_WATER: {
                        heightChange = 0.01;
                        momentum = 0.45f;
                        break;
                    }
                }
                this.motionX *= momentum;
                this.motionZ *= momentum;
                EntityIC2Boat.field_deltaRotation.setFloat(this, EntityIC2Boat.field_deltaRotation.getFloat(this) * momentum);
                this.motionY += generalHeightChangingValue;
                if (heightChange > 0.0) {
                    this.motionY += heightChange * 0.061538461538461535;
                    this.motionY *= 0.75;
                }
            }
            EntityIC2Boat.field_momentum.setFloat(this, momentum);
        }
        catch (final Exception e) {
            throw new RuntimeException("Error reflecting boat in updateMotion", e);
        }
    }
    
    public float getWaterLevelAbove() {
        final AxisAlignedBB boundingBox = this.getEntityBoundingBox();
        final int minX = (int)Math.floor(boundingBox.minX);
        final int maxX = (int)Math.ceil(boundingBox.maxX);
        final int minZ = (int)Math.floor(boundingBox.minZ);
        final int maxZ = (int)Math.ceil(boundingBox.maxZ);
        final BlockPos.PooledMutableBlockPos blockPosPool = BlockPos.PooledMutableBlockPos.retain();
        try {
            final World world = this.getEntityWorld();
            final int maxY = (int)Math.ceil(boundingBox.maxY - EntityIC2Boat.field_lastYd.getDouble(this));
            int y = (int)Math.floor(boundingBox.maxY);
        Label_0206_Outer:
            while (y < maxY) {
                float waterHeight = 0.0f;
                int x = minX;
            Label_0206:
                while (true) {
                    while (x < maxX) {
                        for (int z = minZ; z < maxZ; ++z) {
                            blockPosPool.setPos(x, y, z);
                            final IBlockState block = world.getBlockState((BlockPos)blockPosPool);
                            if (this.isWater(block)) {
                                waterHeight = Math.max(waterHeight, getBlockLiquidHeight(block, (IBlockAccess)world, (BlockPos)blockPosPool));
                            }
                            if (waterHeight >= 1.0f) {
                                break Label_0206;
                            }
                        }
                        ++x;
                        continue Label_0206_Outer;
                        ++y;
                        continue Label_0206_Outer;
                    }
                    if (waterHeight < 1.0f) {
                        return blockPosPool.getY() + waterHeight;
                    }
                    continue Label_0206;
                }
            }
            return (float)(maxY + 1);
        }
        catch (final Exception e) {
            throw new RuntimeException("Error reflecting boat in getWaterLevelAbove", e);
        }
        finally {
            blockPosPool.release();
        }
    }
    
    private EntityBoat.Status getBoatStatus() {
        final EntityBoat.Status isUnderWater = this.getUnderwaterStatus();
        try {
            if (isUnderWater != null) {
                EntityIC2Boat.field_waterLevel.setDouble(this, this.getEntityBoundingBox().maxY);
                return isUnderWater;
            }
            if (this.checkInWater()) {
                return EntityBoat.Status.IN_WATER;
            }
            final float glideSpeed = this.getBoatGlide();
            if (glideSpeed > 0.0f) {
                EntityIC2Boat.field_boatGlide.setFloat(this, glideSpeed);
                return EntityBoat.Status.ON_LAND;
            }
            return EntityBoat.Status.IN_AIR;
        }
        catch (final Exception e) {
            throw new RuntimeException("Error reflecting boat in getBoatStatus", e);
        }
    }
    
    private boolean checkInWater() {
        final World world = this.getEntityWorld();
        final AxisAlignedBB boundingBox = this.getEntityBoundingBox();
        boolean isInWater = false;
        final BlockPos.PooledMutableBlockPos blockPosPool = BlockPos.PooledMutableBlockPos.retain();
        try {
            double waterLevel = Double.MIN_VALUE;
            for (int x = (int)Math.floor(boundingBox.minX); x < Math.ceil(boundingBox.maxX); ++x) {
                for (int y = (int)Math.floor(boundingBox.minY); y < Math.ceil(boundingBox.minY + 0.001); ++y) {
                    for (int z = (int)Math.floor(boundingBox.minZ); z < Math.ceil(boundingBox.maxZ); ++z) {
                        blockPosPool.setPos(x, y, z);
                        final IBlockState block = world.getBlockState((BlockPos)blockPosPool);
                        if (this.isWater(block)) {
                            final float waterHeight = getLiquidHeight(block, (IBlockAccess)world, (BlockPos)blockPosPool);
                            waterLevel = Math.max(waterHeight, waterLevel);
                            isInWater |= (boundingBox.minY < waterHeight);
                        }
                    }
                }
            }
            EntityIC2Boat.field_waterLevel.setDouble(this, waterLevel);
        }
        catch (final Exception e) {
            throw new RuntimeException("Error reflecting boat in checkInWater", e);
        }
        finally {
            blockPosPool.release();
        }
        return isInWater;
    }
    
    @Nullable
    private EntityBoat.Status getUnderwaterStatus() {
        final World world = this.getEntityWorld();
        final AxisAlignedBB boundingBox = this.getEntityBoundingBox();
        final double boatTop = boundingBox.maxY + 0.001;
        final BlockPos.PooledMutableBlockPos blockPosPool = BlockPos.PooledMutableBlockPos.retain();
        try {
            for (int x = (int)Math.floor(boundingBox.minX); x < Math.ceil(boundingBox.maxX); ++x) {
                for (int y = (int)Math.floor(boundingBox.maxY); y < Math.ceil(boatTop); ++y) {
                    for (int z = (int)Math.floor(boundingBox.minZ); z < Math.ceil(boundingBox.maxZ); ++z) {
                        blockPosPool.setPos(x, y, z);
                        final IBlockState block = world.getBlockState((BlockPos)blockPosPool);
                        if (this.isWater(block) && boatTop < getLiquidHeight(block, (IBlockAccess)world, (BlockPos)blockPosPool)) {
                            return ((int)block.getValue((IProperty)BlockLiquid.LEVEL) != 0) ? EntityBoat.Status.UNDER_FLOWING_WATER : EntityBoat.Status.UNDER_WATER;
                        }
                    }
                }
            }
        }
        finally {
            blockPosPool.release();
        }
        return null;
    }
    
    public static float getLiquidHeight(final IBlockState block, final IBlockAccess world, final BlockPos pos) {
        return pos.getY() + getBlockLiquidHeight(block, world, pos);
    }
    
    public static float getBlockLiquidHeight(final IBlockState block, final IBlockAccess world, final BlockPos pos) {
        final int liquidHeight = (int)block.getValue((IProperty)BlockLiquid.LEVEL);
        return ((liquidHeight & 0x7) == 0x0 && world.getBlockState(pos.up()).getMaterial() == block.getMaterial()) ? 1.0f : (1.0f - BlockLiquid.getLiquidHeightPercent(liquidHeight));
    }
    
    private void controlBoat() {
        if (this.isBeingRidden()) {
            float speed = 0.0f;
            try {
                final boolean left = EntityIC2Boat.field_leftInputDown.getBoolean(this);
                final boolean right = EntityIC2Boat.field_rightInputDown.getBoolean(this);
                final boolean forward = EntityIC2Boat.field_forwardInputDown.getBoolean(this);
                final boolean backward = EntityIC2Boat.field_backInputDown.getBoolean(this);
                if (left) {
                    EntityIC2Boat.field_deltaRotation.setFloat(this, EntityIC2Boat.field_deltaRotation.getFloat(this) - 1.0f);
                }
                if (right) {
                    EntityIC2Boat.field_deltaRotation.setFloat(this, EntityIC2Boat.field_deltaRotation.getFloat(this) + 1.0f);
                }
                if (right != left && !forward && !backward) {
                    speed += 0.005f;
                }
                this.rotationYaw += EntityIC2Boat.field_deltaRotation.getFloat(this);
                if (forward) {
                    speed += 0.04f;
                }
                if (backward) {
                    speed -= 0.005f;
                }
                this.motionX += MathHelper.sin(-this.rotationYaw * 3.1415927f / 180.0f) * speed * this.getAccelerationFactor();
                this.motionZ += MathHelper.cos(this.rotationYaw * 3.1415927f / 180.0f) * speed * this.getAccelerationFactor();
                this.setPaddleState((right && !left) || forward, (left && !right) || forward);
            }
            catch (final Exception e) {
                throw new RuntimeException("Error reflecting boat in controlBoat", e);
            }
        }
    }
    
    protected void updateFallState(final double y, final boolean onGround, final IBlockState state, final BlockPos pos) {
        final boolean expectDeath = this.fallDistance > 3.0f && !this.isDead;
        super.updateFallState(y, onGround, state, pos);
        if (expectDeath && this.isDead && this.getEntityWorld().getGameRules().getBoolean("doEntityDrops")) {
            super.entityDropItem(this.getBrokenItem(), 0.0f);
        }
    }
    
    public EntityItem entityDropItem(final ItemStack stack, final float offsetY) {
        if (stack.getItem() == Items.BOAT) {
            return super.entityDropItem(this.getItem(), offsetY);
        }
        return null;
    }
    
    public ItemStack getPickedResult(final RayTraceResult target) {
        return this.getItem();
    }
    
    protected abstract ItemStack getItem();
    
    protected ItemStack getBrokenItem() {
        return this.getItem();
    }
    
    public abstract String getTexture();
    
    protected double getAccelerationFactor() {
        return 1.0;
    }
    
    protected double getTopSpeed() {
        return 0.35;
    }
    
    protected boolean isWater(final IBlockState block) {
        return block.getMaterial() == Material.WATER;
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.beam;

import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;

public class EntityParticle extends Entity
{
    private static final double initialVelocity = 0.5;
    private static final double slowdown = 0.99;
    
    public EntityParticle(final World world) {
        super(world);
        this.noClip = true;
    }
    
    public EntityParticle(final TileEmitter emitter) {
        this(emitter.getWorld());
        final EnumFacing dir = emitter.getFacing();
        final double x = emitter.getPos().getX() + 0.5 + dir.getFrontOffsetX() * 0.5;
        final double y = emitter.getPos().getY() + 0.5 + dir.getFrontOffsetY() * 0.5;
        final double z = emitter.getPos().getZ() + 0.5 + dir.getFrontOffsetZ() * 0.5;
        this.setPosition(x, y, z);
        this.motionX = dir.getFrontOffsetX() * 0.5;
        this.motionY = dir.getFrontOffsetY() * 0.5;
        this.motionZ = dir.getFrontOffsetZ() * 0.5;
        this.setSize(0.2f, 0.2f);
    }
    
    protected void entityInit() {
    }
    
    protected void readEntityFromNBT(final NBTTagCompound nbttagcompound) {
    }
    
    protected void writeEntityToNBT(final NBTTagCompound nbttagcompound) {
    }
    
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.99;
        this.motionY *= 0.99;
        this.motionZ *= 0.99;
        if (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 1.0E-4) {
            this.setDead();
        }
    }
}

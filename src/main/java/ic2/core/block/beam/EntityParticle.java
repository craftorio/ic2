package ic2.core.block.beam;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class EntityParticle extends Entity
{
	private static final double initialVelocity = 0.5D;

	private static final double slowdown = 0.99D;

	public EntityParticle(World world)
	{
		super(world);
		this.noClip = true;
	}

	public EntityParticle(TileEmitter emitter)
	{
		this(emitter.getWorld());
		EnumFacing dir = emitter.getFacing();
		double x = emitter.getPos().getX() + 0.5D + dir.getFrontOffsetX() * 0.5D;
		double y = emitter.getPos().getY() + 0.5D + dir.getFrontOffsetY() * 0.5D;
		double z = emitter.getPos().getZ() + 0.5D + dir.getFrontOffsetZ() * 0.5D;
		setPosition(x, y, z);
		this.motionX = dir.getFrontOffsetX() * 0.5D;
		this.motionY = dir.getFrontOffsetY() * 0.5D;
		this.motionZ = dir.getFrontOffsetZ() * 0.5D;
		setSize(0.2F, 0.2F);
	}

	protected void entityInit()
	{
	}

	protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
	}

	protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
	}

	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.99D;
		this.motionY *= 0.99D;
		this.motionZ *= 0.99D;
		if (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 1.0E-4D)
			setDead();
	}
}

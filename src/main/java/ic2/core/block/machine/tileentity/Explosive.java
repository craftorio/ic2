package ic2.core.block.machine.tileentity;

import ic2.core.block.EntityIC2Explosive;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.util.StackUtil;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public abstract class Explosive extends TileEntityInventory implements Redstone.IRedstoneChangeHandler
{
	protected final Redstone redstone;

	private boolean exploded;

	protected Explosive()
	{
		this.redstone = (Redstone) addComponent((TileEntityComponent) new Redstone(this));
		this.redstone.subscribe(this);
	}

	protected SoundType getBlockSound(Entity entity)
	{
		return SoundType.PLANT;
	}

	public void onRedstoneChange(int newLevel)
	{
		if (newLevel > 0)
			explode(null, false);
	}

	protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (StackUtil.consume(player, hand, StackUtil.sameItem(Items.FIRE_CHARGE), 1) ||
			StackUtil.damage(player, hand, StackUtil.sameItem(Items.FLINT_AND_STEEL), 1))
		{
			explode(player, false);
			return true;
		}
		return super.onActivated(player, hand, side, hitX, hitY, hitZ);
	}

	protected void onExploded(Explosion explosion)
	{
		super.onExploded(explosion);
		explode(explosion.getExplosivePlacedBy(), true);
	}

	protected boolean onRemovedByPlayer(EntityPlayer player, boolean willHarvest)
	{
		if (explodeOnRemoval())
		{
			explode(player, false);
			return true;
		}
		return super.onRemovedByPlayer(player, willHarvest);
	}

	protected void onEntityCollision(Entity entity)
	{
		if (!(getWorld()).isRemote && entity instanceof EntityArrow && entity.isBurning())
		{
			EntityArrow arrow = (EntityArrow) entity;
			explode((arrow.shootingEntity instanceof EntityLivingBase) ? (EntityLivingBase) arrow.shootingEntity : null, false);
		}
	}

	protected ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		if (this.exploded)
			return null;
		return super.adjustDrop(drop, wrench);
	}

	protected boolean explode(EntityLivingBase igniter, boolean shortFuse)
	{
		EntityIC2Explosive entity = getEntity(igniter);
		if (entity == null)
			return false;
		World world = getWorld();
		if (world.isRemote)
			return true;
		entity.setIgniter(igniter);
		onIgnite(igniter);
		world.setBlockToAir(this.pos);
		if (shortFuse)
			entity.fuse = world.rand.nextInt(Math.max(1, entity.fuse / 4)) + entity.fuse / 8;
		world.spawnEntity(entity);
		world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
		this.exploded = true;
		return true;
	}

	protected boolean explodeOnRemoval()
	{
		return false;
	}

	protected abstract EntityIC2Explosive getEntity(EntityLivingBase paramEntityLivingBase);

	protected void onIgnite(EntityLivingBase igniter)
	{
	}
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraft.world.World;
import ic2.core.block.EntityIC2Explosive;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.world.Explosion;
import ic2.core.util.StackUtil;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Redstone;
import ic2.core.block.TileEntityInventory;

public abstract class Explosive extends TileEntityInventory implements Redstone.IRedstoneChangeHandler
{
    protected final Redstone redstone;
    private boolean exploded;
    
    protected Explosive() {
        (this.redstone = this.addComponent(new Redstone(this))).subscribe(this);
    }
    
    @Override
    protected SoundType getBlockSound(final Entity entity) {
        return SoundType.PLANT;
    }
    
    @Override
    public void onRedstoneChange(final int newLevel) {
        if (newLevel > 0) {
            this.explode(null, false);
        }
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (StackUtil.consume(player, hand, StackUtil.sameItem(Items.FIRE_CHARGE), 1) || StackUtil.damage(player, hand, StackUtil.sameItem(Items.FLINT_AND_STEEL), 1)) {
            this.explode((EntityLivingBase)player, false);
            return true;
        }
        return super.onActivated(player, hand, side, hitX, hitY, hitZ);
    }
    
    @Override
    protected void onExploded(final Explosion explosion) {
        super.onExploded(explosion);
        this.explode(explosion.getExplosivePlacedBy(), true);
    }
    
    @Override
    protected boolean onRemovedByPlayer(final EntityPlayer player, final boolean willHarvest) {
        if (this.explodeOnRemoval()) {
            this.explode((EntityLivingBase)player, false);
            return true;
        }
        return super.onRemovedByPlayer(player, willHarvest);
    }
    
    @Override
    protected void onEntityCollision(final Entity entity) {
        if (!this.getWorld().isRemote && entity instanceof EntityArrow && entity.isBurning()) {
            final EntityArrow arrow = (EntityArrow)entity;
            this.explode((arrow.shootingEntity instanceof EntityLivingBase) ? arrow.shootingEntity : null, false);
        }
    }
    
    @Override
    protected ItemStack adjustDrop(final ItemStack drop, final boolean wrench) {
        if (this.exploded) {
            return null;
        }
        return super.adjustDrop(drop, wrench);
    }
    
    protected boolean explode(final EntityLivingBase igniter, final boolean shortFuse) {
        final EntityIC2Explosive entity = this.getEntity(igniter);
        if (entity == null) {
            return false;
        }
        final World world = this.getWorld();
        if (world.isRemote) {
            return true;
        }
        entity.setIgniter(igniter);
        this.onIgnite(igniter);
        world.setBlockToAir(this.pos);
        if (shortFuse) {
            entity.fuse = world.rand.nextInt(Math.max(1, entity.fuse / 4)) + entity.fuse / 8;
        }
        world.spawnEntity((Entity)entity);
        world.playSound((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
        return this.exploded = true;
    }
    
    protected boolean explodeOnRemoval() {
        return false;
    }
    
    protected abstract EntityIC2Explosive getEntity(final EntityLivingBase p0);
    
    protected void onIgnite(final EntityLivingBase igniter) {
    }
}

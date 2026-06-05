package ic2.core.block.machine.tileentity;

import ic2.core.block.EntityIC2Explosive;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Redstone;
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

public abstract class Explosive extends TileEntityInventory implements Redstone.IRedstoneChangeHandler {
   protected final Redstone redstone = this.addComponent(new Redstone(this));
   private boolean exploded;

   protected Explosive() {
      this.redstone.subscribe(this);
   }

   @Override
   protected SoundType getBlockSound(Entity entity) {
      return SoundType.PLANT;
   }

   @Override
   public void onRedstoneChange(int newLevel) {
      if (newLevel > 0) {
         this.explode(null, false);
      }
   }

   @Override
   protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
      if (!StackUtil.consume(player, hand, StackUtil.sameItem(Items.FIRE_CHARGE), 1)
         && !StackUtil.damage(player, hand, StackUtil.sameItem(Items.FLINT_AND_STEEL), 1)) {
         return super.onActivated(player, hand, side, hitX, hitY, hitZ);
      }

      this.explode(player, false);
      return true;
   }

   @Override
   protected void onExploded(Explosion explosion) {
      super.onExploded(explosion);
      this.explode(explosion.getExplosivePlacedBy(), true);
   }

   @Override
   protected boolean onRemovedByPlayer(EntityPlayer player, boolean willHarvest) {
      if (this.explodeOnRemoval()) {
         this.explode(player, false);
         return true;
      } else {
         return super.onRemovedByPlayer(player, willHarvest);
      }
   }

   @Override
   protected void onEntityCollision(Entity entity) {
      if (!this.getWorld().isRemote && entity instanceof EntityArrow && entity.isBurning()) {
         EntityArrow arrow = (EntityArrow)entity;
         this.explode(arrow.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase)arrow.shootingEntity : null, false);
      }
   }

   @Override
   protected ItemStack adjustDrop(ItemStack drop, boolean wrench) {
      return this.exploded ? null : super.adjustDrop(drop, wrench);
   }

   protected boolean explode(EntityLivingBase igniter, boolean shortFuse) {
      EntityIC2Explosive entity = this.getEntity(igniter);
      if (entity == null) {
         return false;
      }

      World world = this.getWorld();
      if (world.isRemote) {
         return true;
      }

      entity.setIgniter(igniter);
      this.onIgnite(igniter);
      world.setBlockToAir(this.pos);
      if (shortFuse) {
         entity.fuse = world.rand.nextInt(Math.max(1, entity.fuse / 4)) + entity.fuse / 8;
      }

      world.spawnEntity(entity);
      world.playSound(
         (EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F
      );
      this.exploded = true;
      return true;
   }

   protected boolean explodeOnRemoval() {
      return false;
   }

   protected abstract EntityIC2Explosive getEntity(EntityLivingBase var1);

   protected void onIgnite(EntityLivingBase igniter) {
   }
}

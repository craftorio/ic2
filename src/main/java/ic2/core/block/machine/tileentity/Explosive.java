package ic2.core.block.machine.tileentity;

import ic2.core.block.EntityIC2Explosive;
import ic2.core.block.TileEntityBlock;
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

public abstract class Explosive extends TileEntityInventory implements Redstone.IRedstoneChangeHandler {
  protected final Redstone redstone;
  
  private boolean exploded;
  
  protected Explosive() {
    this.redstone = (Redstone)addComponent((TileEntityComponent)new Redstone((TileEntityBlock)this));
    this.redstone.subscribe(this);
  }
  
  protected SoundType getBlockSound(Entity entity) {
    return SoundType.field_185850_c;
  }
  
  public void onRedstoneChange(int newLevel) {
    if (newLevel > 0)
      explode((EntityLivingBase)null, false); 
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (StackUtil.consume(player, hand, StackUtil.sameItem(Items.field_151059_bz), 1) || 
      StackUtil.damage(player, hand, StackUtil.sameItem(Items.field_151033_d), 1)) {
      explode((EntityLivingBase)player, false);
      return true;
    } 
    return super.onActivated(player, hand, side, hitX, hitY, hitZ);
  }
  
  protected void onExploded(Explosion explosion) {
    super.onExploded(explosion);
    explode(explosion.func_94613_c(), true);
  }
  
  protected boolean onRemovedByPlayer(EntityPlayer player, boolean willHarvest) {
    if (explodeOnRemoval()) {
      explode((EntityLivingBase)player, false);
      return true;
    } 
    return super.onRemovedByPlayer(player, willHarvest);
  }
  
  protected void onEntityCollision(Entity entity) {
    if (!(getWorld()).isRemote && entity instanceof EntityArrow && entity.func_70027_ad()) {
      EntityArrow arrow = (EntityArrow)entity;
      explode((arrow.field_70250_c instanceof EntityLivingBase) ? (EntityLivingBase)arrow.field_70250_c : null, false);
    } 
  }
  
  protected ItemStack adjustDrop(ItemStack drop, boolean wrench) {
    if (this.exploded)
      return null; 
    return super.adjustDrop(drop, wrench);
  }
  
  protected boolean explode(EntityLivingBase igniter, boolean shortFuse) {
    EntityIC2Explosive entity = getEntity(igniter);
    if (entity == null)
      return false; 
    World world = getWorld();
    if (world.isRemote)
      return true; 
    entity.setIgniter(igniter);
    onIgnite(igniter);
    world.func_175698_g(this.field_174879_c);
    if (shortFuse)
      entity.fuse = world.field_73012_v.nextInt(Math.max(1, entity.fuse / 4)) + entity.fuse / 8; 
    world.func_72838_d((Entity)entity);
    world.func_184148_a((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, SoundEvents.field_187904_gd, SoundCategory.BLOCKS, 1.0F, 1.0F);
    this.exploded = true;
    return true;
  }
  
  protected boolean explodeOnRemoval() {
    return false;
  }
  
  protected abstract EntityIC2Explosive getEntity(EntityLivingBase paramEntityLivingBase);
  
  protected void onIgnite(EntityLivingBase igniter) {}
}

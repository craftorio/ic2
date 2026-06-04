package ic2.core.item;

import ic2.core.block.state.IIdProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Vector3;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemIC2Boat extends ItemMulti<ItemIC2Boat.BoatType> {
  public ItemIC2Boat() {
    super(ItemName.boat, BoatType.class);
  }
  
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    EntityIC2Boat boat = makeBoat(stack, world, player);
    if (boat == null)
      return new ActionResult(EnumActionResult.PASS, stack); 
    Vector3 lookVec = Util.getLookScaled((Entity)player);
    Vector3 start = Util.getEyePosition((Entity)player);
    Vec3d startMc = start.toVec3();
    RayTraceResult hitPos = world.rayTraceBlocks(startMc, start.add(lookVec).toVec3(), true);
    if (hitPos == null)
      return new ActionResult(EnumActionResult.PASS, stack); 
    boolean inEntity = false;
    float border = 1.0F;
    List<Entity> list = world.getEntitiesWithinAABBExcludingEntity((Entity)player, player
        .getEntityBoundingBox()
        .expand(lookVec.x, lookVec.y, lookVec.z)
        .grow(border));
    for (Entity entity : list) {
      if (entity.canBeCollidedWith()) {
        border = entity.getCollisionBorderSize();
        AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(border);
        if (aabb.contains(startMc)) {
          inEntity = true;
          break;
        } 
      } 
    } 
    if (inEntity)
      return new ActionResult(EnumActionResult.PASS, stack); 
    if (hitPos.typeOfHit == RayTraceResult.Type.BLOCK) {
      BlockPos pos = hitPos.getBlockPos();
      if (world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER)
        pos = pos.down(); 
      boat.setPosition(pos.getX() + 0.5D, (pos.getY() + 1), pos.getZ() + 0.5D);
      boat.rotationYaw = (((MathHelper.floor((player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 0x3) - 1) * 90);
      if (!world.getCollisionBoxes((Entity)boat, boat.getCollisionBoundingBox().expand(-0.1D, -0.1D, -0.1D)).isEmpty())
        return new ActionResult(EnumActionResult.PASS, stack); 
      if (!world.isRemote)
        world.spawnEntity((Entity)boat); 
      if (!player.capabilities.isCreativeMode)
        stack = StackUtil.decSize(stack); 
    } 
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  protected EntityIC2Boat makeBoat(ItemStack stack, World world, EntityPlayer player) {
    BoatType type = getType(stack);
    if (type == null)
      return null; 
    switch (type) {
      case carbon:
        return new EntityBoatCarbon(world);
      case rubber:
        return new EntityBoatRubber(world);
      case electric:
        return new EntityBoatElectric(world);
    } 
    return null;
  }
  
  public boolean hasCustomEntity(ItemStack stack) {
    return (getType(stack) == BoatType.electric);
  }
  
  public Entity createEntity(World world, Entity location, ItemStack stack) {
    assert hasCustomEntity(stack);
    assert !world.isRemote;
    EntityItem item = new FireproofItem(world, location.posX, location.posY, location.posZ, stack);
    item.setDefaultPickupDelay();
    item.motionX = location.motionX;
    item.motionY = location.motionY;
    item.motionZ = location.motionZ;
    return (Entity)item;
  }
  
  public enum BoatType implements IIdProvider {
    broken_rubber, rubber, carbon, electric;
    
    public String getName() {
      return name();
    }
    
    public int getId() {
      return ordinal();
    }
  }
  
  public static class FireproofItem extends EntityItem {
    public FireproofItem(World world, double x, double y, double z, ItemStack stack) {
      super(world, x, y, z, stack);
      this.isImmuneToFire = true;
    }
    
    public FireproofItem(World world, double x, double y, double z) {
      super(world, x, y, z);
      this.isImmuneToFire = true;
    }
    
    public FireproofItem(World world) {
      super(world);
      this.isImmuneToFire = true;
    }
    
    public void onUpdate() {
      super.onUpdate();
      extinguish();
    }
    
    protected void dealFireDamage(int amount) {}
    
    public void setFire(int seconds) {
      extinguish();
    }
  }
}

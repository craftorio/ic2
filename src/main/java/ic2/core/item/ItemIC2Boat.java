package ic2.core.item;

import ic2.core.block.state.IIdProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Vector3;
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
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

public class ItemIC2Boat extends ItemMulti<ItemIC2Boat.BoatType> {
   public ItemIC2Boat() {
      super(ItemName.boat, ItemIC2Boat.BoatType.class);
   }

   @Override
   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      EntityIC2Boat boat = this.makeBoat(stack, world, player);
      if (boat == null) {
         return new ActionResult(EnumActionResult.PASS, stack);
      }

      Vector3 lookVec = Util.getLookScaled(player);
      Vector3 start = Util.getEyePosition(player);
      Vec3d startMc = start.toVec3();
      RayTraceResult hitPos = world.rayTraceBlocks(startMc, start.add(lookVec).toVec3(), true);
      if (hitPos == null) {
         return new ActionResult(EnumActionResult.PASS, stack);
      }

      boolean inEntity = false;
      float border = 1.0F;

      for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(lookVec.x, lookVec.y, lookVec.z).grow(border))) {
         if (entity.canBeCollidedWith()) {
            border = entity.getCollisionBorderSize();
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(border);
            if (aabb.contains(startMc)) {
               inEntity = true;
               break;
            }
         }
      }

      if (inEntity) {
         return new ActionResult(EnumActionResult.PASS, stack);
      }

      if (hitPos.typeOfHit == Type.BLOCK) {
         BlockPos pos = hitPos.getBlockPos();
         if (world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER) {
            pos = pos.down();
         }

         boat.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
         boat.rotationYaw = ((MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5) & 3) - 1) * 90;
         if (!world.getCollisionBoxes(boat, boat.getCollisionBoundingBox().expand(-0.1, -0.1, -0.1)).isEmpty()) {
            return new ActionResult(EnumActionResult.PASS, stack);
         }

         if (!world.isRemote) {
            world.spawnEntity(boat);
         }

         if (!player.capabilities.isCreativeMode) {
            stack = StackUtil.decSize(stack);
         }
      }

      return new ActionResult(EnumActionResult.SUCCESS, stack);
   }

   protected EntityIC2Boat makeBoat(ItemStack stack, World world, EntityPlayer player) {
      ItemIC2Boat.BoatType type = this.getType(stack);
      if (type == null) {
         return null;
      }

      switch (type) {
         case carbon:
            return new EntityBoatCarbon(world);
         case rubber:
            return new EntityBoatRubber(world);
         case electric:
            return new EntityBoatElectric(world);
         default:
            return null;
      }
   }

   public boolean hasCustomEntity(ItemStack stack) {
      return this.getType(stack) == ItemIC2Boat.BoatType.electric;
   }

   public Entity createEntity(World world, Entity location, ItemStack stack) {
      assert this.hasCustomEntity(stack);
      assert !world.isRemote;
      EntityItem item = new ItemIC2Boat.FireproofItem(world, location.posX, location.posY, location.posZ, stack);
      item.setDefaultPickupDelay();
      item.motionX = location.motionX;
      item.motionY = location.motionY;
      item.motionZ = location.motionZ;
      return item;
   }

   public enum BoatType implements IIdProvider {
      broken_rubber,
      rubber,
      carbon,
      electric;

      @Override
      public String getName() {
         return this.name();
      }

      @Override
      public int getId() {
         return this.ordinal();
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
         this.extinguish();
      }

      protected void dealFireDamage(int amount) {
      }

      public void setFire(int seconds) {
         this.extinguish();
      }
   }
}

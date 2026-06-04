// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.block.state.IIdProvider;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.Vec3d;
import ic2.core.util.Vector3;
import net.minecraft.util.math.MathHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.Entity;
import ic2.core.util.Util;
import net.minecraft.util.EnumActionResult;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.core.ref.ItemName;

public class ItemIC2Boat extends ItemMulti<BoatType>
{
    public ItemIC2Boat() {
        super(ItemName.boat, BoatType.class);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        final EntityIC2Boat boat = this.makeBoat(stack, world, player);
        if (boat == null) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        final Vector3 lookVec = Util.getLookScaled((Entity)player);
        final Vector3 start = Util.getEyePosition((Entity)player);
        final Vec3d startMc = start.toVec3();
        final RayTraceResult hitPos = world.rayTraceBlocks(startMc, start.add(lookVec).toVec3(), true);
        if (hitPos == null) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        boolean inEntity = false;
        float border = 1.0f;
        final List<Entity> list = world.getEntitiesWithinAABBExcludingEntity((Entity)player, player.getEntityBoundingBox().expand(lookVec.x, lookVec.y, lookVec.z).grow((double)border));
        for (final Entity entity : list) {
            if (entity.canBeCollidedWith()) {
                border = entity.getCollisionBorderSize();
                final AxisAlignedBB aabb = entity.getEntityBoundingBox().grow((double)border);
                if (aabb.contains(startMc)) {
                    inEntity = true;
                    break;
                }
                continue;
            }
        }
        if (inEntity) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        if (hitPos.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = hitPos.getBlockPos();
            if (world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER) {
                pos = pos.down();
            }
            boat.setPosition(pos.getX() + 0.5, (double)(pos.getY() + 1), pos.getZ() + 0.5);
            boat.rotationYaw = (float)(((MathHelper.floor(player.rotationYaw * 4.0f / 360.0f + 0.5) & 0x3) - 1) * 90);
            if (!world.getCollisionBoxes((Entity)boat, boat.getCollisionBoundingBox().expand(-0.1, -0.1, -0.1)).isEmpty()) {
                return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
            }
            if (!world.isRemote) {
                world.spawnEntity((Entity)boat);
            }
            if (!player.capabilities.isCreativeMode) {
                stack = StackUtil.decSize(stack);
            }
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    protected EntityIC2Boat makeBoat(final ItemStack stack, final World world, final EntityPlayer player) {
        final BoatType type = this.getType(stack);
        if (type == null) {
            return null;
        }
        switch (type) {
            case carbon: {
                return new EntityBoatCarbon(world);
            }
            case rubber: {
                return new EntityBoatRubber(world);
            }
            case electric: {
                return new EntityBoatElectric(world);
            }
            default: {
                return null;
            }
        }
    }
    
    public boolean hasCustomEntity(final ItemStack stack) {
        return this.getType(stack) == BoatType.electric;
    }
    
    public Entity createEntity(final World world, final Entity location, final ItemStack stack) {
        assert this.hasCustomEntity(stack);
        assert !world.isRemote;
        final EntityItem item = new FireproofItem(world, location.posX, location.posY, location.posZ, stack);
        item.setDefaultPickupDelay();
        item.motionX = location.motionX;
        item.motionY = location.motionY;
        item.motionZ = location.motionZ;
        return (Entity)item;
    }
    
    public enum BoatType implements IIdProvider
    {
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
    
    public static class FireproofItem extends EntityItem
    {
        public FireproofItem(final World world, final double x, final double y, final double z, final ItemStack stack) {
            super(world, x, y, z, stack);
            this.isImmuneToFire = true;
        }
        
        public FireproofItem(final World world, final double x, final double y, final double z) {
            super(world, x, y, z);
            this.isImmuneToFire = true;
        }
        
        public FireproofItem(final World world) {
            super(world);
            this.isImmuneToFire = true;
        }
        
        public void onUpdate() {
            super.onUpdate();
            this.extinguish();
        }
        
        protected void dealFireDamage(final int amount) {
        }
        
        public void setFire(final int seconds) {
            this.extinguish();
        }
    }
}

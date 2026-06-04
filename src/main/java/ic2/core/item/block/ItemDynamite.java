// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import ic2.core.block.EntityDynamite;
import net.minecraft.entity.Entity;
import ic2.core.block.EntityStickyDynamite;
import ic2.core.IC2;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.IBlockAccess;
import ic2.core.ref.BlockName;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.BehaviorDynamiteDispense;
import net.minecraft.block.BlockDispenser;
import ic2.core.ref.ItemName;
import ic2.api.item.IBoxable;
import ic2.core.item.ItemIC2;

public class ItemDynamite extends ItemIC2 implements IBoxable
{
    public boolean sticky;
    
    public ItemDynamite(final ItemName name) {
        super(name);
        this.sticky = (name == ItemName.dynamite_sticky);
        this.setMaxStackSize(16);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject((Object)this, (Object)new BehaviorDynamiteDispense(this.sticky));
    }
    
    public int getMetadata(final int i) {
        return i;
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, BlockPos pos, final EnumHand hand, final EnumFacing side, final float a, final float b, final float c) {
        if (this.sticky) {
            return EnumActionResult.PASS;
        }
        pos = pos.offset(side);
        final IBlockState state = world.getBlockState(pos);
        final Block dynamite = BlockName.dynamite.getInstance();
        if (state.getBlock().isAir(state, (IBlockAccess)world, pos) && dynamite.canPlaceBlockOnSide(world, pos, side) && dynamite.canPlaceBlockAt(world, pos)) {
            world.setBlockState(pos, dynamite.getStateForPlacement(world, pos, side, a, b, c, 0, (EntityLivingBase)player, hand), 3);
            StackUtil.consumeOrError(player, hand, 1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        ItemStack stack = StackUtil.get(player, hand);
        if (!player.capabilities.isCreativeMode) {
            stack = StackUtil.decSize(stack);
        }
        world.playSound(player, player.getPosition(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.5f, 0.4f / (ItemDynamite.itemRand.nextFloat() * 0.4f + 0.8f));
        if (IC2.platform.isSimulating()) {
            if (this.sticky) {
                world.spawnEntity((Entity)new EntityStickyDynamite(world, (EntityLivingBase)player));
            }
            else {
                world.spawnEntity((Entity)new EntityDynamite(world, (EntityLivingBase)player));
            }
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
}

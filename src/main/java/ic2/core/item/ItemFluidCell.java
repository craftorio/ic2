// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler;
import ic2.core.util.StackUtil;
import net.minecraftforge.fluids.FluidUtil;
import java.util.Iterator;
import net.minecraft.item.Item;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fluids.Fluid;
import ic2.core.util.LiquidUtil;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.DispenseFluidContainer;
import net.minecraft.block.BlockDispenser;
import ic2.core.ref.ItemName;

public class ItemFluidCell extends ItemIC2FluidContainer
{
    public ItemFluidCell() {
        super(ItemName.fluid_cell, 1000);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject((Object)this, (Object)DispenseFluidContainer.getInstance());
    }
    
    public boolean isRepairable() {
        return false;
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, BlockPos pos, final EnumHand hand, final EnumFacing side, final float xOffset, final float yOffset, final float zOffset) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        if (this.interactWithTank(player, hand, world, pos, side)) {
            player.inventoryContainer.detectAndSendChanges();
            return EnumActionResult.SUCCESS;
        }
        final RayTraceResult position = this.rayTrace(world, player, true);
        if (position == null) {
            return EnumActionResult.FAIL;
        }
        if (position.typeOfHit == RayTraceResult.Type.BLOCK) {
            pos = position.getBlockPos();
            if (!world.canMineBlockBody(player, pos)) {
                return EnumActionResult.FAIL;
            }
            if (!player.canPlayerEdit(pos, position.sideHit, player.getHeldItem(hand))) {
                return EnumActionResult.FAIL;
            }
            if (LiquidUtil.drainBlockToContainer(world, pos, player, hand) || LiquidUtil.fillBlockFromContainer(world, pos, player, hand) || LiquidUtil.fillBlockFromContainer(world, pos.offset(side), player, hand)) {
                player.inventoryContainer.detectAndSendChanges();
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }
    
    @Override
    public boolean canfill(final Fluid fluid) {
        return true;
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab) || IC2.version.isClassic()) {
            return;
        }
        final ItemStack emptyStack = new ItemStack((Item)this);
        subItems.add((Object)emptyStack);
        for (final Fluid fluid : LiquidUtil.getAllFluids()) {
            if (fluid == null) {
                continue;
            }
            final ItemStack stack = this.getItemStack(fluid);
            if (stack == null) {
                continue;
            }
            subItems.add((Object)stack);
        }
    }
    
    private boolean interactWithTank(final EntityPlayer player, final EnumHand hand, final World world, final BlockPos pos, final EnumFacing side) {
        assert !world.isRemote;
        final IFluidHandler tileHandler = FluidUtil.getFluidHandler(world, pos, side);
        if (tileHandler == null) {
            return false;
        }
        ItemStack stack = StackUtil.get(player, hand);
        final boolean single = StackUtil.getSize(stack) == 1;
        if (!single) {
            stack = StackUtil.copyWithSize(stack, 1);
        }
        boolean changeMade = false;
        do {
            final IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(StackUtil.copy(stack));
            assert itemHandler != null;
            if (FluidUtil.tryFluidTransfer(tileHandler, (IFluidHandler)itemHandler, Integer.MAX_VALUE, true) == null) {
                break;
            }
            if (single) {
                StackUtil.set(player, hand, itemHandler.getContainer());
                return true;
            }
            StackUtil.consumeOrError(player, hand, 1);
            StackUtil.storeInventoryItem(itemHandler.getContainer(), player, false);
            changeMade = true;
        } while (!StackUtil.isEmpty(player, hand));
        return changeMade;
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.reactor.tileentity;

import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import ic2.core.profile.NotClassic;
import net.minecraft.inventory.IInventory;

@NotClassic
public class TileEntityReactorAccessHatch extends TileEntityReactorVessel implements IInventory
{
    private IItemHandler itemHandler;
    
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        if (reactor != null) {
            final World world = this.getWorld();
            return reactor.getBlockType().onBlockActivated(world, reactor.getPos(), world.getBlockState(reactor.getPos()), player, hand, side, hitX, hitY, hitZ);
        }
        return false;
    }
    
    public String getName() {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return (reactor != null) ? reactor.getName() : "<null>";
    }
    
    public boolean hasCustomName() {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return reactor != null && reactor.hasCustomName();
    }
    
    public ITextComponent getDisplayName() {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return (ITextComponent)((reactor != null) ? reactor.getDisplayName() : new TextComponentString("<null>"));
    }
    
    public int getSizeInventory() {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return (reactor != null) ? reactor.getSizeInventory() : 0;
    }
    
    public boolean isEmpty() {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return reactor == null || reactor.isEmpty();
    }
    
    public ItemStack getStackInSlot(final int index) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return (reactor != null) ? reactor.getStackInSlot(index) : null;
    }
    
    public ItemStack decrStackSize(final int index, final int count) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return (reactor != null) ? reactor.decrStackSize(index, count) : null;
    }
    
    public ItemStack removeStackFromSlot(final int index) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return (reactor != null) ? reactor.removeStackFromSlot(index) : null;
    }
    
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        if (reactor != null) {
            reactor.setInventorySlotContents(index, stack);
        }
    }
    
    public int getInventoryStackLimit() {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return (reactor != null) ? reactor.getInventoryStackLimit() : 0;
    }
    
    public boolean isUsableByPlayer(final EntityPlayer player) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return reactor != null && reactor.isUsableByPlayer(player);
    }
    
    public void openInventory(final EntityPlayer player) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        if (reactor != null) {
            reactor.openInventory(player);
        }
    }
    
    public void closeInventory(final EntityPlayer player) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        if (reactor != null) {
            reactor.closeInventory(player);
        }
    }
    
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return reactor != null && reactor.isItemValidForSlot(index, stack);
    }
    
    public int getField(final int id) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return (reactor != null) ? reactor.getField(id) : 0;
    }
    
    public void setField(final int id, final int value) {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        if (reactor != null) {
            reactor.setField(id, value);
        }
    }
    
    public int getFieldCount() {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        return (reactor != null) ? reactor.getFieldCount() : 0;
    }
    
    public void clear() {
        final TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
        if (reactor != null) {
            reactor.clear();
        }
    }
    
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }
    
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (this.itemHandler == null) {
                this.itemHandler = (IItemHandler)new InvWrapper((IInventory)this);
            }
            return (T)CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast((Object)this.itemHandler);
        }
        return super.getCapability(capability, facing);
    }
}

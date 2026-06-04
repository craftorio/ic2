// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.reactor.tileentity;

import ic2.api.reactor.IReactor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import ic2.api.energy.tile.IEnergyAcceptor;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import java.util.Iterator;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import com.google.common.base.Supplier;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.reactor.IReactorChamber;
import net.minecraft.inventory.IInventory;
import ic2.core.block.TileEntityBlock;

public class TileEntityReactorChamberElectric extends TileEntityBlock implements IInventory, IReactorChamber, IEnergyEmitter
{
    public final Redstone redstone;
    protected final Fluids fluids;
    private TileEntityNuclearReactorElectric reactor;
    private long lastReactorUpdate;
    
    public TileEntityReactorChamberElectric() {
        this.redstone = this.addComponent(new Redstone(this));
        (this.fluids = this.addComponent(new Fluids(this))).addUnmanagedTankHook((Supplier<? extends Collection<Fluids.InternalFluidTank>>)new Supplier<Collection<Fluids.InternalFluidTank>>() {
            public Collection<Fluids.InternalFluidTank> get() {
                final TileEntityNuclearReactorElectric reactor = TileEntityReactorChamberElectric.this.getReactor();
                if (reactor == null) {
                    return (Collection<Fluids.InternalFluidTank>)Collections.emptySet();
                }
                return Arrays.asList(reactor.inputTank, reactor.outputTank);
            }
        });
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateRedstoneLink();
    }
    
    private void updateRedstoneLink() {
        if (this.getWorld().isRemote) {
            return;
        }
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            this.redstone.linkTo(reactor.redstone);
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    protected void updateEntityClient() {
        super.updateEntityClient();
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            TileEntityNuclearReactorElectric.showHeatEffects(this.getWorld(), this.pos, reactor.getHeat());
        }
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            final World world = this.getWorld();
            return reactor.getBlockType().onBlockActivated(world, reactor.getPos(), world.getBlockState(reactor.getPos()), player, hand, side, hitX, hitY, hitZ);
        }
        return false;
    }
    
    @Override
    protected void onNeighborChange(final Block neighbor, final BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        this.lastReactorUpdate = 0L;
        if (this.getReactor() == null) {
            this.destoryChamber(true);
        }
    }
    
    public void destoryChamber(final boolean wrench) {
        final World world = this.getWorld();
        world.setBlockToAir(this.pos);
        for (final ItemStack drop : this.getSelfDrops(0, wrench)) {
            StackUtil.dropAsEntity(world, this.pos, drop);
        }
    }
    
    public String getName() {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (reactor != null) ? reactor.getName() : "<null>";
    }
    
    public boolean hasCustomName() {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null && reactor.hasCustomName();
    }
    
    public ITextComponent getDisplayName() {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (ITextComponent)((reactor != null) ? reactor.getDisplayName() : new TextComponentString("<null>"));
    }
    
    public int getSizeInventory() {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (reactor != null) ? reactor.getSizeInventory() : 0;
    }
    
    public boolean isEmpty() {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor == null || reactor.isEmpty();
    }
    
    public ItemStack getStackInSlot(final int index) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (reactor != null) ? reactor.getStackInSlot(index) : null;
    }
    
    public ItemStack decrStackSize(final int index, final int count) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (reactor != null) ? reactor.decrStackSize(index, count) : null;
    }
    
    public ItemStack removeStackFromSlot(final int index) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (reactor != null) ? reactor.removeStackFromSlot(index) : null;
    }
    
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.setInventorySlotContents(index, stack);
        }
    }
    
    public int getInventoryStackLimit() {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (reactor != null) ? reactor.getInventoryStackLimit() : 0;
    }
    
    public boolean isUsableByPlayer(final EntityPlayer player) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null && reactor.isUsableByPlayer(player);
    }
    
    public void openInventory(final EntityPlayer player) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.openInventory(player);
        }
    }
    
    public void closeInventory(final EntityPlayer player) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.closeInventory(player);
        }
    }
    
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return reactor != null && reactor.isItemValidForSlot(index, stack);
    }
    
    public int getField(final int id) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (reactor != null) ? reactor.getField(id) : 0;
    }
    
    public void setField(final int id, final int value) {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.setField(id, value);
        }
    }
    
    public int getFieldCount() {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        return (reactor != null) ? reactor.getFieldCount() : 0;
    }
    
    public void clear() {
        final TileEntityNuclearReactorElectric reactor = this.getReactor();
        if (reactor != null) {
            reactor.clear();
        }
    }
    
    public boolean emitsEnergyTo(final IEnergyAcceptor receiver, final EnumFacing side) {
        return true;
    }
    
    public TileEntityNuclearReactorElectric getReactorInstance() {
        return this.reactor;
    }
    
    public boolean isWall() {
        return false;
    }
    
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (super.hasCapability(capability, facing)) {
            return super.getCapability(capability, facing);
        }
        if (this.reactor != null) {
            return this.reactor.getCapability(capability, facing);
        }
        return null;
    }
    
    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        return super.hasCapability(capability, facing) || (this.reactor != null && this.reactor.hasCapability(capability, facing));
    }
    
    private TileEntityNuclearReactorElectric getReactor() {
        final long time = this.getWorld().getTotalWorldTime();
        if (time != this.lastReactorUpdate) {
            this.updateReactor();
            this.lastReactorUpdate = time;
        }
        else if (this.reactor != null && this.reactor.isInvalid()) {
            this.reactor = null;
        }
        return this.reactor;
    }
    
    private void updateReactor() {
        final World world = this.getWorld();
        this.reactor = null;
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final TileEntity te = world.getTileEntity(this.pos.offset(facing));
            if (te instanceof TileEntityNuclearReactorElectric) {
                this.reactor = (TileEntityNuclearReactorElectric)te;
                break;
            }
        }
    }
}

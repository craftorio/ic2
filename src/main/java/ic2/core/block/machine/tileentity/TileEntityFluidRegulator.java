// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fluids.FluidTank;
import ic2.core.init.Localization;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiFluidRegulator;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerFluidRegulator;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.LiquidUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.IC2;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.TileEntityBlock;
import ic2.core.network.GuiSynced;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.profile.NotClassic;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IHasGui;

@NotClassic
public class TileEntityFluidRegulator extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener
{
    private int mode;
    private int updateTicker;
    private int outputmb;
    private boolean newActive;
    public final InvSlotOutput wasseroutputSlot;
    public final InvSlotConsumableLiquidByTank wasserinputSlot;
    @GuiSynced
    protected final Fluids.InternalFluidTank fluidTank;
    protected final Fluids fluids;
    
    public TileEntityFluidRegulator() {
        super(10000, 4);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTank("fluidTank", 10000, InvSlot.Access.NONE);
        this.wasserinputSlot = new InvSlotConsumableLiquidByTank(this, "wasserinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, (IFluidTank)this.fluidTank);
        this.wasseroutputSlot = new InvSlotOutput(this, "wasseroutputSlot", 1);
        this.newActive = false;
        this.outputmb = 0;
        this.mode = 0;
        this.updateTicker = IC2.random.nextInt(this.getTickRate());
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.outputmb = nbt.getInteger("outputmb");
        this.mode = nbt.getInteger("mode");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("outputmb", this.outputmb);
        nbt.setInteger("mode", this.mode);
        return nbt;
    }
    
    protected void onLoaded() {
        super.onLoaded();
        this.updateConnectivity();
    }
    
    public void setFacing(final EnumFacing side) {
        super.setFacing(side);
        this.updateConnectivity();
    }
    
    private void updateConnectivity() {
        this.fluids.changeConnectivity(this.fluidTank, (Collection<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)this.getFacing())), (Collection<EnumFacing>)Collections.emptySet());
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.wasserinputSlot.processIntoTank((IFluidTank)this.fluidTank, this.wasseroutputSlot);
        if (this.updateTicker++ % this.getTickRate() != 0 && this.mode == 0) {
            return;
        }
        this.newActive = this.work();
        if (this.getActive() != this.newActive) {
            this.setActive(this.newActive);
        }
    }
    
    private boolean work() {
        if (this.outputmb == 0) {
            return false;
        }
        if (this.energy.getEnergy() < 10.0) {
            return false;
        }
        if (this.fluidTank.getFluidAmount() <= 0) {
            return false;
        }
        final EnumFacing dir = this.getFacing();
        final TileEntity te = this.getWorld().getTileEntity(this.pos.offset(dir));
        final EnumFacing side = dir.getOpposite();
        if (LiquidUtil.isFluidTile(te, side)) {
            final int amount = LiquidUtil.fillTile(te, side, this.fluidTank.drainInternal(this.outputmb, false), false);
            if (amount > 0) {
                this.fluidTank.drainInternal(this.outputmb, true);
                this.energy.useEnergy(10.0);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        if (event == 1001 || event == 1002) {
            if (event == 1001 && this.mode == 0) {
                this.mode = 1;
            }
            if (event == 1002 && this.mode == 1) {
                this.mode = 0;
            }
            return;
        }
        this.outputmb += event;
        if (this.outputmb > 1000) {
            this.outputmb = 1000;
        }
        if (this.outputmb < 0) {
            this.outputmb = 0;
        }
    }
    
    public int getTickRate() {
        return 20;
    }
    
    @Override
    public ContainerBase<TileEntityFluidRegulator> getGuiContainer(final EntityPlayer player) {
        return new ContainerFluidRegulator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiFluidRegulator(new ContainerFluidRegulator(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public int gaugeLiquidScaled(final int i, final int tank) {
        switch (tank) {
            case 0: {
                if (this.fluidTank.getFluidAmount() <= 0) {
                    return 0;
                }
                return this.fluidTank.getFluidAmount() * i / this.fluidTank.getCapacity();
            }
            default: {
                return 0;
            }
        }
    }
    
    public int getoutputmb() {
        return this.outputmb;
    }
    
    public String getmodegui() {
        switch (this.mode) {
            case 0: {
                return Localization.translate("ic2.generic.text.sec");
            }
            case 1: {
                return Localization.translate("ic2.generic.text.tick");
            }
            default: {
                return "";
            }
        }
    }
    
    public FluidTank getFluidTank() {
        return this.fluidTank;
    }
}

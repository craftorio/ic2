// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiFluidDistributor;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidStack;
import java.util.Iterator;
import net.minecraft.world.World;
import java.util.Map;
import net.minecraft.tileentity.TileEntity;
import java.util.EnumMap;
import ic2.core.util.LiquidUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.TileEntityBlock;
import ic2.core.network.GuiSynced;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.profile.NotClassic;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityFluidDistributor extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener
{
    public final InvSlotConsumableLiquidByTank inputSlot;
    public final InvSlotOutput OutputSlot;
    @GuiSynced
    public final Fluids.InternalFluidTank fluidTank;
    protected final Fluids fluids;
    
    public TileEntityFluidDistributor() {
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTank("fluidTank", 1000);
        this.inputSlot = new InvSlotConsumableLiquidByTank(this, "inputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.fluidTank);
        this.OutputSlot = new InvSlotOutput(this, "OutputSlot", 1);
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateConnectivity();
    }
    
    @Override
    public void setActive(final boolean val) {
        super.setActive(val);
        this.updateConnectivity();
    }
    
    public void setFacing(final EnumFacing facing) {
        super.setFacing(facing);
        this.updateConnectivity();
    }
    
    protected void updateConnectivity() {
        EnumSet<EnumFacing> acceptingSides = EnumSet.of(this.getFacing());
        if (this.getActive()) {
            acceptingSides = EnumSet.complementOf(acceptingSides);
        }
        this.fluids.changeConnectivity(this.fluidTank, acceptingSides, (Collection<EnumFacing>)Collections.emptySet());
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.inputSlot.processFromTank((IFluidTank)this.fluidTank, this.OutputSlot);
        if (this.fluidTank.getFluidAmount() > 0) {
            this.moveFluid();
        }
    }
    
    protected void moveFluid() {
        final World world = this.getWorld();
        if (this.getActive()) {
            final TileEntity target = world.getTileEntity(this.pos.offset(this.getFacing()));
            final EnumFacing side = this.getFacing().getOpposite();
            if (LiquidUtil.isFluidTile(target, side)) {
                final int amount = LiquidUtil.fillTile(target, side, this.fluidTank.getFluid(), false);
                if (amount > 0) {
                    this.fluidTank.drainInternal(amount, true);
                }
            }
        }
        else {
            final Map<EnumFacing, TileEntity> acceptingNeighbors = new EnumMap<EnumFacing, TileEntity>(EnumFacing.class);
            int acceptedVolume = 0;
            for (final EnumFacing dir : EnumFacing.VALUES) {
                if (dir != this.getFacing()) {
                    final TileEntity target2 = world.getTileEntity(this.pos.offset(dir));
                    final EnumFacing side2 = dir.getOpposite();
                    if (LiquidUtil.isFluidTile(target2, side2)) {
                        final int amount2 = LiquidUtil.fillTile(target2, side2, this.fluidTank.getFluid(), true);
                        if (amount2 > 0) {
                            acceptingNeighbors.put(dir, target2);
                            acceptedVolume += amount2;
                        }
                    }
                }
            }
            while (!acceptingNeighbors.isEmpty()) {
                int amount = Math.min(acceptedVolume, this.fluidTank.getFluidAmount());
                if (amount <= 0) {
                    break;
                }
                amount /= acceptingNeighbors.size();
                if (amount <= 0) {
                    for (final Map.Entry<EnumFacing, TileEntity> entry : acceptingNeighbors.entrySet()) {
                        final TileEntity target3 = entry.getValue();
                        final EnumFacing side3 = entry.getKey().getOpposite();
                        FluidStack fs = this.fluidTank.getFluid();
                        if (fs == null) {
                            break;
                        }
                        fs = fs.copy();
                        fs.amount = Math.min(acceptedVolume, fs.amount);
                        if (fs.amount <= 0) {
                            break;
                        }
                        final int cAmount = LiquidUtil.fillTile(target3, side3, fs, false);
                        this.fluidTank.drainInternal(cAmount, true);
                        acceptedVolume -= cAmount;
                    }
                    break;
                }
                final Iterator<Map.Entry<EnumFacing, TileEntity>> it = acceptingNeighbors.entrySet().iterator();
                while (it.hasNext()) {
                    final Map.Entry<EnumFacing, TileEntity> entry = it.next();
                    final TileEntity target3 = entry.getValue();
                    final EnumFacing side3 = entry.getKey().getOpposite();
                    FluidStack fs = this.fluidTank.getFluid();
                    if (fs == null) {
                        break;
                    }
                    fs = fs.copy();
                    if (fs.amount <= 0) {
                        break;
                    }
                    fs.amount = Math.min(amount, fs.amount);
                    final int cAmount = LiquidUtil.fillTile(target3, side3, fs, false);
                    this.fluidTank.drainInternal(cAmount, true);
                    acceptedVolume -= cAmount;
                    if (cAmount >= fs.amount) {
                        continue;
                    }
                    it.remove();
                }
            }
        }
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        this.setActive(!this.getActive());
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerFluidDistributor(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiFluidDistributor(new ContainerFluidDistributor(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
}

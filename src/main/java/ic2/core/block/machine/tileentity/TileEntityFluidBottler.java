// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiFluidBottler;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.util.FluidContainerOutputMode;
import ic2.api.recipe.Recipes;
import net.minecraftforge.fluids.FluidStack;
import ic2.api.recipe.IFillFluidContainerRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.IEmptyFluidContainerRecipeManager;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityFluidBottler extends TileEntityStandardMachine<Void, Object, Object>
{
    public final InvSlotConsumableLiquid drainInputSlot;
    public final InvSlotConsumableLiquid fillInputSlot;
    @GuiSynced
    public final FluidTank fluidTank;
    protected final Fluids fluids;
    
    public TileEntityFluidBottler() {
        super(2, 100, 1);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTank("fluidTank", 8000);
        this.drainInputSlot = new InvSlotConsumableLiquidByTank(this, "drainInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, (IFluidTank)this.fluidTank);
        this.fillInputSlot = new InvSlotConsumableLiquidByTank(this, "fillInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.fluidTank);
    }
    
    @Override
    protected Collection<ItemStack> getOutput(final Object output) {
        if (output instanceof IEmptyFluidContainerRecipeManager.Output) {
            return ((IEmptyFluidContainerRecipeManager.Output)output).container;
        }
        return super.getOutput(output);
    }
    
    public void operateOnce(final MachineRecipeResult<Void, Object, Object> result, final Collection<ItemStack> processResult) {
        if (result.getOutput() instanceof IEmptyFluidContainerRecipeManager.Output) {
            this.drainInputSlot.put(result.getAdjustedInput());
            final FluidStack fs = result.getOutput().fluid;
            this.fluidTank.fill(fs, true);
        }
        else {
            final IFillFluidContainerRecipeManager.Input adjInput = result.getAdjustedInput();
            this.fillInputSlot.put(adjInput.container);
            this.fluidTank.drain((adjInput.fluid == null) ? this.fluidTank.getFluidAmount() : (this.fluidTank.getFluidAmount() - adjInput.fluid.amount), true);
        }
        this.outputSlot.add(processResult);
    }
    
    public MachineRecipeResult<Void, Object, Object> getOutput() {
        final MachineRecipeResult<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> emptyRes = Recipes.emptyFluidContainer.apply(this.drainInputSlot.get(), (this.fluidTank.getFluid() == null) ? null : this.fluidTank.getFluid().getFluid(), FluidContainerOutputMode.EmptyFullToOutput, false);
        if (emptyRes != null && emptyRes.getOutput().fluid.amount <= this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount() && this.outputSlot.canAdd(emptyRes.getOutput().container)) {
            return (MachineRecipeResult<Void, Object, Object>)emptyRes;
        }
        final MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> fillRes = Recipes.fillFluidContainer.apply(new IFillFluidContainerRecipeManager.Input(this.fillInputSlot.get(), this.fluidTank.getFluid()), FluidContainerOutputMode.EmptyFullToOutput, false);
        if (fillRes != null && this.outputSlot.canAdd(fillRes.getOutput())) {
            return (MachineRecipeResult<Void, Object, Object>)fillRes;
        }
        return null;
    }
    
    @Override
    public ContainerBase<TileEntityFluidBottler> getGuiContainer(final EntityPlayer player) {
        return new ContainerFluidBottler(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiFluidBottler(new ContainerFluidBottler(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
    }
}

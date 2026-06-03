package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IEmptyFluidContainerRecipeManager;
import ic2.api.recipe.IFillFluidContainerRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.ContainerBase;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.block.machine.gui.GuiFluidBottler;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityFluidBottler extends TileEntityStandardMachine<Void, Object, Object> {
  public final InvSlotConsumableLiquid drainInputSlot;
  
  public final InvSlotConsumableLiquid fillInputSlot;
  
  @GuiSynced
  public final FluidTank fluidTank;
  
  protected final Fluids fluids;
  
  public TileEntityFluidBottler() {
    super(2, 100, 1);
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.fluidTank = (FluidTank)this.fluids.addTank("fluidTank", 8000);
    this.drainInputSlot = (InvSlotConsumableLiquid)new InvSlotConsumableLiquidByTank((IInventorySlotHolder)this, "drainInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, (IFluidTank)this.fluidTank);
    this.fillInputSlot = (InvSlotConsumableLiquid)new InvSlotConsumableLiquidByTank((IInventorySlotHolder)this, "fillInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.fluidTank);
  }
  
  protected Collection<ItemStack> getOutput(Object output) {
    if (output instanceof IEmptyFluidContainerRecipeManager.Output)
      return ((IEmptyFluidContainerRecipeManager.Output)output).container; 
    return super.getOutput(output);
  }
  
  public void operateOnce(MachineRecipeResult<Void, Object, Object> result, Collection<ItemStack> processResult) {
    if (result.getOutput() instanceof IEmptyFluidContainerRecipeManager.Output) {
      this.drainInputSlot.put((ItemStack)result.getAdjustedInput());
      FluidStack fs = ((IEmptyFluidContainerRecipeManager.Output)result.getOutput()).fluid;
      this.fluidTank.fill(fs, true);
    } else {
      IFillFluidContainerRecipeManager.Input adjInput = (IFillFluidContainerRecipeManager.Input)result.getAdjustedInput();
      this.fillInputSlot.put(adjInput.container);
      this.fluidTank.drain((adjInput.fluid == null) ? this.fluidTank.getFluidAmount() : (this.fluidTank.getFluidAmount() - adjInput.fluid.amount), true);
    } 
    this.outputSlot.add(processResult);
  }
  
  public MachineRecipeResult<Void, Object, Object> getOutput() {
    MachineRecipeResult<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> emptyRes = Recipes.emptyFluidContainer.apply(this.drainInputSlot
        .get(), 
        (this.fluidTank.getFluid() == null) ? null : this.fluidTank.getFluid().getFluid(), FluidContainerOutputMode.EmptyFullToOutput, false);
    if (emptyRes != null && ((IEmptyFluidContainerRecipeManager.Output)emptyRes.getOutput()).fluid.amount <= this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount() && this.outputSlot.canAdd(((IEmptyFluidContainerRecipeManager.Output)emptyRes.getOutput()).container))
      return (MachineRecipeResult)emptyRes; 
    MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> fillRes = Recipes.fillFluidContainer.apply(new IFillFluidContainerRecipeManager.Input(this.fillInputSlot
          .get(), this.fluidTank.getFluid()), FluidContainerOutputMode.EmptyFullToOutput, false);
    if (fillRes != null && this.outputSlot.canAdd((Collection)fillRes.getOutput()))
      return (MachineRecipeResult)fillRes; 
    return null;
  }
  
  public ContainerBase<TileEntityFluidBottler> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityFluidBottler>)new ContainerFluidBottler(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiFluidBottler(new ContainerFluidBottler(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Processing, new UpgradableProperty[] { UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing });
  }
}

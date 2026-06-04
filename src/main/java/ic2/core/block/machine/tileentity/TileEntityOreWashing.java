package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.BasicMachineRecipeManager;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityOreWashing extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> {
  public final InvSlotConsumableLiquid fluidSlot;
  
  public final InvSlotOutput cellSlot;
  
  @GuiSynced
  protected final FluidTank fluidTank;
  
  protected final Fluids fluids;
  
  public TileEntityOreWashing() {
    super(16, 500, 3);
    this.inputSlot = (InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack>)new InvSlotProcessableGeneric((IInventorySlotHolder)this, "input", 1, (IMachineRecipeManager)Recipes.oreWashing);
    this.fluidSlot = (InvSlotConsumableLiquid)new InvSlotConsumableLiquidByList((IInventorySlotHolder)this, "fluid", 1, new Fluid[] { FluidRegistry.WATER });
    this.cellSlot = new InvSlotOutput((IInventorySlotHolder)this, "cell", 1);
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.fluidTank = (FluidTank)this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(new Fluid[] { FluidRegistry.WATER }));
  }
  
  public static void init() {
    Recipes.oreWashing = (IBasicMachineRecipeManager)new BasicMachineRecipeManager();
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity())
      gainFluid(); 
  }
  
  public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output, Collection<ItemStack> processResult) {
    super.operateOnce(output, processResult);
    this.fluidTank.drainInternal(output.getRecipe().getMetaData().getInteger("amount"), true);
  }
  
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
    MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getOutput();
    if (ret != null) {
      if (ret.getRecipe().getMetaData() == null)
        return null; 
      if (ret.getRecipe().getMetaData().getInteger("amount") > this.fluidTank.getFluidAmount())
        return null; 
    } 
    return ret;
  }
  
  public boolean gainFluid() {
    return this.fluidSlot.processIntoTank((IFluidTank)this.fluidTank, this.cellSlot);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public ContainerBase<TileEntityOreWashing> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityOreWashing>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Processing, new UpgradableProperty[] { UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming });
  }
}

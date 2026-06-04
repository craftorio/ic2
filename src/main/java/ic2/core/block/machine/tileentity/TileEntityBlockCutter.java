package ic2.core.block.machine.tileentity;

import ic2.api.item.IBlockCuttingBlade;
import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableClass;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityBlockCutter extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> {
  @GuiSynced
  private boolean bladeTooWeak;
  
  public final InvSlotConsumableClass cutterSlot;
  
  public TileEntityBlockCutter() {
    super(4, 450, 1);
    this.bladeTooWeak = false;
    this.inputSlot = (InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack>)new InvSlotProcessableGeneric((IInventorySlotHolder)this, "input", 1, (IMachineRecipeManager)Recipes.blockcutter);
    this.cutterSlot = new InvSlotConsumableClass((IInventorySlotHolder)this, "cutterInputSlot", 1, IBlockCuttingBlade.class);
  }
  
  public static void init() {
    Recipes.blockcutter = (IBasicMachineRecipeManager)new BasicMachineRecipeManager();
  }
  
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
    if (this.cutterSlot.isEmpty()) {
      if (this.bladeTooWeak != true)
        this.bladeTooWeak = true; 
      return null;
    } 
    if (this.bladeTooWeak)
      this.bladeTooWeak = false; 
    MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getOutput();
    if (ret == null || ret.getRecipe().getMetaData() == null)
      return null; 
    ItemStack bladeStack = this.cutterSlot.get();
    IBlockCuttingBlade blade = (IBlockCuttingBlade)bladeStack.getItem();
    if (ret.getRecipe().getMetaData().func_74762_e("hardness") > blade.getHardness(bladeStack)) {
      if (this.bladeTooWeak != true)
        this.bladeTooWeak = true; 
      return null;
    } 
    if (this.bladeTooWeak)
      this.bladeTooWeak = false; 
    return ret;
  }
  
  public ContainerBase<TileEntityBlockCutter> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityBlockCutter>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public boolean getGuiState(String name) {
    if ("isBladeTooWeak".equals(name))
      return this.bladeTooWeak; 
    return super.getGuiState(name);
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
  }
}

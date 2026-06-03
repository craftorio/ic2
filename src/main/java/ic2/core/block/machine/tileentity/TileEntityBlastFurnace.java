package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.item.type.IngotResourceType;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityBlastFurnace extends TileEntityInventory implements IUpgradableBlock, IHasGui, IGuiValueProvider {
  public final InvSlotProcessableGeneric inputSlot = new InvSlotProcessableGeneric((IInventorySlotHolder)this, "input", 1, (IMachineRecipeManager)Recipes.blastfurnace);
  
  public final InvSlotOutput outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 2) {
      public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
        if (player != null && ItemName.ingot.getItemStack((Enum)IngotResourceType.steel).func_77969_a(stack))
          IC2.achievements.issueAchievement(player, "acquireRefinedIron"); 
      }
    };
  
  public final InvSlotConsumableLiquidByList tankInputSlot = new InvSlotConsumableLiquidByList((IInventorySlotHolder)this, "cellInput", 1, new Fluid[] { FluidName.air.getInstance() });
  
  public final InvSlotOutput tankOutputSlot = new InvSlotOutput((IInventorySlotHolder)this, "cellOutput", 1);
  
  public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 2);
  
  protected final Redstone redstone = (Redstone)addComponent((TileEntityComponent)new Redstone((TileEntityBlock)this));
  
  protected final Fluids fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
  
  @GuiSynced
  public final FluidTank fluidTank = (FluidTank)this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(new Fluid[] { FluidName.air.getInstance() }));
  
  public static void init() {
    Recipes.blastfurnace = (IBasicMachineRecipeManager)new BasicMachineRecipeManager();
  }
  
  public void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    heatup();
    MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = getOutput();
    if (result != null && isHot()) {
      setActive(true);
      if (result.getRecipe().getMetaData().func_74762_e("fluid") <= this.fluidTank.getFluidAmount()) {
        this.progress++;
        this.fluidTank.drainInternal(result.getRecipe().getMetaData().func_74762_e("fluid"), true);
      } 
      this.progressNeeded = result.getRecipe().getMetaData().func_74762_e("duration");
      if (this.progress >= result.getRecipe().getMetaData().func_74762_e("duration")) {
        operateOnce(result, (Collection<ItemStack>)result.getOutput());
        needsInvUpdate = true;
        this.progress = 0;
      } 
    } else {
      if (result == null)
        this.progress = 0; 
      setActive(false);
    } 
    if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity())
      gainFluid(); 
    needsInvUpdate |= this.upgradeSlot.tickNoMark();
    this.guiProgress = this.progress / this.progressNeeded;
    this.guiHeat = this.heat / maxHeat;
    if (needsInvUpdate)
      func_70296_d(); 
  }
  
  public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result, Collection<ItemStack> processResult) {
    this.inputSlot.consume(result);
    this.outputSlot.add(processResult);
  }
  
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput() {
    if (this.inputSlot.isEmpty())
      return null; 
    MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = this.inputSlot.process();
    if (output == null || output.getRecipe().getMetaData() == null)
      return null; 
    if (this.outputSlot.canAdd((Collection)output.getOutput()))
      return output; 
    return null;
  }
  
  public boolean gainFluid() {
    return this.tankInputSlot.processIntoTank((IFluidTank)this.fluidTank, this.tankOutputSlot);
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    this.heat = nbt.func_74762_e("heat");
    this.progress = nbt.func_74762_e("progress");
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74768_a("heat", this.heat);
    nbt.func_74768_a("progress", this.progress);
    return nbt;
  }
  
  private void heatup() {
    int coolingPerTick = 1;
    int heatRequested = 0;
    int gainhU = 0;
    if ((!this.inputSlot.isEmpty() || this.progress >= 1) && this.heat <= maxHeat) {
      heatRequested = maxHeat - this.heat + 100;
    } else if (this.redstone.hasRedstoneInput() && this.heat <= maxHeat) {
      heatRequested = maxHeat - this.heat + 100;
    } 
    if (heatRequested > 0) {
      EnumFacing dir = getFacing();
      TileEntity te = func_145831_w().func_175625_s(this.field_174879_c.func_177972_a(dir));
      if (te instanceof IHeatSource) {
        gainhU = ((IHeatSource)te).drawHeat(dir.func_176734_d(), heatRequested, false);
        this.heat += gainhU;
      } 
      if (gainhU == 0)
        this.heat -= Math.min(this.heat, 1); 
    } else {
      this.heat -= Math.min(this.heat, 1);
    } 
  }
  
  public boolean isHot() {
    return (this.heat >= maxHeat);
  }
  
  public ContainerBase<TileEntityBlastFurnace> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityBlastFurnace>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    if (name.equals("progress"))
      return this.guiProgress; 
    if (name.equals("heat"))
      return this.guiHeat; 
    throw new IllegalArgumentException();
  }
  
  public double getEnergy() {
    return 0.0D;
  }
  
  public boolean useEnergy(double amount) {
    return false;
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming);
  }
  
  public int heat = 0;
  
  public static int maxHeat = 50000;
  
  protected int progress = 0;
  
  protected int progressNeeded = 300;
  
  @GuiSynced
  public float guiHeat;
  
  @GuiSynced
  protected float guiProgress;
}

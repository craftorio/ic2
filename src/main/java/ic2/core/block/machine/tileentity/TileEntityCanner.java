package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IEmptyFluidContainerRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotConsumableCanner;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableCanner;
import ic2.core.block.machine.CannerBottleRecipeManager;
import ic2.core.block.machine.CannerEnrichRecipeManager;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.gui.GuiCanner;
import ic2.core.item.type.CraftingItemType;
import ic2.core.item.type.DustResourceType;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock.Delegated;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Delegated(current = TileEntityCanner.class, old = TileEntityClassicCanner.class)
public class TileEntityCanner extends TileEntityStandardMachine<Object, Object, Object> implements INetworkClientTileEntityEventListener {
  private Mode mode;
  
  public static final int eventSetModeBase = 0;
  
  public static Class<? extends TileEntityElectricMachine> delegate() {
    return IC2.version.isClassic() ? (Class)TileEntityClassicCanner.class : (Class)TileEntityCanner.class;
  }
  
  public TileEntityCanner() {
    super(4, 200, 1);
    this.mode = Mode.BottleSolid;
    this.inputSlot = (InvSlotProcessable<Object, Object, Object>)new InvSlotProcessableCanner(this, "input", 1);
    this.canInputSlot = new InvSlotConsumableCanner(this, "canInput", 1);
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.inputTank = (FluidTank)this.fluids.addTankInsert("inputTank", 8000);
    this.outputTank = (FluidTank)this.fluids.addTankExtract("outputTank", 8000);
  }
  
  public static void init() {
    Recipes.cannerBottle = (ICannerBottleRecipeManager)new CannerBottleRecipeManager();
    Recipes.cannerEnrich = (ICannerEnrichRecipeManager)new CannerEnrichRecipeManager();
    ItemStack fuelRod = ItemName.crafting.getItemStack((Enum)CraftingItemType.fuel_rod);
    addBottleRecipe(fuelRod, ItemName.nuclear.getItemStack((Enum)NuclearResourceType.uranium), ItemName.uranium_fuel_rod.getItemStack());
    addBottleRecipe(fuelRod, ItemName.nuclear.getItemStack((Enum)NuclearResourceType.mox), ItemName.mox_fuel_rod.getItemStack());
    ItemStack tinCan = ItemName.crafting.getItemStack((Enum)CraftingItemType.tin_can);
    ItemStack filledTinCan = ItemName.filled_tin_can.getItemStack();
    addBottleRecipe(tinCan, new ItemStack(Items.field_151174_bG), filledTinCan);
    addBottleRecipe(tinCan, 2, new ItemStack(Items.field_151106_aX), StackUtil.copyWithSize(filledTinCan, 2));
    addBottleRecipe(tinCan, 2, new ItemStack(Items.field_151127_ba), StackUtil.copyWithSize(filledTinCan, 2));
    addBottleRecipe(tinCan, 2, new ItemStack(Items.field_151115_aP), StackUtil.copyWithSize(filledTinCan, 2));
    addBottleRecipe(tinCan, 2, new ItemStack(Items.field_151076_bf), StackUtil.copyWithSize(filledTinCan, 2));
    addBottleRecipe(tinCan, 3, new ItemStack(Items.field_151147_al), StackUtil.copyWithSize(filledTinCan, 3));
    addBottleRecipe(tinCan, 3, new ItemStack(Items.field_151082_bd), StackUtil.copyWithSize(filledTinCan, 3));
    addBottleRecipe(tinCan, 4, new ItemStack(Items.field_151034_e), StackUtil.copyWithSize(filledTinCan, 4));
    addBottleRecipe(tinCan, 4, new ItemStack(Items.field_151172_bF), StackUtil.copyWithSize(filledTinCan, 4));
    addBottleRecipe(tinCan, 5, new ItemStack(Items.field_151025_P), StackUtil.copyWithSize(filledTinCan, 5));
    addBottleRecipe(tinCan, 5, new ItemStack(Items.field_179566_aV), StackUtil.copyWithSize(filledTinCan, 5));
    addBottleRecipe(tinCan, 6, new ItemStack(Items.field_151077_bg), StackUtil.copyWithSize(filledTinCan, 6));
    addBottleRecipe(tinCan, 6, new ItemStack(Items.field_151168_bH), StackUtil.copyWithSize(filledTinCan, 6));
    addBottleRecipe(tinCan, 6, new ItemStack(Items.field_151009_A), StackUtil.copyWithSize(filledTinCan, 6));
    addBottleRecipe(tinCan, 6, new ItemStack(Items.field_151158_bO), StackUtil.copyWithSize(filledTinCan, 6));
    addBottleRecipe(tinCan, 8, new ItemStack(Items.field_151157_am), StackUtil.copyWithSize(filledTinCan, 8));
    addBottleRecipe(tinCan, 8, new ItemStack(Items.field_151083_be), StackUtil.copyWithSize(filledTinCan, 8));
    addBottleRecipe(tinCan, 12, new ItemStack(Items.field_151105_aU), StackUtil.copyWithSize(filledTinCan, 12));
    addBottleRecipe(tinCan, new ItemStack(Items.field_151170_bI), 2, filledTinCan);
    addBottleRecipe(tinCan, new ItemStack(Items.field_151078_bh), 2, filledTinCan);
    addEnrichRecipe(FluidRegistry.WATER, ItemName.dust.getItemStack((Enum)DustResourceType.milk), FluidName.milk.getInstance());
    addEnrichRecipe(FluidRegistry.WATER, ItemName.crafting.getItemStack((Enum)CraftingItemType.cf_powder), FluidName.construction_foam.getInstance());
    addEnrichRecipe(FluidRegistry.WATER, Recipes.inputFactory.forOreDict("dustLapis", 8), FluidName.coolant.getInstance());
    addEnrichRecipe(FluidName.distilled_water.getInstance(), Recipes.inputFactory.forOreDict("dustLapis", 1), FluidName.coolant.getInstance());
    addEnrichRecipe(FluidRegistry.WATER, ItemName.crafting.getItemStack((Enum)CraftingItemType.bio_chaff), FluidName.biomass.getInstance());
    addEnrichRecipe(new FluidStack(FluidRegistry.WATER, 6000), Recipes.inputFactory.forStack(new ItemStack(Items.field_151055_y)), new FluidStack(FluidName.hot_water.getInstance(), 1000));
  }
  
  public static void addBottleRecipe(ItemStack container, int conamount, ItemStack fill, int fillamount, ItemStack output) {
    addBottleRecipe(Recipes.inputFactory.forStack(container, conamount), Recipes.inputFactory.forStack(fill, fillamount), output);
  }
  
  public static void addBottleRecipe(ItemStack container, ItemStack fill, int fillamount, ItemStack output) {
    addBottleRecipe(Recipes.inputFactory.forStack(container, 1), Recipes.inputFactory.forStack(fill, fillamount), output);
  }
  
  public static void addBottleRecipe(ItemStack container, int conamount, ItemStack fill, ItemStack output) {
    addBottleRecipe(Recipes.inputFactory.forStack(container, conamount), Recipes.inputFactory.forStack(fill, 1), output);
  }
  
  public static void addBottleRecipe(ItemStack container, ItemStack fill, ItemStack output) {
    addBottleRecipe(Recipes.inputFactory.forStack(container, 1), Recipes.inputFactory.forStack(fill, 1), output);
  }
  
  public static void addBottleRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output) {
    Recipes.cannerBottle.addRecipe(container, fill, output);
  }
  
  public static void addEnrichRecipe(Fluid input, ItemStack additive, Fluid output) {
    addEnrichRecipe(new FluidStack(input, 1000), Recipes.inputFactory.forStack(additive, 1), new FluidStack(output, 1000));
  }
  
  public static void addEnrichRecipe(Fluid input, IRecipeInput additive, Fluid output) {
    addEnrichRecipe(new FluidStack(input, 1000), additive, new FluidStack(output, 1000));
  }
  
  public static void addEnrichRecipe(FluidStack input, IRecipeInput additive, FluidStack output) {
    Recipes.cannerEnrich.addRecipe(input, additive, output);
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    setMode(Mode.values[nbt.func_74762_e("mode")]);
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.func_74768_a("mode", this.mode.ordinal());
    return nbt;
  }
  
  public void operateOnce(MachineRecipeResult<Object, Object, Object> result, Collection<ItemStack> processResult) {
    super.operateOnce(result, processResult);
    if (this.mode == Mode.EmptyLiquid) {
      IEmptyFluidContainerRecipeManager.Output output = (IEmptyFluidContainerRecipeManager.Output)result.getOutput();
      getOutputTank().fill(output.fluid, true);
    } else if (this.mode == Mode.EnrichLiquid) {
      FluidStack output = ((FluidStack)result.getOutput()).copy();
      if (!this.canInputSlot.isEmpty()) {
        LiquidUtil.FluidOperationResult outcome;
        do {
          outcome = LiquidUtil.fillContainer(this.canInputSlot.get(), output, FluidContainerOutputMode.EmptyFullToOutput);
          if (outcome == null)
            continue; 
          if (outcome.extraOutput == null || this.outputSlot.canAdd(outcome.extraOutput)) {
            this.canInputSlot.put(outcome.inPlaceOutput);
            if (outcome.extraOutput != null)
              this.outputSlot.add(outcome.extraOutput); 
            output.amount -= outcome.fluidChange.amount;
          } else {
            outcome = null;
          } 
        } while (outcome != null && output.amount > 0);
      } 
      getOutputTank().fill(output, true);
    } 
  }
  
  protected Collection<ItemStack> getOutput(Object output) {
    if (output instanceof ItemStack)
      return Collections.singletonList((ItemStack)output); 
    if (output instanceof FluidStack)
      return Collections.emptyList(); 
    if (output instanceof IEmptyFluidContainerRecipeManager.Output)
      return ((IEmptyFluidContainerRecipeManager.Output)output).container; 
    return super.getOutput(output);
  }
  
  public MachineRecipeResult<Object, Object, Object> getOutput() {
    if (this.mode == Mode.EmptyLiquid || this.mode == Mode.BottleLiquid) {
      if (this.canInputSlot.isEmpty())
        return null; 
    } else if (this.inputSlot.isEmpty()) {
      return null;
    } 
    MachineRecipeResult<Object, Object, Object> result = this.inputSlot.process();
    if (result == null)
      return null; 
    if (!this.outputSlot.canAdd(getOutput(result.getOutput())))
      return null; 
    if (this.mode == Mode.EmptyLiquid) {
      IEmptyFluidContainerRecipeManager.Output output = (IEmptyFluidContainerRecipeManager.Output)result.getOutput();
      if (getOutputTank().fill(output.fluid, false) != output.fluid.amount)
        return null; 
    } else if (this.mode == Mode.EnrichLiquid) {
      FluidStack output = ((FluidStack)result.getOutput()).copy();
      if (!this.canInputSlot.isEmpty()) {
        LiquidUtil.FluidOperationResult outcome;
        do {
          outcome = LiquidUtil.fillContainer(this.canInputSlot.get(), output, FluidContainerOutputMode.EmptyFullToOutput);
          if (outcome == null)
            continue; 
          if (outcome.extraOutput == null || this.outputSlot.canAdd(outcome.extraOutput)) {
            output.amount -= outcome.fluidChange.amount;
          } else {
            outcome = null;
          } 
        } while (outcome != null && output.amount > 0);
      } 
      if (getOutputTank().fill(output, false) != output.amount)
        return null; 
    } 
    return result;
  }
  
  public FluidTank getInputTank() {
    return this.inputTank;
  }
  
  public FluidTank getOutputTank() {
    return this.outputTank;
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = new ArrayList<>();
    ret.add("canInputSlot");
    ret.addAll(super.getNetworkedFields());
    return ret;
  }
  
  public String getStartSoundFile() {
    switch (this.mode) {
    
    } 
    return null;
  }
  
  public String getInterruptSoundFile() {
    switch (this.mode) {
    
    } 
    return null;
  }
  
  public ContainerBase<TileEntityCanner> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityCanner>)new ContainerCanner(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiCanner(new ContainerCanner(player, this));
  }
  
  public void onNetworkUpdate(String field) {
    super.onNetworkUpdate(field);
    if (field.equals("mode"))
      setMode(this.mode); 
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    if (event >= 0 && event < 0 + Mode.values.length) {
      setMode(Mode.values[event - 0]);
    } else if (event == eventSwapTanks) {
      switchTanks();
    } 
  }
  
  public Mode getMode() {
    return this.mode;
  }
  
  public void setMode(Mode mode) {
    this.mode = mode;
    switch (mode) {
      case BottleSolid:
        this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.None);
        break;
      case BottleLiquid:
        this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Fill);
        break;
      case EmptyLiquid:
        this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Drain);
        break;
      case EnrichLiquid:
        this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Both);
        break;
    } 
    if (IC2.platform.isRendering()) {
      if (this.audioSource != null)
        this.audioSource.stop(); 
      if (getStartSoundFile() != null)
        this.audioSource = IC2.audioManager.createSource(this, getStartSoundFile()); 
    } 
  }
  
  private boolean switchTanks() {
    if (this.progress != 0)
      return false; 
    FluidStack inputStack = this.inputTank.getFluid();
    FluidStack outputStack = this.outputTank.getFluid();
    this.inputTank.setFluid(outputStack);
    this.outputTank.setFluid(inputStack);
    return true;
  }
  
  public enum Mode {
    BottleSolid, EmptyLiquid, BottleLiquid, EnrichLiquid;
    
    public static final Mode[] values = values();
    
    static {
    
    }
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Processing, new UpgradableProperty[] { UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing });
  }
  
  public static final int eventSwapTanks = 0 + Mode.values.length + 1;
  
  public final FluidTank inputTank;
  
  public final FluidTank outputTank;
  
  public final InvSlotConsumableCanner canInputSlot;
  
  protected final Fluids fluids;
}

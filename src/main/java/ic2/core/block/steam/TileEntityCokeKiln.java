package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.recipe.dynamic.DynamicRecipe;
import ic2.core.recipe.dynamic.DynamicRecipeManager;
import ic2.core.recipe.dynamic.RecipeOutputIngredient;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.ParticleUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCokeKiln extends TileEntityInventory implements IMultiBlockController, IHasGui, IGuiValueProvider {
  protected int updateTicker = IC2.random.nextInt(this.tickRate);
  
  protected final InvSlotOutput outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1, InvSlot.InvSide.ANY);
  
  public static void init() {
    recipeManager = new DynamicRecipeManager();
    recipeManager.createRecipe()
      .withInput("logWood")
      .withOutput(new ItemStack(Items.field_151044_h, 1, 1))
      .withOutput(new FluidStack(FluidName.creosote.getInstance(), 250))
      .withOperationDurationTicks(1800)
      .register();
    recipeManager.createRecipe()
      .withInput(new ItemStack(Items.field_151044_h, 1, 0))
      .withOutput(new ItemStack(ItemName.coke.getInstance(), 1))
      .withOutput(new FluidStack(FluidName.creosote.getInstance(), 500))
      .withOperationDurationTicks(1800)
      .register();
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.progress = nbt.func_74762_e("progress");
    this.operationLength = nbt.func_74762_e("operationLength");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.func_74768_a("progress", this.progress);
    nbt.func_74768_a("operationLength", this.operationLength);
    return nbt;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (this.updateTicker++ % this.tickRate != 0)
      return; 
    this.isFormed = hasValidStructure();
    if (!this.isFormed) {
      this.progress = 0;
      this.guiProgress = 0.0F;
      setActive(false);
      return;
    } 
    boolean needsInventoryUpdate = false;
    if (canWork()) {
      setActive(true);
      if (this.progress == 0)
        needsInventoryUpdate = true; 
      int progressNeeded = this.recipe.getOperationDuration();
      if (this.progress < progressNeeded)
        this.progress += 20; 
      if (this.progress >= progressNeeded) {
        finishWork();
        needsInventoryUpdate = true;
      } 
    } else {
      setActive(false);
    } 
    if (this.progress == 0 || this.operationLength == 0) {
      this.guiProgress = 0.0F;
    } else {
      this.guiProgress = this.progress / this.operationLength;
    } 
    if (needsInventoryUpdate)
      func_70296_d(); 
  }
  
  protected boolean canWork() {
    BlockPos hatchPos = new BlockPos(this.field_174879_c.getX() + -getFacing().getFrontOffsetX(), this.field_174879_c.getY() + 1, this.field_174879_c.getZ() + -getFacing().getFrontOffsetZ());
    TileEntity hatch = this.field_145850_b.func_175625_s(hatchPos);
    if (!(hatch instanceof TileEntityCokeKilnHatch))
      return false; 
    ItemStack input = ((TileEntityCokeKilnHatch)hatch).inventory.get();
    if (input.func_190926_b())
      return false; 
    if (this.recipe != null) {
      boolean canUse = recipeManager.apply(this.recipe, new ItemStack[] { input }, new FluidStack[0], true);
      if (!canUse)
        reset(); 
    } 
    DynamicRecipe maybeRecipe = this.recipe;
    if (maybeRecipe != null) {
      for (RecipeOutputIngredient<?> entry : (Iterable<RecipeOutputIngredient<?>>)this.recipe.getOutputIngredients()) {
        if (entry instanceof ic2.core.recipe.dynamic.RecipeOutputItemStack) {
          if (!this.outputSlot.canAdd((ItemStack)entry.ingredient))
            return false; 
          continue;
        } 
        if (entry instanceof ic2.core.recipe.dynamic.RecipeOutputFluidStack) {
          BlockPos gratePos = new BlockPos(this.field_174879_c.getX() + -getFacing().getFrontOffsetX(), this.field_174879_c.getY() - 1, this.field_174879_c.getZ() + -getFacing().getFrontOffsetZ());
          TileEntity grate = this.field_145850_b.func_175625_s(gratePos);
          if (!(grate instanceof TileEntityCokeKilnGrate))
            return false; 
          if (((TileEntityCokeKilnGrate)grate).fluidTank.fillInternal((FluidStack)entry.ingredient, false) < ((FluidStack)entry.ingredient).amount)
            return false; 
        } 
      } 
      return true;
    } 
    maybeRecipe = recipeManager.findRecipe(new ItemStack[] { input }, new FluidStack[0]);
    if (maybeRecipe == null)
      return false; 
    updateRecipe(maybeRecipe);
    for (RecipeOutputIngredient<?> entry : (Iterable<RecipeOutputIngredient<?>>)this.recipe.getOutputIngredients()) {
      if (entry instanceof ic2.core.recipe.dynamic.RecipeOutputItemStack) {
        if (!this.outputSlot.canAdd((ItemStack)entry.ingredient))
          return false; 
        continue;
      } 
      if (entry instanceof ic2.core.recipe.dynamic.RecipeOutputFluidStack) {
        BlockPos gratePos = new BlockPos(this.field_174879_c.getX() + -getFacing().getFrontOffsetX(), this.field_174879_c.getY() - 1, this.field_174879_c.getZ() + -getFacing().getFrontOffsetZ());
        TileEntity grate = this.field_145850_b.func_175625_s(gratePos);
        if (!(grate instanceof TileEntityCokeKilnGrate))
          return false; 
        if (((TileEntityCokeKilnGrate)grate).fluidTank.fillInternal((FluidStack)entry.ingredient, false) < ((FluidStack)entry.ingredient).amount)
          return false; 
      } 
    } 
    return true;
  }
  
  protected void finishWork() {
    BlockPos hatchPos = new BlockPos(this.field_174879_c.getX() + -getFacing().getFrontOffsetX(), this.field_174879_c.getY() + 1, this.field_174879_c.getZ() + -getFacing().getFrontOffsetZ());
    TileEntity hatch = this.field_145850_b.func_175625_s(hatchPos);
    if (!(hatch instanceof TileEntityCokeKilnHatch))
      return; 
    InvSlot inventory = ((TileEntityCokeKilnHatch)hatch).inventory;
    if (inventory.get().func_190926_b())
      return; 
    recipeManager.apply(this.recipe, new ItemStack[] { inventory.get() }, new FluidStack[0], false);
    List<ItemStack> itemOutputs = new ArrayList<>();
    List<FluidStack> fluidOutputs = new ArrayList<>();
    for (RecipeOutputIngredient<?> entry : (Iterable<RecipeOutputIngredient<?>>)this.recipe.getOutputIngredients()) {
      if (entry instanceof ic2.core.recipe.dynamic.RecipeOutputItemStack) {
        itemOutputs.add(StackUtil.copy((ItemStack)entry.ingredient));
        continue;
      } 
      if (entry instanceof ic2.core.recipe.dynamic.RecipeOutputFluidStack)
        fluidOutputs.add(((FluidStack)entry.ingredient).copy()); 
    } 
    for (ItemStack stack : itemOutputs) {
      int amount = this.outputSlot.add(StackUtil.copy(stack));
      stack.func_190918_g(amount);
    } 
    itemOutputs.clear();
    BlockPos gratePos = new BlockPos(this.field_174879_c.getX() + -getFacing().getFrontOffsetX(), this.field_174879_c.getY() - 1, this.field_174879_c.getZ() + -getFacing().getFrontOffsetZ());
    TileEntity grate = this.field_145850_b.func_175625_s(gratePos);
    if (grate instanceof TileEntityCokeKilnGrate)
      for (FluidStack stack : fluidOutputs) {
        int amount = ((TileEntityCokeKilnGrate)grate).fluidTank.fillInternal(stack, true);
        stack.amount -= amount;
      }  
    fluidOutputs.clear();
    this.progress = 0;
  }
  
  protected void updateRecipe(DynamicRecipe recipe) {
    this.operationLength = recipe.getOperationDuration();
    this.recipe = recipe;
  }
  
  protected void reset() {
    this.progress = 0;
    this.operationLength = 0;
    this.recipe = null;
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (getActive()) {
      World world = getWorld();
      ParticleUtil.showFlames(world, this.field_174879_c, getFacing());
      if (world.field_73012_v.nextDouble() < 0.1D)
        world.func_184134_a(this.field_174879_c.getX() + 0.5D, this.field_174879_c.getY() + 0.5D, this.field_174879_c.getZ() + 0.5D, SoundEvents.field_187652_bv, SoundCategory.BLOCKS, 1.0F, 1.0F, false); 
    } 
  }
  
  public boolean hasValidStructure() {
    int range = 2;
    ChunkCache cache = new ChunkCache(getWorld(), this.field_174879_c.func_177982_a(-2, -2, -2), this.field_174879_c.func_177982_a(2, 2, 2), 0);
    BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
    int x;
    for (x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        cPos.func_181079_c(this.field_174879_c.getX() + x - getFacing().getFrontOffsetX(), this.field_174879_c.getY() - 1, this.field_174879_c.getZ() + z - getFacing().getFrontOffsetZ());
        if (x == 0 && z == 0) {
          TileEntity tileEntity = cache.func_175625_s((BlockPos)cPos);
          if (tileEntity == null)
            return false; 
          if (!(tileEntity instanceof TileEntityCokeKilnGrate))
            return false; 
        } else {
          IBlockState state = cache.getBlockState((BlockPos)cPos);
          if (state.getBlock() != BlockName.refractory_bricks.getInstance())
            return false; 
        } 
      } 
    } 
    for (x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        cPos.func_181079_c(this.field_174879_c.getX() + x - getFacing().getFrontOffsetX(), this.field_174879_c.getY(), this.field_174879_c.getZ() + z - getFacing().getFrontOffsetZ());
        if (x == 0 && z == 0) {
          IBlockState state = cache.getBlockState((BlockPos)cPos);
          if (state.getBlock() != Blocks.AIR)
            return false; 
        } else if (this.field_174879_c.getX() == cPos.getX() && this.field_174879_c.getZ() == cPos.getZ()) {
          TileEntity tileEntity = cache.func_175625_s((BlockPos)cPos);
          if (tileEntity == null)
            return false; 
          if (tileEntity != this)
            return false; 
        } else {
          IBlockState state = cache.getBlockState((BlockPos)cPos);
          if (state.getBlock() != BlockName.refractory_bricks.getInstance())
            return false; 
        } 
      } 
    } 
    for (x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        cPos.func_181079_c(this.field_174879_c.getX() + x - getFacing().getFrontOffsetX(), this.field_174879_c.getY() + 1, this.field_174879_c.getZ() + z - getFacing().getFrontOffsetZ());
        if (x == 0 && z == 0) {
          TileEntity tileEntity = cache.func_175625_s((BlockPos)cPos);
          if (tileEntity == null)
            return false; 
          if (!(tileEntity instanceof TileEntityCokeKilnHatch))
            return false; 
        } else {
          IBlockState state = cache.getBlockState((BlockPos)cPos);
          if (state.getBlock() != BlockName.refractory_bricks.getInstance())
            return false; 
        } 
      } 
    } 
    return true;
  }
  
  public boolean isFormed() {
    return this.isFormed;
  }
  
  public ContainerBase<TileEntityCokeKiln> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityCokeKiln>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    if (name.equals("progress"))
      return this.guiProgress; 
    throw new IllegalArgumentException(getClass().getSimpleName() + " Cannot get value for " + name);
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, List<String> info, ITooltipFlag advanced) {
    info.add("");
    info.add("MultiBlock Structure:");
    info.add("");
    info.add(" Bottom Layer - 3x3 of Refractory Blocks with a Coke Kiln Grate in the centre");
    info.add("");
    info.add(" Middle Layer - 3x3 of Refractory Blocks with a hollow centre and this block in the middle of one of the sides");
    info.add("");
    info.add(" Top Layer - 3x3 of Refractory Blocks with a Coke Kiln Hatch in the centre");
    info.add("");
  }
  
  protected int tickRate = 20;
  
  protected boolean isFormed = false;
  
  protected int progress = 0;
  
  protected int operationLength = 0;
  
  protected DynamicRecipe recipe = null;
  
  public static DynamicRecipeManager recipeManager;
  
  @GuiSynced
  protected float guiProgress;
}

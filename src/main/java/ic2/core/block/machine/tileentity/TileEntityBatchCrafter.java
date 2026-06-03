package ic2.core.block.machine.tileentity;

import com.google.common.base.Predicate;
import gnu.trove.TIntCollection;
import ic2.api.network.ClientModifiable;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.block.machine.gui.GuiBatchCrafter;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.profile.NotClassic;
import ic2.core.util.InventorySlotCrafting;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityBatchCrafter extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider, INetworkClientTileEntityEventListener {
  private static final Set<UpgradableProperty> UPGRADES = EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
  
  public static final int defaultTier = 1;
  
  public static final int defaultEnergyConsume = 2;
  
  public static final int defaultOperationLength = 40;
  
  public static final int defaultEnergyStorage = 20000;
  
  @ClientModifiable
  public final ItemStack[] craftingGrid = new ItemStack[9];
  
  public final InvSlot[] ingredientsRow = new InvSlot[this.craftingGrid.length];
  
  public final InvSlotOutput craftingOutput = new InvSlotOutput((IInventorySlotHolder)this, "output", 1, InvSlot.InvSide.SIDE);
  
  public final InvSlotOutput containerOutput = new InvSlotOutput((IInventorySlotHolder)this, "containersOut", this.craftingGrid.length, InvSlot.InvSide.NOTSIDE);
  
  public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 4);
  
  protected final InventoryCrafting crafting = (InventoryCrafting)new InventorySlotCrafting(3, 3) {
      protected ItemStack get(int index) {
        return StackUtil.wrapEmpty(TileEntityBatchCrafter.this.craftingGrid[index]);
      }
      
      protected void put(int index, ItemStack stack) {
        TileEntityBatchCrafter.this.craftingGrid[index] = stack;
      }
      
      public boolean func_191420_l() {
        for (ItemStack stack : TileEntityBatchCrafter.this.craftingGrid) {
          if (!StackUtil.isEmpty(stack))
            return false; 
        } 
        return true;
      }
      
      public void func_174888_l() {
        Arrays.fill((Object[])TileEntityBatchCrafter.this.craftingGrid, StackUtil.emptyStack);
      }
    };
  
  public final InventoryCrafting ingredients = (InventoryCrafting)new InventorySlotCrafting(3, 3) {
      protected ItemStack get(int index) {
        return TileEntityBatchCrafter.this.ingredientsRow[index].get();
      }
      
      protected void put(int index, ItemStack stack) {
        TileEntityBatchCrafter.this.ingredientsRow[index].put(stack);
      }
      
      public boolean func_191420_l() {
        for (InvSlot slot : TileEntityBatchCrafter.this.ingredientsRow) {
          if (!slot.isEmpty())
            return false; 
        } 
        return true;
      }
      
      public void func_174888_l() {
        for (InvSlot slot : TileEntityBatchCrafter.this.ingredientsRow)
          slot.clear(); 
      }
    };
  
  public final Predicate<Tuple.T2<ItemStack, Integer>> acceptPredicate = new Predicate<Tuple.T2<ItemStack, Integer>>() {
      public boolean apply(Tuple.T2<ItemStack, Integer> input) {
        return TileEntityBatchCrafter.this.ingredientsRow[((Integer)input.b).intValue()].accepts((ItemStack)input.a);
      }
    };
  
  protected IRecipe recipe = null;
  
  protected boolean canCraft = false;
  
  protected boolean newChange = true;
  
  protected boolean attemptToBalance = false;
  
  public ItemStack recipeOutput = StackUtil.emptyStack;
  
  public int energyConsume;
  
  public int operationLength;
  
  public int operationsPerTick;
  
  protected short progress = 0;
  
  protected float guiProgress = 0.0F;
  
  public TileEntityBatchCrafter() {
    super(20000, 1);
    for (int i = 0; i < this.ingredientsRow.length; i++) {
      final int slot = i;
      this.ingredientsRow[slot] = new InvSlot((IInventorySlotHolder)this, "ingredient[" + slot + ']', InvSlot.Access.I, 1) {
          public boolean accepts(ItemStack ingredient) {
            IRecipe recipe = TileEntityBatchCrafter.this.field_145850_b.field_72995_K ? TileEntityBatchCrafter.this.findRecipe() : TileEntityBatchCrafter.this.recipe;
            if (recipe == null)
              return false; 
            assert recipe.func_77569_a(TileEntityBatchCrafter.this.crafting, TileEntityBatchCrafter.this.field_145850_b);
            ItemStack recipeStack = TileEntityBatchCrafter.this.craftingGrid[slot];
            try {
              TileEntityBatchCrafter.this.craftingGrid[slot] = ingredient;
              return recipe.func_77569_a(TileEntityBatchCrafter.this.crafting, TileEntityBatchCrafter.this.field_145850_b);
            } finally {
              TileEntityBatchCrafter.this.craftingGrid[slot] = recipeStack;
            } 
          }
          
          public void onChanged() {
            super.onChanged();
            TileEntityBatchCrafter.this.ingredientChange(slot);
          }
        };
    } 
    this.energyConsume = 2;
    this.operationLength = 40;
    this.operationsPerTick = 1;
    this.comparator.setUpdate(() -> this.progress * 15 / this.operationLength);
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    this.progress = nbt.func_74765_d("progress");
    NBTTagList grid = nbt.func_150295_c("grid", 10);
    for (int i = 0; i < grid.func_74745_c(); i++) {
      NBTTagCompound contentTag = grid.func_150305_b(i);
      this.craftingGrid[contentTag.func_74771_c("index")] = new ItemStack(contentTag);
    } 
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74777_a("progress", this.progress);
    NBTTagList grid = new NBTTagList();
    for (byte i = 0; i < this.craftingGrid.length; i = (byte)(i + 1)) {
      ItemStack content = this.craftingGrid[i];
      if (!StackUtil.isEmpty(content)) {
        NBTTagCompound contentTag = new NBTTagCompound();
        contentTag.func_74774_a("index", i);
        content.func_77955_b(contentTag);
        grid.func_74742_a((NBTBase)contentTag);
      } 
    } 
    nbt.func_74782_a("grid", (NBTBase)grid);
    return nbt;
  }
  
  protected IRecipe findRecipe() {
    World world = func_145831_w();
    return CraftingManager.func_192413_b(this.crafting, world);
  }
  
  public void matrixChange(int slot) {
    if (this.recipe == null || !this.recipe.func_77569_a(this.crafting, func_145831_w()))
      this.recipe = findRecipe(); 
    this.recipeOutput = (this.recipe != null) ? this.recipe.func_77572_b(this.crafting) : StackUtil.emptyStack;
    this.newChange = true;
  }
  
  public void ingredientChange(int slot) {
    this.newChange = true;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (!(func_145831_w()).field_72995_K) {
      setOverclockRates();
      matrixChange(-1);
    } 
  }
  
  public void func_70296_d() {
    super.func_70296_d();
    if (!(func_145831_w()).field_72995_K) {
      setOverclockRates();
      this.attemptToBalance = true;
    } 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (this.attemptToBalance) {
      if (!this.ingredients.func_191420_l())
        int i = needsInvUpdate | (!((TIntCollection)(StackUtil.balanceStacks((IInventory)this.ingredients, this.acceptPredicate)).b).isEmpty() ? 1 : 0); 
      this.attemptToBalance = false;
    } 
    if (this.newChange) {
      this.canCraft = canCraft();
      this.newChange = false;
    } 
    if (this.canCraft && this.craftingOutput.canAdd(this.recipeOutput) && this.energy.useEnergy(this.energyConsume)) {
      setActive(true);
      if ((this.progress = (short)(this.progress + 1)) >= this.operationLength) {
        doCrafting();
        this.newChange = needsInvUpdate = true;
        this.progress = 0;
      } 
    } else {
      if (!hasRecipe())
        this.progress = 0; 
      setActive(false);
    } 
    needsInvUpdate |= this.upgradeSlot.tickNoMark();
    this.guiProgress = this.progress / this.operationLength;
    if (needsInvUpdate)
      super.func_70296_d(); 
  }
  
  public boolean hasRecipe() {
    return (this.recipe != null);
  }
  
  public boolean canCraft() {
    if (!hasRecipe())
      return false; 
    for (int slot = 0; slot < this.craftingGrid.length; slot++) {
      if (!StackUtil.isEmpty(this.craftingGrid[slot]) && !this.ingredientsRow[slot].accepts(this.ingredientsRow[slot].get()))
        return false; 
    } 
    return true;
  }
  
  protected void doCrafting() {
    for (int operation = 0; operation < this.operationsPerTick; operation++) {
      List<ItemStack> outputs = Collections.singletonList(this.recipeOutput);
      for (ItemStack stack : this.upgradeSlot) {
        if (stack != null && stack.func_77973_b() instanceof IUpgradeItem)
          ((IUpgradeItem)stack.func_77973_b()).onProcessEnd(stack, this, outputs); 
      } 
      craft();
      if (!hasRecipe() || !this.craftingOutput.canAdd(this.recipeOutput))
        break; 
    } 
  }
  
  protected void craft() {
    assert hasRecipe();
    assert this.craftingOutput.canAdd(this.recipeOutput);
    this.craftingOutput.add(this.recipeOutput);
    NonNullList<ItemStack> nonNullList = this.recipe.func_179532_b(this.ingredients);
    World world = func_145831_w();
    for (int slot = 0; slot < this.ingredientsRow.length; slot++) {
      ItemStack oldStack = this.ingredientsRow[slot].get();
      if (!StackUtil.isEmpty(oldStack) && !StackUtil.isEmpty(this.craftingGrid[slot])) {
        oldStack = StackUtil.decSize(oldStack);
        this.ingredientsRow[slot].put(oldStack);
      } 
      if (nonNullList.size() > slot && !StackUtil.isEmpty(nonNullList.get(slot))) {
        ItemStack newStack = nonNullList.get(slot);
        if (StackUtil.isEmpty(oldStack) && this.ingredientsRow[slot].accepts(newStack)) {
          this.ingredientsRow[slot].put(newStack);
        } else if (StackUtil.checkItemEqualityStrict(oldStack, newStack)) {
          this.ingredientsRow[slot].put(StackUtil.incSize(newStack, StackUtil.getSize(oldStack)));
        } else if (this.containerOutput.canAdd(newStack)) {
          this.containerOutput.add(newStack);
        } else {
          StackUtil.dropAsEntity(world, this.field_174879_c, newStack);
        } 
      } 
    } 
    for (int i = this.ingredientsRow.length; i < nonNullList.size(); i++) {
      ItemStack newStack = nonNullList.get(i);
      if (this.containerOutput.canAdd(newStack)) {
        this.containerOutput.add(newStack);
      } else {
        StackUtil.dropAsEntity(world, this.field_174879_c, newStack);
      } 
    } 
  }
  
  protected void setOverclockRates() {
    this.upgradeSlot.onChanged();
    double previousProgress = this.progress / this.operationLength;
    this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(40);
    this.operationLength = this.upgradeSlot.getOperationLength(40);
    this.energyConsume = this.upgradeSlot.getEnergyDemand(2);
    int tier = this.upgradeSlot.getTier(1);
    this.energy.setSinkTier(tier);
    this.dischargeSlot.setTier(tier);
    this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(20000, 40, 2));
    this.progress = (short)(int)Math.floor(previousProgress * this.operationLength + 0.1D);
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    switch (event) {
      case 0:
        matrixChange(-1);
        break;
    } 
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return UPGRADES;
  }
  
  public double getEnergy() {
    return this.energy.getEnergy();
  }
  
  public boolean useEnergy(double amount) {
    return this.energy.useEnergy(amount);
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)new ContainerBatchCrafter(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiBatchCrafter(new ContainerBatchCrafter(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    if ("progress".equals(name))
      return this.guiProgress; 
    throw new IllegalArgumentException("Unexpected value requested: " + name);
  }
}

package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IListRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.recipe.BasicListRecipeManager;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityRecycler extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack> {
  public TileEntityRecycler() {
    super(1, 45, 1);
    this.inputSlot = (InvSlotProcessable<IRecipeInput, Collection<ItemStack>, ItemStack>)new InvSlotProcessableGeneric((IInventorySlotHolder)this, "input", 1, (IMachineRecipeManager)Recipes.recycler);
  }
  
  public static void init() {
    Recipes.recycler = new RecyclerRecipeManager();
    Recipes.recyclerWhitelist = (IListRecipeManager)new BasicListRecipeManager();
    Recipes.recyclerBlacklist = (IListRecipeManager)new BasicListRecipeManager();
  }
  
  public static void initLate() {
    for (IRecipeInput input : ConfigUtil.asRecipeInputList(MainConfig.get(), "balance/recyclerBlacklist"))
      Recipes.recyclerBlacklist.add(input); 
    for (IRecipeInput input : ConfigUtil.asRecipeInputList(MainConfig.get(), "balance/recyclerWhitelist"))
      Recipes.recyclerWhitelist.add(input); 
  }
  
  public static int recycleChance() {
    return 8;
  }
  
  public String getStartSoundFile() {
    return "Machines/RecyclerOp.ogg";
  }
  
  public String getInterruptSoundFile() {
    return "Machines/InterruptOne.ogg";
  }
  
  public static boolean getIsItemBlacklisted(ItemStack aStack) {
    if (Recipes.recyclerWhitelist.isEmpty())
      return Recipes.recyclerBlacklist.contains(aStack); 
    return !Recipes.recyclerWhitelist.contains(aStack);
  }
  
  public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result, Collection<ItemStack> processResult) {
    this.inputSlot.consume(result);
    if (IC2.random.nextInt(recycleChance()) == 0)
      this.outputSlot.add(processResult); 
  }
  
  private static class RecyclerRecipeManager implements IBasicMachineRecipeManager {
    public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
      return false;
    }
    
    public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean replace, ItemStack... outputs) {
      return false;
    }
    
    public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
      if (StackUtil.isEmpty(input))
        return null; 
      RecipeOutput ret = new RecipeOutput(null, new ArrayList<>(getOutput(input)));
      if (adjustInput)
        input.func_190918_g(1); 
      return ret;
    }
    
    private static Collection<ItemStack> getOutput(ItemStack input) {
      return TileEntityRecycler.getIsItemBlacklisted(input) ? Collections.<ItemStack>emptyList() : Collections.<ItemStack>singletonList(ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap));
    }
    
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
      if (StackUtil.isEmpty(input))
        return null; 
      return (new MachineRecipe(Recipes.inputFactory
          .forStack(input, 1), 
          getOutput(input)))
        .getResult(StackUtil.copyWithSize(input, StackUtil.getSize(input) - 1));
    }
    
    public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
      throw new UnsupportedOperationException();
    }
    
    public boolean isIterable() {
      return false;
    }
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
  }
}

package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IScrapboxManager;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.block.type.ResourceBlock;
import ic2.core.item.type.CraftingItemType;
import ic2.core.item.type.DustResourceType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class ScrapboxRecipeManager implements IScrapboxManager {
  private final List<Drop> drops;
  
  public static void setup() {
    if (Recipes.scrapboxDrops != null)
      throw new IllegalStateException("already initialized"); 
    Recipes.scrapboxDrops = new ScrapboxRecipeManager();
  }
  
  public static void load() {
    ((ScrapboxRecipeManager)Recipes.scrapboxDrops).addBuiltinDrops();
  }
  
  private ScrapboxRecipeManager() {
    this.drops = new ArrayList<>();
  }
  
  public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
    if (!input.matches(ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap_box)))
      throw new IllegalArgumentException("currently only scrap boxes are supported"); 
    if (metadata == null || !metadata.func_74764_b("weight"))
      throw new IllegalArgumentException("no weight metadata"); 
    if (output.size() != 1)
      throw new IllegalArgumentException("currently only a single drop stack is supported"); 
    float weight = metadata.func_74760_g("weight");
    if (weight <= 0.0F || Float.isInfinite(weight) || Float.isNaN(weight))
      throw new IllegalArgumentException("invalid weight"); 
    addDrop(output.iterator().next(), weight);
    return true;
  }
  
  public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean replace, ItemStack... outputs) {
    return addRecipe(input, Arrays.asList(outputs), metadata, replace);
  }
  
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
    if (StackUtil.isEmpty(input) || !StackUtil.checkItemEquality(input, ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap_box)))
      return null; 
    if (this.drops.isEmpty())
      return null; 
    float chance = IC2.random.nextFloat() * Drop.topChance;
    int low = 0;
    int high = this.drops.size() - 1;
    while (low < high) {
      int mid = (high + low) / 2;
      if (chance < ((Drop)this.drops.get(mid)).upperChanceBound) {
        high = mid;
        continue;
      } 
      low = mid + 1;
    } 
    ItemStack drop = ((Drop)this.drops.get(low)).item.copy();
    return (new MachineRecipe(Recipes.inputFactory.forStack(ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap_box)), Collections.singletonList(drop))).getResult(StackUtil.copyShrunk(input, 1));
  }
  
  public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
    MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = apply(input, false);
    if (result == null || ((Collection)result.getOutput()).isEmpty())
      return null; 
    return new RecipeOutput(null, new ArrayList((Collection)result.getOutput()));
  }
  
  public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
    throw new UnsupportedOperationException();
  }
  
  public boolean isIterable() {
    return false;
  }
  
  public void addDrop(ItemStack drop, float rawChance) {
    this.drops.add(new Drop(drop, rawChance));
  }
  
  public ItemStack getDrop(ItemStack input, boolean adjustInput) {
    MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = apply(input, false);
    if (result == null || ((Collection)result.getOutput()).isEmpty())
      return null; 
    if (adjustInput)
      input.func_190920_e(StackUtil.getSize((ItemStack)result.getAdjustedInput())); 
    return ((Collection<ItemStack>)result.getOutput()).iterator().next();
  }
  
  public Map<ItemStack, Float> getDrops() {
    Map<ItemStack, Float> ret = new HashMap<>(this.drops.size());
    for (Drop drop : this.drops)
      ret.put(drop.item, Float.valueOf(drop.originalChance / Drop.topChance)); 
    return ret;
  }
  
  private static class Drop {
    final ItemStack item;
    
    final float originalChance;
    
    final float upperChanceBound;
    
    static float topChance;
    
    Drop(ItemStack item, float chance) {
      this.item = item;
      this.originalChance = chance;
      this.upperChanceBound = topChance += chance;
    }
  }
  
  private void addBuiltinDrops() {
    if (IC2.suddenlyHoes) {
      addDrop(Items.field_151017_I, 9001.0F);
    } else {
      addDrop(Items.field_151017_I, 5.01F);
    } 
    addDrop(Blocks.field_150346_d, 5.0F);
    addDrop(Items.field_151055_y, 4.0F);
    addDrop((Block)Blocks.field_150349_c, 3.0F);
    addDrop(Blocks.field_150351_n, 3.0F);
    addDrop(Blocks.field_150424_aL, 2.0F);
    addDrop(Items.field_151078_bh, 2.0F);
    addDrop(Items.field_151034_e, 1.5F);
    addDrop(Items.field_151025_P, 1.5F);
    addDrop(ItemName.filled_tin_can.getItemStack(), 1.5F);
    addDrop(Items.field_151041_m, 1.0F);
    addDrop(Items.field_151038_n, 1.0F);
    addDrop(Items.field_151039_o, 1.0F);
    addDrop(Blocks.SOUL_SAND, 1.0F);
    addDrop(Items.field_151155_ap, 1.0F);
    addDrop(Items.field_151116_aA, 1.0F);
    addDrop(Items.field_151008_G, 1.0F);
    addDrop(Items.field_151103_aS, 1.0F);
    addDrop(Items.field_151157_am, 0.9F);
    addDrop(Items.field_151083_be, 0.9F);
    addDrop(Blocks.field_150423_aK, 0.9F);
    addDrop(Items.field_151077_bg, 0.9F);
    addDrop(Items.field_151143_au, 0.01F);
    addDrop(Items.REDSTONE, 0.9F);
    addDrop(ItemName.crafting.getItemStack((Enum)CraftingItemType.rubber), 0.8F);
    addDrop(Items.field_151114_aO, 0.8F);
    addDrop(ItemName.dust.getItemStack((Enum)DustResourceType.coal), 0.8F);
    addDrop(ItemName.dust.getItemStack((Enum)DustResourceType.copper), 0.8F);
    addDrop(ItemName.dust.getItemStack((Enum)DustResourceType.tin), 0.8F);
    addDrop(ItemName.single_use_battery.getItemStack(), 0.7F);
    addDrop(ItemName.dust.getItemStack((Enum)DustResourceType.iron), 0.7F);
    addDrop(ItemName.dust.getItemStack((Enum)DustResourceType.gold), 0.7F);
    addDrop(Items.field_151123_aH, 0.6F);
    addDrop(Blocks.field_150366_p, 0.5F);
    addDrop((Item)Items.field_151169_ag, 0.01F);
    addDrop(Blocks.field_150352_o, 0.5F);
    addDrop(Items.field_151105_aU, 0.5F);
    addDrop(Items.DIAMOND, 0.1F);
    addDrop(Items.field_151166_bC, 0.05F);
    addDrop(Items.field_151079_bi, 0.08F);
    addDrop(Items.field_151072_bj, 0.04F);
    addDrop(Items.field_151110_aK, 0.8F);
    addDrop(BlockName.resource.getItemStack((Enum)ResourceBlock.copper_ore), 0.7F);
    addDrop(BlockName.resource.getItemStack((Enum)ResourceBlock.tin_ore), 0.7F);
  }
  
  private void addDrop(Block block, float rawChance) {
    addDrop(new ItemStack(block), rawChance);
  }
  
  private void addDrop(Item item, float rawChance) {
    addDrop(new ItemStack(item), rawChance);
  }
}

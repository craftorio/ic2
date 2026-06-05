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
   private final List<ScrapboxRecipeManager.Drop> drops = new ArrayList<>();

   public static void setup() {
      if (Recipes.scrapboxDrops != null) {
         throw new IllegalStateException("already initialized");
      }

      Recipes.scrapboxDrops = new ScrapboxRecipeManager();
   }

   public static void load() {
      ((ScrapboxRecipeManager)Recipes.scrapboxDrops).addBuiltinDrops();
   }

   private ScrapboxRecipeManager() {
   }

   public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
      if (!input.matches(ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
         throw new IllegalArgumentException("currently only scrap boxes are supported");
      }

      if (metadata != null && metadata.hasKey("weight")) {
         if (output.size() != 1) {
            throw new IllegalArgumentException("currently only a single drop stack is supported");
         } else {
            float weight = metadata.getFloat("weight");
            if (!(weight <= 0.0F) && !Float.isInfinite(weight) && !Float.isNaN(weight)) {
               this.addDrop(output.iterator().next(), weight);
               return true;
            } else {
               throw new IllegalArgumentException("invalid weight");
            }
         }
      } else {
         throw new IllegalArgumentException("no weight metadata");
      }
   }

   @Override
   public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean replace, ItemStack... outputs) {
      return this.addRecipe(input, Arrays.asList(outputs), metadata, replace);
   }

   public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
      if (!StackUtil.isEmpty(input) && StackUtil.checkItemEquality(input, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
         if (this.drops.isEmpty()) {
            return null;
         }

         float chance = IC2.random.nextFloat() * ScrapboxRecipeManager.Drop.topChance;
         int low = 0;
         int high = this.drops.size() - 1;

         while (low < high) {
            int mid = (high + low) / 2;
            if (chance < this.drops.get(mid).upperChanceBound) {
               high = mid;
            } else {
               low = mid + 1;
            }
         }

         ItemStack drop = this.drops.get(low).item.copy();
         return new MachineRecipe<>(Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.scrap_box)), Collections.singletonList(drop))
            .getResult(StackUtil.copyShrunk(input, 1));
      } else {
         return null;
      }
   }

   @Override
   public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.apply(input, false);
      return result != null && !result.getOutput().isEmpty() ? new RecipeOutput(null, new ArrayList<>(result.getOutput())) : null;
   }

   @Override
   public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isIterable() {
      return false;
   }

   @Override
   public void addDrop(ItemStack drop, float rawChance) {
      this.drops.add(new ScrapboxRecipeManager.Drop(drop, rawChance));
   }

   @Override
   public ItemStack getDrop(ItemStack input, boolean adjustInput) {
      MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.apply(input, false);
      if (result != null && !result.getOutput().isEmpty()) {
         if (adjustInput) {
            input.setCount(StackUtil.getSize(result.getAdjustedInput()));
         }

         return result.getOutput().iterator().next();
      } else {
         return null;
      }
   }

   @Override
   public Map<ItemStack, Float> getDrops() {
      Map<ItemStack, Float> ret = new HashMap<>(this.drops.size());

      for (ScrapboxRecipeManager.Drop drop : this.drops) {
         ret.put(drop.item, drop.originalChance / ScrapboxRecipeManager.Drop.topChance);
      }

      return ret;
   }

   private void addBuiltinDrops() {
      if (IC2.suddenlyHoes) {
         this.addDrop(Items.WOODEN_HOE, 9001.0F);
      } else {
         this.addDrop(Items.WOODEN_HOE, 5.01F);
      }

      this.addDrop(Blocks.DIRT, 5.0F);
      this.addDrop(Items.STICK, 4.0F);
      this.addDrop(Blocks.GRASS, 3.0F);
      this.addDrop(Blocks.GRAVEL, 3.0F);
      this.addDrop(Blocks.NETHERRACK, 2.0F);
      this.addDrop(Items.ROTTEN_FLESH, 2.0F);
      this.addDrop(Items.APPLE, 1.5F);
      this.addDrop(Items.BREAD, 1.5F);
      this.addDrop(ItemName.filled_tin_can.getItemStack(), 1.5F);
      this.addDrop(Items.WOODEN_SWORD, 1.0F);
      this.addDrop(Items.WOODEN_SHOVEL, 1.0F);
      this.addDrop(Items.WOODEN_PICKAXE, 1.0F);
      this.addDrop(Blocks.SOUL_SAND, 1.0F);
      this.addDrop(Items.SIGN, 1.0F);
      this.addDrop(Items.LEATHER, 1.0F);
      this.addDrop(Items.FEATHER, 1.0F);
      this.addDrop(Items.BONE, 1.0F);
      this.addDrop(Items.COOKED_PORKCHOP, 0.9F);
      this.addDrop(Items.COOKED_BEEF, 0.9F);
      this.addDrop(Blocks.PUMPKIN, 0.9F);
      this.addDrop(Items.COOKED_CHICKEN, 0.9F);
      this.addDrop(Items.MINECART, 0.01F);
      this.addDrop(Items.REDSTONE, 0.9F);
      this.addDrop(ItemName.crafting.getItemStack(CraftingItemType.rubber), 0.8F);
      this.addDrop(Items.GLOWSTONE_DUST, 0.8F);
      this.addDrop(ItemName.dust.getItemStack(DustResourceType.coal), 0.8F);
      this.addDrop(ItemName.dust.getItemStack(DustResourceType.copper), 0.8F);
      this.addDrop(ItemName.dust.getItemStack(DustResourceType.tin), 0.8F);
      this.addDrop(ItemName.single_use_battery.getItemStack(), 0.7F);
      this.addDrop(ItemName.dust.getItemStack(DustResourceType.iron), 0.7F);
      this.addDrop(ItemName.dust.getItemStack(DustResourceType.gold), 0.7F);
      this.addDrop(Items.SLIME_BALL, 0.6F);
      this.addDrop(Blocks.IRON_ORE, 0.5F);
      this.addDrop(Items.GOLDEN_HELMET, 0.01F);
      this.addDrop(Blocks.GOLD_ORE, 0.5F);
      this.addDrop(Items.CAKE, 0.5F);
      this.addDrop(Items.DIAMOND, 0.1F);
      this.addDrop(Items.EMERALD, 0.05F);
      this.addDrop(Items.ENDER_PEARL, 0.08F);
      this.addDrop(Items.BLAZE_ROD, 0.04F);
      this.addDrop(Items.EGG, 0.8F);
      this.addDrop(BlockName.resource.getItemStack(ResourceBlock.copper_ore), 0.7F);
      this.addDrop(BlockName.resource.getItemStack(ResourceBlock.tin_ore), 0.7F);
   }

   private void addDrop(Block block, float rawChance) {
      this.addDrop(new ItemStack(block), rawChance);
   }

   private void addDrop(Item item, float rawChance) {
      this.addDrop(new ItemStack(item), rawChance);
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
}

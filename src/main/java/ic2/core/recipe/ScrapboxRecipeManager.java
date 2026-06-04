// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import ic2.core.block.type.ResourceBlock;
import ic2.core.ref.BlockName;
import net.minecraft.item.Item;
import ic2.core.item.type.DustResourceType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.MachineRecipe;
import java.util.Collections;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import ic2.api.recipe.MachineRecipeResult;
import java.util.Arrays;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import java.util.ArrayList;
import ic2.api.recipe.Recipes;
import java.util.List;
import ic2.api.recipe.IScrapboxManager;

public final class ScrapboxRecipeManager implements IScrapboxManager
{
    private final List<Drop> drops;
    
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
        this.drops = new ArrayList<Drop>();
    }
    
    @Override
    public boolean addRecipe(final IRecipeInput input, final Collection<ItemStack> output, final NBTTagCompound metadata, final boolean replace) {
        if (!input.matches(ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
            throw new IllegalArgumentException("currently only scrap boxes are supported");
        }
        if (metadata == null || !metadata.hasKey("weight")) {
            throw new IllegalArgumentException("no weight metadata");
        }
        if (output.size() != 1) {
            throw new IllegalArgumentException("currently only a single drop stack is supported");
        }
        final float weight = metadata.getFloat("weight");
        if (weight <= 0.0f || Float.isInfinite(weight) || Float.isNaN(weight)) {
            throw new IllegalArgumentException("invalid weight");
        }
        this.addDrop(output.iterator().next(), weight);
        return true;
    }
    
    @Override
    public boolean addRecipe(final IRecipeInput input, final NBTTagCompound metadata, final boolean replace, final ItemStack... outputs) {
        return this.addRecipe(input, (Collection<ItemStack>)Arrays.asList(outputs), metadata, replace);
    }
    
    @Override
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(final ItemStack input, final boolean acceptTest) {
        if (StackUtil.isEmpty(input) || !StackUtil.checkItemEquality(input, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
            return null;
        }
        if (this.drops.isEmpty()) {
            return null;
        }
        final float chance = IC2.random.nextFloat() * Drop.topChance;
        int low = 0;
        int high = this.drops.size() - 1;
        while (low < high) {
            final int mid = (high + low) / 2;
            if (chance < this.drops.get(mid).upperChanceBound) {
                high = mid;
            }
            else {
                low = mid + 1;
            }
        }
        final ItemStack drop = this.drops.get(low).item.copy();
        return new MachineRecipe<IRecipeInput, Collection<ItemStack>>(Recipes.inputFactory.forStack(ItemName.crafting.getItemStack(CraftingItemType.scrap_box)), Collections.singletonList(drop)).getResult(StackUtil.copyShrunk(input, 1));
    }
    
    @Override
    public RecipeOutput getOutputFor(final ItemStack input, final boolean adjustInput) {
        final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.apply(input, false);
        if (result == null || result.getOutput().isEmpty()) {
            return null;
        }
        return new RecipeOutput(null, new ArrayList<ItemStack>(result.getOutput()));
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
    public void addDrop(final ItemStack drop, final float rawChance) {
        this.drops.add(new Drop(drop, rawChance));
    }
    
    @Override
    public ItemStack getDrop(final ItemStack input, final boolean adjustInput) {
        final MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result = this.apply(input, false);
        if (result == null || result.getOutput().isEmpty()) {
            return null;
        }
        if (adjustInput) {
            input.setCount(StackUtil.getSize(result.getAdjustedInput()));
        }
        return result.getOutput().iterator().next();
    }
    
    @Override
    public Map<ItemStack, Float> getDrops() {
        final Map<ItemStack, Float> ret = new HashMap<ItemStack, Float>(this.drops.size());
        for (final Drop drop : this.drops) {
            ret.put(drop.item, drop.originalChance / Drop.topChance);
        }
        return ret;
    }
    
    private void addBuiltinDrops() {
        if (IC2.suddenlyHoes) {
            this.addDrop(Items.WOODEN_HOE, 9001.0f);
        }
        else {
            this.addDrop(Items.WOODEN_HOE, 5.01f);
        }
        this.addDrop(Blocks.DIRT, 5.0f);
        this.addDrop(Items.STICK, 4.0f);
        this.addDrop((Block)Blocks.GRASS, 3.0f);
        this.addDrop(Blocks.GRAVEL, 3.0f);
        this.addDrop(Blocks.NETHERRACK, 2.0f);
        this.addDrop(Items.ROTTEN_FLESH, 2.0f);
        this.addDrop(Items.APPLE, 1.5f);
        this.addDrop(Items.BREAD, 1.5f);
        this.addDrop(ItemName.filled_tin_can.getItemStack(), 1.5f);
        this.addDrop(Items.WOODEN_SWORD, 1.0f);
        this.addDrop(Items.WOODEN_SHOVEL, 1.0f);
        this.addDrop(Items.WOODEN_PICKAXE, 1.0f);
        this.addDrop(Blocks.SOUL_SAND, 1.0f);
        this.addDrop(Items.SIGN, 1.0f);
        this.addDrop(Items.LEATHER, 1.0f);
        this.addDrop(Items.FEATHER, 1.0f);
        this.addDrop(Items.BONE, 1.0f);
        this.addDrop(Items.COOKED_PORKCHOP, 0.9f);
        this.addDrop(Items.COOKED_BEEF, 0.9f);
        this.addDrop(Blocks.PUMPKIN, 0.9f);
        this.addDrop(Items.COOKED_CHICKEN, 0.9f);
        this.addDrop(Items.MINECART, 0.01f);
        this.addDrop(Items.REDSTONE, 0.9f);
        this.addDrop(ItemName.crafting.getItemStack(CraftingItemType.rubber), 0.8f);
        this.addDrop(Items.GLOWSTONE_DUST, 0.8f);
        this.addDrop(ItemName.dust.getItemStack(DustResourceType.coal), 0.8f);
        this.addDrop(ItemName.dust.getItemStack(DustResourceType.copper), 0.8f);
        this.addDrop(ItemName.dust.getItemStack(DustResourceType.tin), 0.8f);
        this.addDrop(ItemName.single_use_battery.getItemStack(), 0.7f);
        this.addDrop(ItemName.dust.getItemStack(DustResourceType.iron), 0.7f);
        this.addDrop(ItemName.dust.getItemStack(DustResourceType.gold), 0.7f);
        this.addDrop(Items.SLIME_BALL, 0.6f);
        this.addDrop(Blocks.IRON_ORE, 0.5f);
        this.addDrop((Item)Items.GOLDEN_HELMET, 0.01f);
        this.addDrop(Blocks.GOLD_ORE, 0.5f);
        this.addDrop(Items.CAKE, 0.5f);
        this.addDrop(Items.DIAMOND, 0.1f);
        this.addDrop(Items.EMERALD, 0.05f);
        this.addDrop(Items.ENDER_PEARL, 0.08f);
        this.addDrop(Items.BLAZE_ROD, 0.04f);
        this.addDrop(Items.EGG, 0.8f);
        this.addDrop(BlockName.resource.getItemStack(ResourceBlock.copper_ore), 0.7f);
        this.addDrop(BlockName.resource.getItemStack(ResourceBlock.tin_ore), 0.7f);
    }
    
    private void addDrop(final Block block, final float rawChance) {
        this.addDrop(new ItemStack(block), rawChance);
    }
    
    private void addDrop(final Item item, final float rawChance) {
        this.addDrop(new ItemStack(item), rawChance);
    }
    
    private static class Drop
    {
        final ItemStack item;
        final float originalChance;
        final float upperChanceBound;
        static float topChance;
        
        Drop(final ItemStack item, final float chance) {
            this.item = item;
            this.originalChance = chance;
            this.upperChanceBound = (Drop.topChance += chance);
        }
    }
}

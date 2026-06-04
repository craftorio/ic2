// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import java.util.Collections;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.Collection;
import net.minecraftforge.common.MinecraftForge;
import java.util.Iterator;
import ic2.core.util.StackUtil;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.IRecipeInput;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import net.minecraft.item.Item;
import ic2.api.recipe.MachineRecipe;
import java.util.Map;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IMachineRecipeManager;

public abstract class MachineRecipeHelper<RI, RO> implements IMachineRecipeManager<RI, RO, ItemStack>
{
    protected final Map<RI, MachineRecipe<RI, RO>> recipes;
    private final Map<Item, List<MachineRecipe<RI, RO>>> recipeCache;
    private final List<MachineRecipe<RI, RO>> uncacheableRecipes;
    private static boolean oreRegisterEventSubscribed;
    private static final Set<MachineRecipeHelper<?, ?>> watchingManagers;
    
    public MachineRecipeHelper() {
        this.recipes = new HashMap<RI, MachineRecipe<RI, RO>>();
        this.recipeCache = new IdentityHashMap<Item, List<MachineRecipe<RI, RO>>>();
        this.uncacheableRecipes = new ArrayList<MachineRecipe<RI, RO>>();
    }
    
    protected abstract IRecipeInput getForInput(final RI p0);
    
    protected IRecipeInput getForRecipe(final MachineRecipe<RI, RO> recipe) {
        return this.getForInput(recipe.getInput());
    }
    
    protected boolean consumeContainer(final ItemStack input, final ItemStack container, final MachineRecipe<RI, RO> recipe) {
        return false;
    }
    
    @Override
    public MachineRecipeResult<RI, RO, ItemStack> apply(final ItemStack input, final boolean acceptTest) {
        if (StackUtil.isEmpty(input)) {
            return null;
        }
        final MachineRecipe<RI, RO> recipe = this.getRecipe(input);
        if (recipe == null) {
            return null;
        }
        final IRecipeInput recipeInput = this.getForRecipe(recipe);
        if (StackUtil.getSize(input) < recipeInput.getAmount()) {
            return null;
        }
        ItemStack adjustedInput;
        if (input.getItem().hasContainerItem(input) && !StackUtil.isEmpty(adjustedInput = input.getItem().getContainerItem(input)) && !acceptTest && !this.consumeContainer(input, adjustedInput, recipe)) {
            if (!acceptTest && StackUtil.getSize(input) != recipeInput.getAmount()) {
                return null;
            }
            adjustedInput = StackUtil.copy(adjustedInput);
        }
        else {
            adjustedInput = StackUtil.copyWithSize(input, StackUtil.getSize(input) - recipeInput.getAmount());
        }
        return recipe.getResult(adjustedInput);
    }
    
    @Override
    public Iterable<? extends MachineRecipe<RI, RO>> getRecipes() {
        return new Iterable<MachineRecipe<RI, RO>>() {
            @Override
            public Iterator<MachineRecipe<RI, RO>> iterator() {
                return new Iterator<MachineRecipe<RI, RO>>() {
                    private final Iterator<MachineRecipe<RI, RO>> recipeIt = MachineRecipeHelper.this.recipes.values().iterator();
                    private RI lastInput;
                    
                    @Override
                    public boolean hasNext() {
                        return this.recipeIt.hasNext();
                    }
                    
                    @Override
                    public MachineRecipe<RI, RO> next() {
                        final MachineRecipe<RI, RO> next = this.recipeIt.next();
                        this.lastInput = next.getInput();
                        return next;
                    }
                    
                    @Override
                    public void remove() {
                        this.recipeIt.remove();
                        MachineRecipeHelper.this.removeCachedRecipes(this.lastInput);
                    }
                };
            }
        };
    }
    
    @Override
    public boolean isIterable() {
        return true;
    }
    
    protected MachineRecipe<RI, RO> getRecipe(final ItemStack input) {
        if (StackUtil.isEmpty(input)) {
            return null;
        }
        final List<MachineRecipe<RI, RO>> recipes = this.recipeCache.get(input.getItem());
        if (recipes != null) {
            for (final MachineRecipe<RI, RO> recipe : recipes) {
                if (this.getForRecipe(recipe).matches(input)) {
                    return recipe;
                }
            }
        }
        for (final MachineRecipe<RI, RO> recipe : this.uncacheableRecipes) {
            if (this.getForRecipe(recipe).matches(input)) {
                return recipe;
            }
        }
        return null;
    }
    
    protected void addToCache(final MachineRecipe<RI, RO> recipe) {
        final Collection<Item> items = this.getItemsFromRecipe(recipe.getInput());
        if (items != null) {
            for (final Item item : items) {
                this.addToCache(item, recipe);
            }
            if (recipe.getInput().getClass() == RecipeInputOreDict.class) {
                if (!MachineRecipeHelper.oreRegisterEventSubscribed) {
                    MinecraftForge.EVENT_BUS.register((Object)MachineRecipeHelper.class);
                    MachineRecipeHelper.oreRegisterEventSubscribed = true;
                }
                MachineRecipeHelper.watchingManagers.add(this);
            }
        }
        else {
            this.uncacheableRecipes.add(recipe);
        }
    }
    
    private void addToCache(final Item item, final MachineRecipe<RI, RO> recipe) {
        List<MachineRecipe<RI, RO>> recipes = this.recipeCache.get(item);
        if (recipes == null) {
            recipes = new ArrayList<MachineRecipe<RI, RO>>();
            this.recipeCache.put(item, recipes);
        }
        if (!recipes.contains(recipe)) {
            recipes.add(recipe);
        }
    }
    
    protected void removeCachedRecipes(final RI input) {
        final Collection<Item> items = this.getItemsFromRecipe(input);
        if (items != null) {
            for (final Item item : items) {
                final List<MachineRecipe<RI, RO>> recipes = this.recipeCache.get(item);
                if (recipes == null) {
                    IC2.log.warn(LogCategory.Recipe, "Inconsistent recipe cache, the entry for the item " + item + " is missing.");
                }
                else {
                    this.removeInputFromRecipes(recipes.iterator(), input);
                    if (!recipes.isEmpty()) {
                        continue;
                    }
                    this.recipeCache.remove(item);
                }
            }
        }
        else {
            this.removeInputFromRecipes(this.uncacheableRecipes.iterator(), input);
        }
    }
    
    private void removeInputFromRecipes(final Iterator<MachineRecipe<RI, RO>> it, final RI target) {
        assert target != null;
        while (it.hasNext()) {
            if (target.equals(it.next().getInput())) {
                it.remove();
            }
        }
    }
    
    private Collection<Item> getItemsFromRecipe(final RI input) {
        return this.getItemsFromRecipe(this.getForInput(input));
    }
    
    private Collection<Item> getItemsFromRecipe(final IRecipeInput recipe) {
        final Class<?> recipeClass = recipe.getClass();
        if (recipeClass == RecipeInputItemStack.class || recipeClass == RecipeInputOreDict.class) {
            final List<ItemStack> inputs = recipe.getInputs();
            final Set<Item> ret = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>(inputs.size()));
            for (final ItemStack stack : inputs) {
                ret.add(stack.getItem());
            }
            return ret;
        }
        return null;
    }
    
    private void onOreRegister(final Item item, final String name) {
        for (final MachineRecipe<RI, RO> rawRecipe : this.recipes.values()) {
            if (rawRecipe.getInput().getClass() != RecipeInputOreDict.class) {
                continue;
            }
            final RecipeInputOreDict recipe = (RecipeInputOreDict)rawRecipe.getInput();
            if (!recipe.input.equals(name)) {
                continue;
            }
            this.addToCache(item, rawRecipe);
        }
    }
    
    @SubscribeEvent
    public static void onOreRegister(final OreDictionary.OreRegisterEvent event) {
        final Item item = event.getOre().getItem();
        if (item == null) {
            IC2.log.warn(LogCategory.Recipe, "Found null item ore dict registration.", new Throwable());
            return;
        }
        for (final MachineRecipeHelper<?, ?> manager : MachineRecipeHelper.watchingManagers) {
            manager.onOreRegister(item, event.getName());
        }
    }
    
    static {
        watchingManagers = Collections.newSetFromMap(new IdentityHashMap<MachineRecipeHelper<?, ?>, Boolean>());
    }
}

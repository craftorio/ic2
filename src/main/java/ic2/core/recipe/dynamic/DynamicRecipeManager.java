// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import ic2.core.init.MainConfig;
import java.util.Collections;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.util.StackUtil;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import java.util.ListIterator;
import net.minecraftforge.fluids.Fluid;
import java.util.Iterator;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import net.minecraft.item.Item;
import java.util.Collection;
import java.util.Map;

public class DynamicRecipeManager implements IDynamicRecipeManager
{
    protected final Map<Collection<RecipeInputIngredient>, DynamicRecipe> recipes;
    private final Map<Item, List<DynamicRecipe>> recipeCacheItem;
    private final Map<String, List<DynamicRecipe>> recipeCacheFluid;
    private final List<DynamicRecipe> uncacheableRecipes;
    private static boolean oreRegisterEventSubscribed;
    private static final Set<DynamicRecipeManager> watchingManagers;
    
    public DynamicRecipeManager() {
        this.recipes = new HashMap<Collection<RecipeInputIngredient>, DynamicRecipe>();
        this.recipeCacheItem = new IdentityHashMap<Item, List<DynamicRecipe>>();
        this.recipeCacheFluid = new IdentityHashMap<String, List<DynamicRecipe>>();
        this.uncacheableRecipes = new ArrayList<DynamicRecipe>();
    }
    
    public DynamicRecipe createRecipe() {
        return new DynamicRecipe(this);
    }
    
    @Override
    public boolean addRecipe(DynamicRecipe recipe, final boolean replace) {
        if (recipe.getInputIngredients() == null) {
            throw new NullPointerException("The recipe input is null");
        }
        if (recipe.getInputIngredients().size() <= 0) {
            throw new IllegalArgumentException("No inputs");
        }
        if (recipe.getOutputIngredients() == null) {
            throw new NullPointerException("The recipe output is null");
        }
        if (recipe.getOutputIngredients().size() <= 0) {
            throw new IllegalArgumentException("No outputs");
        }
        final List<RecipeInputIngredient> listOfInputs = new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients().size());
        for (final RecipeInputIngredient entry : recipe.getInputIngredients()) {
            if (entry.isEmpty()) {
                this.displayError("The RecipeInputIngredient " + entry.toStringSafe() + " is invalid.");
                return false;
            }
            listOfInputs.add(entry);
        }
        final List<RecipeOutputIngredient> listOfOutputs = new ArrayList<RecipeOutputIngredient>(recipe.getOutputIngredients().size());
        for (final RecipeOutputIngredient entry2 : recipe.getOutputIngredients()) {
            if (entry2.isEmpty()) {
                this.displayError("The RecipeOutputIngredient " + entry2.toStringSafe() + " is invalid.");
                return false;
            }
            listOfOutputs.add(entry2);
        }
        final DynamicRecipe temp = this.getRecipe(recipe.getInputIngredients());
        if (temp != null) {
            if (!replace) {
                IC2.log.error(LogCategory.Recipe, "Skipping %s => %s due to duplicate recipe for %s (%s => %s)", recipe.getInputIngredients(), recipe.getOutputIngredients(), recipe.getInputIngredients(), recipe.getInputIngredients(), recipe.getOutputIngredients());
                return false;
            }
            do {
                this.recipes.remove(recipe.getInputIngredients());
                this.removeCachedRecipes(recipe.getInputIngredients());
                recipe = this.getRecipe(recipe.getInputIngredients());
            } while (recipe != null);
        }
        final DynamicRecipe newRecipe = this.createRecipe().withInput(listOfInputs).withOutput(listOfOutputs).withOperationEnergyCost(recipe.getOperationEnergyCost()).withOperationDurationTicks(recipe.getOperationDuration()).withMetadata(recipe.getMetadata());
        this.recipes.put(recipe.getInputIngredients(), newRecipe);
        this.addToCache(newRecipe);
        return true;
    }
    
    protected DynamicRecipe getRecipe(final Collection<RecipeInputIngredient> input) {
        if (input.isEmpty()) {
            return null;
        }
        final List<DynamicRecipe> recipes = new ArrayList<DynamicRecipe>();
        for (final RecipeInputIngredient entry : input) {
            final Object unspecific = entry.getUnspecific();
            if (unspecific instanceof Item) {
                if (this.recipeCacheItem.get(unspecific) == null) {
                    continue;
                }
                recipes.addAll(this.recipeCacheItem.get(unspecific));
            }
            else {
                if (!(unspecific instanceof Fluid) || !this.recipeCacheFluid.containsKey(((Fluid)unspecific).getName())) {
                    continue;
                }
                recipes.addAll(this.recipeCacheFluid.get(((Fluid)unspecific).getName()));
            }
        }
        if (!recipes.isEmpty()) {
        Label_0173:
            for (final DynamicRecipe recipe : recipes) {
                if (input.size() != recipe.getInputIngredients().size()) {
                    continue;
                }
                final ListIterator<RecipeInputIngredient> itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
            Label_0240:
                for (final RecipeInputIngredient entry2 : input) {
                    while (itB.hasNext()) {
                        final RecipeInputIngredient temp = itB.next();
                        if (temp.matches(entry2.ingredient) && entry2.getCount() >= temp.getCount()) {
                            itB.remove();
                            while (itB.hasPrevious()) {
                                itB.previous();
                            }
                            continue Label_0240;
                        }
                    }
                    continue Label_0173;
                }
                return recipe;
            }
        }
    Label_0354:
        for (final DynamicRecipe recipe : this.uncacheableRecipes) {
            if (input.size() != recipe.getInputIngredients().size()) {
                continue;
            }
            final ListIterator<RecipeInputIngredient> itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
        Label_0421:
            for (final RecipeInputIngredient entry2 : input) {
                while (itB.hasNext()) {
                    final RecipeInputIngredient temp = itB.next();
                    if (temp.matches(entry2.ingredient) && entry2.getCount() >= temp.getCount()) {
                        itB.remove();
                        while (itB.hasPrevious()) {
                            itB.previous();
                        }
                        continue Label_0421;
                    }
                }
                continue Label_0354;
            }
            return recipe;
        }
        return null;
    }
    
    public DynamicRecipe findRecipe(final ItemStack[] items, final FluidStack[] fluids) {
        final List<RecipeInputIngredient> inputs = new ArrayList<RecipeInputIngredient>();
        for (final ItemStack stack : items) {
            if (StackUtil.isEmpty(stack)) {
                return null;
            }
            inputs.add(RecipeInputItemStack.of(stack));
        }
        for (final FluidStack stack2 : fluids) {
            if (stack2.amount <= 0) {
                return null;
            }
            inputs.add(RecipeInputFluidStack.of(stack2));
        }
        if (inputs.isEmpty()) {
            return null;
        }
        final List<DynamicRecipe> recipes = new ArrayList<DynamicRecipe>();
        for (final RecipeInputIngredient entry : inputs) {
            final Object unspecific = entry.getUnspecific();
            if (unspecific instanceof Item) {
                if (this.recipeCacheItem.get(unspecific) == null) {
                    continue;
                }
                recipes.addAll(this.recipeCacheItem.get(unspecific));
            }
            else {
                if (!(unspecific instanceof Fluid) || !this.recipeCacheFluid.containsKey(((Fluid)unspecific).getName())) {
                    continue;
                }
                recipes.addAll(this.recipeCacheFluid.get(((Fluid)unspecific).getName()));
            }
        }
        if (!recipes.isEmpty()) {
        Label_0296:
            for (final DynamicRecipe recipe : recipes) {
                if (inputs.size() != recipe.getInputIngredients().size()) {
                    continue;
                }
                final ListIterator<RecipeInputIngredient> itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
            Label_0365:
                for (final RecipeInputIngredient entry2 : inputs) {
                    while (itB.hasNext()) {
                        final RecipeInputIngredient temp = itB.next();
                        if (temp.matches(entry2.ingredient) && entry2.getCount() >= temp.getCount()) {
                            itB.remove();
                            while (itB.hasPrevious()) {
                                itB.previous();
                            }
                            continue Label_0365;
                        }
                    }
                    continue Label_0296;
                }
                return recipe;
            }
        }
    Label_0480:
        for (final DynamicRecipe recipe : this.uncacheableRecipes) {
            if (inputs.size() != recipe.getInputIngredients().size()) {
                continue;
            }
            final ListIterator<RecipeInputIngredient> itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
        Label_0549:
            for (final RecipeInputIngredient entry2 : inputs) {
                while (itB.hasNext()) {
                    final RecipeInputIngredient temp = itB.next();
                    if (temp.matches(entry2.ingredient) && entry2.getCount() >= temp.getCount()) {
                        itB.remove();
                        while (itB.hasPrevious()) {
                            itB.previous();
                        }
                        continue Label_0549;
                    }
                }
                continue Label_0480;
            }
            return recipe;
        }
        return null;
    }
    
    public boolean isPartOfRecipe(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        final RecipeInputItemStack subject = RecipeInputItemStack.of(stack);
        final List<DynamicRecipe> recipes = new ArrayList<DynamicRecipe>();
        final Object unspecific = subject.getUnspecific();
        if (unspecific instanceof Item) {
            if (this.recipeCacheItem.get(unspecific) != null) {
                recipes.addAll(this.recipeCacheItem.get(unspecific));
            }
        }
        else if (unspecific instanceof Fluid && this.recipeCacheFluid.containsKey(((Fluid)unspecific).getName())) {
            recipes.addAll(this.recipeCacheFluid.get(((Fluid)unspecific).getName()));
        }
        if (!recipes.isEmpty()) {
            for (final DynamicRecipe recipe : recipes) {
                final ListIterator<RecipeInputIngredient> itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
                while (itB.hasNext()) {
                    final RecipeInputIngredient temp = itB.next();
                    if (temp.matches(subject.ingredient)) {
                        return true;
                    }
                }
            }
        }
        if (!this.uncacheableRecipes.isEmpty()) {
            for (final DynamicRecipe recipe : this.uncacheableRecipes) {
                final ListIterator<RecipeInputIngredient> itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
                final RecipeInputIngredient temp = itB.next();
                if (temp.matches(subject.ingredient)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public DynamicRecipe apply(final ItemStack[] items, final FluidStack[] fluids, final boolean simulate) {
        final List<RecipeInputIngredient> inputs = new ArrayList<RecipeInputIngredient>();
        for (final ItemStack stack : items) {
            if (StackUtil.isEmpty(stack)) {
                return null;
            }
            inputs.add(RecipeInputItemStack.of(stack));
        }
        for (final FluidStack stack2 : fluids) {
            if (stack2.amount <= 0) {
                return null;
            }
            inputs.add(RecipeInputFluidStack.of(stack2));
        }
        final DynamicRecipe recipe = this.getRecipe(inputs);
        if (recipe == null) {
            return null;
        }
        if (items.length + fluids.length != recipe.getInputIngredients().size()) {
            return null;
        }
        ListIterator<RecipeInputIngredient> itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
    Label_0178:
        for (final RecipeInputIngredient entry : inputs) {
            while (itB.hasNext()) {
                final RecipeInputIngredient temp = itB.next();
                if (temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()) {
                    itB.remove();
                    while (itB.hasPrevious()) {
                        itB.previous();
                    }
                    continue Label_0178;
                }
            }
            return null;
        }
        if (!simulate) {
            itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
            Label_0311:
            for (final RecipeInputIngredient entry : inputs) {
                while (itB.hasNext()) {
                    final RecipeInputIngredient temp = itB.next();
                    if (temp.matches(entry.ingredient)) {
                        entry.shrink(temp.getCount());
                        itB.remove();
                        while (itB.hasPrevious()) {
                            itB.previous();
                        }
                        continue Label_0311;
                    }
                }
                return null;
            }
        }
        return recipe;
    }
    
    public boolean apply(final DynamicRecipe recipe, final ItemStack[] items, final FluidStack[] fluids, final boolean simulate) {
        if (recipe == null) {
            return false;
        }
        final List<RecipeInputIngredient> inputs = new ArrayList<RecipeInputIngredient>();
        for (final ItemStack stack : items) {
            if (StackUtil.isEmpty(stack)) {
                return false;
            }
            inputs.add(RecipeInputItemStack.of(stack));
        }
        for (final FluidStack stack2 : fluids) {
            if (stack2.amount <= 0) {
                return false;
            }
            inputs.add(RecipeInputFluidStack.of(stack2));
        }
        if (items.length + fluids.length != recipe.getInputIngredients().size()) {
            return false;
        }
        ListIterator<RecipeInputIngredient> itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
    Label_0167:
        for (final RecipeInputIngredient entry : inputs) {
            while (itB.hasNext()) {
                final RecipeInputIngredient temp = itB.next();
                if (temp.matches(entry.ingredient) && entry.getCount() >= temp.getCount()) {
                    itB.remove();
                    while (itB.hasPrevious()) {
                        itB.previous();
                    }
                    continue Label_0167;
                }
            }
            return false;
        }
        if (!simulate) {
            itB = (ListIterator<RecipeInputIngredient>)new ArrayList<RecipeInputIngredient>(recipe.getInputIngredients()).listIterator();
            Label_0300:
            for (final RecipeInputIngredient entry : inputs) {
                while (itB.hasNext()) {
                    final RecipeInputIngredient temp = itB.next();
                    if (temp.matches(entry.ingredient)) {
                        entry.shrink(temp.getCount());
                        itB.remove();
                        while (itB.hasPrevious()) {
                            itB.previous();
                        }
                        continue Label_0300;
                    }
                }
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Iterable<? extends DynamicRecipe> getRecipes() {
        return () -> new Iterator<DynamicRecipe>() {
            private final Iterator recipeIt;
            private Collection lastInput;
            
            {
                this.recipeIt = DynamicRecipeManager.this.recipes.values().iterator();
            }
            
            @Override
            public boolean hasNext() {
                return this.recipeIt.hasNext();
            }
            
            @Override
            public DynamicRecipe next() {
                final DynamicRecipe next = this.recipeIt.next();
                this.lastInput = next.getInputIngredients();
                return next;
            }
            
            @Override
            public void remove() {
                this.recipeIt.remove();
                DynamicRecipeManager.this.removeCachedRecipes(this.lastInput);
            }
        };
    }
    
    @Override
    public boolean isIterable() {
        return true;
    }
    
    protected void addToCache(final DynamicRecipe recipe) {
        if (recipe.getInputIngredients().stream().anyMatch(entry -> entry instanceof RecipeInputOreDictionary)) {
            if (!DynamicRecipeManager.oreRegisterEventSubscribed) {
                MinecraftForge.EVENT_BUS.register((Object)DynamicRecipeManager.class);
                DynamicRecipeManager.oreRegisterEventSubscribed = true;
            }
            DynamicRecipeManager.watchingManagers.add(this);
        }
        final Collection<Item> items = this.getItemsFromRecipe(recipe.getInputIngredients());
        final Collection<Fluid> fluids = this.getFluidsFromRecipe(recipe.getInputIngredients());
        if (items != null) {
            for (final Item item : items) {
                this.addToCache(item, recipe);
            }
        }
        if (fluids != null) {
            for (final Fluid fluid : fluids) {
                this.addToCache(fluid, recipe);
            }
        }
        if (items == null && fluids == null) {
            this.uncacheableRecipes.add(recipe);
        }
    }
    
    private void addToCache(final Item item, final DynamicRecipe recipe) {
        final List<DynamicRecipe> recipes = this.recipeCacheItem.computeIfAbsent(item, newValue -> new ArrayList());
        if (!recipes.contains(recipe)) {
            recipes.add(recipe);
        }
    }
    
    private void addToCache(final Fluid fluid, final DynamicRecipe recipe) {
        final List<DynamicRecipe> recipes = this.recipeCacheFluid.computeIfAbsent(fluid.getName(), newValue -> new ArrayList());
        if (!recipes.contains(recipe)) {
            recipes.add(recipe);
        }
    }
    
    protected void removeCachedRecipes(final Collection<RecipeInputIngredient> input) {
        final Collection<Item> items = this.getItemsFromRecipe(input);
        final Collection<Fluid> fluids = this.getFluidsFromRecipe(input);
        if (items != null) {
            for (final Item item : items) {
                final List<DynamicRecipe> recipes = this.recipeCacheItem.get(item);
                if (recipes == null) {
                    IC2.log.warn(LogCategory.Recipe, "Inconsistent recipe cache, the entry for the item " + item + " is missing.");
                }
                else {
                    this.removeInputFromRecipes(recipes.iterator(), input);
                    if (!recipes.isEmpty()) {
                        continue;
                    }
                    this.recipeCacheItem.remove(item);
                }
            }
        }
        if (fluids != null) {
            for (final Fluid fluid : fluids) {
                final List<DynamicRecipe> recipes = this.recipeCacheFluid.get(fluid.getName());
                if (recipes == null) {
                    IC2.log.warn(LogCategory.Recipe, "Inconsistent recipe cache, the entry for the fluid " + fluid + " is missing.");
                }
                else {
                    this.removeInputFromRecipes(recipes.iterator(), input);
                    if (!recipes.isEmpty()) {
                        continue;
                    }
                    this.recipeCacheFluid.remove(fluid.getName());
                }
            }
        }
        if (items == null && fluids == null) {
            this.removeInputFromRecipes(this.uncacheableRecipes.iterator(), input);
        }
    }
    
    private void removeInputFromRecipes(final Iterator<DynamicRecipe> it, final Collection<RecipeInputIngredient> target) {
        assert target != null;
        while (it.hasNext()) {
            if (target.equals(it.next().getInputIngredients())) {
                it.remove();
            }
        }
    }
    
    private Collection<Item> getItemsFromRecipe(final Collection<RecipeInputIngredient> recipe) {
        final List<ItemStack> inputs = new ArrayList<ItemStack>();
        for (final RecipeInputIngredient entry : recipe) {
            if (entry instanceof RecipeInputItemStack) {
                inputs.add((ItemStack)((RecipeInputItemStack)entry).ingredient);
            }
        }
        if (inputs.isEmpty()) {
            return null;
        }
        final Set<Item> ret = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>(inputs.size()));
        for (final ItemStack stack : inputs) {
            ret.add(stack.getItem());
        }
        return ret;
    }
    
    private Collection<Fluid> getFluidsFromRecipe(final Collection<RecipeInputIngredient> recipe) {
        final List<FluidStack> inputs = new ArrayList<FluidStack>();
        for (final RecipeInputIngredient entry : recipe) {
            if (entry instanceof RecipeInputFluidStack) {
                inputs.add((FluidStack)((RecipeInputFluidStack)entry).ingredient);
            }
        }
        if (inputs.isEmpty()) {
            return null;
        }
        final Set<Fluid> ret = Collections.newSetFromMap(new IdentityHashMap<Fluid, Boolean>(inputs.size()));
        for (final FluidStack stack : inputs) {
            ret.add(stack.getFluid());
        }
        return ret;
    }
    
    @Override
    public boolean removeRecipe(final Collection<RecipeInputIngredient> input, final Collection<RecipeOutputIngredient> output) {
        final DynamicRecipe recipe = this.getRecipe(input);
        if (recipe == null) {
            return false;
        }
        if (checkListEqualityIngredient(output, recipe.getOutputIngredients(), true)) {
            this.recipes.remove(recipe.getInputIngredients());
            this.removeCachedRecipes(recipe.getInputIngredients());
        }
        return false;
    }
    
    private static boolean checkListEqualityIngredient(final Collection<? extends RecipeIngredient> first, final Collection<? extends RecipeIngredient> second, final boolean strict) {
        if (first.size() != second.size()) {
            return false;
        }
        final ListIterator<RecipeIngredient> itB = (ListIterator<RecipeIngredient>)new ArrayList<RecipeIngredient>(second).listIterator();
    Label_0037:
        for (final RecipeIngredient ingredient : first) {
            while (itB.hasNext()) {
                if (strict) {
                    if (!ingredient.matchesStrict(itB.next())) {
                        continue;
                    }
                }
                else if (!ingredient.matches(itB.next())) {
                    continue;
                }
                itB.remove();
                while (itB.hasPrevious()) {
                    itB.previous();
                }
                continue Label_0037;
            }
            return false;
        }
        return true;
    }
    
    protected void displayError(final String message) {
        if (MainConfig.ignoreInvalidRecipes) {
            IC2.log.warn(LogCategory.Recipe, message);
            return;
        }
        throw new RuntimeException(message);
    }
    
    @SubscribeEvent
    public static void onOreRegister(final OreDictionary.OreRegisterEvent event) {
        final Item item = event.getOre().getItem();
        if (item == null) {
            IC2.log.warn(LogCategory.Recipe, "Found null item ore dict registration.", new Throwable());
            return;
        }
        for (final DynamicRecipeManager manager : DynamicRecipeManager.watchingManagers) {
            manager.onOreRegister(item, event.getName());
        }
    }
    
    private void onOreRegister(final Item item, final String name) {
        for (final DynamicRecipe rawRecipe : this.recipes.values()) {
            for (final RecipeInputIngredient entry : rawRecipe.getInputIngredients()) {
                if (!(entry instanceof RecipeInputOreDictionary)) {
                    continue;
                }
                final RecipeInputOreDictionary input = (RecipeInputOreDictionary)entry;
                if (!input.matchesStrict(name)) {
                    continue;
                }
                this.addToCache(item, rawRecipe);
            }
        }
    }
    
    static {
        watchingManagers = Collections.newSetFromMap(new IdentityHashMap<DynamicRecipeManager, Boolean>());
    }
}

package ic2.core.recipe.dynamic;

import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

public class DynamicRecipeManager implements IDynamicRecipeManager {
  public DynamicRecipe createRecipe() {
    return new DynamicRecipe(this);
  }
  
  public boolean addRecipe(DynamicRecipe recipe, boolean replace) {
    if (recipe.getInputIngredients() == null)
      throw new NullPointerException("The recipe input is null"); 
    if (recipe.getInputIngredients().size() <= 0)
      throw new IllegalArgumentException("No inputs"); 
    if (recipe.getOutputIngredients() == null)
      throw new NullPointerException("The recipe output is null"); 
    if (recipe.getOutputIngredients().size() <= 0)
      throw new IllegalArgumentException("No outputs"); 
    List<RecipeInputIngredient> listOfInputs = new ArrayList<>(recipe.getInputIngredients().size());
    for (RecipeInputIngredient entry : recipe.getInputIngredients()) {
      if (entry.isEmpty()) {
        displayError("The RecipeInputIngredient " + entry.toStringSafe() + " is invalid.");
        return false;
      } 
      listOfInputs.add(entry);
    } 
    List<RecipeOutputIngredient> listOfOutputs = new ArrayList<>(recipe.getOutputIngredients().size());
    for (RecipeOutputIngredient entry : recipe.getOutputIngredients()) {
      if (entry.isEmpty()) {
        displayError("The RecipeOutputIngredient " + entry.toStringSafe() + " is invalid.");
        return false;
      } 
      listOfOutputs.add(entry);
    } 
    DynamicRecipe temp = getRecipe(recipe.getInputIngredients());
    if (temp != null)
      if (replace) {
        do {
          this.recipes.remove(recipe.getInputIngredients());
          removeCachedRecipes(recipe.getInputIngredients());
          recipe = getRecipe(recipe.getInputIngredients());
        } while (recipe != null);
      } else {
        IC2.log.error(LogCategory.Recipe, "Skipping %s => %s due to duplicate recipe for %s (%s => %s)", new Object[] { recipe.getInputIngredients(), recipe.getOutputIngredients(), recipe.getInputIngredients(), recipe.getInputIngredients(), recipe.getOutputIngredients() });
        return false;
      }  
    DynamicRecipe newRecipe = createRecipe().withInput(listOfInputs).withOutput(listOfOutputs).withOperationEnergyCost(recipe.getOperationEnergyCost()).withOperationDurationTicks(recipe.getOperationDuration()).withMetadata(recipe.getMetadata());
    this.recipes.put(recipe.getInputIngredients(), newRecipe);
    addToCache(newRecipe);
    return true;
  }
  
  protected DynamicRecipe getRecipe(Collection<RecipeInputIngredient> input) {
    if (input.isEmpty())
      return null; 
    List<DynamicRecipe> recipes = new ArrayList<>();
    for (RecipeInputIngredient entry : input) {
      Object unspecific = entry.getUnspecific();
      if (unspecific instanceof Item) {
        if (this.recipeCacheItem.get(unspecific) != null)
          recipes.addAll(this.recipeCacheItem.get(unspecific)); 
        continue;
      } 
      if (unspecific instanceof Fluid && 
        this.recipeCacheFluid.containsKey(((Fluid)unspecific).getName()))
        recipes.addAll(this.recipeCacheFluid.get(((Fluid)unspecific).getName())); 
    } 
    if (!recipes.isEmpty())
      label61: for (DynamicRecipe recipe : recipes) {
        if (input.size() != recipe.getInputIngredients().size())
          continue; 
        ListIterator<RecipeInputIngredient> itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
        for (RecipeInputIngredient entry : input) {
          while (itB.hasNext()) {
            RecipeInputIngredient temp = itB.next();
            if (temp.matches(entry.ingredient) && 
              entry.getCount() >= temp.getCount()) {
              itB.remove();
              while (itB.hasPrevious())
                itB.previous(); 
            } 
          } 
          continue label61;
        } 
        return recipe;
      }  
    label62: for (DynamicRecipe recipe : this.uncacheableRecipes) {
      if (input.size() != recipe.getInputIngredients().size())
        continue; 
      ListIterator<RecipeInputIngredient> itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
      for (RecipeInputIngredient entry : input) {
        while (itB.hasNext()) {
          RecipeInputIngredient temp = itB.next();
          if (temp.matches(entry.ingredient) && 
            entry.getCount() >= temp.getCount()) {
            itB.remove();
            while (itB.hasPrevious())
              itB.previous(); 
          } 
        } 
        continue label62;
      } 
      return recipe;
    } 
    return null;
  }
  
  public DynamicRecipe findRecipe(ItemStack[] items, FluidStack[] fluids) {
    List<RecipeInputIngredient> inputs = new ArrayList<>();
    for (ItemStack stack : items) {
      if (StackUtil.isEmpty(stack))
        return null; 
      inputs.add(RecipeInputItemStack.of(stack));
    } 
    for (FluidStack stack : fluids) {
      if (stack.amount <= 0)
        return null; 
      inputs.add(RecipeInputFluidStack.of(stack));
    } 
    if (inputs.isEmpty())
      return null; 
    List<DynamicRecipe> recipes = new ArrayList<>();
    for (RecipeInputIngredient entry : inputs) {
      Object unspecific = entry.getUnspecific();
      if (unspecific instanceof Item) {
        if (this.recipeCacheItem.get(unspecific) != null)
          recipes.addAll(this.recipeCacheItem.get(unspecific)); 
        continue;
      } 
      if (unspecific instanceof Fluid && 
        this.recipeCacheFluid.containsKey(((Fluid)unspecific).getName()))
        recipes.addAll(this.recipeCacheFluid.get(((Fluid)unspecific).getName())); 
    } 
    if (!recipes.isEmpty())
      label79: for (DynamicRecipe recipe : recipes) {
        if (inputs.size() != recipe.getInputIngredients().size())
          continue; 
        ListIterator<RecipeInputIngredient> itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
        for (RecipeInputIngredient entry : inputs) {
          while (itB.hasNext()) {
            RecipeInputIngredient temp = itB.next();
            if (temp.matches(entry.ingredient) && 
              entry.getCount() >= temp.getCount()) {
              itB.remove();
              while (itB.hasPrevious())
                itB.previous(); 
            } 
          } 
          continue label79;
        } 
        return recipe;
      }  
    label80: for (DynamicRecipe recipe : this.uncacheableRecipes) {
      if (inputs.size() != recipe.getInputIngredients().size())
        continue; 
      ListIterator<RecipeInputIngredient> itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
      for (RecipeInputIngredient entry : inputs) {
        while (itB.hasNext()) {
          RecipeInputIngredient temp = itB.next();
          if (temp.matches(entry.ingredient) && 
            entry.getCount() >= temp.getCount()) {
            itB.remove();
            while (itB.hasPrevious())
              itB.previous(); 
          } 
        } 
        continue label80;
      } 
      return recipe;
    } 
    return null;
  }
  
  public boolean isPartOfRecipe(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return false; 
    RecipeInputItemStack subject = RecipeInputItemStack.of(stack);
    List<DynamicRecipe> recipes = new ArrayList<>();
    Object unspecific = subject.getUnspecific();
    if (unspecific instanceof Item) {
      if (this.recipeCacheItem.get(unspecific) != null)
        recipes.addAll(this.recipeCacheItem.get(unspecific)); 
    } else if (unspecific instanceof Fluid && 
      this.recipeCacheFluid.containsKey(((Fluid)unspecific).getName())) {
      recipes.addAll(this.recipeCacheFluid.get(((Fluid)unspecific).getName()));
    } 
    if (!recipes.isEmpty())
      for (DynamicRecipe recipe : recipes) {
        ListIterator<RecipeInputIngredient> itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
        while (itB.hasNext()) {
          RecipeInputIngredient temp = itB.next();
          if (temp.matches(subject.ingredient))
            return true; 
        } 
      }  
    if (!this.uncacheableRecipes.isEmpty())
      for (DynamicRecipe recipe : this.uncacheableRecipes) {
        ListIterator<RecipeInputIngredient> itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
        RecipeInputIngredient temp = itB.next();
        if (temp.matches(subject.ingredient))
          return true; 
      }  
    return false;
  }
  
  public DynamicRecipe apply(ItemStack[] items, FluidStack[] fluids, boolean simulate) {
    List<RecipeInputIngredient> inputs = new ArrayList<>();
    for (ItemStack stack : items) {
      if (StackUtil.isEmpty(stack))
        return null; 
      inputs.add(RecipeInputItemStack.of(stack));
    } 
    for (FluidStack stack : fluids) {
      if (stack.amount <= 0)
        return null; 
      inputs.add(RecipeInputFluidStack.of(stack));
    } 
    DynamicRecipe recipe = getRecipe(inputs);
    if (recipe == null)
      return null; 
    if (items.length + fluids.length != recipe.getInputIngredients().size())
      return null; 
    ListIterator<RecipeInputIngredient> itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
    for (RecipeInputIngredient entry : inputs) {
      while (itB.hasNext()) {
        RecipeInputIngredient temp = itB.next();
        if (temp.matches(entry.ingredient) && 
          entry.getCount() >= temp.getCount()) {
          itB.remove();
          while (itB.hasPrevious())
            itB.previous(); 
        } 
      } 
      return null;
    } 
    if (!simulate) {
      itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
      for (RecipeInputIngredient entry : inputs) {
        while (itB.hasNext()) {
          RecipeInputIngredient temp = itB.next();
          if (temp.matches(entry.ingredient)) {
            entry.shrink(temp.getCount());
            itB.remove();
            while (itB.hasPrevious())
              itB.previous(); 
          } 
        } 
        return null;
      } 
    } 
    return recipe;
  }
  
  public boolean apply(DynamicRecipe recipe, ItemStack[] items, FluidStack[] fluids, boolean simulate) {
    if (recipe == null)
      return false; 
    List<RecipeInputIngredient> inputs = new ArrayList<>();
    for (ItemStack stack : items) {
      if (StackUtil.isEmpty(stack))
        return false; 
      inputs.add(RecipeInputItemStack.of(stack));
    } 
    for (FluidStack stack : fluids) {
      if (stack.amount <= 0)
        return false; 
      inputs.add(RecipeInputFluidStack.of(stack));
    } 
    if (items.length + fluids.length != recipe.getInputIngredients().size())
      return false; 
    ListIterator<RecipeInputIngredient> itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
    for (RecipeInputIngredient entry : inputs) {
      while (itB.hasNext()) {
        RecipeInputIngredient temp = itB.next();
        if (temp.matches(entry.ingredient) && 
          entry.getCount() >= temp.getCount()) {
          itB.remove();
          while (itB.hasPrevious())
            itB.previous(); 
        } 
      } 
      return false;
    } 
    if (!simulate) {
      itB = (new ArrayList<>(recipe.getInputIngredients())).listIterator();
      for (RecipeInputIngredient entry : inputs) {
        while (itB.hasNext()) {
          RecipeInputIngredient temp = itB.next();
          if (temp.matches(entry.ingredient)) {
            entry.shrink(temp.getCount());
            itB.remove();
            while (itB.hasPrevious())
              itB.previous(); 
          } 
        } 
        return false;
      } 
    } 
    return true;
  }
  
  public Iterable<? extends DynamicRecipe> getRecipes() {
    return () -> new Iterator<DynamicRecipe>() {
        public boolean hasNext() {
          return this.recipeIt.hasNext();
        }
        
        public DynamicRecipe next() {
          DynamicRecipe next = this.recipeIt.next();
          this.lastInput = next.getInputIngredients();
          return next;
        }
        
        public void remove() {
          this.recipeIt.remove();
          DynamicRecipeManager.this.removeCachedRecipes(this.lastInput);
        }
        
        private final Iterator recipeIt = DynamicRecipeManager.this.recipes.values().iterator();
        
        private Collection lastInput;
      };
  }
  
  public boolean isIterable() {
    return true;
  }
  
  protected void addToCache(DynamicRecipe recipe) {
    if (recipe.getInputIngredients().stream().anyMatch(entry -> entry instanceof RecipeInputOreDictionary)) {
      if (!oreRegisterEventSubscribed) {
        MinecraftForge.EVENT_BUS.register(DynamicRecipeManager.class);
        oreRegisterEventSubscribed = true;
      } 
      watchingManagers.add(this);
    } 
    Collection<Item> items = getItemsFromRecipe(recipe.getInputIngredients());
    Collection<Fluid> fluids = getFluidsFromRecipe(recipe.getInputIngredients());
    if (items != null)
      for (Item item : items)
        addToCache(item, recipe);  
    if (fluids != null)
      for (Fluid fluid : fluids)
        addToCache(fluid, recipe);  
    if (items == null && fluids == null)
      this.uncacheableRecipes.add(recipe); 
  }
  
  private void addToCache(Item item, DynamicRecipe recipe) {
    List<DynamicRecipe> recipes = this.recipeCacheItem.computeIfAbsent(item, newValue -> new ArrayList());
    if (!recipes.contains(recipe))
      recipes.add(recipe); 
  }
  
  private void addToCache(Fluid fluid, DynamicRecipe recipe) {
    List<DynamicRecipe> recipes = this.recipeCacheFluid.computeIfAbsent(fluid.getName(), newValue -> new ArrayList());
    if (!recipes.contains(recipe))
      recipes.add(recipe); 
  }
  
  protected void removeCachedRecipes(Collection<RecipeInputIngredient> input) {
    Collection<Item> items = getItemsFromRecipe(input);
    Collection<Fluid> fluids = getFluidsFromRecipe(input);
    if (items != null)
      for (Item item : items) {
        List<DynamicRecipe> recipes = this.recipeCacheItem.get(item);
        if (recipes == null) {
          IC2.log.warn(LogCategory.Recipe, "Inconsistent recipe cache, the entry for the item " + item + " is missing.");
          continue;
        } 
        removeInputFromRecipes(recipes.iterator(), input);
        if (recipes.isEmpty())
          this.recipeCacheItem.remove(item); 
      }  
    if (fluids != null)
      for (Fluid fluid : fluids) {
        List<DynamicRecipe> recipes = this.recipeCacheFluid.get(fluid.getName());
        if (recipes == null) {
          IC2.log.warn(LogCategory.Recipe, "Inconsistent recipe cache, the entry for the fluid " + fluid + " is missing.");
          continue;
        } 
        removeInputFromRecipes(recipes.iterator(), input);
        if (recipes.isEmpty())
          this.recipeCacheFluid.remove(fluid.getName()); 
      }  
    if (items == null && fluids == null)
      removeInputFromRecipes(this.uncacheableRecipes.iterator(), input); 
  }
  
  private void removeInputFromRecipes(Iterator<DynamicRecipe> it, Collection<RecipeInputIngredient> target) {
    assert target != null;
    while (it.hasNext()) {
      if (target.equals(((DynamicRecipe)it.next()).getInputIngredients()))
        it.remove(); 
    } 
  }
  
  private Collection<Item> getItemsFromRecipe(Collection<RecipeInputIngredient> recipe) {
    List<ItemStack> inputs = new ArrayList<>();
    for (RecipeInputIngredient entry : recipe) {
      if (entry instanceof RecipeInputItemStack)
        inputs.add(((RecipeInputItemStack)entry).ingredient); 
    } 
    if (inputs.isEmpty())
      return null; 
    Set<Item> ret = Collections.newSetFromMap(new IdentityHashMap<>(inputs.size()));
    for (ItemStack stack : inputs)
      ret.add(stack.getItem()); 
    return ret;
  }
  
  private Collection<Fluid> getFluidsFromRecipe(Collection<RecipeInputIngredient> recipe) {
    List<FluidStack> inputs = new ArrayList<>();
    for (RecipeInputIngredient entry : recipe) {
      if (entry instanceof RecipeInputFluidStack)
        inputs.add(((RecipeInputFluidStack)entry).ingredient); 
    } 
    if (inputs.isEmpty())
      return null; 
    Set<Fluid> ret = Collections.newSetFromMap(new IdentityHashMap<>(inputs.size()));
    for (FluidStack stack : inputs)
      ret.add(stack.getFluid()); 
    return ret;
  }
  
  public boolean removeRecipe(Collection<RecipeInputIngredient> input, Collection<RecipeOutputIngredient> output) {
    DynamicRecipe recipe = getRecipe(input);
    if (recipe == null)
      return false; 
    if (checkListEqualityIngredient((Collection)output, (Collection)recipe.getOutputIngredients(), true)) {
      this.recipes.remove(recipe.getInputIngredients());
      removeCachedRecipes(recipe.getInputIngredients());
    } 
    return false;
  }
  
  private static boolean checkListEqualityIngredient(Collection<? extends RecipeIngredient> first, Collection<? extends RecipeIngredient> second, boolean strict) {
    if (first.size() != second.size())
      return false; 
    ListIterator<RecipeIngredient> itB = (new ArrayList<>(second)).listIterator();
    for (RecipeIngredient ingredient : first) {
      while (itB.hasNext()) {
        if (strict ? ingredient.matchesStrict(itB.next()) : ingredient.matches(itB.next())) {
          itB.remove();
          while (itB.hasPrevious())
            itB.previous(); 
        } 
      } 
      return false;
    } 
    return true;
  }
  
  protected void displayError(String message) {
    if (MainConfig.ignoreInvalidRecipes) {
      IC2.log.warn(LogCategory.Recipe, message);
    } else {
      throw new RuntimeException(message);
    } 
  }
  
  protected final Map<Collection<RecipeInputIngredient>, DynamicRecipe> recipes = new HashMap<>();
  
  private final Map<Item, List<DynamicRecipe>> recipeCacheItem = new IdentityHashMap<>();
  
  private final Map<String, List<DynamicRecipe>> recipeCacheFluid = new IdentityHashMap<>();
  
  private final List<DynamicRecipe> uncacheableRecipes = new ArrayList<>();
  
  private static boolean oreRegisterEventSubscribed;
  
  @SubscribeEvent
  public static void onOreRegister(OreDictionary.OreRegisterEvent event) {
    Item item = event.getOre().getItem();
    if (item == null) {
      IC2.log.warn(LogCategory.Recipe, "Found null item ore dict registration.", new Object[] { new Throwable() });
      return;
    } 
    for (DynamicRecipeManager manager : watchingManagers)
      manager.onOreRegister(item, event.getName()); 
  }
  
  private void onOreRegister(Item item, String name) {
    for (DynamicRecipe rawRecipe : this.recipes.values()) {
      for (RecipeInputIngredient entry : rawRecipe.getInputIngredients()) {
        if (!(entry instanceof RecipeInputOreDictionary))
          continue; 
        RecipeInputOreDictionary input = (RecipeInputOreDictionary)entry;
        if (input.matchesStrict(name))
          addToCache(item, rawRecipe); 
      } 
    } 
  }
  
  private static final Set<DynamicRecipeManager> watchingManagers = Collections.newSetFromMap(new IdentityHashMap<>());
}

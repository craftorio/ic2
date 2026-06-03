package ic2.core.uu;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.item.ItemStack;

public class UuIndex {
  public static final UuIndex instance = new UuIndex();
  
  public void addResolver(IRecipeResolver resolver) {
    this.resolvers.add(resolver);
  }
  
  public void addResolver(ILateRecipeResolver resolver) {
    this.lateResolvers.add(resolver);
  }
  
  public void add(ItemStack stack, double value) {
    if (stack == null || stack.func_77973_b() == null)
      throw new NullPointerException("invalid itemstack to add"); 
    UuGraph.set(stack, value);
  }
  
  public double get(ItemStack request) {
    return UuGraph.get(request);
  }
  
  public double getInBuckets(ItemStack request) {
    double ret = UuGraph.get(request);
    ret *= 1.0E-5D;
    return ret;
  }
  
  public void init() {
    addResolver(new VanillaSmeltingResolver());
    addResolver(new RecipeResolver());
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.macerator));
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.extractor));
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.compressor));
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.centrifuge));
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.blockcutter));
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.blastfurnace));
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.metalformerExtruding));
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.metalformerCutting));
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.metalformerRolling));
    addResolver(new MachineRecipeResolver((IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?>)Recipes.oreWashing));
    addResolver(new CannerBottleSolidResolver());
    addResolver(new ScrapBoxResolver());
    addResolver(new ManualRecipeResolver());
    addResolver(new RecyclerResolver());
  }
  
  public void refresh(boolean reset) {
    Config config = MainConfig.get().getSub("balance/uu-values/world scan");
    if (config == null) {
      IC2.log.info(LogCategory.Uu, "Loading predefined UU world scan values, run /ic2 uu-world-scan <small|medium|large> to calibrate them for your world.");
      config = new Config("uu scan values");
      try {
        config.load(IC2.class.getResourceAsStream("/assets/ic2/config/uu_scan_values.ini"));
      } catch (Exception e) {
        throw new RuntimeException("Error loading base config", e);
      } 
    } else {
      IC2.log.debug(LogCategory.Uu, "Loading UU world scan values from the user config.");
    } 
    Iterator<Config.Value> it;
    for (it = config.valueIterator(); it.hasNext(); ) {
      ItemStack stack;
      Config.Value value = it.next();
      try {
        stack = ConfigUtil.asStack(value.name);
      } catch (ParseException e) {
        throw new Config.ParseException("invalid key", value, e);
      } 
      if (stack == null) {
        IC2.log.warn(LogCategory.Uu, "UU world-scan config: Can't find ItemStack for %s, ignoring the entry in line %d.", new Object[] { value.name, Integer.valueOf(value.getLine()) });
        continue;
      } 
      add(stack, value.getDouble());
    } 
    for (it = MainConfig.get().getSub("balance/uu-values/predefined").valueIterator(); it.hasNext(); ) {
      ItemStack stack;
      Config.Value value = it.next();
      try {
        stack = ConfigUtil.asStack(value.name);
      } catch (ParseException e) {
        throw new Config.ParseException("invalid key", value, e);
      } 
      if (stack == null) {
        IC2.log.warn(LogCategory.Uu, "UU predefined config: Can't find ItemStack for %s, ignoring the entry in line %d.", new Object[] { value.name, Integer.valueOf(value.getLine()) });
        continue;
      } 
      add(stack, value.getDouble());
    } 
    UuGraph.build(reset);
  }
  
  protected final List<IRecipeResolver> resolvers = new ArrayList<>();
  
  protected final List<ILateRecipeResolver> lateResolvers = new ArrayList<>();
}

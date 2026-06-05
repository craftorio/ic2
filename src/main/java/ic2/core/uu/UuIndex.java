package ic2.core.uu;

import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.item.ItemStack;

public class UuIndex {
   public static final UuIndex instance = new UuIndex();
   protected final List<IRecipeResolver> resolvers = new ArrayList<>();
   protected final List<ILateRecipeResolver> lateResolvers = new ArrayList<>();

   private UuIndex() {
   }

   public void addResolver(IRecipeResolver resolver) {
      this.resolvers.add(resolver);
   }

   public void addResolver(ILateRecipeResolver resolver) {
      this.lateResolvers.add(resolver);
   }

   public void add(ItemStack stack, double value) {
      if (stack != null && stack.getItem() != null) {
         UuGraph.set(stack, value);
      } else {
         throw new NullPointerException("invalid itemstack to add");
      }
   }

   public double get(ItemStack request) {
      return UuGraph.get(request);
   }

   public double getInBuckets(ItemStack request) {
      double ret = UuGraph.get(request);
      return ret * 1.0E-5;
   }

   public void init() {
      this.addResolver(new VanillaSmeltingResolver());
      this.addResolver(new RecipeResolver());
      this.addResolver(new MachineRecipeResolver(Recipes.macerator));
      this.addResolver(new MachineRecipeResolver(Recipes.extractor));
      this.addResolver(new MachineRecipeResolver(Recipes.compressor));
      this.addResolver(new MachineRecipeResolver(Recipes.centrifuge));
      this.addResolver(new MachineRecipeResolver(Recipes.blockcutter));
      this.addResolver(new MachineRecipeResolver(Recipes.blastfurnace));
      this.addResolver(new MachineRecipeResolver(Recipes.metalformerExtruding));
      this.addResolver(new MachineRecipeResolver(Recipes.metalformerCutting));
      this.addResolver(new MachineRecipeResolver(Recipes.metalformerRolling));
      this.addResolver(new MachineRecipeResolver(Recipes.oreWashing));
      this.addResolver(new CannerBottleSolidResolver());
      this.addResolver(new ScrapBoxResolver());
      this.addResolver(new ManualRecipeResolver());
      this.addResolver(new RecyclerResolver());
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

      Iterator<Config.Value> it = config.valueIterator();

      while (it.hasNext()) {
         Config.Value value = it.next();

         ItemStack stack;
         try {
            stack = ConfigUtil.asStack(value.name);
         } catch (ParseException e) {
            throw new Config.ParseException("invalid key", value, e);
         }

         if (stack == null) {
            IC2.log.warn(LogCategory.Uu, "UU world-scan config: Can't find ItemStack for %s, ignoring the entry in line %d.", value.name, value.getLine());
         } else {
            this.add(stack, value.getDouble());
         }
      }

      it = MainConfig.get().getSub("balance/uu-values/predefined").valueIterator();

      while (it.hasNext()) {
         Config.Value value = it.next();

         ItemStack stack;
         try {
            stack = ConfigUtil.asStack(value.name);
         } catch (ParseException e) {
            throw new Config.ParseException("invalid key", value, e);
         }

         if (stack == null) {
            IC2.log.warn(LogCategory.Uu, "UU predefined config: Can't find ItemStack for %s, ignoring the entry in line %d.", value.name, value.getLine());
         } else {
            this.add(stack, value.getDouble());
         }
      }

      UuGraph.build(reset);
   }
}

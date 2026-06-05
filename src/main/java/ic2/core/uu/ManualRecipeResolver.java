package ic2.core.uu;

import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.ItemName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;

public class ManualRecipeResolver implements IRecipeResolver {
   private static final double transformCost = 0.0;

   @Override
   public List<RecipeTransformation> getTransformations() {
      List<RecipeTransformation> ret = new ArrayList<>();
      ret.add(toTransform(ItemName.uranium_fuel_rod.getItemStack(), ItemName.nuclear.getItemStack(NuclearResourceType.depleted_uranium)));
      ret.add(toTransform(ItemName.dual_uranium_fuel_rod.getItemStack(), ItemName.nuclear.getItemStack(NuclearResourceType.depleted_dual_uranium)));
      ret.add(toTransform(ItemName.quad_uranium_fuel_rod.getItemStack(), ItemName.nuclear.getItemStack(NuclearResourceType.depleted_quad_uranium)));
      ret.add(toTransform(ItemName.mox_fuel_rod.getItemStack(), ItemName.nuclear.getItemStack(NuclearResourceType.depleted_mox)));
      ret.add(toTransform(ItemName.dual_mox_fuel_rod.getItemStack(), ItemName.nuclear.getItemStack(NuclearResourceType.depleted_dual_mox)));
      ret.add(toTransform(ItemName.quad_mox_fuel_rod.getItemStack(), ItemName.nuclear.getItemStack(NuclearResourceType.depleted_quad_mox)));
      return ret;
   }

   private static RecipeTransformation toTransform(ItemStack input, ItemStack output) {
      List<List<LeanItemStack>> inputs = Collections.singletonList(Collections.singletonList(new LeanItemStack(input)));
      List<LeanItemStack> outputs = Collections.singletonList(new LeanItemStack(output));
      return new RecipeTransformation(0.0, inputs, outputs);
   }
}

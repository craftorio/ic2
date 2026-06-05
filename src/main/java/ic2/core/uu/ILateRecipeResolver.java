package ic2.core.uu;

import java.util.List;

public interface ILateRecipeResolver {
   List<RecipeTransformation> getTransformations(Iterable<LeanItemStack> var1);
}

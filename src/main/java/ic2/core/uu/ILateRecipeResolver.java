// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.List;

public interface ILateRecipeResolver
{
    List<RecipeTransformation> getTransformations(final Iterable<LeanItemStack> p0);
}

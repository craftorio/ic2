package ic2.api.recipe;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface IRecipeInput
{
	boolean matches(ItemStack var1);

	int getAmount();

	List<ItemStack> getInputs();

	default Ingredient getIngredient()
	{
		return Ingredient.m_43921_(this.getInputs().stream());
	}
}

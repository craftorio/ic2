package ic2.core.recipe.v2;

import ic2.api.recipe.MachineRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record RecipeHolder<I, O>(MachineRecipe<I, O> recipe, ResourceLocation id, RecipeSerializer<?> serializer,
                                 RecipeType<?> type)
	implements Recipe<Container>
{
	public boolean matches(Container inventory, Level world)
	{
		throw new UnsupportedOperationException("Not supported for IC2 machine recipes.");
	}

	public ItemStack assemble(Container inventory)
	{
		throw new UnsupportedOperationException("Not supported for IC2 machine recipes.");
	}

	public ItemStack assemble(Container inventory, RegistryAccess registryAccess)
	{
		throw new UnsupportedOperationException("Not supported for IC2 machine recipes.");
	}

	public boolean canCraftInDimensions(int width, int height)
	{
		throw new UnsupportedOperationException("Not supported for IC2 machine recipes.");
	}

	public ItemStack getResultItem()
	{
		return ItemStack.EMPTY;
	}

	public ItemStack getResultItem(RegistryAccess registryAccess)
	{
		return ItemStack.EMPTY;
	}

	public ResourceLocation getId()
	{
		return this.id;
	}

	public RecipeSerializer<?> getSerializer()
	{
		return this.serializer;
	}

	public RecipeType<?> getType()
	{
		return this.type;
	}
}

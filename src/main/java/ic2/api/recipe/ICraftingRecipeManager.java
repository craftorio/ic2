package ic2.api.recipe;

import net.minecraft.item.ItemStack;

public interface ICraftingRecipeManager
{
	void addRecipe(ItemStack paramItemStack, Object... paramVarArgs);

	void addShapelessRecipe(ItemStack paramItemStack, Object... paramVarArgs);

	public static class AttributeContainer
	{
		public final boolean hidden;

		public final boolean consuming;

		public final boolean fixedSize;

		public AttributeContainer(boolean hidden, boolean consuming)
		{
			this(hidden, consuming, false);
		}

		public AttributeContainer(boolean hidden, boolean consuming, boolean fixedSize)
		{
			this.hidden = hidden;
			this.consuming = consuming;
			this.fixedSize = fixedSize;
		}
	}
}

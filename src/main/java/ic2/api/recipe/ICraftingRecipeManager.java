package ic2.api.recipe;

import net.minecraft.world.item.ItemStack;

public interface ICraftingRecipeManager
{
	void addRecipe(ItemStack var1, Object... var2);

	void addShapelessRecipe(ItemStack var1, Object... var2);

	class AttributeContainer
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

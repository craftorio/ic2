package ic2.core.block.invslot;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.tileentity.TileEntitySolidCanner;
import net.minecraft.world.item.ItemStack;

public class InvSlotProcessableSolidCanner extends InvSlotProcessable<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput>
{
	public InvSlotProcessableSolidCanner(TileEntitySolidCanner base1, String name1, int count)
	{
		super(base1, name1, count, Recipes.cannerBottle);
	}

	protected ICannerBottleRecipeManager.RawInput getInput(ItemStack stack)
	{
		return new ICannerBottleRecipeManager.RawInput(((TileEntitySolidCanner) this.base).canInputSlot.get(), stack);
	}

	protected void setInput(ICannerBottleRecipeManager.RawInput input)
	{
		((TileEntitySolidCanner) this.base).canInputSlot.put(input.container);
		this.put(input.fill);
	}
}

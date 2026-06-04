package ic2.core.block.invslot;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.tileentity.TileEntitySolidCanner;
import net.minecraft.item.ItemStack;

public class InvSlotConsumableSolidCanner extends InvSlotConsumableLiquid
{
	public InvSlotConsumableSolidCanner(TileEntitySolidCanner base1, String name1, int count)
	{
		super(base1, name1, count);
	}

	public boolean accepts(ItemStack stack)
	{
		return (Recipes.cannerBottle.apply(new ICannerBottleRecipeManager.RawInput(stack, ((TileEntitySolidCanner) this.base).inputSlot.get()), true) != null);
	}
}

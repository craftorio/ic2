package ic2.core.block.invslot;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import net.minecraft.world.item.ItemStack;

public class InvSlotConsumableCanner extends InvSlotConsumableLiquid
{
	public InvSlotConsumableCanner(TileEntityCanner base1, String name1, int count)
	{
		super(base1, name1, count);
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid:
				return Recipes.cannerBottle
					.get(this.base.getParent().getLevel())
					.apply(new ICannerBottleRecipeManager.RawInput(stack, ((TileEntityCanner) this.base).inputSlot.get()), true)
					!= null;
			case BottleLiquid:
			case EmptyLiquid:
			case EnrichLiquid:
				return super.accepts(stack);
			default:
				assert false;
				return false;
		}
	}
}

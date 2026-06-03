package ic2.core.block.invslot;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import net.minecraft.item.ItemStack;

public class InvSlotConsumableCanner extends InvSlotConsumableLiquid {
  public InvSlotConsumableCanner(TileEntityCanner base1, String name1, int count) {
    super((IInventorySlotHolder<?>)base1, name1, count);
  }
  
  public boolean accepts(ItemStack stack) {
    switch (((TileEntityCanner)this.base).getMode()) {
      case BottleSolid:
        return (Recipes.cannerBottle.apply(new ICannerBottleRecipeManager.RawInput(stack, ((TileEntityCanner)this.base).inputSlot.get()), true) != null);
      case BottleLiquid:
      case EmptyLiquid:
      case EnrichLiquid:
        return super.accepts(stack);
    } 
    assert false;
    return false;
  }
}

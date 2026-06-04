// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.Recipes;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.machine.tileentity.TileEntityCanner;

public class InvSlotConsumableCanner extends InvSlotConsumableLiquid
{
    public InvSlotConsumableCanner(final TileEntityCanner base1, final String name1, final int count) {
        super(base1, name1, count);
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        switch (((TileEntityCanner)this.base).getMode()) {
            case BottleSolid: {
                return ((IMachineRecipeManager<Object, Object, ICannerBottleRecipeManager.RawInput>)Recipes.cannerBottle).apply(new ICannerBottleRecipeManager.RawInput(stack, ((TileEntityCanner)this.base).inputSlot.get()), true) != null;
            }
            case BottleLiquid:
            case EmptyLiquid:
            case EnrichLiquid: {
                return super.accepts(stack);
            }
            default: {
                assert false;
                return false;
            }
        }
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.api.info.Info;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;

public class InvSlotConsumableFuel extends InvSlotConsumable
{
    public final boolean allowLava;
    
    public InvSlotConsumableFuel(final IInventorySlotHolder<?> base1, final String name1, final int count, final boolean allowLava1) {
        super(base1, name1, Access.I, count, InvSide.SIDE);
        this.allowLava = allowLava1;
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        return Info.itemInfo.getFuelValue(stack, this.allowLava) > 0;
    }
    
    public int consumeFuel() {
        final ItemStack fuel = this.consume(1);
        if (fuel == null) {
            return 0;
        }
        return Info.itemInfo.getFuelValue(fuel, this.allowLava);
    }
}

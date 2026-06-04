// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotConsumable;

public class InvSlotConsumableBlock extends InvSlotConsumable
{
    public InvSlotConsumableBlock(final TileEntityInventory base1, final String name1, final int count) {
        this(base1, name1, Access.I, count, InvSide.TOP);
    }
    
    public InvSlotConsumableBlock(final TileEntityInventory base1, final String name1, final Access access1, final int count, final InvSide preferredSide1) {
        super(base1, name1, access1, count, preferredSide1);
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        return stack.getItem() instanceof ItemBlock;
    }
}

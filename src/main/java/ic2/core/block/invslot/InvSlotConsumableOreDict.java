// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import net.minecraftforge.oredict.OreDictionary;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;

public class InvSlotConsumableOreDict extends InvSlotConsumable
{
    protected final String oreDict;
    
    public InvSlotConsumableOreDict(final IInventorySlotHolder<?> base, final String name, final int count, final String oreDict) {
        super(base, name, count);
        this.oreDict = oreDict;
    }
    
    public InvSlotConsumableOreDict(final IInventorySlotHolder<?> base, final String name, final Access access, final int count, final InvSide side, final String oreDict) {
        super(base, name, access, count, side);
        this.oreDict = oreDict;
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        for (final int ID : OreDictionary.getOreIDs(stack)) {
            if (this.oreDict.equals(OreDictionary.getOreName(ID))) {
                return true;
            }
        }
        return false;
    }
}

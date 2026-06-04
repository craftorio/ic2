// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import net.minecraft.block.properties.IProperty;
import ic2.core.block.BlockMultiID;
import ic2.core.block.state.IIdProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;

public class ItemBlockMulti extends ItemBlockIC2
{
    public ItemBlockMulti(final Block block) {
        super(block);
        this.setHasSubtypes(true);
    }
    
    public int getMetadata(final int damage) {
        return damage;
    }
    
    @Override
    public String getUnlocalizedName(final ItemStack stack) {
        final String name = ((IIdProvider)this.block.getStateFromMeta(stack.getMetadata()).getValue((IProperty)((BlockMultiID)this.block).getTypeProperty())).getName();
        return super.getUnlocalizedName(stack) + "." + name;
    }
}

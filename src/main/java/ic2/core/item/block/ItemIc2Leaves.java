// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import ic2.core.init.Localization;
import net.minecraft.block.properties.IProperty;
import ic2.core.block.Ic2Leaves;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.Block;
import net.minecraft.item.ItemLeaves;

public class ItemIc2Leaves extends ItemLeaves
{
    public ItemIc2Leaves(final Block block) {
        super((BlockLeaves)block);
        this.setHasSubtypes(false);
    }
    
    public String getUnlocalizedName() {
        return "ic2." + super.getUnlocalizedName().substring(5);
    }
    
    public String getUnlocalizedName(final ItemStack stack) {
        return this.getUnlocalizedName() + "." + ((Ic2Leaves.LeavesType)this.block.getStateFromMeta(stack.getMetadata()).getValue((IProperty)Ic2Leaves.typeProperty)).getName();
    }
    
    public String getItemStackDisplayName(final ItemStack stack) {
        return Localization.translate(this.getUnlocalizedName(stack));
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import ic2.core.block.BlockBase;
import net.minecraft.item.EnumRarity;
import net.minecraft.block.material.Material;
import ic2.core.block.BlockScaffold;
import ic2.core.ref.BlockName;
import net.minecraft.block.state.IBlockState;
import ic2.core.init.Localization;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import java.util.function.Function;
import net.minecraft.item.ItemBlock;

public class ItemBlockIC2 extends ItemBlock
{
    public static Function<Block, Item> supplier;
    
    public ItemBlockIC2(final Block block) {
        super(block);
    }
    
    public String getUnlocalizedName(final ItemStack stack) {
        return this.getUnlocalizedName();
    }
    
    public String getItemStackDisplayName(final ItemStack stack) {
        return Localization.translate(this.getUnlocalizedName(stack));
    }
    
    public boolean canHarvestBlock(final IBlockState block, final ItemStack stack) {
        return block.getBlock() == BlockName.scaffold.getInstance();
    }
    
    public int getItemBurnTime(final ItemStack stack) {
        if (this.block == BlockName.scaffold.getInstance()) {
            final BlockScaffold scaffold = (BlockScaffold)this.block;
            final IBlockState state = scaffold.getState(scaffold.getVariant(stack));
            return (state.getMaterial() == Material.WOOD) ? 300 : 0;
        }
        return -1;
    }
    
    public EnumRarity getRarity(final ItemStack stack) {
        if (this.block instanceof BlockBase) {
            return ((BlockBase)this.block).getRarity(stack);
        }
        return super.getRarity(stack);
    }
    
    static {
        ItemBlockIC2.supplier = (Function<Block, Item>)ItemBlockIC2::new;
    }
}

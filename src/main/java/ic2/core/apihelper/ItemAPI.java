// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.apihelper;

import ic2.core.ref.IMultiBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import ic2.core.ref.FluidName;
import net.minecraft.block.Block;
import ic2.core.ref.ItemName;
import ic2.core.ref.BlockName;
import net.minecraft.item.ItemStack;
import ic2.api.item.IItemAPI;

public class ItemAPI implements IItemAPI
{
    @Override
    public ItemStack getItemStack(String name, String variant) {
        if (name == null) {
            return null;
        }
        if (variant == null) {
            final int idx = name.indexOf(35);
            if (idx != -1) {
                variant = name.substring(idx + 1);
                name = name.substring(0, idx);
            }
        }
        final BlockName blockName = this.getBlockName(name);
        if (blockName != null) {
            return blockName.getItemStack(variant);
        }
        final ItemName itemName = this.getItemName(name);
        if (itemName != null) {
            return itemName.getItemStack(variant);
        }
        return null;
    }
    
    @Override
    public Block getBlock(final String name) {
        if (name == null) {
            return null;
        }
        final BlockName blockName = this.getBlockName(name);
        if (blockName != null) {
            return blockName.getInstance();
        }
        final FluidName fluidName = this.getFluidName(name);
        if (fluidName != null) {
            return fluidName.getInstance().getBlock();
        }
        return null;
    }
    
    @Override
    public Item getItem(final String name) {
        if (name == null) {
            return null;
        }
        final ItemName itemName = this.getItemName(name);
        if (itemName != null) {
            return itemName.getInstance();
        }
        final Block block = this.getBlock(name);
        if (block != null) {
            final Item ret = Item.getItemFromBlock(block);
            if (ret != Items.AIR || block == Blocks.AIR) {
                return ret;
            }
        }
        return null;
    }
    
    @Override
    public IBlockState getBlockState(String name, String variant) {
        if (variant == null) {
            final int idx = name.indexOf(35);
            if (idx != -1) {
                variant = name.substring(idx + 1);
                name = name.substring(0, idx);
            }
        }
        final BlockName blockName = this.getBlockName(name);
        if (blockName != null) {
            final Block block = blockName.getInstance();
            if (block instanceof IMultiBlock) {
                return ((IMultiBlock)block).getState(variant);
            }
            assert variant == null;
            return block.getDefaultState();
        }
        else {
            final FluidName fluidName = this.getFluidName(name);
            if (fluidName == null) {
                return null;
            }
            assert variant == null;
            return fluidName.getInstance().getBlock().getDefaultState();
        }
    }
    
    private ItemName getItemName(final String itemName) {
        for (final ItemName name : ItemName.values) {
            if (name.name().equalsIgnoreCase(itemName)) {
                return name;
            }
        }
        return null;
    }
    
    private BlockName getBlockName(final String blockName) {
        for (final BlockName name : BlockName.values) {
            if (name.name().equalsIgnoreCase(blockName)) {
                return name;
            }
        }
        return null;
    }
    
    private FluidName getFluidName(final String fluidName) {
        for (final FluidName name : FluidName.values) {
            if (name.name().equalsIgnoreCase(fluidName)) {
                return name;
            }
        }
        return null;
    }
}

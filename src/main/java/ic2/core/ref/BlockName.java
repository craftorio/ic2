// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.block.state.IBlockState;
import ic2.core.block.state.IIdProvider;
import net.minecraft.block.Block;

public enum BlockName
{
    te, 
    resource, 
    leaves, 
    rubber_wood, 
    sapling, 
    scaffold, 
    foam, 
    fence, 
    sheet, 
    glass, 
    wall, 
    mining_pipe, 
    reinforced_door, 
    dynamite, 
    refractory_bricks;
    
    private Block instance;
    public static final BlockName[] values;
    
    public boolean hasInstance() {
        return this.instance != null;
    }
    
    public <T extends Block & IBlockModelProvider> T getInstance() {
        if (this.instance == null) {
            throw new IllegalStateException("the requested block instance for " + this.name() + " isn't set (yet)");
        }
        return (T)this.instance;
    }
    
    public <T extends Block & IBlockModelProvider> void setInstance(final T instance) {
        if (this.instance != null) {
            throw new IllegalStateException("conflicting instance");
        }
        this.instance = instance;
    }
    
    public <T extends IIdProvider> IBlockState getBlockState(final T variant) {
        if (this.instance == null) {
            return null;
        }
        if (this.instance instanceof IMultiBlock) {
            final IMultiBlock<T> block = (IMultiBlock<T>)this.instance;
            return block.getState(variant);
        }
        if (variant == null) {
            return this.instance.getDefaultState();
        }
        throw new IllegalArgumentException("not applicable");
    }
    
    public boolean hasItemStack() {
        if (this.instance == null) {
            return false;
        }
        if (this.instance instanceof IMultiItem) {
            return true;
        }
        final Item item = Item.getItemFromBlock(this.instance);
        return item != null && item != Items.AIR;
    }
    
    public <T extends Enum<T> & IIdProvider> ItemStack getItemStack() {
        return this.getItemStack((String)null);
    }
    
    public <T extends Enum<T> & IIdProvider> ItemStack getItemStack(final T variant) {
        if (this.instance == null) {
            return null;
        }
        if (this.instance instanceof IMultiItem) {
            final IMultiItem<T> multiItem = (IMultiItem<T>)this.instance;
            return multiItem.getItemStack(variant);
        }
        if (variant == null) {
            return this.getItemStack((String)null);
        }
        throw new IllegalArgumentException("not applicable");
    }
    
    public <T extends Enum<T> & IIdProvider> ItemStack getItemStack(final String variant) {
        if (this.instance == null) {
            return null;
        }
        if (this.instance instanceof IMultiItem) {
            final IMultiItem<T> multiItem = (IMultiItem<T>)this.instance;
            return multiItem.getItemStack(variant);
        }
        if (variant != null) {
            throw new IllegalArgumentException("not applicable");
        }
        final Item item = Item.getItemFromBlock(this.instance);
        if (item == null || item == Items.AIR) {
            throw new IllegalArgumentException("No item found for " + this.instance);
        }
        return new ItemStack(item);
    }
    
    public String getVariant(final ItemStack stack) {
        if (this.instance == null) {
            return null;
        }
        if (this.instance instanceof IMultiItem) {
            return ((IMultiItem)this.instance).getVariant(stack);
        }
        return null;
    }
    
    static {
        values = values();
    }
}

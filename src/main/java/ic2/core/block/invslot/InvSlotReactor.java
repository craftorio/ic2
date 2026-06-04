// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;

public class InvSlotReactor extends InvSlot
{
    private final int rows = 6;
    private final int maxCols = 9;
    
    public InvSlotReactor(final TileEntityNuclearReactorElectric base1, final String name1, final int count) {
        super(base1, name1, Access.IO, count);
        this.setStackSizeLimit(1);
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        return ((TileEntityNuclearReactorElectric)this.base).isUsefulItem(stack, true);
    }
    
    @Override
    public int size() {
        return ((TileEntityNuclearReactorElectric)this.base).getReactorSize() * 6;
    }
    
    public int rawSize() {
        return super.size();
    }
    
    @Override
    public ItemStack get(final int index) {
        return super.get(this.mapIndex(index));
    }
    
    public ItemStack get(final int x, final int y) {
        return super.get(y * 9 + x);
    }
    
    @Override
    protected void putFromNBT(final int index, final ItemStack content) {
        super.putFromNBT(this.mapIndex(index), content);
    }
    
    @Override
    public void put(final int index, final ItemStack content) {
        super.put(this.mapIndex(index), content);
    }
    
    public void put(final int x, final int y, final ItemStack content) {
        super.put(y * 9 + x, content);
    }
    
    private int mapIndex(int index) {
        final int size = this.size();
        final int cols = size / 6;
        if (index < size) {
            final int row = index / cols;
            final int col = index % cols;
            return row * 9 + col;
        }
        index -= size;
        final int remCols = 9 - cols;
        final int row2 = index / remCols;
        final int col2 = cols + index % remCols;
        return row2 * 9 + col2;
    }
}

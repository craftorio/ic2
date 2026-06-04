// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;
import java.util.function.IntSupplier;

public abstract class BasicRedstoneComponent extends TileEntityComponent
{
    private int level;
    private IntSupplier update;
    
    public BasicRedstoneComponent(final TileEntityBlock parent) {
        super(parent);
    }
    
    public int getLevel() {
        return this.level;
    }
    
    public void setLevel(final int newLevel) {
        if (newLevel == this.level) {
            return;
        }
        this.level = newLevel;
        this.onChange();
    }
    
    public abstract void onChange();
    
    @Override
    public boolean enableWorldTick() {
        return this.update != null;
    }
    
    @Override
    public void onWorldTick() {
        assert this.update != null;
        this.setLevel(this.update.getAsInt());
    }
    
    public void setUpdate(final IntSupplier update) {
        this.update = update;
    }
}

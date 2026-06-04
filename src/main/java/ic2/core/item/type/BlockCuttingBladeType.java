// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.type;

import ic2.core.profile.NotClassic;
import ic2.core.block.state.IIdProvider;

@NotClassic
public enum BlockCuttingBladeType implements IIdProvider
{
    iron(0), 
    steel(1), 
    diamond(2);
    
    private final int id;
    
    private BlockCuttingBladeType(final int id) {
        this.id = id;
    }
    
    @Override
    public String getName() {
        return this.name();
    }
    
    @Override
    public int getId() {
        return this.id;
    }
}

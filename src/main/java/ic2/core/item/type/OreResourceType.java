// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.type;

import ic2.core.profile.NotClassic;
import ic2.core.block.state.IIdProvider;

@NotClassic
public enum OreResourceType implements IIdProvider
{
    copper(0), 
    gold(1), 
    iron(2), 
    lead(3), 
    silver(4), 
    tin(5), 
    uranium(6);
    
    private final int id;
    
    private OreResourceType(final int id) {
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

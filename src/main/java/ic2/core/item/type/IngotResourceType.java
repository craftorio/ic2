// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.type;

import ic2.core.profile.NotExperimental;
import ic2.core.profile.NotClassic;
import ic2.core.block.state.IIdProvider;

public enum IngotResourceType implements IIdProvider
{
    alloy(0), 
    bronze(1), 
    copper(2), 
    @NotClassic
    lead(3), 
    @NotClassic
    silver(4), 
    @NotClassic
    steel(5), 
    tin(6), 
    @NotExperimental
    refined_iron(7), 
    @NotExperimental
    uranium(8);
    
    private final int id;
    
    private IngotResourceType(final int id) {
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

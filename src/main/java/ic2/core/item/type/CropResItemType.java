// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.type;

import ic2.core.profile.NotClassic;
import ic2.core.block.state.IIdProvider;

public enum CropResItemType implements IIdProvider
{
    coffee_beans(0), 
    coffee_powder(1), 
    fertilizer(2), 
    grin_powder(3), 
    hops(4), 
    @NotClassic
    weed(5), 
    milk_wart(6), 
    oil_berry(7), 
    bobs_yer_uncle_ranks_berry(8);
    
    private final int id;
    
    private CropResItemType(final int id) {
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

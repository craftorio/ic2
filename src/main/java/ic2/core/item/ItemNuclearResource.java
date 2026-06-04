// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.reactor.IBaseReactorComponent;
import ic2.core.item.type.NuclearResourceType;

public class ItemNuclearResource extends ItemMulti<NuclearResourceType> implements IBaseReactorComponent
{
    public ItemNuclearResource() {
        super(ItemName.nuclear, NuclearResourceType.class);
    }
    
    @Override
    public boolean canBePlacedIn(final ItemStack stack, final IReactor reactor) {
        return false;
    }
}

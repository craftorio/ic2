// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.reactor.tileentity;

import ic2.api.reactor.IReactor;
import ic2.core.block.comp.FluidReactorLookup;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.TileEntityBlock;

public class TileEntityReactorVessel extends TileEntityBlock implements IReactorChamber
{
    protected final FluidReactorLookup lookup;
    
    public TileEntityReactorVessel() {
        this.lookup = this.addComponent(new FluidReactorLookup(this));
    }
    
    @Override
    public TileEntityNuclearReactorElectric getReactorInstance() {
        return this.lookup.getReactor();
    }
    
    @Override
    public boolean isWall() {
        return true;
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import ic2.core.util.Util;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.EnergyNet;
import ic2.core.block.TileEntityBlock;
import ic2.core.IC2;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.ref.TeBlock;

@TeBlock.Delegated(current = TileEntityCableDetector.class, old = TileEntityClassicCableDetector.class)
public class TileEntityCableDetector extends TileEntityCable
{
    private static final int tickRate = 32;
    protected final RedstoneEmitter rsEmitter;
    protected final ComparatorEmitter comparator;
    private int ticker;
    
    public static Class<? extends TileEntityCable> delegate() {
        return (Class<? extends TileEntityCable>)(IC2.version.isClassic() ? TileEntityClassicCableDetector.class : TileEntityCableDetector.class);
    }
    
    public TileEntityCableDetector() {
        super(CableType.detector, 0);
        this.ticker = 0;
        this.rsEmitter = this.addComponent(new RedstoneEmitter(this));
        this.comparator = this.addComponent(new ComparatorEmitter(this));
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (++this.ticker % 32 == 0) {
            final double energy = EnergyNet.instance.getNodeStats(this).getEnergyIn();
            if (energy > 0.0) {
                this.setActive(true);
                this.rsEmitter.setLevel(15);
            }
            else {
                this.setActive(false);
                this.rsEmitter.setLevel(0);
            }
            this.comparator.setLevel((int)Util.map(EnergyNet.instance.getNodeStats(this).getEnergyIn() / (this.getConductorBreakdownEnergy() - 1.0), 1.0, 15.0));
        }
    }
}

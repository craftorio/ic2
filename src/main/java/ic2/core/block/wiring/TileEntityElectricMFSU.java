// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import ic2.core.IC2;
import ic2.core.ref.TeBlock;

@TeBlock.Delegated(current = TileEntityElectricMFSU.class, old = TileEntityElectricClassicMFSU.class)
public class TileEntityElectricMFSU extends TileEntityElectricBlock
{
    public static Class<? extends TileEntityElectricBlock> delegate() {
        return (Class<? extends TileEntityElectricBlock>)(IC2.version.isClassic() ? TileEntityElectricClassicMFSU.class : TileEntityElectricMFSU.class);
    }
    
    public TileEntityElectricMFSU() {
        super(4, 2048, 40000000);
    }
    
    @TeBlock.Delegated(current = TileEntityElectricMFSU.class, old = TileEntityElectricClassicMFSU.class)
    public static class TileEntityElectricClassicMFSU extends TileEntityElectricBlock
    {
        public TileEntityElectricClassicMFSU() {
            super(3, 512, 10000000);
            this.chargeSlot.setTier(4);
            this.dischargeSlot.setTier(4);
        }
    }
}

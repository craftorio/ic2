// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import ic2.core.IC2;
import ic2.core.ref.TeBlock;

@TeBlock.Delegated(current = TileEntityElectricMFE.class, old = TileEntityElectricClassicMFE.class)
public class TileEntityElectricMFE extends TileEntityElectricBlock
{
    public static Class<? extends TileEntityElectricBlock> delegate() {
        return (Class<? extends TileEntityElectricBlock>)(IC2.version.isClassic() ? TileEntityElectricClassicMFE.class : TileEntityElectricMFE.class);
    }
    
    public TileEntityElectricMFE() {
        super(3, 512, 4000000);
    }
    
    @TeBlock.Delegated(current = TileEntityElectricMFE.class, old = TileEntityElectricClassicMFE.class)
    public static class TileEntityElectricClassicMFE extends TileEntityElectricBlock
    {
        public TileEntityElectricClassicMFE() {
            super(2, 128, 600000);
            this.chargeSlot.setTier(3);
            this.dischargeSlot.setTier(3);
        }
    }
}

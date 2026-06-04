// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.WorldSearchUtil;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;

public class FluidReactorLookup extends TileEntityComponent
{
    private TileEntityNuclearReactorElectric reactor;
    private long lastReactorUpdate;
    
    public FluidReactorLookup(final TileEntityBlock parent) {
        super(parent);
    }
    
    public TileEntityNuclearReactorElectric getReactor() {
        final long time = this.parent.getWorld().getTotalWorldTime();
        if (time != this.lastReactorUpdate) {
            this.updateReactor();
            this.lastReactorUpdate = time;
        }
        else if (this.reactor != null && (this.reactor.isInvalid() || !this.reactor.isFluidCooled())) {
            this.reactor = null;
        }
        return this.reactor;
    }
    
    private void updateReactor() {
        final int dist = 2;
        final World world = this.parent.getWorld();
        final BlockPos pos = this.parent.getPos();
        if (!world.isAreaLoaded(pos, 2)) {
            this.reactor = null;
            return;
        }
        if (this.reactor != null && !this.reactor.isInvalid() && this.reactor.isFluidCooled() && this.reactor.getWorld() == world && world.getTileEntity(this.reactor.getPos()) == this.reactor) {
            final BlockPos reactorPos = this.reactor.getPos();
            final int dx = Math.abs(pos.getX() - reactorPos.getX());
            final int dy = Math.abs(pos.getY() - reactorPos.getY());
            final int dz = Math.abs(pos.getZ() - reactorPos.getZ());
            if (dx <= 2 && dy <= 2 && dz <= 2 && (dx == 2 || dy == 2 || dz == 2)) {
                return;
            }
        }
        this.reactor = null;
        WorldSearchUtil.findTileEntities(world, pos, 2, new WorldSearchUtil.ITileEntityResultHandler() {
            @Override
            public boolean onMatch(final TileEntity te) {
                if (te instanceof TileEntityNuclearReactorElectric) {
                    final TileEntityNuclearReactorElectric cReactor = (TileEntityNuclearReactorElectric)te;
                    if (cReactor.isFluidCooled()) {
                        FluidReactorLookup.this.reactor = cReactor;
                        return true;
                    }
                }
                return false;
            }
        });
    }
}

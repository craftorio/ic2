// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import net.minecraft.util.math.Vec3i;
import java.util.stream.Stream;
import java.util.Iterator;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import java.util.Set;
import net.minecraft.world.World;

public class TradingMarket
{
    private final World world;
    private final Set<BlockPos> traders;
    private final List<MarketWatcher> watchers;
    
    public TradingMarket(final World world) {
        this.traders = new HashSet<BlockPos>();
        this.watchers = new ArrayList<MarketWatcher>(1);
        this.world = world;
        this.watchers.add(new MarketWatcher() {
            @Override
            public void onAdd(final BlockPos pos) {
                TradingMarket.this.traders.add(pos);
            }
            
            @Override
            public void onRemove(final BlockPos pos) {
                TradingMarket.this.traders.remove(pos);
            }
        });
        if (Util.inDev()) {
            this.watchers.add(new MarketWatcher() {
                @Override
                public void onAdd(final BlockPos pos) {
                    IC2.log.info(LogCategory.Block, "Market registration at " + Util.formatPosition(world.getTileEntity(pos)));
                }
                
                @Override
                public void onRemove(final BlockPos pos) {
                    IC2.log.info(LogCategory.Block, "Market removal at " + Util.formatPosition(world.getTileEntity(pos)));
                }
            });
        }
    }
    
    public void registerTradeOMat(final TileEntityTradeOMat tradeOMat) {
        assert tradeOMat.hasWorld() && !tradeOMat.getWorld().isRemote;
        assert tradeOMat.getWorld() == this.world;
        assert !this.traders.contains(tradeOMat.getPos());
        for (final MarketWatcher watcher : this.watchers) {
            watcher.onAdd(tradeOMat.getPos());
        }
    }
    
    public void unregisterTradeOMat(final TileEntityTradeOMat tradeOMat) {
        assert tradeOMat.hasWorld() && !tradeOMat.getWorld().isRemote;
        assert this.traders.contains(tradeOMat.getPos());
        for (final MarketWatcher watcher : this.watchers) {
            watcher.onRemove(tradeOMat.getPos());
        }
    }
    
    public void registerWatcher(final MarketWatcher watcher) {
        assert !this.watchers.contains(watcher);
        this.watchers.add(watcher);
    }
    
    public void unregisterWatcher(final MarketWatcher watcher) {
        assert this.watchers.contains(watcher);
        this.watchers.remove(watcher);
    }
    
    public Stream<BlockPos> tradersAround(final BlockPos position, final int radius) {
        final long squareRadius = radius * radius;
        return this.traders.stream().filter(pos -> position.distanceSq((Vec3i)pos) <= squareRadius);
    }
    
    public interface MarketWatcher
    {
        void onAdd(final BlockPos p0);
        
        void onRemove(final BlockPos p0);
    }
}

package ic2.core.block.personal;

import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class TradingMarket {
  private final World world;
  
  private final Set<BlockPos> traders;
  
  private final List<MarketWatcher> watchers;
  
  public TradingMarket(final World world) {
    this.traders = new HashSet<>();
    this.watchers = new ArrayList<>(1);
    this.world = world;
    this.watchers.add(new MarketWatcher() {
          public void onAdd(BlockPos pos) {
            TradingMarket.this.traders.add(pos);
          }
          
          public void onRemove(BlockPos pos) {
            TradingMarket.this.traders.remove(pos);
          }
        });
    if (Util.inDev())
      this.watchers.add(new MarketWatcher() {
            public void onAdd(BlockPos pos) {
              IC2.log.info(LogCategory.Block, "Market registration at " + Util.formatPosition(world.func_175625_s(pos)));
            }
            
            public void onRemove(BlockPos pos) {
              IC2.log.info(LogCategory.Block, "Market removal at " + Util.formatPosition(world.func_175625_s(pos)));
            }
          }); 
  }
  
  public void registerTradeOMat(TileEntityTradeOMat tradeOMat) {
    assert tradeOMat.func_145830_o() && !(tradeOMat.func_145831_w()).field_72995_K;
    assert tradeOMat.func_145831_w() == this.world;
    assert !this.traders.contains(tradeOMat.func_174877_v());
    for (MarketWatcher watcher : this.watchers)
      watcher.onAdd(tradeOMat.func_174877_v()); 
  }
  
  public void unregisterTradeOMat(TileEntityTradeOMat tradeOMat) {
    assert tradeOMat.func_145830_o() && !(tradeOMat.func_145831_w()).field_72995_K;
    assert this.traders.contains(tradeOMat.func_174877_v());
    for (MarketWatcher watcher : this.watchers)
      watcher.onRemove(tradeOMat.func_174877_v()); 
  }
  
  public void registerWatcher(MarketWatcher watcher) {
    assert !this.watchers.contains(watcher);
    this.watchers.add(watcher);
  }
  
  public void unregisterWatcher(MarketWatcher watcher) {
    assert this.watchers.contains(watcher);
    this.watchers.remove(watcher);
  }
  
  public Stream<BlockPos> tradersAround(BlockPos position, int radius) {
    long squareRadius = (radius * radius);
    return this.traders.stream().filter(pos -> (position.func_177951_i((Vec3i)pos) <= squareRadius));
  }
  
  public static interface MarketWatcher {
    void onAdd(BlockPos param1BlockPos);
    
    void onRemove(BlockPos param1BlockPos);
  }
}

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
import net.minecraft.world.World;

public class TradingMarket {
   private final World world;
   private final Set<BlockPos> traders = new HashSet<>();
   private final List<TradingMarket.MarketWatcher> watchers = new ArrayList<>(1);

   public TradingMarket(final World world) {
      this.world = world;
      this.watchers.add(new TradingMarket.MarketWatcher() {
         @Override
         public void onAdd(BlockPos pos) {
            TradingMarket.this.traders.add(pos);
         }

         @Override
         public void onRemove(BlockPos pos) {
            TradingMarket.this.traders.remove(pos);
         }
      });
      if (Util.inDev()) {
         this.watchers.add(new TradingMarket.MarketWatcher() {
            @Override
            public void onAdd(BlockPos pos) {
               IC2.log.info(LogCategory.Block, "Market registration at " + Util.formatPosition(world.getTileEntity(pos)));
            }

            @Override
            public void onRemove(BlockPos pos) {
               IC2.log.info(LogCategory.Block, "Market removal at " + Util.formatPosition(world.getTileEntity(pos)));
            }
         });
      }
   }

   public void registerTradeOMat(TileEntityTradeOMat tradeOMat) {
      assert tradeOMat.hasWorld() && !tradeOMat.getWorld().isRemote;
      assert tradeOMat.getWorld() == this.world;
      assert !this.traders.contains(tradeOMat.getPos());

      for (TradingMarket.MarketWatcher watcher : this.watchers) {
         watcher.onAdd(tradeOMat.getPos());
      }
   }

   public void unregisterTradeOMat(TileEntityTradeOMat tradeOMat) {
      assert tradeOMat.hasWorld() && !tradeOMat.getWorld().isRemote;
      assert this.traders.contains(tradeOMat.getPos());

      for (TradingMarket.MarketWatcher watcher : this.watchers) {
         watcher.onRemove(tradeOMat.getPos());
      }
   }

   public void registerWatcher(TradingMarket.MarketWatcher watcher) {
      assert !this.watchers.contains(watcher);
      this.watchers.add(watcher);
   }

   public void unregisterWatcher(TradingMarket.MarketWatcher watcher) {
      assert this.watchers.contains(watcher);
      this.watchers.remove(watcher);
   }

   public Stream<BlockPos> tradersAround(BlockPos position, int radius) {
      long squareRadius = radius * radius;
      return this.traders.stream().filter(pos -> position.distanceSq(pos) <= squareRadius);
   }

   public interface MarketWatcher {
      void onAdd(BlockPos var1);

      void onRemove(BlockPos var1);
   }
}

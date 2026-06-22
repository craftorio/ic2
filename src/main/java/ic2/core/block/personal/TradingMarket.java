package ic2.core.block.personal;

import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TradingMarket
{
	private final Level world;
	private final Set<BlockPos> traders = new HashSet<>();
	private final List<TradingMarket.MarketWatcher> watchers = new ArrayList<>(1);

	public TradingMarket(Level world)
	{
		this.world = world;
		this.watchers.add(new TradingMarket.MarketWatcher()
		{
			@Override
			public void onAdd(BlockPos pos)
			{
				TradingMarket.this.traders.add(pos);
			}

			@Override
			public void onRemove(BlockPos pos)
			{
				TradingMarket.this.traders.remove(pos);
			}
		});
		if (Util.inDev())
		{
			this.watchers.add(new TradingMarket.MarketWatcher()
			{
				@Override
				public void onAdd(BlockPos pos)
				{
					IC2.log.info(LogCategory.Block, "Market registration at " + Util.formatPosition(world.getBlockEntity(pos)));
				}

				@Override
				public void onRemove(BlockPos pos)
				{
					IC2.log.info(LogCategory.Block, "Market removal at " + Util.formatPosition(world.getBlockEntity(pos)));
				}
			});
		}
	}

	public void registerTradeOMat(TileEntityTradeOMat tradeOMat)
	{
		assert tradeOMat.hasLevel() && !tradeOMat.getLevel().isClientSide;
		assert tradeOMat.getLevel() == this.world;
		assert !this.traders.contains(tradeOMat.getBlockPos());

		for (TradingMarket.MarketWatcher watcher : this.watchers)
		{
			watcher.onAdd(tradeOMat.getBlockPos());
		}
	}

	public void unregisterTradeOMat(TileEntityTradeOMat tradeOMat)
	{
		assert tradeOMat.hasLevel() && !tradeOMat.getLevel().isClientSide;
		assert this.traders.contains(tradeOMat.getBlockPos());

		for (TradingMarket.MarketWatcher watcher : this.watchers)
		{
			watcher.onRemove(tradeOMat.getBlockPos());
		}
	}

	public void registerWatcher(TradingMarket.MarketWatcher watcher)
	{
		assert !this.watchers.contains(watcher);
		this.watchers.add(watcher);
	}

	public void unregisterWatcher(TradingMarket.MarketWatcher watcher)
	{
		assert this.watchers.contains(watcher);
		this.watchers.remove(watcher);
	}

	public Stream<BlockPos> tradersAround(BlockPos position, int radius)
	{
		long squareRadius = (long) radius * radius;
		return this.traders.stream().filter(pos -> position.distSqr(pos) <= squareRadius);
	}

	public interface MarketWatcher
	{
		void onAdd(BlockPos var1);

		void onRemove(BlockPos var1);
	}
}

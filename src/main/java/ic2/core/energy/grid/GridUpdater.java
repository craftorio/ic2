package ic2.core.energy.grid;

import ic2.api.energy.tile.IEnergyTile;
import ic2.core.IC2;
import ic2.core.util.LogCategory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

class GridUpdater implements Runnable
{
	private static final long AWAIT_TIMEOUT_MS = 5000L;
	private final EnergyNetLocal enet;
	private final Queue<GridChange> changes = new ArrayDeque<>();
	private final GridUpdater.GridCalcTask[] calcTaskCache = new GridUpdater.GridCalcTask[16];
	private final AtomicInteger pendingCalculations = new AtomicInteger(0);
	private final AtomicInteger generation = new AtomicInteger(0);
	private boolean busy;
	private boolean isChangeStep;

	GridUpdater(EnergyNetLocal enet)
	{
		this.enet = enet;
	}

	void startChangeCalc(Queue<GridChange> changes, Map<IEnergyTile, GridChange> additions)
	{
		assert !changes.isEmpty();
		assert this.changes.isEmpty();
		assert !this.busy;
		this.busy = true;
		this.isChangeStep = true;
		this.generation.incrementAndGet();

		GridChange change;
		while ((change = changes.poll()) != null && change != EnergyNetLocal.QUEUE_DELAY_CHANGE)
		{
			this.changes.add(change);
			if (change.type == GridChange.Type.ADDITION)
			{
				GridChange removedChange = additions.remove(change.ioTile);
				assert removedChange == change;
			}
		}

		this.prepareUpdate();
		IC2.threadPool.execute(this);
	}

	void startTransferCalc()
	{
		assert !this.busy;
		this.isChangeStep = false;
		if (this.enet.hasGrids() && EnergyNetGlobal.getCalculator().runSyncStep(this.enet))
		{
			this.busy = true;
			this.generation.incrementAndGet();
			Collection<Grid> grids = this.enet.getGrids();
			this.pendingCalculations.set(grids.size());
			int cacheIdx = 0;

			for (Grid grid : grids)
			{
				if (EnergyNetGlobal.getCalculator().runSyncStep(grid))
				{
					GridUpdater.GridCalcTask task;
					if (cacheIdx < this.calcTaskCache.length)
					{
						task = this.calcTaskCache[cacheIdx];
						if (task == null)
						{
							this.calcTaskCache[cacheIdx] = task = new GridUpdater.GridCalcTask();
						}

						cacheIdx++;
					} else
					{
						task = new GridUpdater.GridCalcTask();
					}

					task.grid = grid;
					task.taskGeneration = this.generation.get();
					IC2.threadPool.execute(task);
				} else
				{
					this.pendingCalculations.decrementAndGet();
				}
			}

			if (grids.size() > 1)
			{
				this.enet.shuffleGrids();
			}

			if (this.pendingCalculations.get() == 0)
			{
				this.busy = false;
			}
		}
	}

	void awaitCompletion()
	{
		try
		{
			synchronized (this)
			{
				long startTime = System.currentTimeMillis();

				while (this.busy)
				{
					long elapsed = System.currentTimeMillis() - startTime;
					long remaining = AWAIT_TIMEOUT_MS - elapsed;
					if (remaining <= 0L)
					{
						IC2.log.error(LogCategory.EnergyNet, "GridUpdater.awaitCompletion() timed out after %dms! " +
							"pendingCalculations=%d, isChangeStep=%b, changes=%d. " +
							"Forcing state reset to prevent server hang. This may indicate a deadlock or infinite loop in energy net calculation.",
							elapsed, this.pendingCalculations.get(), this.isChangeStep, this.changes.size());
						this.forceClear();
						break;
					}

					this.wait(remaining);
				}
			}
		} catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	private synchronized void forceClear()
	{
		this.generation.incrementAndGet();
		this.pendingCalculations.set(0);
		this.busy = false;
		this.notifyAll();
	}

	public boolean isInChangeStep()
	{
		return this.isChangeStep;
	}

	@Override
	public void run()
	{
		try
		{
			this.updateGrid();
		} catch (Throwable t)
		{
			IC2.log.error(LogCategory.EnergyNet, t, "Unhandled exception/error in GridUpdater.run(), force-clearing busy flag.");
			this.forceClear();
		}
	}

	private void prepareUpdate()
	{

		this.changes.removeIf(change -> !ChangeHandler.prepareSync(this.enet, change));
	}

	private void updateGrid()
	{
		long startTime = 0L;
		int totalChanges = this.changes.size();

		try
		{
			GridChange change;
			while ((change = this.changes.poll()) != null)
			{
				switch (change.type)
				{
					case ADDITION:
						ChangeHandler.applyAddition(this.enet, change.ioTile, change.pos, change.subTiles, this.changes);
						break;
					case REMOVAL:
						ChangeHandler.applyRemoval(this.enet, change.ioTile, change.pos);
				}
			}

			this.notifyCalculator();
		} catch (Throwable t)
		{
			IC2.log.error(LogCategory.EnergyNet, t, "Unhandled exception/error in GridUpdater.updateGrid(), force-clearing busy flag.");
			this.forceClear();
		}
	}

	private void notifyCalculator()
	{
		int gen = this.generation.get();
		List<Grid> dirtyGrids = new ArrayList<>();

		for (Grid grid : this.enet.getGrids())
		{
			if (grid.clearDirty())
			{
				dirtyGrids.add(grid);
			}
		}

		if (dirtyGrids.isEmpty())
		{
			this.clearBusy();
		} else
		{
			this.pendingCalculations.set(dirtyGrids.size());
			if (dirtyGrids.size() > 1)
			{
				for (int i = 1; i < dirtyGrids.size(); i++)
				{
					GridUpdater.GridUpdateTask task = new GridUpdater.GridUpdateTask();
					task.grid = dirtyGrids.get(i);
					task.taskGeneration = gen;
					IC2.threadPool.execute(task);
				}
			}

			EnergyNetGlobal.getCalculator().handleGridChange(dirtyGrids.get(0));
			this.onTaskDone(gen);
		}
	}

	private void onTaskDone(int taskGeneration)
	{
		if (this.generation.get() != taskGeneration)
		{
			return;
		}

		if (this.pendingCalculations.decrementAndGet() == 0)
		{
			this.clearBusy();
		}
	}

	private synchronized void clearBusy()
	{
		this.busy = false;
		this.notifyAll();
	}

	private class GridCalcTask implements Runnable
	{
		Grid grid;
		int taskGeneration;

		@Override
		public void run()
		{
			try
			{
				EnergyNetGlobal.getCalculator().runAsyncStep(this.grid);
			} catch (Throwable t)
			{
				IC2.log.error(LogCategory.EnergyNet, t, "Unhandled exception/error in GridCalcTask.run() for grid %s.", this.grid);
			}

			this.grid = null;
			GridUpdater.this.onTaskDone(this.taskGeneration);
		}
	}

	private class GridUpdateTask implements Runnable
	{
		Grid grid;
		int taskGeneration;

		@Override
		public void run()
		{
			try
			{
				EnergyNetGlobal.getCalculator().handleGridChange(this.grid);
			} catch (Throwable t)
			{
				IC2.log.error(LogCategory.EnergyNet, t, "Unhandled exception/error in GridUpdateTask.run() for grid %s.", this.grid);
			}

			this.grid = null;
			GridUpdater.this.onTaskDone(this.taskGeneration);
		}
	}
}

package ic2.core.energy.grid;

import ic2.api.energy.tile.IEnergyTile;
import ic2.core.IC2;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

class GridUpdater implements Runnable {
  private final EnergyNetLocal enet;
  
  private final Queue<GridChange> changes;
  
  private final GridCalcTask[] calcTaskCache;
  
  private final AtomicInteger pendingCalculations;
  
  private boolean busy;
  
  private boolean isChangeStep;
  
  GridUpdater(EnergyNetLocal enet) {
    this.changes = new ArrayDeque<>();
    this.calcTaskCache = new GridCalcTask[16];
    this.pendingCalculations = new AtomicInteger(0);
    this.enet = enet;
  }
  
  void startChangeCalc(Queue<GridChange> changes, Map<IEnergyTile, GridChange> additions) {
    assert !changes.isEmpty();
    assert this.changes.isEmpty();
    assert !this.busy;
    this.busy = true;
    this.isChangeStep = true;
    GridChange change;
    while ((change = changes.poll()) != null && change != EnergyNetLocal.QUEUE_DELAY_CHANGE) {
      this.changes.add(change);
      if (change.type == GridChange.Type.ADDITION) {
        GridChange removedChange = additions.remove(change.ioTile);
        assert removedChange == change;
      } 
    } 
    prepareUpdate();
    (IC2.getInstance()).threadPool.execute(this);
  }
  
  void startTransferCalc() {
    assert !this.busy;
    this.isChangeStep = false;
    if (this.enet.hasGrids() && EnergyNetGlobal.getCalculator().runSyncStep(this.enet)) {
      this.busy = true;
      Collection<Grid> grids = this.enet.getGrids();
      this.pendingCalculations.set(grids.size());
      int cacheIdx = 0;
      for (Grid grid : grids) {
        if (EnergyNetGlobal.getCalculator().runSyncStep(grid)) {
          GridCalcTask task;
          if (cacheIdx < this.calcTaskCache.length) {
            task = this.calcTaskCache[cacheIdx];
            if (task == null)
              this.calcTaskCache[cacheIdx] = task = new GridCalcTask(); 
            cacheIdx++;
          } else {
            task = new GridCalcTask();
          } 
          task.grid = grid;
          (IC2.getInstance()).threadPool.execute(task);
          continue;
        } 
        this.pendingCalculations.decrementAndGet();
      } 
      if (grids.size() > 1)
        this.enet.shuffleGrids(); 
      if (this.pendingCalculations.get() == 0)
        this.busy = false; 
    } 
  }
  
  void awaitCompletion() {
    try {
      synchronized (this) {
        while (this.busy)
          wait(); 
      } 
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public boolean isInChangeStep() {
    return this.isChangeStep;
  }
  
  public void run() {
    updateGrid();
  }
  
  private void prepareUpdate() {
    for (Iterator<GridChange> it = this.changes.iterator(); it.hasNext(); ) {
      GridChange change = it.next();
      if (!ChangeHandler.prepareSync(this.enet, change))
        it.remove(); 
    } 
  }
  
  private void updateGrid() {
    long startTime = 0L;
    int totalChanges = this.changes.size();
    GridChange change;
    while ((change = this.changes.poll()) != null) {
      switch (change.type) {
        case ADDITION:
          ChangeHandler.applyAddition(this.enet, change.ioTile, change.pos, change.subTiles, this.changes);
        case REMOVAL:
          ChangeHandler.applyRemoval(this.enet, change.ioTile, change.pos);
      } 
    } 
    notifyCalculator();
  }
  
  private void notifyCalculator() {
    List<Grid> dirtyGrids = new ArrayList<>();
    for (Grid grid : this.enet.getGrids()) {
      if (grid.clearDirty())
        dirtyGrids.add(grid); 
    } 
    if (dirtyGrids.isEmpty()) {
      clearBusy();
      return;
    } 
    this.pendingCalculations.set(dirtyGrids.size());
    if (dirtyGrids.size() > 1)
      for (int i = 1; i < dirtyGrids.size(); i++) {
        GridUpdateTask task = new GridUpdateTask();
        task.grid = dirtyGrids.get(i);
        (IC2.getInstance()).threadPool.execute(task);
      }  
    EnergyNetGlobal.getCalculator().handleGridChange(dirtyGrids.get(0));
    onTaskDone();
  }
  
  private void onTaskDone() {
    if (this.pendingCalculations.decrementAndGet() == 0)
      clearBusy(); 
  }
  
  private synchronized void clearBusy() {
    this.busy = false;
    notifyAll();
  }
  
  private class GridUpdateTask implements Runnable {
    Grid grid;
    
    private GridUpdateTask() {}
    
    public void run() {
      EnergyNetGlobal.getCalculator().handleGridChange(this.grid);
      this.grid = null;
      GridUpdater.this.onTaskDone();
    }
  }
  
  private class GridCalcTask implements Runnable {
    Grid grid;
    
    private GridCalcTask() {}
    
    public void run() {
      EnergyNetGlobal.getCalculator().runAsyncStep(this.grid);
      this.grid = null;
      GridUpdater.this.onTaskDone();
    }
  }
}

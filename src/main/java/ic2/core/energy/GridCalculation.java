// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy;

import java.util.concurrent.Callable;

public class GridCalculation implements Callable<Iterable<Node>>
{
    private final Grid grid;
    
    public GridCalculation(final Grid grid1) {
        this.grid = grid1;
    }
    
    @Override
    public Iterable<Node> call() throws Exception {
        return this.grid.calculate();
    }
}

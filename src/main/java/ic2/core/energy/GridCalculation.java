package ic2.core.energy;

import java.util.concurrent.Callable;

public class GridCalculation implements Callable<Iterable<Node>>
{
	private final Grid grid;

	public GridCalculation(Grid grid1)
	{
		this.grid = grid1;
	}

	public Iterable<Node> call() throws Exception
	{
		return this.grid.calculate();
	}
}

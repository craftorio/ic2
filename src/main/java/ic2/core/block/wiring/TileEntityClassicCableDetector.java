package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.ref.TeBlock;
import ic2.core.util.Util;

@TeBlock.Delegated(current = TileEntityCableDetector.class, old = TileEntityClassicCableDetector.class)
public class TileEntityClassicCableDetector extends TileEntityClassicCable
{
	private int ticker = 0;
	private static final int tickRate = 32;
	protected final RedstoneEmitter rsEmitter = this.addComponent(new RedstoneEmitter(this));
	protected final ComparatorEmitter comparator = this.addComponent(new ComparatorEmitter(this));

	public TileEntityClassicCableDetector()
	{
		super(CableType.detector, 0);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (++this.ticker % 32 == 0)
		{
			double energy = EnergyNet.instance.getNodeStats(this).getEnergyIn();
			if (energy > 0.0)
			{
				this.setActive(true);
				this.rsEmitter.setLevel(15);
			} else
			{
				this.setActive(false);
				this.rsEmitter.setLevel(0);
			}

			this.comparator.setLevel((int) Util.map(energy / (this.getConductorBreakdownEnergy() - 1.0), 1.0, 15.0));
		}
	}
}

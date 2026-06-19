package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemReactorVent extends ItemReactorHeatStorage
{
	public final int selfVent;
	public final int reactorVent;

	public ItemReactorVent(Properties settings, int heatStorage, int selfvent, int reactorvent)
	{
		super(settings, heatStorage);
		this.selfVent = selfvent;
		this.reactorVent = reactorvent;
	}

	@Override
	public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatRun)
	{
		if (heatRun)
		{
			if (this.reactorVent > 0)
			{
				int rheat = reactor.getHeat();
				int reactorDrain = rheat;
				if (reactorDrain > this.reactorVent)
				{
					reactorDrain = this.reactorVent;
				}

				rheat -= reactorDrain;
				if (this.alterHeat(stack, reactor, x, y, reactorDrain) > 0)
				{
					return;
				}

				reactor.setHeat(rheat);
			}

			int self = this.alterHeat(stack, reactor, x, y, -this.selfVent);
			if (self <= 0)
			{
				reactor.addEmitHeat(self + this.selfVent);
			}
		}
	}
}

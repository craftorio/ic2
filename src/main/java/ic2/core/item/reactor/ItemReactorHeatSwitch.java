package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.ref.ItemName;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

public class ItemReactorHeatSwitch extends ItemReactorHeatStorage
{
	public final int switchSide;
	public final int switchReactor;

	public ItemReactorHeatSwitch(ItemName name, int heatStorage, int switchside, int switchreactor)
	{
		super(name, heatStorage);
		this.switchSide = switchside;
		this.switchReactor = switchreactor;
	}

	@Override
	public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatrun)
	{
		if (heatrun)
		{
			int myHeat = 0;
			ArrayList<ItemReactorHeatSwitch.ItemStackCoord> heatAcceptors = new ArrayList<>();
			if (this.switchSide > 0)
			{
				this.checkHeatAcceptor(reactor, x - 1, y, heatAcceptors);
				this.checkHeatAcceptor(reactor, x + 1, y, heatAcceptors);
				this.checkHeatAcceptor(reactor, x, y - 1, heatAcceptors);
				this.checkHeatAcceptor(reactor, x, y + 1, heatAcceptors);
			}

			if (this.switchSide > 0)
			{
				for (ItemReactorHeatSwitch.ItemStackCoord stackcoord : heatAcceptors)
				{
					IReactorComponent heatable = (IReactorComponent) stackcoord.stack.getItem();
					double mymed = this.getCurrentHeat(stack, reactor, x, y) * 100.0 / this.getMaxHeat(stack, reactor, x, y);
					double heatablemed = heatable.getCurrentHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y)
						* 100.0
						/ heatable.getMaxHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y);
					int add = (int) (heatable.getMaxHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y) / 100.0 * (heatablemed + mymed / 2.0));
					if (add > this.switchSide)
					{
						add = this.switchSide;
					}

					if (heatablemed + mymed / 2.0 < 1.0)
					{
						add = this.switchSide / 2;
					}

					if (heatablemed + mymed / 2.0 < 0.75)
					{
						add = this.switchSide / 4;
					}

					if (heatablemed + mymed / 2.0 < 0.5)
					{
						add = this.switchSide / 8;
					}

					if (heatablemed + mymed / 2.0 < 0.25)
					{
						add = 1;
					}

					if (Math.round(heatablemed * 10.0) / 10.0 > Math.round(mymed * 10.0) / 10.0)
					{
						add -= 2 * add;
					} else if (Math.round(heatablemed * 10.0) / 10.0 == Math.round(mymed * 10.0) / 10.0)
					{
						add = 0;
					}

					myHeat -= add;
					add = heatable.alterHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y, add);
					myHeat += add;
				}
			}

			if (this.switchReactor > 0)
			{
				double mymed = this.getCurrentHeat(stack, reactor, x, y) * 100.0 / this.getMaxHeat(stack, reactor, x, y);
				double Reactormed = reactor.getHeat() * 100.0 / reactor.getMaxHeat();
				int add = (int) Math.round(reactor.getMaxHeat() / 100.0 * (Reactormed + mymed / 2.0));
				if (add > this.switchReactor)
				{
					add = this.switchReactor;
				}

				if (Reactormed + mymed / 2.0 < 1.0)
				{
					add = this.switchSide / 2;
				}

				if (Reactormed + mymed / 2.0 < 0.75)
				{
					add = this.switchSide / 4;
				}

				if (Reactormed + mymed / 2.0 < 0.5)
				{
					add = this.switchSide / 8;
				}

				if (Reactormed + mymed / 2.0 < 0.25)
				{
					add = 1;
				}

				if (Math.round(Reactormed * 10.0) / 10.0 > Math.round(mymed * 10.0) / 10.0)
				{
					add -= 2 * add;
				} else if (Math.round(Reactormed * 10.0) / 10.0 == Math.round(mymed * 10.0) / 10.0)
				{
					add = 0;
				}

				myHeat -= add;
				reactor.setHeat(reactor.getHeat() + add);
			}

			this.alterHeat(stack, reactor, x, y, myHeat);
		}
	}

	private void checkHeatAcceptor(IReactor reactor, int x, int y, ArrayList<ItemReactorHeatSwitch.ItemStackCoord> heatAcceptors)
	{
		ItemStack stack = reactor.getItemAt(x, y);
		if (stack != null && stack.getItem() instanceof IReactorComponent)
		{
			IReactorComponent comp = (IReactorComponent) stack.getItem();
			if (comp.canStoreHeat(stack, reactor, x, y))
			{
				heatAcceptors.add(new ItemReactorHeatSwitch.ItemStackCoord(stack, x, y));
			}
		}
	}

	private class ItemStackCoord
	{
		public final ItemStack stack;
		public final int x;
		public final int y;

		public ItemStackCoord(ItemStack stack1, int x1, int y1)
		{
			this.stack = stack1;
			this.x = x1;
			this.y = y1;
		}
	}
}

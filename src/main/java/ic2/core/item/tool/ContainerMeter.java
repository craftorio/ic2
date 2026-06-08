package ic2.core.item.tool;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.network.ClientModifiable;
import ic2.core.IC2;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.ref.Ic2ScreenHandlers;
import net.minecraft.world.inventory.DataSlot;

public class ContainerMeter extends ContainerHandHeldInventory<HandHeldMeter>
{
	private IEnergyTile uut;
	private DataSlot resultAvg = DataSlot.standalone();
	private DataSlot resultMin = DataSlot.standalone();
	private DataSlot resultMax = DataSlot.standalone();
	private DataSlot resultCount = DataSlot.standalone();
	@ClientModifiable
	private ContainerMeter.Mode mode = ContainerMeter.Mode.EnergyIn;

	public ContainerMeter(int syncId, HandHeldMeter inventory)
	{
		super(Ic2ScreenHandlers.METER, syncId, inventory);
		this.addDataSlot(this.resultAvg);
		this.addDataSlot(this.resultMin);
		this.addDataSlot(this.resultMax);
		this.addDataSlot(this.resultCount);
	}

	@Override
	public void broadcastChanges()
	{
		super.broadcastChanges();
		if (this.uut != null)
		{
			NodeStats stats = EnergyNet.instance.getNodeStats(this.uut);
			if (stats == null)
			{
				this.base.closeGUI();
			} else
			{
				double result = switch (this.mode)
				{
					case EnergyIn -> stats.getEnergyIn();
					case EnergyOut -> stats.getEnergyOut();
					case EnergyGain -> stats.getEnergyIn() - stats.getEnergyOut();
					case Voltage -> stats.getVoltage();
				};
				if (this.resultCount.get() == 0)
				{
					this.resultAvg.set((int) result);
					this.resultMin.set((int) result);
					this.resultMax.set((int) result);
				} else
				{
					if (result < this.resultMin.get())
					{
						this.resultMin.set((int) result);
					}

					if (result > this.resultMax.get())
					{
						this.resultMax.set((int) result);
					}

					this.resultAvg.set((int) ((this.resultAvg.get() * this.resultCount.get() + result) / (this.resultCount.get() + 1)));
				}

				this.resultCount.set(this.resultCount.get() + 1);
			}
		}
	}

	public double getResultAvg()
	{
		return this.resultAvg.get();
	}

	public double getResultMin()
	{
		return this.resultMin.get();
	}

	public double getResultMax()
	{
		return this.resultMax.get();
	}

	public int getResultCount()
	{
		return this.resultCount.get();
	}

	public ContainerMeter.Mode getMode()
	{
		return this.mode;
	}

	public void setMode(ContainerMeter.Mode mode)
	{
		this.mode = mode;
		IC2.network.get(false).sendContainerField(this, "mode");
		this.reset();
	}

	public void reset()
	{
		if (IC2.sideProxy.isSimulating())
		{
			this.resultCount.set(0);
		} else
		{
			IC2.network.get(false).sendContainerEvent(this, "reset");
		}
	}

	public void setUut(IEnergyTile uut)
	{
		if (this.uut == null)
		{
			this.uut = uut;
		}
	}

	@Override
	public void onContainerEvent(String event)
	{
		super.onContainerEvent(event);
		if ("reset".equals(event))
		{
			this.reset();
		}
	}

	public enum Mode
	{
		EnergyIn,
		EnergyOut,
		EnergyGain,
		Voltage;
	}
}

package ic2.core.item.tool;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.network.ClientModifiable;
import ic2.core.IC2;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.ref.Ic2ScreenHandlers;

public class ContainerMeter extends ContainerHandHeldInventory<HandHeldMeter>
{
	private IEnergyTile uut;
	private double resultAvg;
	private double resultMin;
	private double resultMax;
	private int resultCount;
	@ClientModifiable
	private ContainerMeter.Mode mode = ContainerMeter.Mode.EnergyIn;

	public ContainerMeter(int syncId, HandHeldMeter inventory)
	{
		super(Ic2ScreenHandlers.METER, syncId, inventory);
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

				if (this.resultCount == 0)
				{
					this.resultAvg = this.resultMin = this.resultMax = result;
				} else
				{
					if (result < this.resultMin)
					{
						this.resultMin = result;
					}

					if (result > this.resultMax)
					{
						this.resultMax = result;
					}

					this.resultAvg = (this.resultAvg * this.resultCount + result) / (this.resultCount + 1);
				}

				this.resultCount++;
				IC2.network.get(true).sendContainerFields(this, "resultAvg", "resultMin", "resultMax", "resultCount");
			}
		}
	}

	public double getResultAvg()
	{
		return this.resultAvg;
	}

	public double getResultMin()
	{
		return this.resultMin;
	}

	public double getResultMax()
	{
		return this.resultMax;
	}

	public int getResultCount()
	{
		return this.resultCount;
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
			this.resultCount = 0;
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
		Voltage
	}
}

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
	private DataSlot resultAvg = DataSlot.m_39401_();
	private DataSlot resultMin = DataSlot.m_39401_();
	private DataSlot resultMax = DataSlot.m_39401_();
	private DataSlot resultCount = DataSlot.m_39401_();
	@ClientModifiable
	private ContainerMeter.Mode mode = ContainerMeter.Mode.EnergyIn;

	public ContainerMeter(int syncId, HandHeldMeter inventory)
	{
		super(Ic2ScreenHandlers.METER, syncId, inventory);
		this.m_38895_(this.resultAvg);
		this.m_38895_(this.resultMin);
		this.m_38895_(this.resultMax);
		this.m_38895_(this.resultCount);
	}

	@Override
	public void m_38946_()
	{
		super.m_38946_();
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
				if (this.resultCount.m_6501_() == 0)
				{
					this.resultAvg.m_6422_((int) result);
					this.resultMin.m_6422_((int) result);
					this.resultMax.m_6422_((int) result);
				} else
				{
					if (result < this.resultMin.m_6501_())
					{
						this.resultMin.m_6422_((int) result);
					}

					if (result > this.resultMax.m_6501_())
					{
						this.resultMax.m_6422_((int) result);
					}

					this.resultAvg.m_6422_((int) ((this.resultAvg.m_6501_() * this.resultCount.m_6501_() + result) / (this.resultCount.m_6501_() + 1)));
				}

				this.resultCount.m_6422_(this.resultCount.m_6501_() + 1);
			}
		}
	}

	public double getResultAvg()
	{
		return this.resultAvg.m_6501_();
	}

	public double getResultMin()
	{
		return this.resultMin.m_6501_();
	}

	public double getResultMax()
	{
		return this.resultMax.m_6501_();
	}

	public int getResultCount()
	{
		return this.resultCount.m_6501_();
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
			this.resultCount.m_6422_(0);
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

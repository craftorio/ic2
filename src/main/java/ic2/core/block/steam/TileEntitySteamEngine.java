package ic2.core.block.steam;

import ic2.core.IC2;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.ref.FluidName;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileEntitySteamEngine extends TileEntityInventory implements IKineticProvider
{
	protected int power = 0;
	protected int delta = 0;
	protected int activityMeter = 0;
	protected int ticksSinceLastActiveUpdate = IC2.random.nextInt(128);
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	protected final Fluids.InternalFluidTank fluidTank = this.fluids
		.addTankInsert("steam", 1000, InvSlot.InvSide.ANY, Fluids.fluidPredicate(FluidName.biomass.getInstance()));

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.delta = nbt.getInteger("delta");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("delta", this.delta);
		return nbt;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInventoryUpdate = false;
		boolean newActive = this.work();
		if (needsInventoryUpdate)
		{
			this.markDirty();
		}

		if (!this.delayActiveUpdate())
		{
			this.setActive(newActive);
		} else
		{
			if (this.ticksSinceLastActiveUpdate % 128 == 0)
			{
				this.setActive(this.activityMeter > 0);
				this.activityMeter = 0;
			}

			if (newActive)
			{
				this.activityMeter++;
			} else
			{
				this.activityMeter--;
			}

			this.ticksSinceLastActiveUpdate++;
		}
	}

	public boolean work()
	{
		if (this.fluidTank.getFluidAmount() > 1)
		{
			this.fluidTank.drainInternal(1, true);
			this.delta = Math.min(++this.delta, 200);
			this.power = (int) (this.getMaxPower() / 10.0 * (this.delta / 20));
			return true;
		} else
		{
			this.delta = Math.max(--this.delta, 0);
			this.power = (int) (this.getMaxPower() / 10.0 * (this.delta / 20));
			return false;
		}
	}

	public boolean delayActiveUpdate()
	{
		return false;
	}

	@Override
	public int getProvidedPower(EnumFacing side)
	{
		return side == this.getFacing() ? this.power : 0;
	}

	@Override
	public int getMaxPower()
	{
		return 4;
	}
}

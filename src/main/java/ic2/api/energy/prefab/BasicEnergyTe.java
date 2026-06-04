package ic2.api.energy.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class BasicEnergyTe<T extends BasicEnergyTile> extends TileEntity
{
	protected T energyBuffer;

	public static class Sink extends BasicEnergyTe<BasicSink>
	{
		public Sink(int capacity, int tier)
		{
			this.energyBuffer = new BasicSink(this, capacity, tier);
		}
	}

	public static class Source extends BasicEnergyTe<BasicSource>
	{
		public Source(int capacity, int tier)
		{
			this.energyBuffer = new BasicSource(this, capacity, tier);
		}
	}

	public T getEnergyBuffer()
	{
		return this.energyBuffer;
	}

	public void onLoad()
	{
		this.energyBuffer.onLoad();
	}

	public void invalidate()
	{
		super.invalidate();
		this.energyBuffer.invalidate();
	}

	public void onChunkUnload()
	{
		this.energyBuffer.onChunkUnload();
	}

	public void readFromNBT(@Nonnull NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.energyBuffer.readFromNBT(nbt);
	}

	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt)
	{
		return this.energyBuffer.writeToNBT(super.writeToNBT(nbt));
	}
}

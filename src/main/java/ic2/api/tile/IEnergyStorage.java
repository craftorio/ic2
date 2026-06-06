package ic2.api.tile;

import net.minecraft.util.EnumFacing;

public interface IEnergyStorage
{
	int getStored();

	void setStored(int var1);

	int addEnergy(int var1);

	int getCapacity();

	int getOutput();

	double getOutputEnergyUnitsPerTick();

	boolean isTeleporterCompatible(EnumFacing var1);
}

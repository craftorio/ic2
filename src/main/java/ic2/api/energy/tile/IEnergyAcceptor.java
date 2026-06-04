package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IEnergyAcceptor extends IEnergyTile
{
	boolean acceptsEnergyFrom(IEnergyEmitter paramIEnergyEmitter, EnumFacing paramEnumFacing);
}

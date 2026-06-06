package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IEnergyEmitter extends IEnergyTile
{
	boolean emitsEnergyTo(IEnergyAcceptor var1, EnumFacing var2);
}

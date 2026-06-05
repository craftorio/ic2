package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IEnergyAcceptor extends IEnergyTile {
   boolean acceptsEnergyFrom(IEnergyEmitter var1, EnumFacing var2);
}

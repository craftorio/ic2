package ic2.api.transport;

import net.minecraftforge.fluids.FluidTank;

public interface IFluidPipe extends IPipe {
   int getTransferRate();

   FluidTank getTank();

   int getCurrentInnerCapacity();

   int getMaxInnerCapacity();
}

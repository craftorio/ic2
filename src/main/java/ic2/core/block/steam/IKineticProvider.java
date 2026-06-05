package ic2.core.block.steam;

import net.minecraft.util.EnumFacing;

public interface IKineticProvider {
   int getProvidedPower(EnumFacing var1);

   int getMaxPower();
}

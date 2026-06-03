package ic2.core.ref;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IFluidModelProvider {
  @SideOnly(Side.CLIENT)
  void registerModels(FluidName paramFluidName);
}

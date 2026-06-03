package ic2.core.ref;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemModelProvider {
  @SideOnly(Side.CLIENT)
  void registerModels(ItemName paramItemName);
}

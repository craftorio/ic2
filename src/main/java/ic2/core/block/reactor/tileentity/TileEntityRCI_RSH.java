package ic2.core.block.reactor.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

@NotClassic
public class TileEntityRCI_RSH extends TileEntityAbstractRCI {
  public TileEntityRCI_RSH() {
    super(ItemName.rsh_condensator.getItemStack(), new ItemStack(Blocks.REDSTONE_BLOCK));
  }
}

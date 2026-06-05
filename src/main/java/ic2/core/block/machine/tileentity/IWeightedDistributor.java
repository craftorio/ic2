package ic2.core.block.machine.tileentity;

import java.util.List;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IWeightedDistributor extends IInventory {
   EnumFacing getFacing();

   @SideOnly(Side.CLIENT)
   List<EnumFacing> getPriority();

   void updatePriority(boolean var1);
}

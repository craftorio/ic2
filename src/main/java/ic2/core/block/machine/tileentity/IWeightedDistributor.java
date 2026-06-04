package ic2.core.block.machine.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public interface IWeightedDistributor extends IInventory
{
	EnumFacing getFacing();

	@SideOnly(Side.CLIENT)
	List<EnumFacing> getPriority();

	void updatePriority(boolean paramBoolean);
}

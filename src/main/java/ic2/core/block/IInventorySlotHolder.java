package ic2.core.block;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.Ic2TileEntity;
import net.minecraft.world.Container;

public interface IInventorySlotHolder<P extends Ic2TileEntity & Container>
{
	P getParent();

	InvSlot getInventorySlot(String var1);

	void addInventorySlot(InvSlot var1);

	int getBaseIndex(InvSlot var1);
}

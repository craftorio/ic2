package ic2.core.item.tool;

import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotBoxable;
import net.minecraft.world.inventory.Slot;

public class ContainerToolbox extends ContainerHandHeldInventory<HandHeldToolbox>
{
	protected static final int height = 166;
	protected static final int windowBorder = 8;
	protected static final int slotSize = 16;
	protected static final int slotDistance = 2;
	protected static final int slotSeparator = 4;
	protected static final int hotbarYOffset = -24;
	protected static final int inventoryYOffset = -82;

	public ContainerToolbox(int syncId, HandHeldToolbox inventory)
	{
		super(Ic2ScreenHandlers.TOOL_BOX, syncId, inventory);

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new SlotBoxable(inventory, col, 8 + col * 18, 41));
		}

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(this.player.getInventory(), col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(this.player.getInventory(), col, 8 + col * 18, 142));
		}
	}
}

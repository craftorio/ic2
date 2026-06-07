package ic2.core;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public abstract class ContainerFullInv<T extends Container> extends ContainerBase<T>
{
	public ContainerFullInv(MenuType<?> type, int syncId, Inventory playerInventory, T base, int height)
	{
		super(type, syncId, playerInventory, base);
		this.addPlayerInventorySlots(playerInventory, height);
	}

	public ContainerFullInv(MenuType<?> type, int syncId, Inventory playerInventory, T base, int width, int height)
	{
		super(type, syncId, playerInventory, base);
		this.addPlayerInventorySlots(playerInventory, width, height);
	}
}

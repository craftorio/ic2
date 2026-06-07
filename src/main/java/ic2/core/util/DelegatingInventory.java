package ic2.core.util;

import java.util.Set;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DelegatingInventory implements Container
{
	private final Container parent;

	public DelegatingInventory(Container parent)
	{
		this.parent = parent;
	}

	public void clearContent()
	{
		this.parent.clearContent();
	}

	public int getContainerSize()
	{
		return this.parent.getContainerSize();
	}

	public boolean isEmpty()
	{
		return this.parent.isEmpty();
	}

	public ItemStack getItem(int slot)
	{
		return this.parent.getItem(slot);
	}

	public ItemStack removeItem(int slot, int amount)
	{
		return this.parent.removeItem(slot, amount);
	}

	public ItemStack removeItemNoUpdate(int slot)
	{
		return this.parent.removeItemNoUpdate(slot);
	}

	public void setItem(int slot, ItemStack stack)
	{
		this.parent.setItem(slot, stack);
	}

	public int getMaxStackSize()
	{
		return this.parent.getMaxStackSize();
	}

	public void setChanged()
	{
		this.parent.setChanged();
	}

	public boolean stillValid(Player player)
	{
		return this.parent.stillValid(player);
	}

	public void startOpen(Player player)
	{
		this.parent.startOpen(player);
	}

	public void stopOpen(Player player)
	{
		this.parent.stopOpen(player);
	}

	public boolean canPlaceItem(int slot, ItemStack stack)
	{
		return this.parent.canPlaceItem(slot, stack);
	}

	public int m_18947_(Item item)
	{
		return this.parent.m_18947_(item);
	}

	public boolean m_18949_(Set<Item> items)
	{
		return this.parent.m_18949_(items);
	}
}

package ic2.core.slot;

import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotHologramSlot extends Slot
{
	protected final ItemStack[] stacks;
	protected final int index;
	protected final int stackSizeLimit;
	protected final SlotHologramSlot.ChangeCallback changeCallback;

	public SlotHologramSlot(ItemStack[] stacks, int index, int x, int y, int stackSizeLimit, SlotHologramSlot.ChangeCallback changeCallback)
	{
		super(new SlotHologramSlot.DummyInventory(), 0, x, y);
		if (index >= stacks.length)
		{
			throw new ArrayIndexOutOfBoundsException(index);
		}

		this.stacks = stacks;
		this.index = index;
		this.stackSizeLimit = stackSizeLimit;
		this.changeCallback = changeCallback;
	}

	public boolean mayPickup(Player player)
	{
		return false;
	}

	public int getMaxStackSize()
	{
		return this.stackSizeLimit;
	}

	public boolean mayPlace(ItemStack stack)
	{
		return false;
	}

	public ItemStack getItem()
	{
		return StackUtil.wrapEmpty(this.stacks[this.index]);
	}

	public void set(ItemStack stack)
	{
		this.stacks[this.index] = stack;
	}

	public void setChanged()
	{
		if (Util.inDev())
		{
			System.out.println(StackUtil.toStringSafe(this.stacks));
		}

		if (this.changeCallback != null)
		{
			this.changeCallback.onChanged(this.index);
		}
	}

	public ItemStack remove(int amount)
	{
		return StackUtil.emptyStack;
	}

	public ItemStack slotClick(int button, ClickType clickType, Player player, AbstractContainerMenu screenHandler)
	{
		if (Util.inDev() && player.getCommandSenderWorld().isClientSide)
		{
			System.out.printf("button=%d clickType=%s stack=%s%n", button, clickType, screenHandler.getCarried());
		}

		if (clickType == ClickType.PICKUP && (button == 0 || button == 1))
		{
			ItemStack playerStack = screenHandler.getCarried();
			ItemStack slotStack = this.stacks[this.index];
			if (!StackUtil.isEmpty(playerStack))
			{
				int curSize = StackUtil.getSize(slotStack);
				int extraSize = button == 0 ? StackUtil.getSize(playerStack) : 1;
				int limit = Math.min(playerStack.getMaxStackSize(), this.stackSizeLimit);
				if (curSize + extraSize > limit)
				{
					extraSize = Math.max(0, limit - curSize);
				}

				if (curSize == 0)
				{
					this.stacks[this.index] = StackUtil.copyWithSize(playerStack, extraSize);
				} else if (StackUtil.checkItemEquality(playerStack, slotStack))
				{
					if (Util.inDev())
					{
						System.out.println("add " + extraSize + " to " + slotStack + " -> " + (curSize + extraSize));
					}

					this.stacks[this.index] = StackUtil.incSize(slotStack, extraSize);
				} else
				{
					this.stacks[this.index] = StackUtil.copyWithSize(playerStack, Math.min(StackUtil.getSize(playerStack), limit));
				}
			} else if (!StackUtil.isEmpty(slotStack))
			{
				if (button == 0)
				{
					this.stacks[this.index] = StackUtil.emptyStack;
				} else
				{
					int newSize = StackUtil.getSize(slotStack) / 2;
					if (newSize <= 0)
					{
						this.stacks[this.index] = StackUtil.emptyStack;
					} else
					{
						this.stacks[this.index] = StackUtil.setSize(slotStack, newSize);
					}
				}
			}

			this.setChanged();
		}

		return StackUtil.emptyStack;
	}

	public interface ChangeCallback
	{
		void onChanged(int var1);
	}

	private static final class DummyInventory implements Container
	{
		public int getContainerSize()
		{
			return 1;
		}

		public boolean isEmpty()
		{
			return false;
		}

		public ItemStack getItem(int slot)
		{
			return StackUtil.emptyStack;
		}

		public ItemStack removeItem(int slot, int amount)
		{
			return StackUtil.emptyStack;
		}

		public ItemStack removeItemNoUpdate(int slot)
		{
			return StackUtil.emptyStack;
		}

		public void setItem(int slot, ItemStack stack)
		{
		}

		public void setChanged()
		{
		}

		public boolean stillValid(Player player)
		{
			return true;
		}

		public void clearContent()
		{
		}
	}
}

package ic2.core;

import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class ContainerBase<T extends Container> extends AbstractContainerMenu
{
	protected static final int windowBorder = 8;
	protected static final int slotSize = 16;
	protected static final int slotDistance = 2;
	protected static final int slotSeparator = 4;
	protected static final int hotbarYOffset = -24;
	protected static final int inventoryYOffset = -82;
	private static final Field field_Container_listeners = ReflectionUtil.getField(AbstractContainerMenu.class, "listeners", "field_7765", "containerListeners");
	protected final Player player;
	public final T base;

	public ContainerBase(MenuType<?> type, int syncId, Inventory playerInventory, T base)
	{
		super(type, syncId);
		this.player = playerInventory.player;
		this.base = base;
	}

	protected void addPlayerInventorySlots(Inventory playerInventory, int height)
	{
		this.addPlayerInventorySlots(playerInventory, 178, height);
	}

	protected void addPlayerInventorySlots(Inventory playerInventory, int width, int height)
	{
		int xStart = (width - 162) / 2;

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, xStart + col * 18, height + -82 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, xStart + col * 18, height + -24));
		}
	}

	public void clicked(int slotIndex, int button, ClickType clickType, Player player)
	{
		Slot slot;
		if (slotIndex >= 0 && slotIndex < this.slots.size() && (slot = (Slot) this.slots.get(slotIndex)) instanceof SlotHologramSlot)
		{
			((SlotHologramSlot) slot).slotClick(button, clickType, player, this);
		} else
		{
			super.clicked(slotIndex, button, clickType, player);
		}
	}

	public final ItemStack quickMoveStack(Player player, int sourceSlotIndex)
	{
		Slot sourceSlot = (Slot) this.slots.get(sourceSlotIndex);
		if (sourceSlot != null && sourceSlot.hasItem())
		{
			ItemStack sourceItemStack = sourceSlot.getItem();
			int oldSourceItemStackSize = StackUtil.getSize(sourceItemStack);
			ItemStack resultStack;
			if (sourceSlot.container == player.getInventory())
			{
				resultStack = this.handlePlayerSlotShiftClick(player, sourceItemStack);
			} else
			{
				resultStack = this.handleGUISlotShiftClick(player, sourceItemStack);
			}

			if (StackUtil.isEmpty(resultStack) || StackUtil.getSize(resultStack) != oldSourceItemStackSize)
			{
				sourceSlot.set(resultStack);
				if (!(sourceSlot instanceof ResultSlot))
				{
					sourceSlot.onTake(player, sourceItemStack);
				}

				if (!player.getCommandSenderWorld().isClientSide)
				{
					this.broadcastChanges();
				}
			}
		}

		return StackUtil.emptyStack;
	}

	protected ItemStack handlePlayerSlotShiftClick(Player player, ItemStack sourceItemStack)
	{
		for (int run = 0; run < 4 && !StackUtil.isEmpty(sourceItemStack); run++)
		{
			for (Slot targetSlot : this.slots)
			{
				if (targetSlot.container != player.getInventory() && isValidTargetSlot(targetSlot, sourceItemStack, run % 2 == 1, run < 2))
				{
					sourceItemStack = this.transfer(sourceItemStack, targetSlot);
					if (!StackUtil.isEmpty(sourceItemStack))
					{
						continue;
					}
					break;
				}
			}
		}

		return sourceItemStack;
	}

	protected ItemStack handleGUISlotShiftClick(Player player, ItemStack sourceItemStack)
	{
		for (int run = 0; run < 2 && !StackUtil.isEmpty(sourceItemStack); run++)
		{
			ListIterator<Slot> it = this.slots.listIterator(this.slots.size());

			while (it.hasPrevious())
			{
				Slot targetSlot = it.previous();
				if (targetSlot.container == player.getInventory() && isValidTargetSlot(targetSlot, sourceItemStack, run == 1, false))
				{
					sourceItemStack = this.transfer(sourceItemStack, targetSlot);
					if (!StackUtil.isEmpty(sourceItemStack))
					{
						continue;
					}
					break;
				}
			}
		}

		return sourceItemStack;
	}

	protected static final boolean isValidTargetSlot(Slot slot, ItemStack stack, boolean allowEmpty, boolean requireInputOnly)
	{
		if (slot instanceof SlotInvSlotReadOnly || slot instanceof SlotHologramSlot)
		{
			return false;
		} else if (!slot.mayPlace(stack))
		{
			return false;
		} else if (!allowEmpty && !slot.hasItem())
		{
			return false;
		} else
		{
			return !requireInputOnly ? true : slot instanceof SlotInvSlot && ((SlotInvSlot) slot).invSlot.canInput();
		}
	}

	public boolean stillValid(Player entityplayer)
	{
		return this.base.stillValid(entityplayer);
	}

	public void broadcastChanges()
	{
		super.broadcastChanges();
		if (this.base instanceof BlockEntity)
		{
			for (String name : this.getNetworkedFields())
			{
				if (this.player instanceof ServerPlayer)
				{
					IC2.network.get(true).updateTileEntityFieldTo((BlockEntity) this.base, name, (ServerPlayer) this.player);
				}
			}

			if (this.base instanceof Ic2TileEntity)
			{
				for (TileEntityComponent component : ((Ic2TileEntity) this.base).getComponents())
				{
					if (this.player instanceof ServerPlayer)
					{
						component.onContainerUpdate((ServerPlayer) this.player);
					}
				}
			}
		}
	}

	public List<String> getNetworkedFields()
	{
		return new ArrayList<>();
	}

	public final List<ContainerListener> getListeners()
	{
		try
		{
			return (List<ContainerListener>) field_Container_listeners.get(this);
		} catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}

	public Player getPlayer()
	{
		return this.player;
	}

	public void onContainerEvent(String event)
	{
	}

	protected final ItemStack transfer(ItemStack stack, Slot dst)
	{
		int amount = this.getTransferAmount(stack, dst);
		if (amount <= 0)
		{
			return stack;
		}

		ItemStack dstStack = dst.getItem();
		if (StackUtil.isEmpty(dstStack))
		{
			dst.set(StackUtil.copyWithSize(stack, amount));
		} else
		{
			dst.set(StackUtil.incSize(dstStack, amount));
		}

		return StackUtil.decSize(stack, amount);
	}

	private int getTransferAmount(ItemStack stack, Slot dst)
	{
		int amount = Math.min(dst.container.getMaxStackSize(), dst.getMaxStackSize());
		amount = Math.min(amount, stack.isStackable() ? stack.getMaxStackSize() : 1);
		ItemStack dstStack = dst.getItem();
		if (!StackUtil.isEmpty(dstStack))
		{
			if (!StackUtil.checkItemEqualityStrict(stack, dstStack))
			{
				return 0;
			}

			amount -= StackUtil.getSize(dstStack);
		}

		return Math.min(amount, StackUtil.getSize(stack));
	}
}

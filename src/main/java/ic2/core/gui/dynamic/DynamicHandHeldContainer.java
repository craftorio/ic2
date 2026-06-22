package ic2.core.gui.dynamic;

import ic2.core.item.tool.HandHeldInventory;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DynamicHandHeldContainer<T extends HandHeldInventory> extends DynamicContainer<T>
{
	protected DynamicHandHeldContainer(MenuType<DynamicContainer<T>> type, int syncId, Inventory playerInventory, T base, GuiParser.GuiNode guiNode)
	{
		super(type, syncId, playerInventory, base, guiNode);
	}

	public static DynamicHandHeldContainer<HandHeldInventory> create(int syncId, Inventory playerInventory, HandHeldInventory base, GuiParser.GuiNode guiNode)
	{
		return new DynamicHandHeldContainer<>(Ic2ScreenHandlers.DYNAMIC_ITEM, syncId, playerInventory, base, guiNode);
	}

	public static <T extends HandHeldInventory> DynamicHandHeldContainer<T> create(
		MenuType<DynamicContainer<T>> type, int syncId, Inventory playerInventory, T base, GuiParser.GuiNode guiNode
	)
	{
		return new DynamicHandHeldContainer<>(type, syncId, playerInventory, base, guiNode);
	}

	@Override
	protected SlotHologramSlot.ChangeCallback getCallback()
	{
		return this.base.makeSaveCallback();
	}

	@Override
	public void onContainerEvent(String event)
	{
		this.base.onEvent(event);
		super.onContainerEvent(event);
	}

	@Override
	public void clicked(int slot, int button, @NotNull ClickType type, @NotNull Player player)
	{
		ItemStack stack = null;
		boolean thrown = false;
		Slot realSlot = null;
		if (!player.getCommandSenderWorld().isClientSide && slot >= 0 && slot < this.slots.size())
		{
			realSlot = (Slot) this.slots.get(slot);
			stack = realSlot.getItem();
			thrown = this.base.isThisContainer(stack);
		}

		super.clicked(slot, button, type, player);
		if (thrown && !realSlot.hasItem())
		{
			this.base.saveAsThrown(stack);
			((ServerPlayer) player).closeContainer();
		}
	}

	public void removed(Player player)
	{
		this.base.onScreenClosed(player);
		super.removed(player);
	}
}

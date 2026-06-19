package ic2.core.item;

import ic2.core.ContainerBase;
import ic2.core.item.tool.HandHeldInventory;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerHandHeldInventory<T extends HandHeldInventory> extends ContainerBase<T>
{
	public ContainerHandHeldInventory(MenuType<?> type, int syncId, T inventory)
	{
		super(type, syncId, inventory.player.getInventory(), inventory);
	}

	@Override
	public void clicked(int slot, int button, ClickType type, Player player)
	{
		ItemStack stack = null;
		boolean closeGUI = false;
		switch (type)
		{
			case CLONE:
			case PICKUP_ALL:
			case QUICK_CRAFT:
				break;
			case PICKUP:
			case THROW:
				if (slot >= 0 && slot < this.slots.size())
				{
					stack = this.slots.get(slot).getItem();
					closeGUI = this.base.isThisContainer(stack);
				}
				break;
			case QUICK_MOVE:
				if (slot >= 0 && slot < this.slots.size() && this.base.isThisContainer(this.slots.get(slot).getItem()))
				{
					return;
				}
				break;
			case SWAP:
				assert slot >= 0 && slot < this.slots.size();
				int playerInxSlotIdx = this.findSlot(player.getInventory(), button).orElse(-1);
				assert playerInxSlotIdx >= 0;
				int newSlot = -1;
				if (this.base.isThisContainer(player.getInventory().getItem(button)))
				{
					Slot targetSlot = this.slots.get(slot);
					int targetIdx = targetSlot.getContainerSlot();
					if (targetSlot.container == player.getInventory() && targetIdx >= 0 && targetIdx < 9)
					{
						newSlot = targetIdx;
					}
				} else if (this.base.isThisContainer(this.slots.get(slot).getItem()))
				{
					newSlot = button;
				}

				if (newSlot >= 0 && player instanceof ServerPlayer)
				{
					((ServerPlayer) player).connection.send(new ClientboundSetCarriedItemPacket(newSlot));
				}
				break;
			default:
				throw new RuntimeException("Unexpected ClickType: " + type);
		}

		super.clicked(slot, button, type, player);
		if (closeGUI && !player.getCommandSenderWorld().isClientSide)
		{
			this.base.saveAsThrown(stack);
			player.closeContainer();
		} else if (type == ClickType.CLONE)
		{
			ItemStack held = this.getCarried();
			if (this.base.isThisContainer(held))
			{
				held.getTag().remove("uid");
			}
		}
	}

	public void removed(Player player)
	{
		this.base.onScreenClosed(player);
		super.removed(player);
	}
}

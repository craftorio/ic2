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
	public void m_150399_(int slot, int button, ClickType type, Player player)
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
				if (slot >= 0 && slot < this.f_38839_.size())
				{
					stack = ((Slot) this.f_38839_.get(slot)).m_7993_();
					closeGUI = this.base.isThisContainer(stack);
				}
				break;
			case QUICK_MOVE:
				if (slot >= 0 && slot < this.f_38839_.size() && this.base.isThisContainer(((Slot) this.f_38839_.get(slot)).m_7993_()))
				{
					return;
				}
				break;
			case SWAP:
				assert slot >= 0 && slot < this.f_38839_.size();
				int playerInxSlotIdx = this.m_182417_(player.getInventory(), button).orElse(-1);
				assert playerInxSlotIdx >= 0;
				int newSlot = -1;
				if (this.base.isThisContainer(player.getInventory().getItem(button)))
				{
					Slot targetSlot = (Slot) this.f_38839_.get(slot);
					int targetIdx = targetSlot.m_150661_();
					if (targetSlot.f_40218_ == player.getInventory() && targetIdx >= 0 && targetIdx < 9)
					{
						newSlot = targetIdx;
					}
				} else if (this.base.isThisContainer(((Slot) this.f_38839_.get(slot)).m_7993_()))
				{
					newSlot = button;
				}

				if (newSlot >= 0 && player instanceof ServerPlayer)
				{
					((ServerPlayer) player).f_8906_.m_9829_(new ClientboundSetCarriedItemPacket(newSlot));
				}
				break;
			default:
				throw new RuntimeException("Unexpected ClickType: " + type);
		}

		super.m_150399_(slot, button, type, player);
		if (closeGUI && !player.getCommandSenderWorld().isClientSide)
		{
			assert stack != null;
			this.base.saveAsThrown(stack);
			((ServerPlayer) player).m_6915_();
		} else if (type == ClickType.CLONE)
		{
			ItemStack held = this.m_142621_();
			if (this.base.isThisContainer(held))
			{
				held.getTag().m_128473_("uid");
			}
		}
	}

	public void removed(Player player)
	{
		this.base.onScreenClosed(player);
		super.removed(player);
	}
}

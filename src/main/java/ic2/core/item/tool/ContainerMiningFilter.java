package ic2.core.item.tool;

import ic2.api.network.ClientModifiable;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerMiningFilter extends ContainerHandHeldInventory<HandHeldMiningFilter>
{
	@ClientModifiable
	protected boolean blacklist;

	public ContainerMiningFilter(int syncId, Inventory playerInventory, HandHeldMiningFilter base)
	{
		super(Ic2ScreenHandlers.MINING_FILTER, syncId, base);
		this.blacklist = base.blacklist;

		for (int row = 0; row < 5; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				int idx = row * 9 + col;
				this.addSlot(new SlotHologramSlot(
					base.inventory, idx,
					8 + col * 18, 32 + row * 18,
					1, base.makeSaveCallback()
				));
			}
		}

		this.addPlayerInventorySlots(playerInventory, 215);
	}

	@Override
	public void removed(Player player)
	{
		this.base.blacklist = this.blacklist;
		super.removed(player);
	}
}

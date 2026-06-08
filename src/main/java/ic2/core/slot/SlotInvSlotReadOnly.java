package ic2.core.slot;

import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SlotInvSlotReadOnly extends SlotInvSlot
{
	public SlotInvSlotReadOnly(InvSlot invSlot, int index, int xDisplayPosition, int yDisplayPosition)
	{
		super(invSlot, index, xDisplayPosition, yDisplayPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		return false;
	}

	@Override
	public void onTake(Player player, ItemStack stack)
	{
	}

	public boolean mayPickup(Player player)
	{
		return false;
	}

	@Override
	public ItemStack remove(int par1)
	{
		return StackUtil.emptyStack;
	}
}

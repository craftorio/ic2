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
	public boolean m_5857_(ItemStack stack)
	{
		return false;
	}

	@Override
	public void m_142406_(Player player, ItemStack stack)
	{
	}

	public boolean m_8010_(Player player)
	{
		return false;
	}

	@Override
	public ItemStack m_6201_(int par1)
	{
		return StackUtil.emptyStack;
	}
}

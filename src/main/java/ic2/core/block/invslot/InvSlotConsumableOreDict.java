package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class InvSlotConsumableOreDict extends InvSlotConsumable
{
	protected final String oreDict;

	public InvSlotConsumableOreDict(IInventorySlotHolder<?> base, String name, int count, String oreDict)
	{
		super(base, name, count);
		this.oreDict = oreDict;
	}

	public InvSlotConsumableOreDict(IInventorySlotHolder<?> base, String name, InvSlot.Access access, int count, InvSlot.InvSide side, String oreDict)
	{
		super(base, name, access, count, side);
		this.oreDict = oreDict;
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return false;
		}

		for (int ID : OreDictionary.getOreIDs(stack))
		{
			if (this.oreDict.equals(OreDictionary.getOreName(ID)))
			{
				return true;
			}
		}

		return false;
	}
}

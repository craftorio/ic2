package ic2.core.block.invslot;

import ic2.api.item.IKineticRotor;
import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.network.NetworkManager;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class InvSlotConsumableKineticRotor extends InvSlotConsumableClass
{
	private final String updateName;

	private final IKineticRotor.GearboxType type;

	public InvSlotConsumableKineticRotor(IInventorySlotHolder<?> base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1, IKineticRotor.GearboxType type)
	{
		this(base1, name1, access1, count, preferredSide1, type, null);
	}

	public InvSlotConsumableKineticRotor(IInventorySlotHolder<?> base, String name, InvSlot.Access access, int count, InvSlot.InvSide preferredSide, IKineticRotor.GearboxType type, String field)
	{
		super(base, name, access, count, preferredSide, IKineticRotor.class);
		this.type = type;
		this.updateName = field;
	}

	public boolean accepts(ItemStack stack)
	{
		if (super.accepts(stack))
			return ((IKineticRotor) stack.getItem()).isAcceptedType(stack, this.type);
		return false;
	}

	public void onChanged()
	{
		if (this.updateName != null && this.base.getParent().hasWorld() && !(this.base.getParent().getWorld()).isRemote)
			IC2.network.get(true).updateTileEntityField(this.base.getParent(), this.updateName);
	}
}

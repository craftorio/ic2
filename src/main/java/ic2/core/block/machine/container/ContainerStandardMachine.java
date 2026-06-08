package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class ContainerStandardMachine<T extends TileEntityStandardMachine<?, ?, ?>> extends ContainerElectricMachine<T>
{
	public ContainerStandardMachine(MenuType<? extends ContainerStandardMachine<T>> type, int syncId, Inventory playerInventory, T base)
	{
		this(type, syncId, playerInventory, base, 166, 56, 53, 56, 17, 116, 35, 152, 8);
	}

	public ContainerStandardMachine(
		MenuType<? extends ContainerStandardMachine<T>> type,
		int syncId,
		Inventory playerInventory,
		T base,
		int height,
		int dischargeX,
		int dischargeY,
		int inputX,
		int inputY,
		int outputX,
		int outputY,
		int upgradeX,
		int upgradeY
	)
	{
		super(type, syncId, playerInventory, base, height, dischargeX, dischargeY);
		if (base.inputSlot != null)
		{
			this.addSlot(new SlotInvSlot(base.inputSlot, 0, inputX, inputY));
		}

		if (base.outputSlot != null)
		{
			this.addSlot(new SlotInvSlot(base.outputSlot, 0, outputX, outputY));
		}

		for (int i = 0; i < 4; i++)
		{
			this.addSlot(new SlotInvSlot(base.upgradeSlot, i, upgradeX, upgradeY + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("guiProgress");
		return ret;
	}
}

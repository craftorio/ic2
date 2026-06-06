package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerStandardMachine<T extends TileEntityStandardMachine<?, ?, ?>> extends ContainerElectricMachine<T>
{
	public ContainerStandardMachine(EntityPlayer player, T tileEntity1)
	{
		this(player, tileEntity1, 166, 56, 53, 56, 17, 116, 35, 152, 8);
	}

	public ContainerStandardMachine(
		EntityPlayer player,
		T tileEntity1,
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
		super(player, tileEntity1, height, dischargeX, dischargeY);
		if (tileEntity1.inputSlot != null)
		{
			this.addSlotToContainer(new SlotInvSlot(tileEntity1.inputSlot, 0, inputX, inputY));
		}

		if (tileEntity1.outputSlot != null)
		{
			this.addSlotToContainer(new SlotInvSlot(tileEntity1.outputSlot, 0, outputX, outputY));
		}

		for (int i = 0; i < 4; i++)
		{
			this.addSlotToContainer(new SlotInvSlot(tileEntity1.upgradeSlot, i, upgradeX, upgradeY + i * 18));
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

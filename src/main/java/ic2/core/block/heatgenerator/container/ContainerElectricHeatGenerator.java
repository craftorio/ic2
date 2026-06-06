package ic2.core.block.heatgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerElectricHeatGenerator extends ContainerFullInv<TileEntityElectricHeatGenerator>
{
	public ContainerElectricHeatGenerator(EntityPlayer player, TileEntityElectricHeatGenerator tileEntity1)
	{
		super(player, tileEntity1, 166);

		for (int i = 0; i < 5; i++)
		{
			this.addSlotToContainer(new SlotInvSlot(tileEntity1.coilSlot, i, 44 + i * 18, 27));
		}

		for (int i = 5; i < 10; i++)
		{
			this.addSlotToContainer(new SlotInvSlot(tileEntity1.coilSlot, i, 44 + (i - 5) * 18, 45));
		}

		this.addSlotToContainer(new SlotInvSlot(tileEntity1.dischargeSlot, 0, 8, 62));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("transmitHeat");
		ret.add("maxHeatEmitpeerTick");
		return ret;
	}
}

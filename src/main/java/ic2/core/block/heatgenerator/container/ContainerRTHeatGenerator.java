package ic2.core.block.heatgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerRTHeatGenerator extends ContainerFullInv<TileEntityRTHeatGenerator>
{
	public ContainerRTHeatGenerator(EntityPlayer player, TileEntityRTHeatGenerator tileEntity1)
	{
		super(player, tileEntity1, 166);
		int i;
		for (i = 0; i < 3; i++)
			addSlotToContainer(new SlotInvSlot(tileEntity1.fuelSlot, i, 62 + i * 18, 27));
		for (i = 3; i < 6; i++)
			addSlotToContainer(new SlotInvSlot(tileEntity1.fuelSlot, i, 62 + (i - 3) * 18, 45));
	}
  
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("transmitHeat");
		ret.add("maxHeatEmitPerTick");
		return ret;
	}
}

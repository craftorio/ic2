package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerLiquidHeatExchanger extends ContainerFullInv<TileEntityLiquidHeatExchanger>
{
	public ContainerLiquidHeatExchanger(EntityPlayer player, TileEntityLiquidHeatExchanger tileEntity)
	{
		super(player, tileEntity, 204);
		addSlotToContainer(new SlotInvSlot(tileEntity.hotfluidinputSlot, 0, 8, 103));
		addSlotToContainer(new SlotInvSlot(tileEntity.cooloutputSlot, 0, 152, 103));
		addSlotToContainer(new SlotInvSlot(tileEntity.coolfluidinputSlot, 0, 134, 103));
		addSlotToContainer(new SlotInvSlot(tileEntity.hotoutputSlot, 0, 26, 103));
		int i;
		for (i = 0; i < 3; i++)
			addSlotToContainer(new SlotInvSlot(tileEntity.upgradeSlot, i, 62 + i * 18, 103));
		for (i = 0; i < 5; i++)
			addSlotToContainer(new SlotInvSlot(tileEntity.heatexchangerslots, i, 46 + i * 17, 50));
		for (i = 5; i < 10; i++)
			addSlotToContainer(new SlotInvSlot(tileEntity.heatexchangerslots, i, 46 + (i - 5) * 17, 72));
	}

	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("inputTank");
		ret.add("outputTank");
		ret.add("transmitHeat");
		ret.add("maxHeatEmitPerTick");
		return ret;
	}
}

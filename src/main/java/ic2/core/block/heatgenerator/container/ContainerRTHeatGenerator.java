package ic2.core.block.heatgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerRTHeatGenerator extends ContainerFullInv<TileEntityRTHeatGenerator>
{
	public ContainerRTHeatGenerator(int syncId, Inventory playerInventory, TileEntityRTHeatGenerator be)
	{
		super(Ic2ScreenHandlers.RT_HEAT_GENERATOR, syncId, playerInventory, be, 166);

		for (int i = 0; i < 3; i++)
		{
			this.m_38897_(new SlotInvSlot(be.fuelSlot, i, 62 + i * 18, 27));
		}

		for (int i = 3; i < 6; i++)
		{
			this.m_38897_(new SlotInvSlot(be.fuelSlot, i, 62 + (i - 3) * 18, 45));
		}
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

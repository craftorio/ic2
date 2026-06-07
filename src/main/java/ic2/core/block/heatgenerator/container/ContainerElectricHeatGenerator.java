package ic2.core.block.heatgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerElectricHeatGenerator extends ContainerFullInv<TileEntityElectricHeatGenerator>
{
	public ContainerElectricHeatGenerator(int syncId, Inventory playerInventory, TileEntityElectricHeatGenerator be)
	{
		super(Ic2ScreenHandlers.ELECTRIC_HEAT_GENERATOR, syncId, playerInventory, be, 166);

		for (int i = 0; i < 5; i++)
		{
			this.m_38897_(new SlotInvSlot(be.coilSlot, i, 44 + i * 18, 27));
		}

		for (int i = 5; i < 10; i++)
		{
			this.m_38897_(new SlotInvSlot(be.coilSlot, i, 44 + (i - 5) * 18, 45));
		}

		this.m_38897_(new SlotInvSlot(be.dischargeSlot, 0, 8, 62));
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

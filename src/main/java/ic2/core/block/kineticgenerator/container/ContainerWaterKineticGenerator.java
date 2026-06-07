package ic2.core.block.kineticgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerWaterKineticGenerator extends ContainerFullInv<TileEntityWaterKineticGenerator>
{
	public ContainerWaterKineticGenerator(int syncId, Inventory playerInventory, TileEntityWaterKineticGenerator be)
	{
		super(Ic2ScreenHandlers.WATER_KINETIC_GENERATOR, syncId, playerInventory, be, 166);
		this.m_38897_(new SlotInvSlot(be.rotorSlot, 0, 80, 26));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("waterFlow");
		ret.add("type");
		return ret;
	}
}

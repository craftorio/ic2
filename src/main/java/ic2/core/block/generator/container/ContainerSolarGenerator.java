package ic2.core.block.generator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerSolarGenerator extends ContainerFullInv<TileEntitySolarGenerator>
{
	public ContainerSolarGenerator(int syncId, Inventory playerInventory, TileEntitySolarGenerator base)
	{
		super(Ic2ScreenHandlers.SOLAR_GENERATOR, syncId, playerInventory, base, 166);
		this.m_38897_(new SlotInvSlot(base.chargeSlot, 0, 80, 26));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("sunlight");
		return ret;
	}
}

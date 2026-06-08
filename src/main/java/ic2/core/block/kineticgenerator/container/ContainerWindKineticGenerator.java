package ic2.core.block.kineticgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerWindKineticGenerator extends ContainerFullInv<TileEntityWindKineticGenerator>
{
	public ContainerWindKineticGenerator(int syncId, Inventory playerInventory, TileEntityWindKineticGenerator be)
	{
		super(Ic2ScreenHandlers.WIND_KINETIC_GENERATOR, syncId, playerInventory, be, 166);
		this.addSlot(new SlotInvSlot(be.rotorSlot, 0, 80, 26));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("windStrength");
		return ret;
	}
}

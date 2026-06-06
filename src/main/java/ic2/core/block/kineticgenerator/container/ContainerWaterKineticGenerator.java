package ic2.core.block.kineticgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerWaterKineticGenerator extends ContainerFullInv<TileEntityWaterKineticGenerator>
{
	public ContainerWaterKineticGenerator(EntityPlayer player, TileEntityWaterKineticGenerator tileEntity1)
	{
		super(player, tileEntity1, 166);
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.rotorSlot, 0, 80, 26));
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

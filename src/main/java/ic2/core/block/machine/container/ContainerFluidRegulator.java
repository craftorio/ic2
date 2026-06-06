package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityFluidRegulator;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerFluidRegulator extends ContainerElectricMachine<TileEntityFluidRegulator>
{
	public ContainerFluidRegulator(EntityPlayer player, TileEntityFluidRegulator tileEntite)
	{
		super(player, tileEntite, 184, 8, 57);
		this.addSlotToContainer(new SlotInvSlot(tileEntite.wasserinputSlot, 0, 58, 53));
		this.addSlotToContainer(new SlotInvSlot(tileEntite.wasseroutputSlot, 0, 58, 71));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		ret.add("outputmb");
		ret.add("mode");
		return ret;
	}
}

package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.ref.Ic2ScreenHandlers;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerMetalFormer extends ContainerStandardMachine<TileEntityMetalFormer>
{
	public ContainerMetalFormer(int syncId, Inventory playerInventory, TileEntityMetalFormer be)
	{
		super(Ic2ScreenHandlers.METAL_FORMER, syncId, playerInventory, be, 166, 17, 53, 17, 17, 116, 35, 152, 8);
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("mode");
		return ret;
	}
}

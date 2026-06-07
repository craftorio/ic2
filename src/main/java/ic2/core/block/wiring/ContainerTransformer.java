package ic2.core.block.wiring;

import ic2.core.ContainerFullInv;
import ic2.core.block.wiring.tileentity.TileEntityTransformer;
import ic2.core.ref.Ic2ScreenHandlers;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerTransformer extends ContainerFullInv<TileEntityTransformer>
{
	public ContainerTransformer(int syncId, Inventory playerInventory, TileEntityTransformer tileEntity1)
	{
		super(Ic2ScreenHandlers.TRANSFORMER, syncId, playerInventory, tileEntity1, 219);
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("configuredMode");
		ret.add("inputFlow");
		ret.add("outputFlow");
		return ret;
	}
}

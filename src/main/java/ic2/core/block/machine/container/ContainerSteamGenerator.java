package ic2.core.block.machine.container;

import ic2.core.ContainerBase;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerSteamGenerator extends ContainerBase<TileEntitySteamGenerator>
{
	public ContainerSteamGenerator(EntityPlayer player, TileEntitySteamGenerator te)
	{
		super(te);
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("waterTank");
		ret.add("heatInput");
		ret.add("inputMB");
		ret.add("outputMB");
		ret.add("pressure");
		ret.add("systemHeat");
		ret.add("outputFluid");
		ret.add("calcification");
		return ret;
	}
}

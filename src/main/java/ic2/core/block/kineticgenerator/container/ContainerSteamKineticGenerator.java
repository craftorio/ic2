package ic2.core.block.kineticgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerSteamKineticGenerator extends ContainerFullInv<TileEntitySteamKineticGenerator>
{
	public ContainerSteamKineticGenerator(EntityPlayer player, TileEntitySteamKineticGenerator te)
	{
		super(player, te, 166);
		addSlotToContainer(new SlotInvSlot(te.upgradeSlot, 0, 152, 26));
		addSlotToContainer(new SlotInvSlot(te.turbineSlot, 0, 80, 26));
	}

	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("distilledWaterTank");
		ret.add("kUoutput");
		ret.add("ventingSteam");
		ret.add("throttled");
		ret.add("isTurbineFilledWithWater");
		return ret;
	}
}

package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerMatter extends ContainerFullInv<TileEntityMatter>
{
	public ContainerMatter(int syncId, Inventory playerInventory, TileEntityMatter be)
	{
		super(Ic2ScreenHandlers.MATTER_GENERATOR, syncId, playerInventory, be, 166);
		this.addSlot(new SlotInvSlot(be.amplifierSlot, 0, 72, 40));
		this.addSlot(new SlotInvSlot(be.outputSlot, 0, 125, 59));
		this.addSlot(new SlotInvSlot(be.containerslot, 0, 125, 23));

		for (int i = 0; i < 4; i++)
		{
			this.addSlot(new SlotInvSlot(be.upgradeSlot, i, 152, 8 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("energy");
		ret.add("scrap");
		ret.add("fluidTank");
		return ret;
	}
}

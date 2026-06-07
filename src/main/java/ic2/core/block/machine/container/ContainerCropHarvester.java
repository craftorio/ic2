package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityCropHarvester;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerCropHarvester extends ContainerElectricMachine<TileEntityCropHarvester>
{
	public ContainerCropHarvester(int syncId, Inventory playerInventory, TileEntityCropHarvester base)
	{
		super(Ic2ScreenHandlers.CROP_HARVESTER, syncId, playerInventory, base, 166, 16, 53);

		for (int y = 0; y < base.contentSlot.size() / 5; y++)
		{
			for (int x = 0; x < 5; x++)
			{
				this.m_38897_(new SlotInvSlot(base.contentSlot, x + y * 5, 48 + x * 18, 17 + y * 18));
			}
		}

		for (int i = 0; i < 4; i++)
		{
			this.m_38897_(new SlotInvSlot(base.upgradeSlot, i, 152, 8 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("energy");
		return ret;
	}
}

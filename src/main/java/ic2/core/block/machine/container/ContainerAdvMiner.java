package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerAdvMiner extends ContainerElectricMachine<TileEntityAdvMiner>
{
	public ContainerAdvMiner(int syncId, Inventory playerInventory, TileEntityAdvMiner be)
	{
		super(Ic2ScreenHandlers.ADVANCED_MINER, syncId, playerInventory, be, 203, 8, 80);
		this.m_38897_(new SlotInvSlot(be.scannerSlot, 0, 8, 26));

		for (int i = 0; i < 4; i++)
		{
			this.m_38897_(new SlotInvSlot(be.upgradeSlot, i, 152, 26 + i * 18));
		}

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 5; col++)
			{
				this.m_38897_(new SlotInvSlot(be.filterSlot, col + row * 5, 36 + col * 18, 44 + row * 18));
			}
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("mineTarget");
		ret.add("blacklist");
		ret.add("silkTouch");
		return ret;
	}
}

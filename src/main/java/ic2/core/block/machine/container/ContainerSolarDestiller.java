package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerSolarDestiller extends ContainerFullInv<TileEntitySolarDestiller>
{
	public ContainerSolarDestiller(int syncId, Inventory playerInventory, TileEntitySolarDestiller be)
	{
		super(Ic2ScreenHandlers.SOLAR_DISTILLER, syncId, playerInventory, be, 184);
		this.m_38897_(new SlotInvSlot(be.waterinputSlot, 0, 17, 27));
		this.m_38897_(new SlotInvSlot(be.destiwaterinputSlot, 0, 136, 64));
		this.m_38897_(new SlotInvSlot(be.wateroutputSlot, 0, 17, 45));
		this.m_38897_(new SlotInvSlot(be.destiwateroutputSlott, 0, 136, 82));

		for (int i = 0; i < 2; i++)
		{
			this.m_38897_(new SlotInvSlot(be.upgradeSlot, i, 152, 8 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("skyLight");
		ret.add("inputTank");
		ret.add("outputTank");
		return ret;
	}
}

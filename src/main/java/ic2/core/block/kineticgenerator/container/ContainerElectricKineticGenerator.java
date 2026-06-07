package ic2.core.block.kineticgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerElectricKineticGenerator extends ContainerFullInv<TileEntityElectricKineticGenerator>
{
	public ContainerElectricKineticGenerator(int syncId, Inventory playerInventory, TileEntityElectricKineticGenerator be)
	{
		super(Ic2ScreenHandlers.ELECTRIC_KINETIC_GENERATOR, syncId, playerInventory, be, 166);

		for (int i = 0; i < 5; i++)
		{
			this.m_38897_(new SlotInvSlot(be.slotMotor, i, 44 + i * 18, 27));
		}

		for (int i = 5; i < 10; i++)
		{
			this.m_38897_(new SlotInvSlot(be.slotMotor, i, 44 + (i - 5) * 18, 45));
		}

		this.m_38897_(new SlotInvSlot(be.dischargeSlot, 0, 8, 62));
	}
}

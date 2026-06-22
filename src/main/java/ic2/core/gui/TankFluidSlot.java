package ic2.core.gui;

import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TankFluidSlot extends AbstractFluidSlot
{
	final Ic2FluidTank tank;

	protected TankFluidSlot(Ic2Gui<?> gui, int x, int y, int width, int height, Ic2FluidTank tank)
	{
		super(gui, x, y, width, height);
		if (tank == null)
		{
			throw new NullPointerException("Null FluidTank instance.");
		}

		this.tank = tank;
	}

	public static TankFluidSlot createFluidSlot(Ic2Gui<?> gui, int x, int y, Ic2FluidTank tank)
	{
		return new TankFluidSlot(gui, x, y, 18, 18, tank);
	}

	@Override
	protected Ic2FluidStack getFluidStack()
	{
		return this.tank.getFluidStack();
	}

	@Override
	protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button)
	{
		if (button == MouseButton.left)
		{
			Container base = this.getBase();
			if (base instanceof BlockEntity)
			{
				IC2.network.get(false).initiateClientTileEntityEvent((BlockEntity) base, 0);
				return true;
			}
		}
		return false;
	}
}

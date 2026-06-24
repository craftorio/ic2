package ic2.core.gui;

import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.fluid.Ic2FluidStack;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class ElectrolyzerTank extends AbstractFluidSlot
{
	private final TileEntityElectrolyzer electrolyzer;
	private final int id;
	private Pair<Ic2FluidStack, Direction> output;

	public ElectrolyzerTank(GuiElectrolyzer gui, int x, int y, int id)
	{
		super(gui, x, y, 18, 18);
		this.electrolyzer = gui.getContainer().base;
		this.id = id;
	}

	public boolean isActive()
	{
		return this.output != null;
	}

	@Override
	public void tick()
	{
		this.output = null;
		if (this.electrolyzer.hasRecipe())
		{
			IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs = this.electrolyzer.getCurrentRecipe().outputs();
			int length = outputs.length;
			if (length < this.id)
			{
				if ((length & 1) == 1 && this.id == 2)
				{
					this.output = outputs[length / 2].getFullOutput();
				}

				if (length >= 2)
				{
					if (this.id == 1)
					{
						this.output = outputs[length < 4 ? 0 : 1].getFullOutput();
					}

					if (this.id == 3)
					{
						this.output = outputs[length - (length < 4 ? 1 : 2)].getFullOutput();
					}
				}

				if (length >= 4)
				{
					if (this.id == 0)
					{
						this.output = outputs[0].getFullOutput();
					}

					if (this.id == 4)
					{
						this.output = outputs[length - 1].getFullOutput();
					}
				}
			}
		}
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> ret = super.getToolTip();
		if (this.output != null)
		{
			ret.add(Component.literal("Output Tank: " + StringUtils.capitalize(((Direction) this.output.getRight()).getSerializedName())));
		}

		return ret;
	}

	@Override
	protected Ic2FluidStack getFluidStack()
	{
		return this.output != null ? (Ic2FluidStack) this.output.getLeft() : null;
	}
}

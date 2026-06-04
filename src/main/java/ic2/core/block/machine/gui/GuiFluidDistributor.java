package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiFluidDistributor extends GuiIC2<ContainerFluidDistributor>
{
	public GuiFluidDistributor(ContainerFluidDistributor container)
	{
		super(container, 184);
		addElement(TankGauge.createPlain(this, 29, 38, 55, 47, container.base.fluidTank));
	}

	protected void drawForegroundLayer(int mouseX, int mouseY)
	{
		super.drawForegroundLayer(mouseX, mouseY);
		this.fontRenderer.drawString(Localization.translate("ic2.FluidDistributor.gui.mode.info"), 112, 47, 5752026);
		if (this.container.base.getActive())
		{
			this.fontRenderer.drawString(Localization.translate("ic2.FluidDistributor.gui.mode.concentrate"), 95, 71, 5752026);
		} else
		{
			this.fontRenderer.drawString(Localization.translate("ic2.FluidDistributor.gui.mode.distribute"), 95, 71, 5752026);
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		mouseX -= this.guiLeft;
		mouseY -= this.guiTop;
		if (mouseX >= 117 && mouseY >= 58 && mouseX <= 135 && mouseY <= 66)
			IC2.network.get(false).initiateClientTileEntityEvent(this.container.base, 1);
	}

	protected ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUIFluidDistributor.png");
	}
}

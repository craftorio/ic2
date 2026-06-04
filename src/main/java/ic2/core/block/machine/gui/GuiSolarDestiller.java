package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSolarDestiller extends GuiIC2<ContainerSolarDestiller>
{
	public GuiSolarDestiller(ContainerSolarDestiller container)
	{
		super(container, 184);
		addElement(TankGauge.createPlain(this, 37, 43, 53, 18, container.base.inputTank));
		addElement(TankGauge.createPlain(this, 115, 55, 17, 43, container.base.outputTank));
	}

	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);
		bindTexture();
		if (this.container.base.canWork())
			drawTexturedModalRect(this.guiLeft + 36, this.guiTop + 26, 0, 184, 97, 29);
	}

	protected ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUISolarDestiller.png");
	}
}

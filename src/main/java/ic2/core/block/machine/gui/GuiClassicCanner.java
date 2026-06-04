package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerClassicCanner;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.LinkedGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiClassicCanner extends GuiIC2<ContainerClassicCanner>
{
	public GuiClassicCanner(ContainerClassicCanner container)
	{
		super(container);
		addElement(new LinkedGauge(this, 74, 36, container.base, "progress", Gauge.GaugeStyle.ProgressLongArrow));
		addElement(EnergyGauge.asBolt(this, 34, 28, container.base));
	}

	protected ResourceLocation getTexture()
	{
		return background;
	}

	public static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUI_Canner_Classic.png");
}

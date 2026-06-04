package ic2.core.block.kineticgenerator.gui;

import ic2.core.GuiIC2;
import ic2.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElectricKineticGenertor extends GuiIC2<ContainerElectricKineticGenerator>
{
	public GuiElectricKineticGenertor(ContainerElectricKineticGenerator container)
	{
		super(container);
		addElement((new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal)).withTooltip("ic2.ElectricKineticGenerator.gui.motors"));
		addElement(EnergyGauge.asBolt(this, 12, 44, container.base));
		addElement(Text.create(this, 29, 66, 119, 13, TextProvider.of(() -> Localization.translate("ic2.ElectricKineticGenerator.gui.kUmax", container.base.getMaxKU(), container.base.getMaxKUForGUI())), 5752026, false, true, true).withTooltip("ic2.ElectricKineticGenerator.gui.tooltipkin"));
	}

	protected ResourceLocation getTexture()
	{
		return background;
	}

	private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIElectricKineticGenerator.png");
}

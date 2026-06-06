package ic2.core.block.kineticgenerator.gui;

import com.google.common.base.Supplier;
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
	private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIElectricKineticGenerator.png");

	public GuiElectricKineticGenertor(ContainerElectricKineticGenerator container)
	{
		super(container);
		this.addElement(new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal).withTooltip("ic2.ElectricKineticGenerator.gui.motors"));
		this.addElement(EnergyGauge.asBolt(this, 12, 44, container.base));
		this.addElement(
			Text.create(
					this,
					29,
					66,
					119,
					13,
					TextProvider.of(
						new Supplier<String>()
						{
							public String get()
							{
								return Localization.translate(
									"ic2.ElectricKineticGenerator.gui.kUmax",
									GuiElectricKineticGenertor.this.container.base.getMaxKU(),
									GuiElectricKineticGenertor.this.container.base.getMaxKUForGUI()
								);
							}
						}
					),
					5752026,
					false,
					true,
					true
				)
				.withTooltip("ic2.ElectricKineticGenerator.gui.tooltipkin")
		);
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}
}

package ic2.core.block.heatgenerator.gui;

import com.google.common.base.Supplier;
import ic2.core.Ic2Gui;
import ic2.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiElectricHeatGenerator extends Ic2Gui<ContainerElectricHeatGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guielectricheatgenerator.png");

	public GuiElectricHeatGenerator(ContainerElectricHeatGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal).withTooltip("ic2.ElectricHeatGenerator.gui.coils"));
		this.addElement(EnergyGauge.asBolt(this, 12, 44, container.base));
		this.addElement(
			TextLabel.create(
					this,
					34,
					66,
					109,
					13,
					TextProvider.of(
						new Supplier<String>()
						{
							public String get()
							{
								return Localization.translate(
									"ic2.ElectricHeatGenerator.gui.hUmax",
									((ContainerElectricHeatGenerator) GuiElectricHeatGenerator.this.menu).base.gettransmitHeat(),
									((ContainerElectricHeatGenerator) GuiElectricHeatGenerator.this.menu).base.getMaxHeatEmittedPerTick()
								);
							}
						}
					),
					5752026,
					false,
					true,
					true
				)
				.withTooltip("ic2.ElectricHeatGenerator.gui.tooltipheat")
		);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}

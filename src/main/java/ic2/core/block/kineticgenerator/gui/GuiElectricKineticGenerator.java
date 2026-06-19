package ic2.core.block.kineticgenerator.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiElectricKineticGenerator extends Ic2Gui<ContainerElectricKineticGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guielectrickineticgenerator.png");

	public GuiElectricKineticGenerator(ContainerElectricKineticGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal).withTooltip("ic2.ElectricKineticGenerator.gui.motors"));
		this.addElement(EnergyGauge.asBolt(this, 12, 44, container.base));
		this.addElement(TextLabel.create(this, 29, 66, 119, 13, TextProvider.of(() -> Component.translatable("ic2.ElectricKineticGenerator.gui.kUmax", GuiElectricKineticGenerator.this.menu.base.getMaxKU(), GuiElectricKineticGenerator.this.menu.base.getMaxKUForGUI()).getString()), 5752026, false, true, true).withTooltip("ic2.ElectricKineticGenerator.gui.tooltipkin"));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}

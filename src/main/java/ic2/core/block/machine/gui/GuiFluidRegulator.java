package ic2.core.block.machine.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerFluidRegulator;
import ic2.core.gui.CustomButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.TankGauge;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiFluidRegulator extends Ic2Gui<ContainerFluidRegulator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guifluidregulator.png");

	public GuiFluidRegulator(ContainerFluidRegulator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(EnergyGauge.asBolt(this, 12, 39, container.base));
		this.addElement(TankGauge.createNormal(this, 78, 34, container.base.getFluidTank()));

		for (int i = 0; i < 4; i++)
		{
			int val = (int) Math.pow(10.0, 3 - i);
			this.addElement(new CustomButton(this, 102 + i * 10, 44, 9, 9, this.createEventSender(val)));
			this.addElement(new CustomButton(this, 102 + i * 10, 68, 9, 9, this.createEventSender(-val)));
		}

		this.addElement(new CustomButton(this, 152, 44, 9, 9, this.createEventSender(1001)));
		this.addElement(new CustomButton(this, 152, 68, 9, 9, this.createEventSender(1002)));
		this.addElement(
			TextLabel.create(this, 105, 57, TextProvider.of(() -> container.base.getoutputmb() + Localization.translate("ic2.generic.text.mb")), 2157374, false)
		);
		this.addElement(TextLabel.create(this, 145, 57, TextProvider.of(() -> container.base.getmodegui()), 2157374, false));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}

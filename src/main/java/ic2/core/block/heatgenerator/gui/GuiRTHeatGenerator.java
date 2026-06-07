package ic2.core.block.heatgenerator.gui;

import com.google.common.base.Supplier;
import ic2.core.Ic2Gui;
import ic2.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiRTHeatGenerator extends Ic2Gui<ContainerRTHeatGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guirtheatgenerator.png");

	public GuiRTHeatGenerator(ContainerRTHeatGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TextLabel.create(this, 49, 66, 79, 13, TextProvider.of(new Supplier<String>()
		{
			public String get()
			{
				return container.base.gettransmitHeat() + " / " + container.base.getMaxHeatEmittedPerTick();
			}
		}), 5752026, false, 0, 0, true, true).withTooltip("ic2.RTHeatGenerator.gui.tooltipheat"));
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}
}

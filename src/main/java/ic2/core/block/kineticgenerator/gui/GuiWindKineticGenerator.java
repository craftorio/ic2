package ic2.core.block.kineticgenerator.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.kineticgenerator.container.ContainerWindKineticGenerator;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Image;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiWindKineticGenerator extends Ic2Gui<ContainerWindKineticGenerator>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiwindkineticgenerator.png");

	public GuiWindKineticGenerator(ContainerWindKineticGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TextLabel.create(this, 17, 48, 143, 13, TextProvider.of(() ->
		{
			if (!container.base.hasRotor())
			{
				return Component.translatable("ic2.wind_kinetic_generator.gui.rotormiss").getString();
			} else if (!container.base.rotorHasSpace())
			{
				return Component.translatable("ic2.wind_kinetic_generator.gui.rotorspace").getString();
			} else
			{
				return !container.base.isWindStrongEnough() ? Component.translatable("ic2.wind_kinetic_generator.gui.windweak1").getString() : Component.translatable("ic2.wind_kinetic_generator.gui.output", container.base.getKuOutput()).getString();
			}
		}), 2157374, false, 4, 0, false, true));
		this.addElement(TextLabel.create(this, 17, 66, 143, 13, TextProvider.of(() ->
		{
			if (!container.base.hasRotor() || !container.base.rotorHasSpace())
			{
				return null;
			} else
			{
				return !container.base.isWindStrongEnough() ? Component.translatable("ic2.wind_kinetic_generator.gui.windweak2").getString() : container.base.getRotorHealth() + " %";
			}
		}), 2157374, false, 4, 0, false, true));
		IEnableHandler warningEnabler = container.base::isRotorOverloaded;
		this.addElement(Image.create(this, 44, 20, 30, 26, background, 256, 256, 176, 0, 206, 26).withEnableHandler(warningEnabler).withTooltip("ic2.wind_kinetic_generator.error.overload"));
		this.addElement(Image.create(this, 102, 20, 30, 26, background, 256, 256, 176, 0, 206, 26).withEnableHandler(warningEnabler).withTooltip("ic2.wind_kinetic_generator.error.overload"));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}

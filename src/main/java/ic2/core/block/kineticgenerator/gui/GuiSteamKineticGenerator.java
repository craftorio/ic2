package ic2.core.block.kineticgenerator.gui;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.gui.Image;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TankGauge;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;

public class GuiSteamKineticGenerator extends GuiIC2<ContainerSteamKineticGenerator>
{
	public GuiSteamKineticGenerator(final ContainerSteamKineticGenerator container)
	{
		super(container);
		addElement(TankGauge.createPlain(this, 75, 21, 26, 26, container.base.getDistilledWaterTank()));
		addElement((new SlotGrid(this, 80, 26, SlotGrid.SlotStyle.Plain)).withTooltip(() -> container.base.hasTurbine() ? null : "ic2.SteamKineticGenerator.gui.turbineslot"));
		addElement(Image.create(this, 36, 20, 30, 26, TEXTURE, 256, 256, 176, 0, 206, 26).withEnableHandler(() -> (container.base.hasTurbine() && container.base.isVentingSteam())).withTooltip("ic2.SteamKineticGenerator.gui.ventingWarning"));
		addElement(Image.create(this, 110, 20, 30, 26, TEXTURE, 256, 256, 176, 0, 206, 26).withEnableHandler(() -> (container.base.hasTurbine() && container.base.isThrottled())).withTooltip("ic2.SteamKineticGenerator.gui.condensationwarrning"));
		addElement(Text.create(this, 8, 51, 160, 13, TextProvider.of(new Supplier<String>()
		{
			public String get()
			{
				return Localization.translate(getRaw());
			}

			private String getRaw()
			{
				if (!container.base.hasTurbine())
					return "ic2.SteamKineticGenerator.gui.error.noturbine";
				if (container.base.isTurbineBlockedByWater())
					return "ic2.SteamKineticGenerator.gui.error.filledupwithwater";
				if (container.base.getActive())
					return "ic2.SteamKineticGenerator.gui.aktive";
				return "ic2.SteamKineticGenerator.gui.waiting";
			}
		}), () ->
		{
			if (!container.base.hasTurbine() || container.base.isTurbineBlockedByWater())
				return 14946604;
			return 2157374;
		}, false, 4, 0, false, true));
		addElement(Text.create(this, 8, 68, 160, 13, TextProvider.of(() -> Localization.translate("ic2.SteamKineticGenerator.gui.turbine.ouput", container.base.getKUoutput())), 2157374, false, 4, 0, false, true).withEnableHandler(() -> (container.base.hasTurbine() && !container.base.isTurbineBlockedByWater())));
	}

	protected ResourceLocation getTexture()
	{
		return TEXTURE;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUISteamKineticGenerator.png");
}

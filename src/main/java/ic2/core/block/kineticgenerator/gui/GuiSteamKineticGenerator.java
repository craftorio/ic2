package ic2.core.block.kineticgenerator.gui;

import com.google.common.base.Supplier;
import ic2.core.Ic2Gui;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Image;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TankGauge;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiSteamKineticGenerator extends Ic2Gui<ContainerSteamKineticGenerator>
{
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guisteamkineticgenerator.png");

	public GuiSteamKineticGenerator(ContainerSteamKineticGenerator container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(TankGauge.createPlain(this, 75, 21, 26, 26, container.base.getDistilledWaterTank()));
		this.addElement(new SlotGrid(this, 80, 26, SlotGrid.SlotStyle.Plain).withTooltip(new Supplier<String>()
		{
			public String get()
			{
				return !container.base.hasTurbine() ? "ic2.SteamKineticGenerator.gui.turbineslot" : null;
			}
		}));
		this.addElement(Image.create(this, 36, 20, 30, 26, TEXTURE, 256, 256, 176, 0, 206, 26).withEnableHandler(new IEnableHandler()
		{
			@Override
			public boolean isEnabled()
			{
				return container.base.hasTurbine() && container.base.isVentingSteam();
			}
		}).withTooltip("ic2.SteamKineticGenerator.gui.ventingWarning"));
		this.addElement(Image.create(this, 110, 20, 30, 26, TEXTURE, 256, 256, 176, 0, 206, 26).withEnableHandler(new IEnableHandler()
		{
			@Override
			public boolean isEnabled()
			{
				return container.base.hasTurbine() && container.base.isThrottled();
			}
		}).withTooltip("ic2.SteamKineticGenerator.gui.condensationwarrning"));
		this.addElement(TextLabel.create(this, 8, 51, 160, 13, TextProvider.of(new Supplier<String>()
		{
			public String get()
			{
				return Localization.translate(this.getRaw());
			}

			private String getRaw()
			{
				if (!container.base.hasTurbine())
				{
					return "ic2.SteamKineticGenerator.gui.error.noturbine";
				} else if (container.base.isTurbineBlockedByWater())
				{
					return "ic2.SteamKineticGenerator.gui.error.filledupwithwater";
				} else
				{
					return container.base.getActive() ? "ic2.SteamKineticGenerator.gui.aktive" : "ic2.SteamKineticGenerator.gui.waiting";
				}
			}
		}), () -> container.base.hasTurbine() && !container.base.isTurbineBlockedByWater() ? 2157374 : 14946604, false, 4, 0, false, true));
		this.addElement(TextLabel.create(this, 8, 68, 160, 13, TextProvider.of(new Supplier<String>()
		{
			public String get()
			{
				return Localization.translate("ic2.SteamKineticGenerator.gui.turbine.ouput", container.base.getKUoutput());
			}
		}), 2157374, false, 4, 0, false, true).withEnableHandler(new IEnableHandler()
		{
			@Override
			public boolean isEnabled()
			{
				return container.base.hasTurbine() && !container.base.isTurbineBlockedByWater();
			}
		}));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return TEXTURE;
	}
}

package ic2.core.block.reactor.gui;

import com.google.common.base.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.gui.Area;
import ic2.core.gui.Gauge;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.TankGauge;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiNuclearReactor extends Ic2Gui<ContainerNuclearReactor>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guinuclearreactor.png");
	private static final ResourceLocation backgroundFluid = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guinuclearreactorfluid.png");

	public GuiNuclearReactor(ContainerNuclearReactor container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 212, 243);
		IEnableHandler enableHandler = GuiNuclearReactor.this.menu.base::isFluidCooled;
		this.addElement(TankGauge.createBorderless(this, 10, 54, container.base.getinputtank(), true).withEnableHandler(enableHandler));
		this.addElement(TankGauge.createBorderless(this, 190, 54, container.base.getoutputtank(), false).withEnableHandler(enableHandler));
		this.addElement(new LinkedGauge(this, 7, 136, container.base, "heat", Gauge.GaugeStyle.HeatNuclearReactor).withTooltip((Supplier<String>) () -> Component.translatable("ic2.NuclearReactor.gui.info.temp", GuiNuclearReactor.this.menu.base.getGuiValue("heat") * 100.0).getString()));
		this.addElement(TextLabel.create(this, 107, 136, 200, 13, TextProvider.of(() -> GuiNuclearReactor.this.menu.base.isFluidCooled() ? Component.translatable("ic2.NuclearReactor.gui.info.HUoutput", GuiNuclearReactor.this.menu.base.EmitHeat).getString() : Component.translatable("ic2.NuclearReactor.gui.info.EUoutput", Math.round(GuiNuclearReactor.this.menu.base.getOfferedEnergy())).getString()), 5752026, false, 4, 0, false, true));
		this.addElement(new Area(this, 5, 160, 18, 18).withTooltip((Supplier<String>) () -> GuiNuclearReactor.this.menu.base.isFluidCooled() ? "ic2.NuclearReactor.gui.mode.fluid" : "ic2.NuclearReactor.gui.mode.electric"));
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY)
	{
		super.renderBg(guiGraphics, delta, mouseX, mouseY);
		int size = this.menu.base.getReactorSize();
		this.bindTexture();

		for (int y = 0; y < 6; y++)
		{
			for (int x = size; x < 9; x++)
			{
				this.drawTexturedRect(guiGraphics.pose(), 26 + x * 18, 25 + y * 18, 16.0, 16.0, 213.0, 1.0);
			}
		}

		if (this.menu.base.isFluidCooled())
		{
			int heat = this.menu.base.gaugeHeatScaled(160);
			this.drawTexturedRect(guiGraphics.pose(), 186 - heat, 23.0, 0.0, 243.0, heat, 2.0);
			this.drawTexturedRect(guiGraphics.pose(), 186 - heat, 41.0, 0.0, 243.0, heat, 2.0);
			this.drawTexturedRect(guiGraphics.pose(), 186 - heat, 59.0, 0.0, 243.0, heat, 2.0);
			this.drawTexturedRect(guiGraphics.pose(), 186 - heat, 77.0, 0.0, 243.0, heat, 2.0);
			this.drawTexturedRect(guiGraphics.pose(), 186 - heat, 95.0, 0.0, 243.0, heat, 2.0);
			this.drawTexturedRect(guiGraphics.pose(), 186 - heat, 113.0, 0.0, 243.0, heat, 2.0);
			this.drawTexturedRect(guiGraphics.pose(), 186 - heat, 131.0, 0.0, 243.0, heat, 2.0);
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return this.menu.base.isFluidCooled() ? backgroundFluid : background;
	}
}

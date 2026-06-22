package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.gui.CustomButton;
import ic2.core.gui.CycleHandler;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.INumericValueHandler;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCanner extends Ic2Gui<ContainerCanner>
{
	public static final ResourceLocation texture = IC2.getIdentifier("textures/gui/guicanner.png");

	public GuiCanner(ContainerCanner container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(EnergyGauge.asBolt(this, 12, 62, container.base));
		CycleHandler cycleHandler = new CycleHandler(176, 18, 226, 32, 14, true, 4, new INumericValueHandler()
		{
			@Override
			public int getValue()
			{
				return GuiCanner.this.menu.base.getMode().ordinal();
			}

			@Override
			public void onChange(int value)
			{
				IC2.network.get(false).initiateClientTileEntityEvent(GuiCanner.this.menu.base, value);
			}
		});
		this.addElement(new CustomButton(this, 63, 81, 50, 14, cycleHandler, texture, cycleHandler).withTooltip((Supplier<String>) () ->
			switch (GuiCanner.this.menu.base.getMode())
			{
				case BottleSolid -> "ic2.Canner.gui.switch.BottleSolid";
				case EmptyLiquid -> "ic2.Canner.gui.switch.EmptyLiquid";
				case BottleLiquid -> "ic2.Canner.gui.switch.BottleLiquid";
				case EnrichLiquid -> "ic2.Canner.gui.switch.EnrichLiquid";
			}));
		this.addElement(new CustomButton(this, 77, 64, 22, 13, this.createEventSender(TileEntityCanner.eventSwapTanks)).withTooltip("ic2.Canner.gui.switchTanks"));
		this.addElement(TankGauge.createNormal(this, 39, 42, container.base.getInputTank()));
		this.addElement(TankGauge.createNormal(this, 117, 42, container.base.getOutputTank()));
		if (RecipeButton.canUse())
		{
			for (final TileEntityCanner.Mode mode : TileEntityCanner.Mode.values)
			{
				this.addElement(new RecipeButton(this, 74, 22, 23, 14, new String[] { "canner_" + mode }).withEnableHandler(() -> GuiCanner.this.menu.base.getMode() == mode));
			}
		}
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY)
	{
		super.renderBg(guiGraphics, delta, mouseX, mouseY);
		this.bindTexture();
		switch (this.menu.base.getMode())
		{
			case BottleSolid:
				this.drawTexturedRect(guiGraphics.pose(), 59.0, 53.0, 9.0, 18.0, 3.0, 4.0);
				this.drawTexturedRect(guiGraphics.pose(), 99.0, 53.0, 18.0, 23.0, 3.0, 4.0);
				break;
			case EmptyLiquid:
				this.drawTexturedRect(guiGraphics.pose(), 71.0, 43.0, 26.0, 18.0, 196.0, 0.0);
				this.drawTexturedRect(guiGraphics.pose(), 59.0, 53.0, 9.0, 18.0, 3.0, 4.0);
				break;
			case BottleLiquid:
				this.drawTexturedRect(guiGraphics.pose(), 99.0, 53.0, 18.0, 23.0, 3.0, 4.0);
				this.drawTexturedRect(guiGraphics.pose(), 71.0, 43.0, 26.0, 18.0, 196.0, 0.0);
			case EnrichLiquid:
		}

		int progressSize = Math.round(this.menu.base.getProgress() * 23.0F);
		if (progressSize > 0)
		{
			this.drawTexturedRect(guiGraphics.pose(), 74.0, 22.0, progressSize, 14.0, 233.0, 0.0);
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return texture;
	}
}

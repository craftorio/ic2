package ic2.core.item.tool;

import ic2.core.Ic2Gui;
import ic2.core.ref.Ic2Items;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiCropAnalyzer extends Ic2Gui<ContainerAnalyzer>
{
	private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guicrop_analyzer.png");

	public GuiCropAnalyzer(ContainerAnalyzer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 223);
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
		HandHeldCropAnalyzer analyzer = this.getContainer().base;
		int scannedLevel = analyzer.getScannedLevel();
		if (scannedLevel == 0)
		{
			this.drawString(guiGraphics, 8, 37, "UNKNOWN", 0xFFFFFF);
		}

		if (scannedLevel >= 1)
		{
			this.drawString(guiGraphics, 8, 37, Component.translatable(analyzer.getSeedName()).toString(), 0xFFFFFF);
		}

		if (scannedLevel >= 2)
		{
			this.drawString(guiGraphics, 8, 50, "Tier: " + analyzer.getSeedTier(), 0xFFFFFF);
			this.drawString(guiGraphics, 8, 73, "Discovered by:", 0xFFFFFF);
			this.drawString(guiGraphics, 8, 86, analyzer.getSeedDiscoveredBy(), 0xFFFFFF);
		}

		if (scannedLevel >= 3)
		{
			this.drawString(guiGraphics, 8, 109, analyzer.getSeedDesc(0), 0xFFFFFF);
			this.drawString(guiGraphics, 8, 122, analyzer.getSeedDesc(1), 0xFFFFFF);
		}

		if (scannedLevel >= 4)
		{
			this.drawString(guiGraphics, 118, 37, "Growth:", 0xAE26E6);
			this.drawString(guiGraphics, 118, 50, Integer.toString(analyzer.getSeedGrowth()), 0xAE26E6);
			this.drawString(guiGraphics, 118, 73, "Gain:", 0xEEC900);
			this.drawString(guiGraphics, 118, 86, Integer.toString(analyzer.getSeedGain()), 0xEEC900);
			this.drawString(guiGraphics, 118, 109, "Resistance:", 0x00CED1);
			this.drawString(guiGraphics, 118, 122, Integer.toString(analyzer.getSeedResistance()), 0x00CED1);
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return BACKGROUND;
	}
}

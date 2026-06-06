package ic2.core.item.tool;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.ref.ItemName;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCropnalyzer extends GuiIC2<ContainerCropnalyzer>
{
	private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUICropnalyzer.png");

	public GuiCropnalyzer(ContainerCropnalyzer container)
	{
		super(container, 223);
		this.addElement(Text.create(this, 74, 11, ItemName.cropnalyzer.getItemStack().getDisplayName(), 0, false));
		this.addElement(Text.create(this, 8, 37, "UNKNOWN", 16777215, false).withEnableHandler(() -> container.base.getScannedLevel() == 0));
		this.addElement(Text.create(this, 8, 37, this.cropSensitiveText(container.base::getSeedName), 16777215, false).withEnableHandler(this.atLeastLevel(1)));
		IEnableHandler atLeast2 = this.atLeastLevel(2);
		this.addElement(
			Text.create(this, 8, 50, this.cropSensitiveText(() -> "Tier: " + container.base.getSeedTier()), 16777215, false).withEnableHandler(atLeast2)
		);
		this.addElement(Text.create(this, 8, 73, "Discovered by:", 16777215, false).withEnableHandler(atLeast2));
		this.addElement(Text.create(this, 8, 86, this.cropSensitiveText(container.base::getSeedDiscovered), 16777215, false).withEnableHandler(atLeast2));
		IEnableHandler atLeast3 = this.atLeastLevel(3);
		this.addElement(Text.create(this, 8, 109, this.cropSensitiveText(() -> container.base.getSeedDesc(0)), 16777215, false).withEnableHandler(atLeast3));
		this.addElement(Text.create(this, 8, 122, this.cropSensitiveText(() -> container.base.getSeedDesc(1)), 16777215, false).withEnableHandler(atLeast3));
		IEnableHandler atLeast4 = this.atLeastLevel(4);
		this.addElement(Text.create(this, 118, 37, "Growth:", 11403055, false).withEnableHandler(atLeast4));
		this.addElement(
			Text.create(this, 118, 50, this.cropSensitiveText(() -> Integer.toString(container.base.getSeedGrowth())), 11403055, false)
				.withEnableHandler(atLeast4)
		);
		this.addElement(Text.create(this, 118, 73, "Gain:", 15649024, false).withEnableHandler(atLeast4));
		this.addElement(
			Text.create(this, 118, 86, this.cropSensitiveText(() -> Integer.toString(container.base.getSeedGain())), 15649024, false).withEnableHandler(atLeast4)
		);
		this.addElement(Text.create(this, 118, 109, "Resis.:", 52945, false).withEnableHandler(atLeast4));
		this.addElement(
			Text.create(this, 118, 122, this.cropSensitiveText(() -> Integer.toString(container.base.getSeedResistence())), 52945, false)
				.withEnableHandler(atLeast4)
		);
	}

	private IEnableHandler atLeastLevel(int level)
	{
		return () -> this.container.base.getScannedLevel() >= level;
	}

	private TextProvider.ITextProvider cropSensitiveText(Supplier<String> text)
	{
		return TextProvider.of(() -> this.container.base.getScannedLevel() > -1 ? (String) text.get() : "");
	}

	@Override
	protected void drawBackgroundAndTitle(float partialTicks, int mouseX, int mouseY)
	{
		this.bindTexture();
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}
}

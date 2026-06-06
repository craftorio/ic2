package ic2.core.item.tool;

import ic2.core.GuiIC2;
import ic2.core.gui.Text;
import ic2.core.ref.ItemName;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiToolbox extends GuiIC2<ContainerToolbox>
{
	private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIToolbox.png");

	public GuiToolbox(ContainerToolbox container)
	{
		super(container);
		this.addElement(Text.create(this, 65, 11, ItemName.tool_box.getItemStack().getDisplayName(), 0, false));
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

package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.ItemStackImage;
import ic2.core.gui.LinkedGauge;
import ic2.core.util.StackUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBatchCrafter extends GuiIC2<ContainerBatchCrafter>
{
	public GuiBatchCrafter(ContainerBatchCrafter container)
	{
		super(container, 206);
		addElement(EnergyGauge.asBolt(this, 12, 45, container.base));
		addElement(new LinkedGauge(this, 90, 35, container.base, "progress", Gauge.GaugeStyle.ProgressArrow));
		addElement(new ItemStackImage(this, 94, 14, () -> StackUtil.wrapEmpty(GuiBatchCrafter.this.container.base.recipeOutput)));
	}

	protected ResourceLocation getTexture()
	{
		return TEXTURE;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIBatchCrafter.png");
}

package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerFluidBottler;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFluidBottler extends GuiIC2<ContainerFluidBottler>
{
	public GuiFluidBottler(ContainerFluidBottler container)
	{
		super(container, 184);
		this.addElement(EnergyGauge.asBolt(this, 12, 35, container.base));
		this.addElement(TankGauge.createNormal(this, 78, 34, container.base.fluidTank));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		super.drawGuiContainerBackgroundLayer(f, x, y);
		this.bindTexture();
		int progressSize = Math.round(this.container.base.getProgress() * 16.0F);
		if (progressSize > 0)
		{
			this.drawTexturedModalRect(this.guiLeft + 61, this.guiTop + 36, 198, 0, progressSize, 13);
			this.drawTexturedModalRect(this.guiLeft + 61, this.guiTop + 73, 198, 0, progressSize, 13);
			this.drawTexturedModalRect(this.guiLeft + 99, this.guiTop + 55, 198, 0, progressSize, 13);
		}
	}

	@Override
	public ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUIBottler.png");
	}
}

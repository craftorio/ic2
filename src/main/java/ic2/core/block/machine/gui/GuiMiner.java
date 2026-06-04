package ic2.core.block.machine.gui;

import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.gui.EnergyGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMiner extends GuiIC2<ContainerMiner>
{
	public GuiMiner(ContainerMiner container)
	{
		super(container);
		addElement(EnergyGauge.asBolt(this, 155, 41, container.base));
	}

	protected ResourceLocation getTexture()
	{
		return new ResourceLocation("ic2", "textures/gui/GUIMiner.png");
	}
}

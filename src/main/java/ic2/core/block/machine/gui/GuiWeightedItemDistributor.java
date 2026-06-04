package ic2.core.block.machine.gui;

import ic2.core.block.machine.container.ContainerWeightedItemDistributor;
import net.minecraft.util.ResourceLocation;

public class GuiWeightedItemDistributor extends GuiWeightedDistributor<ContainerWeightedItemDistributor>
{
	public GuiWeightedItemDistributor(ContainerWeightedItemDistributor container)
	{
		super(container, 211);
	}

	protected ResourceLocation getTexture()
	{
		return TEXTURE;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIWeightedItemDistributor.png");
}

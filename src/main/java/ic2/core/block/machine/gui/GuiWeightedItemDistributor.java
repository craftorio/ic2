package ic2.core.block.machine.gui;

import ic2.core.block.machine.container.ContainerWeightedItemDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiWeightedItemDistributor extends GuiWeightedDistributor<ContainerWeightedItemDistributor>
{
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiweighteditemdistributor.png");

	public GuiWeightedItemDistributor(ContainerWeightedItemDistributor container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 211);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return TEXTURE;
	}
}

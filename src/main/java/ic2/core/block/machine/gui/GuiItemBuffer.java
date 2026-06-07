package ic2.core.block.machine.gui;

import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerItemBuffer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiItemBuffer extends Ic2Gui<ContainerItemBuffer>
{
	public GuiItemBuffer(ContainerItemBuffer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 176, 232);
	}

	@Override
	public ResourceLocation getTexture()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guiitembuffer.png");
	}
}

package ic2.core.block.machine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerSolarDestiller;
import ic2.core.gui.TankGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiSolarDestiller extends Ic2Gui<ContainerSolarDestiller>
{
	public GuiSolarDestiller(ContainerSolarDestiller container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 184);
		this.addElement(TankGauge.createPlain(this, 37, 43, 53, 18, container.base.inputTank));
		this.addElement(TankGauge.createPlain(this, 115, 55, 17, 43, container.base.outputTank));
	}

	@Override
	protected void m_7286_(PoseStack matrices, float delta, int mouseX, int mouseY)
	{
		super.m_7286_(matrices, delta, mouseX, mouseY);
		this.bindTexture();
		if (((ContainerSolarDestiller) this.menu).base.canWork())
		{
			this.drawTexturedRect(matrices, this.f_97735_ + 36, this.f_97736_ + 26, 0.0, 184.0, 97.0, 29.0);
		}
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guisolardestiller.png");
	}
}

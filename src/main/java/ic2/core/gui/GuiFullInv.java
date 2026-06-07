package ic2.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.ContainerBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

public class GuiFullInv<T extends ContainerBase<? extends Container>> extends GuiDefaultBackground<T>
{
	public GuiFullInv(T container, Inventory playerInventory, Component title)
	{
		this(container, playerInventory, title, 176, 166);
	}

	public GuiFullInv(T container, Inventory playerInventory, Component title, int ySize)
	{
		this(container, playerInventory, title, 176, ySize);
	}

	public GuiFullInv(T container, Inventory playerInventory, Component title, int xSize, int ySize)
	{
		super(container, playerInventory, title, xSize, ySize);
		this.addElement(new SlotGrid(this, 7, 83, 9, 3, SlotGrid.SlotStyle.Normal, 0, 0));
		this.addElement(new SlotGrid(this, 7, 141, 9, 1, SlotGrid.SlotStyle.Normal, 0, 0));
	}

	@Override
	protected void drawBackgroundAndTitle(PoseStack matrices, float partialTicks, int mouseX, int mouseY)
	{
		super.drawBackgroundAndTitle(matrices, partialTicks, mouseX, mouseY);
		this.drawXCenteredString(matrices, this.imageWidth / 2, 6, this.f_96539_, 4210752, false);
	}
}

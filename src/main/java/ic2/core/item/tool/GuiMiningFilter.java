package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.gui.GuiDefaultBackground;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.VanillaButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiMiningFilter extends GuiDefaultBackground<ContainerMiningFilter>
{
	public GuiMiningFilter(ContainerMiningFilter container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 215);

		this.addElement(new VanillaButton(this, 8, 10, 100, 15, button ->
		{
			this.menu.blacklist = !this.menu.blacklist;
			IC2.network.get(false).sendContainerField(this.menu, "blacklist");
		}).withText(() ->
			Component.translatable(this.menu.blacklist ? "ic2.MiningFilter.gui.mode.blacklist" : "ic2.MiningFilter.gui.mode.whitelist").getString()
		));

		this.addElement(new SlotGrid(this, 7, 31, 9, 5, SlotGrid.SlotStyle.Normal));
		this.addElement(new SlotGrid(this, 7, 132, 9, 3, SlotGrid.SlotStyle.Normal));
		this.addElement(new SlotGrid(this, 7, 190, 9, 1, SlotGrid.SlotStyle.Normal));
	}

	@Override
	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(guiGraphics, mouseX, mouseY);
	}
}

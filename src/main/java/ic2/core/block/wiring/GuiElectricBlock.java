package ic2.core.block.wiring;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.VanillaButton;
import ic2.core.init.Localization;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiElectricBlock extends Ic2Gui<ContainerElectricBlock>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guielectricblock.png");

	public GuiElectricBlock(ContainerElectricBlock container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 196);
		this.addElement(EnergyGauge.asBar(this, 79, 38, container.base));
		this.addElement(new VanillaButton(this, 152, 4, 20, 20, this.createEventSender(0)).withIcon(new Supplier<ItemStack>()
		{
			public ItemStack get()
			{
				return new ItemStack(Items.REDSTONE);
			}
		}).withTooltip(new Supplier<String>()
		{
			public String get()
			{
				return container.base.getRedstoneMode();
			}
		}));
	}

	@Override
	protected void drawForegroundLayer(PoseStack matrices, int mouseX, int mouseY)
	{
		super.drawForegroundLayer(matrices, mouseX, mouseY);
		this.drawString(matrices, 8, 74, Localization.translate("ic2.EUStorage.gui.info.armor"), 4210752);
		this.drawString(matrices, 79, 40, Localization.translate("ic2.EUStorage.gui.info.level"), 4210752);
		int e = (int) Math.min(((ContainerElectricBlock) this.menu).base.energy.getEnergy(), ((ContainerElectricBlock) this.menu).base.energy.getCapacity());
		this.drawString(matrices, 110, 50, " " + e, 4210752);
		this.drawString(matrices, 110, 60, "/" + (int) ((ContainerElectricBlock) this.menu).base.energy.getCapacity(), 4210752);
		String output = Localization.translate("ic2.EUStorage.gui.info.output", ((ContainerElectricBlock) this.menu).base.getOutput());
		this.drawString(matrices, 85, 75, output, 4210752);
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}

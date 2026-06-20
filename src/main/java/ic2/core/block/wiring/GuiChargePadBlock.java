package ic2.core.block.wiring;

import ic2.core.Ic2Gui;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.TextLabel;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.dynamic.TextProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiChargePadBlock extends Ic2Gui<ContainerChargepadBlock>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guichargepadblock.png");

	public GuiChargePadBlock(ContainerChargepadBlock container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 161);
		this.addElement(EnergyGauge.asBar(this, 79, 38, container.base));
		this.addElement(new VanillaButton(this, 152, 4, 20, 20, this.createEventSender(0)).withIcon(() -> new ItemStack(Items.REDSTONE)).withTooltip(container.base::getRedstoneMode));
		this.addElement(TextLabel.create(this, 79, 25, TextProvider.ofTranslated("ic2.EUStorage.gui.info.level"), 4210752, false));
		this.addElement(TextLabel.create(this, 110, 35, TextProvider.of(() -> " " + (int) Math.min(container.base.energy.getEnergy(), container.base.energy.getCapacity())), 4210752, false));
		this.addElement(TextLabel.create(this, 110, 45, TextProvider.of(() -> "/" + (int) container.base.energy.getCapacity()), 4210752, false));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return background;
	}
}

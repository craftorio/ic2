package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.gui.CustomGauge;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.VanillaButton;
import ic2.core.ref.Ic2Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiMetalFormer extends Ic2Gui<ContainerMetalFormer>
{
	public GuiMetalFormer(ContainerMetalFormer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title);
		this.addElement(EnergyGauge.asBolt(this, 20, 37, container.base));
		this.addElement(CustomGauge.create(this, 52, 39, container.base::getProgress, Gauge.GaugeStyle.ProgressMetalFormer));
		this.addElement(new VanillaButton(this, 65, 53, 20, 20, this.createEventSender(0)).withIcon((Supplier<ItemStack>) () -> switch (container.base.getMode())
		{
			case 0 -> new ItemStack(Ic2Items.COPPER_CABLE);
			case 1 -> new ItemStack(Ic2Items.FORGE_HAMMER);
			case 2 -> new ItemStack(Ic2Items.CUTTER);
			default -> null;
		}).withTooltip((Supplier<String>) () -> switch (container.base.getMode())
		{
			case 0 -> Component.translatable("ic2.MetalFormer.gui.switch.Extruding").getString();
			case 1 -> Component.translatable("ic2.MetalFormer.gui.switch.Rolling").getString();
			case 2 -> Component.translatable("ic2.MetalFormer.gui.switch.Cutting").getString();
			default -> null;
		}));
		if (RecipeButton.canUse())
		{
			for (int i = 0; i < 3; i++)
			{
				final int mode = i;
				this.addElement(new RecipeButton(this, 52, 39, 46, 9, new String[] { "metal_former" + mode }).withEnableHandler(() -> container.base.getMode() == mode));
			}
		}
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guimetalformer.png");
	}
}

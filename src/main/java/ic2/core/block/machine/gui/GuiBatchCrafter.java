package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.ItemStackImage;
import ic2.core.gui.LinkedGauge;
import ic2.core.util.StackUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiBatchCrafter extends Ic2Gui<ContainerBatchCrafter>
{
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guibatchcrafter.png");

	public GuiBatchCrafter(ContainerBatchCrafter container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 206);
		this.addElement(EnergyGauge.asBolt(this, 12, 45, container.base));
		this.addElement(new LinkedGauge(this, 90, 35, container.base, "progress", Gauge.GaugeStyle.ProgressArrow));
		this.addElement(new ItemStackImage(this, 94, 14, new Supplier<ItemStack>()
		{
			public ItemStack get()
			{
				return StackUtil.wrapEmpty(((ContainerBatchCrafter) GuiBatchCrafter.this.menu).base.recipeOutput);
			}
		}));
	}

	@Override
	protected ResourceLocation getTextureLocation()
	{
		return TEXTURE;
	}
}

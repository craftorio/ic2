package ic2.core.block.wiring;

import ic2.core.Ic2Gui;
import ic2.core.block.wiring.tileentity.TileEntityTransformer;
import ic2.core.gui.ItemImage;
import ic2.core.gui.TextLabel;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.ref.Ic2Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiTransformer extends Ic2Gui<ContainerTransformer>
{
	public String[] mode = new String[] { "", "", "", "" };
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guitransfomer.png");

	public GuiTransformer(ContainerTransformer container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 219);
		this.addElement(TextLabel.create(this, 6, 56, TextProvider.ofTranslated("ic2.Transformer.gui.Output"), 4210752, true));
		this.addElement(TextLabel.create(this, 6, 70, TextProvider.ofTranslated("ic2.Transformer.gui.Input"), 4210752, true));
		this.addElement(
			TextLabel.create(
				this,
				52,
				56,
				TextProvider.of(() -> ((ContainerTransformer) this.menu).base.getoutputflow() + " " + Localization.translate("ic2.generic.text.EUt")),
				2157374,
				true
			)
		);
		this.addElement(
			TextLabel.create(
				this,
				52,
				72,
				TextProvider.of(() -> ((ContainerTransformer) this.menu).base.getinputflow() + " " + Localization.translate("ic2.generic.text.EUt")),
				2157374,
				true
			)
		);
		this.addElement(new VanillaButton(this, 7, 65, 144, 20, this.createEventSender(0)).withText(Localization.translate("ic2.Transformer.gui.switch.mode1")));
		this.addElement(new VanillaButton(this, 7, 85, 144, 20, this.createEventSender(1)).withText(Localization.translate("ic2.Transformer.gui.switch.mode2")));
		this.addElement(new VanillaButton(this, 7, 105, 144, 20, this.createEventSender(2)).withText(Localization.translate("ic2.Transformer.gui.switch.mode3")));
		this.addElement(
			new ItemImage(this, 152, 67, () -> new ItemStack(Ic2Items.WRENCH))
				.withEnableHandler(() -> ((ContainerTransformer) this.menu).base.getMode() == TileEntityTransformer.Mode.redstone)
		);
		this.addElement(
			new ItemImage(this, 152, 87, () -> new ItemStack(Ic2Items.WRENCH))
				.withEnableHandler(() -> ((ContainerTransformer) this.menu).base.getMode() == TileEntityTransformer.Mode.stepdown)
		);
		this.addElement(
			new ItemImage(this, 152, 107, () -> new ItemStack(Ic2Items.WRENCH))
				.withEnableHandler(() -> ((ContainerTransformer) this.menu).base.getMode() == TileEntityTransformer.Mode.stepup)
		);
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}
}

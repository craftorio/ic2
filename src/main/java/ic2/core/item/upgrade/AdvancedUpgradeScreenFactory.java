package ic2.core.item.upgrade;

import com.google.common.base.Supplier;
import ic2.core.IC2;
import ic2.core.gui.EnumCycleHandler;
import ic2.core.gui.MouseButton;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.init.Localization;
import ic2.core.proxy.ClientEnvProxy;
import ic2.core.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AdvancedUpgradeScreenFactory implements ClientEnvProxy.ScreenFactory<DynamicContainer<HandHeldAdvancedUpgrade>>
{
	public AbstractContainerScreen<DynamicContainer<HandHeldAdvancedUpgrade>> create(
		DynamicContainer<HandHeldAdvancedUpgrade> container, Inventory playerInventory, Component title
	)
	{
		final DynamicGui<HandHeldAdvancedUpgrade> gui = DynamicGui.create(container, playerInventory, title);
		if (Util.inDev())
		{
			gui.addElement(
				new VanillaButton(gui, 10, 62, 50, 20, new EnumCycleHandler<NbtSettings>(NbtSettings.VALUES, container.base.nbt)
				{
					@Override
					public void onClick(MouseButton button)
					{
						super.onClick(button);
						container.base.nbt = this.getCurrentValue();
						IC2.network.get(false).sendHandHeldInvField(gui.getContainer(), "nbt");
					}
				})
					.withText("ic2.upgrade.advancedGUI.nbt")
					.withTooltip(
						new Supplier<String>()
						{
							private final String NBT = Localization.translate("ic2.upgrade.advancedGUI.nbt");

							public String get()
							{
								return Localization.translate(
									"ic2.upgrade.advancedGUI.nbt.desc",
									Localization.translate(container.base.nbt.name),
									ChatFormatting.GRAY,
									Localization.translate(container.base.nbt.name + ".desc", this.NBT)
								);
							}
						}
					)
			);
		}

		return gui;
	}
}

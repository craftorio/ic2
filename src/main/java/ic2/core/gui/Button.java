package ic2.core.gui;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.proxy.SideProxyClient;

import java.util.function.Supplier;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public abstract class Button<T extends Button<T>> extends GuiElement<T>
{
	private final IClickHandler handler;
	private Supplier<String> textProvider;
	private Supplier<ItemStack> iconProvider;

	protected Button(Ic2Gui<?> gui, int x, int y, int width, int height, IClickHandler handler)
	{
		super(gui, x, y, width, height);
		this.handler = handler;
	}

	public T withText(String text)
	{
		return this.withText(() -> text);
	}

	public T withText(Supplier<String> textProvider)
	{
		this.textProvider = textProvider;
		return (T) this;
	}

	public T withIcon(Supplier<ItemStack> iconProvider)
	{
		this.iconProvider = iconProvider;
		return (T) this;
	}

	protected int getTextColor(int mouseX, int mouseY)
	{
		return 14540253;
	}

	@Override
	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		if (this.textProvider != null)
		{
			String text = this.textProvider.get();
			if (text != null && !text.isEmpty())
			{
				text = Component.translatable(text).getString();
				this.gui.drawXYCenteredString(guiGraphics, this.x + this.width / 2, this.y + this.height / 2, text, this.getTextColor(mouseX, mouseY), true);
			}
		} else if (this.iconProvider != null)
		{
			ItemStack stack = this.iconProvider.get();
			if (stack != null)
			{
				this.gui.drawItem(this.x + (this.width - 16) / 2, this.y + (this.height - 16) / 2, stack);
			}
		}
	}

	@Override
	protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button)
	{
		SideProxyClient.mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		this.handler.onClick(button);
		return false;
	}
}

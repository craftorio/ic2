package ic2.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.init.Localization;
import ic2.core.proxy.SideProxyClient;

import java.util.function.Supplier;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public abstract class Button<T extends Button<T>> extends GuiElement<T>
{
	private static final int iconSize = 16;
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
		return this.withText(new Supplier<String>()
		{
			public String get()
			{
				return text;
			}
		});
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
	public void drawBackground(PoseStack matrices, int mouseX, int mouseY)
	{
		if (this.textProvider != null)
		{
			String text = this.textProvider.get();
			if (text != null && !text.isEmpty())
			{
				text = Localization.translate(text);
				this.gui.drawXYCenteredString(matrices, this.x + this.width / 2, this.y + this.height / 2, text, this.getTextColor(mouseX, mouseY), true);
			}
		} else if (this.iconProvider != null)
		{
			ItemStack stack = this.iconProvider.get();
			if (stack != null && stack.getItem() != null)
			{
				this.gui.drawItem(this.x + (this.width - 16) / 2, this.y + (this.height - 16) / 2, stack);
			}
		}
	}

	@Override
	protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button)
	{
		SideProxyClient.mc.m_91106_().m_120367_(SimpleSoundInstance.m_119752_(SoundEvents.f_12490_, 1.0F));
		this.handler.onClick(button);
		return false;
	}
}

package ic2.core.gui;

import ic2.core.Ic2Gui;

public class StickyVanillaButton extends VanillaButton
{
	protected boolean isOn = false;

	public StickyVanillaButton(Ic2Gui<?> gui, int x, int y, int width, int height, IClickHandler handler)
	{
		super(gui, x, y, width, height, handler);
	}

	public boolean isOn()
	{
		return this.isOn;
	}

	public void setOn(boolean on)
	{
		this.isOn = on;
	}

	public StickyVanillaButton withDisableHandler(IEnableHandler handler)
	{
		super.withDisableHandler(handler);
		return this;
	}

	public StickyVanillaButton withText(String text)
	{
		super.withText(text);
		return this;
	}

	public StickyVanillaButton withTooltip(String tooltip)
	{
		super.withTooltip(tooltip);
		return this;
	}

	@Override
	protected boolean isActive(int mouseX, int mouseY)
	{
		return this.isOn || super.isActive(mouseX, mouseY);
	}
}

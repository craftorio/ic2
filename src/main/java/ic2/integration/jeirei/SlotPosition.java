package ic2.integration.jeirei;

import ic2.core.gui.SlotGrid;

public record SlotPosition(int x, int y, SlotGrid.SlotStyle style)
{
	public SlotPosition(int x, int y)
	{
		this(x, y, SlotGrid.SlotStyle.Normal);
	}

	public SlotPosition(SlotPosition old, int x, int y)
	{
		this(old.x + x, old.y + y, old.style);
	}

}

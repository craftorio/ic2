package ic2.core.gui;

import ic2.core.Ic2Gui;

import java.util.ArrayList;
import java.util.List;

public class ScrollableList extends AbstractScrollingList<ScrollableList, ScrollableList.IListItem>
{
	public ScrollableList(Ic2Gui<?> gui, int x, int y, int width, int height)
	{
		this(gui, x, y, width, height, new ArrayList<>());
	}

	public ScrollableList(Ic2Gui<?> gui, int x, int y, int width, int height, List<ScrollableList.IListItem> items)
	{
		super(gui, x, y, width, height, items);
	}

	protected boolean onItemClick(ScrollableList.IListItem item, MouseButton button, int mouseX, int mouseY)
	{
		return item.onClick(button, mouseX, mouseY);
	}

	public interface IListItem extends AbstractScrollingList.IListItem
	{
		boolean onClick(MouseButton var1, int var2, int var3);
	}
}

package ic2.core.gui;

import ic2.core.Ic2Gui;

import java.util.List;

public class ScrollableSelectiveList extends AbstractScrollingList<ScrollableSelectiveList, ScrollableSelectiveList.ISelectableListItem>
{
	protected ScrollableSelectiveList.ISelectableListItem currentSelected;

	public ScrollableSelectiveList(Ic2Gui<?> gui, int x, int y, int width, int height)
	{
		super(gui, x, y, width, height);
	}

	public ScrollableSelectiveList(Ic2Gui<?> gui, int x, int y, int width, int height, List<ScrollableSelectiveList.ISelectableListItem> items)
	{
		super(gui, x, y, width, height, items);
	}

	public ScrollableSelectiveList setSelected(int index)
	{
		return this.setSelected(this.items.get(index));
	}

	public ScrollableSelectiveList setSelected(ScrollableSelectiveList.ISelectableListItem item)
	{
		if (this.currentSelected != null)
		{
			this.currentSelected.onDeselected();
		}

		assert this.items.contains(item);
		item.onSelected();
		return this;
	}

	public ScrollableSelectiveList clearSelected()
	{
		if (this.currentSelected != null)
		{
			this.currentSelected.onDeselected();
		}

		return this;
	}

	protected boolean onItemClick(ScrollableSelectiveList.ISelectableListItem item, MouseButton button, int mouseX, int mouseY)
	{
		return switch (button)
		{
			case left ->
			{
				if (this.currentSelected != item)
				{
					this.setSelected(item);
					yield true;
				}

				yield false;
			}
			case right ->
			{
				if (this.currentSelected == item)
				{
					item.onDeselected();
					yield true;
				}

				yield false;
			}
			default -> false;
		};
	}

	public interface ISelectableListItem extends AbstractScrollingList.IListItem
	{
		void onSelected();

		void onDeselected();
	}
}

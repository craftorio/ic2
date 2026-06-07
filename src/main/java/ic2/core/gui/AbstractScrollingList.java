package ic2.core.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;
import ic2.core.proxy.SideProxyClient;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

public abstract class AbstractScrollingList<T extends AbstractScrollingList<T, I>, I extends AbstractScrollingList.IListItem> extends GuiElement<T>
{
	protected float scroll = 0.0F;
	protected boolean scrolling = false;
	protected int mouseScrollOffset = -1;
	protected final List<I> items;
	private static final int SCROLL_BAR_WIDTH = 5;
	private static final int LIST_AREA_WIDTH = 7;
	private static final int ITEM_HEIGHT = 10;
	private static final int ITEM_GAP = 11;
	private static final int SCROLL_SPEED = 3;
	private static final boolean DEBUG_SCISSOR = false;

	public AbstractScrollingList(Ic2Gui<?> gui, int x, int y, int width, int height)
	{
		this(gui, x, y, width, height, new ArrayList<>());
	}

	protected AbstractScrollingList(Ic2Gui<?> gui, int x, int y, int width, int height, List<I> items)
	{
		super(gui, x, y, width, height);
		this.items = items;
	}

	public T addItem(I item)
	{
		assert item != null;
		this.items.add(item);
		this.scroll = Util.limit(this.scroll, 0.0F, this.getMaxScroll());
		return (T) this;
	}

	public T addItem(int index, I item)
	{
		assert item != null;
		this.items.add(index, item);
		this.scroll = Util.limit(this.scroll, 0.0F, this.getMaxScroll());
		return (T) this;
	}

	public T removeItem(I item)
	{
		assert item != null;
		this.items.remove(item);
		this.scroll = Util.limit(this.scroll, 0.0F, this.getMaxScroll());
		return (T) this;
	}

	public T removeItem(int index)
	{
		assert index >= 0 && index < this.items.size();
		this.items.remove(index);
		this.scroll = Util.limit(this.scroll, 0.0F, this.getMaxScroll());
		return (T) this;
	}

	@Override
	public void drawBackground(PoseStack matrices, int mouseX, int mouseY)
	{
		bindCommonTexture();
		this.gui.drawColoredRect(matrices, this.x, this.y, this.width, this.height, -16777216);
		this.gui.drawColoredRect(matrices, this.x + 1, this.y + 1, this.width - 7 - 1, this.height - 2, -6250336);
		this.gui.drawColoredRect(matrices, this.x + this.width - 7 + 1, this.y + 1, 5, this.height - 2, -6250241);
		int scrollStart;
		int scrollHeight;
		if (this.items.size() * 11 - 1 <= this.height - 2)
		{
			scrollStart = this.y + 1;
			scrollHeight = this.height - 2;
		} else
		{
			scrollHeight = Math.max(this.height - 2 - (this.items.size() * 11 - 1 - (this.height - 2)), 1);
			scrollStart = (int) Util.lerp(this.y + 1, this.y + this.height - 1 - scrollHeight, this.scroll / this.getMaxScroll());
		}

		this.gui.drawColoredRect(matrices, this.x + this.width - 7 + 1, scrollStart, 5, scrollHeight, -16777216);
	}

	@Override
	public void drawForeground(PoseStack matrices, int mouseX, int mouseY)
	{
		super.drawForeground(matrices, mouseX, mouseY);
		int left = this.gui.getX();
		int top = this.gui.getY();
		this.doScissor(left, top);
		int xOffset = this.x + 1 - left;
		int currentY = -((int) this.scroll) + 1;
		mouseX -= this.x + 1;
		mouseY -= this.y;

		for (I item : this.items)
		{
			if (currentY > -10)
			{
				item.draw(matrices, xOffset, this.y - top + currentY, this.width - 7 - 1, 10, mouseX, mouseY - currentY);
			}

			currentY += 11;
			if (currentY > 0)
			{
				int bottomGap = currentY - this.height;
				if (bottomGap >= 0)
				{
					break;
				}

				this.gui.drawColoredRect(matrices, xOffset, this.y - top + currentY - 1, this.width - 7, 1, -16777216);
				if (bottomGap == -1)
				{
					break;
				}
			}
		}

		assert GL11.glIsEnabled(3089);
		GL11.glDisable(3089);
	}

	private void doScissor(int GUIwidth, int GUIheight)
	{
		int left = GUIwidth + this.x + 1;
		int bottom = GUIheight + this.y + this.height - 1;
		int viewWidth = this.width - 7 - 1;
		int viewHeight = this.height - 2;
		Window window = SideProxyClient.mc.m_91268_();
		int scale = (int) window.m_85449_();
		GL11.glEnable(3089);
		GL11.glScissor(left * scale, window.m_85444_() - bottom * scale, viewWidth * scale, viewHeight * scale);
	}

	@Override
	protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button)
	{
		mouseX -= this.x;
		mouseY -= this.y;
		if (!this.items.isEmpty() && mouseX > 0 && mouseY > 0 && mouseY < this.height - 1)
		{
			if (mouseX < this.width - 7)
			{
				int index = (mouseY + (int) this.scroll) / 11;
				if (index >= 0 && index < this.items.size())
				{
					int clickX = mouseX - 1;
					int clickY = (mouseY + (int) this.scroll) % 11 - 1;
					if (clickY >= 0)
					{
						return this.onItemClick(this.items.get(index), button, clickX, clickY);
					}
				}
			} else if (mouseX > this.width - 7
				&& mouseX < this.width - 1
				&& mouseY >= Math.floor(this.scroll * this.getScrollScale())
				&& mouseY
				<= this.scroll * this.getScrollScale()
				+ (
				this.items.size() * 11 - 1 <= this.height - 2
					? this.height - 2
					: Math.max(this.height - 2 - (this.items.size() * 11 - 1 - (this.height - 2)), 1)
			))
			{
				this.mouseScrollOffset = mouseY;
				return this.scrolling = true;
			}
		}

		return false;
	}

	protected abstract boolean onItemClick(I var1, MouseButton var2, int var3, int var4);

	@Override
	public boolean onMouseDrag(int mouseX, int mouseY, MouseButton button, boolean onThis)
	{
		if (this.scrolling)
		{
			assert this.mouseScrollOffset >= 0;
			mouseY -= this.y;
			float startingScroll = this.scroll;
			float mouseMovement = this.scroll + (mouseY - this.mouseScrollOffset) * this.getInverseScrollScale();
			this.scroll = Util.limit(mouseMovement, 0.0F, this.getMaxScroll());
			if (!Util.isSimilar(mouseMovement, this.scroll))
			{
				if (!Util.isSimilar(startingScroll, this.scroll))
				{
					this.mouseScrollOffset = (int) (this.mouseScrollOffset + (this.scroll - startingScroll) * this.getScrollScale());
					assert this.mouseScrollOffset >= 0 : "Left the scroll bar dragging from " + startingScroll + " to " + this.scroll;
				}
			} else
			{
				this.mouseScrollOffset = mouseY;
				if (mouseY == 0)
				{
					this.scroll = 0.0F;
				}

				assert mouseY >= 0 : "Left the scroll bar dragging to " + mouseY;
			}

			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public boolean onMouseRelease(int mouseX, int mouseY, MouseButton button, boolean onThis)
	{
		if (this.scrolling)
		{
			this.scrolling = false;
			this.mouseScrollOffset = -1;
		}

		return false;
	}

	@Override
	public void onMouseScroll(int mouseX, int mouseY, ScrollDirection direction)
	{
		this.scroll = Util.limit(this.scroll + direction.multiplier * 3, 0.0F, this.getMaxScroll());
	}

	protected int getMaxScroll()
	{
		return Math.max(this.items.size() * 11 - 1 - (this.height - 2), 0);
	}

	private float getScrollScale()
	{
		int maxScroll = this.getMaxScroll();
		int scrollRoom = this.height - 2;
		return maxScroll <= scrollRoom ? 1.0F : (float) scrollRoom / maxScroll;
	}

	private float getInverseScrollScale()
	{
		int maxScroll = this.getMaxScroll();
		int scrollRoom = this.height - 2;
		return maxScroll <= scrollRoom ? 1.0F : (float) maxScroll / scrollRoom;
	}

	public interface IListItem
	{
		void draw(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7);
	}
}

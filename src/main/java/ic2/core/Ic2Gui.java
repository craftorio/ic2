package ic2.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.upgrade.UpgradeRegistry;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IKeyboardDependent;
import ic2.core.gui.MouseButton;
import ic2.core.gui.ScrollDirection;
import ic2.core.util.StackUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class Ic2Gui<T extends ContainerBase<? extends Container>> extends AbstractContainerScreen<T>
{
	public static final int textHeight = 8;
	protected static Runnable closeHandler;
	private boolean fixKeyEvents = false;
	private final Set<GuiElement.ImplementedMethod> elementMethods = EnumSet.noneOf(GuiElement.ImplementedMethod.class);
	private final Queue<Ic2Gui.Tooltip> queuedTooltips = new ArrayDeque<>();
	protected final List<GuiElement<?>> elements = new ArrayList<>();

	public Ic2Gui(T container, Inventory playerInventory, Component title)
	{
		this(container, playerInventory, title, 176, 166);
	}

	public Ic2Gui(T container, Inventory playerInventory, Component title, int ySize)
	{
		this(container, playerInventory, title, 176, ySize);
	}

	public Ic2Gui(T container, Inventory playerInventory, Component title, int xSize, int ySize)
	{
		super(container, playerInventory, title);
		this.imageHeight = ySize;
		this.imageWidth = xSize;
	}

	public final T getContainer()
	{
		return (T) this.menu;
	}

	public final int getX()
	{
		return this.f_97735_;
	}

	public final int getY()
	{
		return this.f_97736_;
	}

	public final Slot getFocusedSlot()
	{
		return this.f_97734_;
	}

	public void m_7856_()
	{
		super.m_7856_();

		for (GuiElement<?> element : this.elements)
		{
			if (element instanceof IKeyboardDependent)
			{
				this.f_96541_.f_91068_.m_90926_(true);
				this.fixKeyEvents = true;
				break;
			}
		}
	}

	public void m_6305_(PoseStack matrices, int mouseX, int mouseY, float partialTicks)
	{
		this.m_7333_(matrices);
		super.m_6305_(matrices, mouseX, mouseY, partialTicks);
		this.m_7025_(matrices, mouseX, mouseY);
	}

	public void m_181908_()
	{
		super.m_181908_();
		if (this.elementMethods.contains(GuiElement.ImplementedMethod.tick))
		{
			for (GuiElement<?> element : this.elements)
			{
				if (element.isEnabled())
				{
					element.tick();
				}
			}
		}
	}

	protected void m_7286_(PoseStack matrices, float delta, int mouseX, int mouseY)
	{
		mouseX -= this.f_97735_;
		mouseY -= this.f_97736_;
		this.drawBackgroundAndTitle(matrices, delta, mouseX, mouseY);
		if (((ContainerBase) this.menu).base instanceof IUpgradableBlock)
		{
			bindTexture(ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/infobutton.png"));
			this.drawTexturedRect(matrices, 3.0, 3.0, 10.0, 10.0, 0.0, 0.0);
		}

		if (this.elementMethods.contains(GuiElement.ImplementedMethod.drawBackground))
		{
			for (GuiElement<?> element : this.elements)
			{
				if (element.isEnabled())
				{
					element.drawBackground(matrices, mouseX, mouseY);
				}
			}
		}
	}

	protected void drawBackgroundAndTitle(PoseStack matrices, float partialTicks, int mouseX, int mouseY)
	{
		this.bindTexture();
		this.m_93228_(matrices, this.f_97735_, this.f_97736_, 0, 0, this.imageWidth, this.imageHeight);
		this.drawXCenteredString(matrices, this.imageWidth / 2, 6, this.f_96539_, 4210752, false);
	}

	protected final void m_7027_(PoseStack matrices, int mouseX, int mouseY)
	{
		this.drawForegroundLayer(matrices, mouseX - this.f_97735_, mouseY - this.f_97736_);
		this.flushTooltips(matrices);
	}

	protected void drawForegroundLayer(PoseStack matrices, int mouseX, int mouseY)
	{
		if (((ContainerBase) this.menu).base instanceof IUpgradableBlock)
		{
			this.handleUpgradeTooltip(mouseX, mouseY);
		}

		for (GuiElement<?> element : this.elements)
		{
			if (element.isEnabled())
			{
				element.drawForeground(matrices, mouseX, mouseY);
			}
		}
	}

	private void handleUpgradeTooltip(int mouseX, int mouseY)
	{
		int areaSize = 12;
		if (mouseX >= 0 && mouseX <= 12 && mouseY >= 0 && mouseY <= 12)
		{
			List<Component> text = new ArrayList<>();
			text.add(Component.m_237115_("ic2.generic.text.upgrade"));

			for (ItemStack stack : getCompatibleUpgrades((IUpgradableBlock) ((ContainerBase) this.menu).base))
			{
				text.add(stack.m_41786_().m_6881_().m_130940_(ChatFormatting.GRAY));
			}

			this.drawTooltip(mouseX, mouseY, text);
		}
	}

	private static List<ItemStack> getCompatibleUpgrades(IUpgradableBlock block)
	{
		List<ItemStack> ret = new ArrayList<>();
		Set<UpgradableProperty> properties = block.getUpgradableProperties();

		for (ItemStack stack : UpgradeRegistry.getUpgrades())
		{
			IUpgradeItem item = (IUpgradeItem) stack.getItem();
			if (item.isSuitableFor(stack, properties))
			{
				ret.add(stack);
			}
		}

		return ret;
	}

	public boolean m_6050_(double mouseX, double mouseY, double scrollDelta)
	{
		if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseScroll))
		{
			ScrollDirection direction;
			if (scrollDelta != 0.0)
			{
				direction = scrollDelta < 0.0 ? ScrollDirection.down : ScrollDirection.up;
			} else
			{
				direction = ScrollDirection.stopped;
			}

			for (GuiElement<?> element : this.elements)
			{
				if (element.isEnabled() && element.contains((int) mouseX, (int) mouseY))
				{
					element.onMouseScroll((int) mouseX, (int) mouseY, direction);
				}
			}
		}

		return super.m_6050_(mouseX, mouseY, scrollDelta);
	}

	public boolean m_6375_(double mouseX, double mouseY, int mouseButton)
	{
		if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseClick))
		{
			MouseButton button = MouseButton.get(mouseButton);
			if (button != null)
			{
				boolean handled = false;
				mouseX -= this.f_97735_;
				mouseY -= this.f_97736_;

				for (GuiElement<?> element : this.elements)
				{
					if (element.isEnabled())
					{
						handled |= element.onMouseClick((int) mouseX, (int) mouseY, button, element.contains((int) mouseX, (int) mouseY));
					}
				}

				if (handled)
				{
					return true;
				}

				mouseX += this.f_97735_;
				mouseY += this.f_97736_;
			}
		}

		return super.m_6375_(mouseX, mouseY, mouseButton);
	}

	public boolean m_7979_(double mouseX, double mouseY, int clickedMouseButton, double deltaX, double deltaY)
	{
		if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseDrag))
		{
			MouseButton button = MouseButton.get(clickedMouseButton);
			if (button != null)
			{
				boolean handled = false;
				mouseX -= this.f_97735_;
				mouseY -= this.f_97736_;

				for (GuiElement<?> element : this.elements)
				{
					if (element.isEnabled())
					{
						handled |= element.onMouseDrag((int) mouseX, (int) mouseY, button, element.contains((int) mouseX, (int) mouseY));
					}
				}

				if (handled)
				{
					return true;
				}

				mouseX += this.f_97735_;
				mouseY += this.f_97736_;
			}
		}

		return super.m_7979_(mouseX, mouseY, clickedMouseButton, deltaX, deltaY);
	}

	public boolean m_6348_(double mouseX, double mouseY, int state)
	{
		if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseRelease))
		{
			MouseButton button = MouseButton.get(state);
			if (button != null)
			{
				boolean handled = false;
				mouseX -= this.f_97735_;
				mouseY -= this.f_97736_;

				for (GuiElement<?> element : this.elements)
				{
					if (element.isEnabled())
					{
						handled |= element.onMouseRelease((int) mouseX, (int) mouseY, button, element.contains((int) mouseX, (int) mouseY));
					}
				}

				if (handled)
				{
					return true;
				}

				mouseX += this.f_97735_;
				mouseY += this.f_97736_;
			}
		}

		return super.m_6348_(mouseX, mouseY, state);
	}

	public boolean m_5534_(char typedChar, int keyCode)
	{
		if (this.elementMethods.contains(GuiElement.ImplementedMethod.onKeyTyped))
		{
			boolean handled = false;

			for (GuiElement<?> element : this.elements)
			{
				if (element.isEnabled())
				{
					handled |= element.onKeyTyped(typedChar, keyCode);
				}
			}

			if (handled)
			{
				return true;
			}
		}

		return super.m_5534_(typedChar, keyCode);
	}

	public void m_7861_()
	{
		super.m_7861_();
		if (this.fixKeyEvents)
		{
			this.f_96541_.f_91068_.m_90926_(false);
		}

		if (closeHandler != null)
		{
			closeHandler.run();
		}
	}

	public void drawTexturedRect(PoseStack matrices, double x, double y, double width, double height, double texX, double texY)
	{
		this.drawTexturedRect(matrices, x, y, width, height, texX, texY, false);
	}

	public void drawTexturedRect(PoseStack matrices, double x, double y, double width, double height, double texX, double texY, boolean mirrorX)
	{
		this.drawTexturedRect(matrices, x, y, width, height, texX / 256.0, texY / 256.0, (texX + width) / 256.0, (texY + height) / 256.0, mirrorX);
	}

	public void drawTexturedRect(
		PoseStack matrices, double x, double y, double width, double height, double uS, double vS, double uE, double vE, boolean mirrorX
	)
	{
		x += this.f_97735_;
		y += this.f_97736_;
		double xE = x + width;
		double yE = y + height;
		if (mirrorX)
		{
			double tmp = uS;
			uS = uE;
			uE = tmp;
		}

		Matrix4f matrix = matrices.m_85850_().m_85861_();
		int z = this.m_93252_();
		RenderSystem.m_157427_(GameRenderer::m_172817_);
		BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
		buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);
		buffer.m_85982_(matrix, (float) x, (float) y, z).m_7421_((float) uS, (float) vS).m_5752_();
		buffer.m_85982_(matrix, (float) x, (float) yE, z).m_7421_((float) uS, (float) vE).m_5752_();
		buffer.m_85982_(matrix, (float) xE, (float) yE, z).m_7421_((float) uE, (float) vE).m_5752_();
		buffer.m_85982_(matrix, (float) xE, (float) y, z).m_7421_((float) uE, (float) vS).m_5752_();
		BufferUploader.m_231202_(buffer.m_231175_());
	}

	public void drawSprite(
		PoseStack matrices,
		double x,
		double y,
		double width,
		double height,
		TextureAtlasSprite sprite,
		int color,
		double scale,
		boolean fixRight,
		boolean fixBottom
	)
	{
		if (sprite == null)
		{
			sprite = ((TextureAtlas) this.f_96541_.m_91097_().m_118506_(InventoryMenu.f_39692_)).m_118316_(MissingTextureAtlasSprite.m_118071_());
		}

		x += this.f_97735_;
		y += this.f_97736_;
		scale *= 16.0;
		double spriteUS = sprite.m_118409_();
		double spriteVS = sprite.m_118411_();
		double spriteWidth = sprite.m_118410_() - spriteUS;
		double spriteHeight = sprite.m_118412_() - spriteVS;
		int a = color >>> 24 & 0xFF;
		int r = color >>> 16 & 0xFF;
		int g = color >>> 8 & 0xFF;
		int b = color & 0xFF;
		Matrix4f matrix = matrices.m_85850_().m_85861_();
		int z = this.m_93252_();
		RenderSystem.m_157427_(GameRenderer::m_172820_);
		BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
		buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85819_);
		double xS = x;

		while (xS < x + width)
		{
			double maxWidth;
			double uS;
			if (xS == x && fixRight && (maxWidth = width % scale) > 0.0)
			{
				uS = spriteUS + spriteWidth * (1.0 - maxWidth / scale);
			} else
			{
				maxWidth = scale;
				uS = spriteUS;
			}

			double xE = Math.min(xS + maxWidth, x + width);
			double uE = uS + (xE - xS) / scale * spriteWidth;
			double yS = y;

			while (yS < y + height)
			{
				double maxHeight;
				double vS;
				if (yS == y && fixBottom && (maxHeight = height % scale) > 0.0)
				{
					vS = spriteVS + spriteHeight * (1.0 - maxHeight / scale);
				} else
				{
					maxHeight = scale;
					vS = spriteVS;
				}

				double yE = Math.min(yS + maxHeight, y + height);
				double vE = vS + (yE - yS) / scale * spriteHeight;
				buffer.m_85982_(matrix, (float) xS, (float) yS, z).m_7421_((float) uS, (float) vS).m_6122_(r, g, b, a).m_5752_();
				buffer.m_85982_(matrix, (float) xS, (float) yE, z).m_7421_((float) uS, (float) vE).m_6122_(r, g, b, a).m_5752_();
				buffer.m_85982_(matrix, (float) xE, (float) yE, z).m_7421_((float) uE, (float) vE).m_6122_(r, g, b, a).m_5752_();
				buffer.m_85982_(matrix, (float) xE, (float) yS, z).m_7421_((float) uE, (float) vS).m_6122_(r, g, b, a).m_5752_();
				yS += maxHeight;
			}

			xS += maxWidth;
		}

		BufferUploader.m_231202_(buffer.m_231175_());
	}

	public void drawItem(int x, int y, ItemStack stack)
	{
		this.f_96542_.m_115123_(stack, this.f_97735_ + x, this.f_97736_ + y);
	}

	public void drawItemStack(int x, int y, ItemStack stack)
	{
		this.drawItem(x, y, stack);
		this.f_96542_.m_115174_(this.f_96547_, stack, this.f_97735_ + x, this.f_97736_ + y, null);
	}

	public void drawColoredRect(PoseStack matrices, int x, int y, int width, int height, int color)
	{
		x += this.f_97735_;
		y += this.f_97736_;
		int alpha = color >>> 24;
		boolean blend = alpha != 255 && alpha != 0;
		Matrix4f matrix = matrices.m_85850_().m_85861_();
		int xE = x + width;
		int yE = y + height;
		int z = this.m_93252_();
		if (blend)
		{
			RenderSystem.m_69478_();
		}

		RenderSystem.m_157427_(GameRenderer::m_172811_);
		BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
		buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
		buffer.m_85982_(matrix, x, y, z).m_193479_(color).m_5752_();
		buffer.m_85982_(matrix, x, yE, z).m_193479_(color).m_5752_();
		buffer.m_85982_(matrix, xE, yE, z).m_193479_(color).m_5752_();
		buffer.m_85982_(matrix, xE, y, z).m_193479_(color).m_5752_();
		BufferUploader.m_231202_(buffer.m_231175_());
		if (blend)
		{
			RenderSystem.m_69461_();
		}
	}

	public int drawString(PoseStack matrices, int x, int y, String text, int color)
	{
		return this.f_96547_.m_92883_(matrices, text, x, y, color);
	}

	public int drawString(PoseStack matrices, int x, int y, String text, int color, boolean shadow)
	{
		return !shadow
			? this.f_96547_.m_92883_(matrices, text, this.f_97735_ + x, this.f_97736_ + y, color) - this.f_97735_
			: this.f_96547_.m_92750_(matrices, text, this.f_97735_ + x, this.f_97736_ + y, color) - this.f_97735_;
	}

	public void drawTrimmedString(PoseStack matrices, int x, int y, String text, int maxWidth, int color)
	{
		MultiLineLabel.m_94341_(this.f_96547_, Component.m_237113_(text), maxWidth).m_6508_(matrices, x, y, 10, color);
	}

	public void drawXCenteredString(PoseStack matrices, int x, int y, String text, int color, boolean shadow)
	{
		this.drawCenteredString(matrices, x, y, text, color, shadow, true, false);
	}

	public void drawXCenteredString(PoseStack matrices, int x, int y, Component text, int color, boolean shadow)
	{
		this.drawCenteredString(matrices, x, y, text, color, shadow, true, false);
	}

	public void drawXYCenteredString(PoseStack matrices, int x, int y, String text, int color, boolean shadow)
	{
		this.drawCenteredString(matrices, x, y, text, color, shadow, true, true);
	}

	public void drawXYCenteredString(PoseStack matrices, int x, int y, Component text, int color, boolean shadow)
	{
		this.drawCenteredString(matrices, x, y, text, color, shadow, true, true);
	}

	public void drawCenteredString(PoseStack matrices, int x, int y, String text, int color, boolean shadow, boolean centerX, boolean centerY)
	{
		if (centerX)
		{
			x -= this.getStringWidth(text) / 2;
		}

		if (centerY)
		{
			y -= 4;
		}

		this.f_96547_.m_92883_(matrices, text, this.f_97735_ + x, this.f_97736_ + y, color);
	}

	public void drawCenteredString(PoseStack matrices, int x, int y, Component text, int color, boolean shadow, boolean centerX, boolean centerY)
	{
		if (centerX)
		{
			x -= this.getStringWidth(text) / 2;
		}

		if (centerY)
		{
			y -= 4;
		}

		this.f_96547_.m_92889_(matrices, text, this.f_97735_ + x, this.f_97736_ + y, color);
	}

	public int getStringWidth(String text)
	{
		return this.f_96547_.m_92895_(text);
	}

	public int getStringWidth(Component text)
	{
		return this.f_96547_.m_92852_(text);
	}

	public String trimStringToWidth(String text, int width)
	{
		return this.f_96547_.m_92837_(text, width, false);
	}

	public String trimStringToWidthReverse(String text, int width)
	{
		return this.f_96547_.m_92837_(text, width, true);
	}

	public void drawTooltip(int x, int y, List<Component> text)
	{
		this.queuedTooltips.add(new Ic2Gui.Tooltip(text, x, y));
	}

	public void drawTooltip(PoseStack matrices, int x, int y, ItemStack stack)
	{
		assert !StackUtil.isEmpty(stack);
		this.m_6057_(matrices, stack, x, y);
	}

	protected void flushTooltips(PoseStack matrices)
	{
		for (Ic2Gui.Tooltip tooltip : this.queuedTooltips)
		{
			this.m_169388_(matrices, tooltip.text, Optional.empty(), tooltip.x, tooltip.y);
		}

		this.queuedTooltips.clear();
	}

	protected void addElement(GuiElement<?> element)
	{
		this.elements.add(element);
		this.elementMethods.addAll(element.getImplementedMethods());
	}

	protected final void bindTexture()
	{
		bindTexture(this.getTexture());
	}

	public static void bindTexture(ResourceLocation id)
	{
		RenderSystem.m_157456_(0, id);
	}

	protected IClickHandler createEventSender(int event)
	{
		if (((ContainerBase) this.menu).base instanceof BlockEntity)
		{
			return new IClickHandler()
			{
				@Override
				public void onClick(MouseButton button)
				{
					IC2.network.get(false).initiateClientTileEntityEvent((BlockEntity) ((ContainerBase) Ic2Gui.this.menu).base, event);
				}
			};
		} else
		{
			throw new IllegalArgumentException("not applicable for " + ((ContainerBase) this.menu).base);
		}
	}

	protected abstract ResourceLocation getTexture();

	private static class Tooltip
	{
		final int x;
		final int y;
		final List<Component> text;

		Tooltip(List<Component> text, int x, int y)
		{
			this.text = text;
			this.x = x;
			this.y = y;
		}
	}
}

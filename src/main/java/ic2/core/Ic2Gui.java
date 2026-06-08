package ic2.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
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
import net.minecraft.client.gui.GuiGraphics;
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
import org.joml.Matrix4f;

public abstract class Ic2Gui<T extends ContainerBase<? extends Container>> extends AbstractContainerScreen<T>
{
	public static final int textHeight = 8;
	protected static Runnable closeHandler;
	private boolean fixKeyEvents = false;
	private GuiGraphics guiGraphics;
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
		return this.menu;
	}

	public final int getX()
	{
		return this.leftPos;
	}

	public final int getY()
	{
		return this.topPos;
	}

	public final Slot getFocusedSlot()
	{
		return this.hoveredSlot;
	}

	public void init()
	{
		super.init();

		for (GuiElement<?> element : this.elements)
		{
			if (element instanceof IKeyboardDependent)
			{
				// TODO: In 1.20.1, keyboardHandler.setSendRepeatsToGui was removed.
				// this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
				this.fixKeyEvents = true;
				break;
			}
		}
	}

	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
	{
		this.guiGraphics = guiGraphics;
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	public void containerTick()
	{
		super.containerTick();
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

	protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY)
	{
		mouseX -= this.leftPos;
		mouseY -= this.topPos;
		this.drawBackgroundAndTitle(guiGraphics, delta, mouseX, mouseY);
		if (this.menu.base instanceof IUpgradableBlock)
		{
			bindTexture(ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/infobutton.png"));
			this.drawTexturedRect(guiGraphics.pose(), 3.0, 3.0, 10.0, 10.0, 0.0, 0.0);
		}

		if (this.elementMethods.contains(GuiElement.ImplementedMethod.drawBackground))
		{
			for (GuiElement<?> element : this.elements)
			{
				if (element.isEnabled())
				{
					element.drawBackground(guiGraphics, mouseX, mouseY);
				}
			}
		}
	}

	protected void drawBackgroundAndTitle(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY)
	{
		this.bindTexture();
		guiGraphics.blit(this.getTextureLocation(), this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		this.drawXCenteredString(guiGraphics, this.imageWidth / 2, 6, this.title, 4210752, false);
	}

	protected final void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		this.drawForegroundLayer(guiGraphics, mouseX - this.leftPos, mouseY - this.topPos);
		this.flushTooltips(guiGraphics);
	}

	protected void drawForegroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		if (this.menu.base instanceof IUpgradableBlock)
		{
			this.handleUpgradeTooltip(mouseX, mouseY);
		}

		for (GuiElement<?> element : this.elements)
		{
			if (element.isEnabled())
			{
				element.drawForeground(guiGraphics, mouseX, mouseY);
			}
		}
	}

	private void handleUpgradeTooltip(int mouseX, int mouseY)
	{
		int areaSize = 12;
		if (mouseX >= 0 && mouseX <= 12 && mouseY >= 0 && mouseY <= 12)
		{
			List<Component> text = new ArrayList<>();
			text.add(Component.translatable("ic2.generic.text.upgrade"));

			for (ItemStack stack : getCompatibleUpgrades((IUpgradableBlock) this.menu.base))
			{
				text.add(stack.getHoverName().copy().withStyle(ChatFormatting.GRAY));
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

	public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta)
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

		return super.mouseScrolled(mouseX, mouseY, scrollDelta);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
	{
		if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseClick))
		{
			MouseButton button = MouseButton.get(mouseButton);
			if (button != null)
			{
				boolean handled = false;
				mouseX -= this.leftPos;
				mouseY -= this.topPos;

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

				mouseX += this.leftPos;
				mouseY += this.topPos;
			}
		}

		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double deltaX, double deltaY)
	{
		if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseDrag))
		{
			MouseButton button = MouseButton.get(clickedMouseButton);
			if (button != null)
			{
				boolean handled = false;
				mouseX -= this.leftPos;
				mouseY -= this.topPos;

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

				mouseX += this.leftPos;
				mouseY += this.topPos;
			}
		}

		return super.mouseDragged(mouseX, mouseY, clickedMouseButton, deltaX, deltaY);
	}

	public boolean mouseReleased(double mouseX, double mouseY, int state)
	{
		if (this.elementMethods.contains(GuiElement.ImplementedMethod.onMouseRelease))
		{
			MouseButton button = MouseButton.get(state);
			if (button != null)
			{
				boolean handled = false;
				mouseX -= this.leftPos;
				mouseY -= this.topPos;

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

				mouseX += this.leftPos;
				mouseY += this.topPos;
			}
		}

		return super.mouseReleased(mouseX, mouseY, state);
	}

	public boolean charTyped(char typedChar, int keyCode)
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

		return super.charTyped(typedChar, keyCode);
	}

	public void removed()
	{
		super.removed();
		if (this.fixKeyEvents)
		{
			// TODO: In 1.20.1, keyboardHandler.setSendRepeatsToGui was removed.
			// this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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
		x += this.leftPos;
		y += this.topPos;
		double xE = x + width;
		double yE = y + height;
		if (mirrorX)
		{
			double tmp = uS;
			uS = uE;
			uE = tmp;
		}

		Matrix4f matrix = matrices.last().pose();
		int z = 0;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.vertex(matrix, (float) x, (float) y, z).uv((float) uS, (float) vS).endVertex();
		buffer.vertex(matrix, (float) x, (float) yE, z).uv((float) uS, (float) vE).endVertex();
		buffer.vertex(matrix, (float) xE, (float) yE, z).uv((float) uE, (float) vE).endVertex();
		buffer.vertex(matrix, (float) xE, (float) y, z).uv((float) uE, (float) vS).endVertex();
		BufferUploader.drawWithShader(buffer.end());
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
			sprite = ((TextureAtlas) this.minecraft.getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS)).getSprite(MissingTextureAtlasSprite.getLocation());
		}

		x += this.leftPos;
		y += this.topPos;
		scale *= 16.0;
		double spriteUS = sprite.getU0();
		double spriteVS = sprite.getV0();
		double spriteWidth = sprite.getU1() - spriteUS;
		double spriteHeight = sprite.getV1() - spriteVS;
		int a = color >>> 24 & 0xFF;
		int r = color >>> 16 & 0xFF;
		int g = color >>> 8 & 0xFF;
		int b = color & 0xFF;
		Matrix4f matrix = matrices.last().pose();
		int z = 0;
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
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
				buffer.vertex(matrix, (float) xS, (float) yS, z).uv((float) uS, (float) vS).color(r, g, b, a).endVertex();
				buffer.vertex(matrix, (float) xS, (float) yE, z).uv((float) uS, (float) vE).color(r, g, b, a).endVertex();
				buffer.vertex(matrix, (float) xE, (float) yE, z).uv((float) uE, (float) vE).color(r, g, b, a).endVertex();
				buffer.vertex(matrix, (float) xE, (float) yS, z).uv((float) uE, (float) vS).color(r, g, b, a).endVertex();
				yS += maxHeight;
			}

			xS += maxWidth;
		}

		BufferUploader.drawWithShader(buffer.end());
	}

	public void drawItem(int x, int y, ItemStack stack)
	{
		if (this.guiGraphics != null)
		{
			this.guiGraphics.renderItem(stack, this.leftPos + x, this.topPos + y);
		}
	}

	public void drawItemStack(int x, int y, ItemStack stack)
	{
		this.drawItem(x, y, stack);
		if (this.guiGraphics != null)
		{
			this.guiGraphics.renderItemDecorations(this.font, stack, this.leftPos + x, this.topPos + y, null);
		}
	}

	public void drawColoredRect(PoseStack matrices, int x, int y, int width, int height, int color)
	{
		x += this.leftPos;
		y += this.topPos;
		int alpha = color >>> 24;
		boolean blend = alpha != 255 && alpha != 0;
		Matrix4f matrix = matrices.last().pose();
		int xE = x + width;
		int yE = y + height;
		int z = 0;
		if (blend)
		{
			RenderSystem.enableBlend();
		}

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(matrix, x, y, z).color(color).endVertex();
		buffer.vertex(matrix, x, yE, z).color(color).endVertex();
		buffer.vertex(matrix, xE, yE, z).color(color).endVertex();
		buffer.vertex(matrix, xE, y, z).color(color).endVertex();
		BufferUploader.drawWithShader(buffer.end());
		if (blend)
		{
			RenderSystem.disableBlend();
		}
	}

	public int drawString(GuiGraphics guiGraphics, int x, int y, String text, int color)
	{
		return guiGraphics.drawString(this.font, text, x, y, color);
	}

	public int drawString(GuiGraphics guiGraphics, int x, int y, String text, int color, boolean shadow)
	{
		return guiGraphics.drawString(this.font, text, this.leftPos + x, this.topPos + y, color, shadow) - this.leftPos;
	}

	public void drawTrimmedString(GuiGraphics guiGraphics, int x, int y, String text, int maxWidth, int color)
	{
		MultiLineLabel.create(this.font, Component.literal(text), maxWidth).renderLeftAlignedNoShadow(guiGraphics, x, y, 10, color);
	}

	public void drawXCenteredString(GuiGraphics guiGraphics, int x, int y, String text, int color, boolean shadow)
	{
		this.drawCenteredString(guiGraphics, x, y, text, color, shadow, true, false);
	}

	public void drawXCenteredString(GuiGraphics guiGraphics, int x, int y, Component text, int color, boolean shadow)
	{
		this.drawCenteredString(guiGraphics, x, y, text, color, shadow, true, false);
	}

	public void drawXYCenteredString(GuiGraphics guiGraphics, int x, int y, String text, int color, boolean shadow)
	{
		this.drawCenteredString(guiGraphics, x, y, text, color, shadow, true, true);
	}

	public void drawXYCenteredString(GuiGraphics guiGraphics, int x, int y, Component text, int color, boolean shadow)
	{
		this.drawCenteredString(guiGraphics, x, y, text, color, shadow, true, true);
	}

	public void drawCenteredString(GuiGraphics guiGraphics, int x, int y, String text, int color, boolean shadow, boolean centerX, boolean centerY)
	{
		if (centerX)
		{
			x -= this.getStringWidth(text) / 2;
		}

		if (centerY)
		{
			y -= 4;
		}

		guiGraphics.drawString(this.font, text, this.leftPos + x, this.topPos + y, color, shadow);
	}

	public void drawCenteredString(GuiGraphics guiGraphics, int x, int y, Component text, int color, boolean shadow, boolean centerX, boolean centerY)
	{
		if (centerX)
		{
			x -= this.getStringWidth(text) / 2;
		}

		if (centerY)
		{
			y -= 4;
		}

		guiGraphics.drawString(this.font, text, this.leftPos + x, this.topPos + y, color, shadow);
	}

	public int getStringWidth(String text)
	{
		return this.font.width(text);
	}

	public int getStringWidth(Component text)
	{
		return this.font.width(text);
	}

	public String trimStringToWidth(String text, int width)
	{
		return this.font.plainSubstrByWidth(text, width, false);
	}

	public String trimStringToWidthReverse(String text, int width)
	{
		return this.font.plainSubstrByWidth(text, width, true);
	}

	public void drawTooltip(int x, int y, List<Component> text)
	{
		this.queuedTooltips.add(new Ic2Gui.Tooltip(text, x, y));
	}

	public void drawTooltip(GuiGraphics guiGraphics, int x, int y, ItemStack stack)
	{
		assert !StackUtil.isEmpty(stack);
		guiGraphics.renderTooltip(this.font, stack, x, y);
	}

	protected void flushTooltips(GuiGraphics guiGraphics)
	{
		for (Ic2Gui.Tooltip tooltip : this.queuedTooltips)
		{
			guiGraphics.renderTooltip(this.font, tooltip.text, java.util.Optional.empty(), tooltip.x, tooltip.y);
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
		bindTexture(this.getTextureLocation());
	}

	public static void bindTexture(ResourceLocation id)
	{
		RenderSystem.setShaderTexture(0, id);
	}

	protected IClickHandler createEventSender(int event)
	{
		if (this.menu.base instanceof BlockEntity)
		{
			return new IClickHandler()
			{
				@Override
				public void onClick(MouseButton button)
				{
					IC2.network.get(false).initiateClientTileEntityEvent((BlockEntity) Ic2Gui.this.menu.base, event);
				}
			};
		} else
		{
			throw new IllegalArgumentException("not applicable for " + ((ContainerBase) this.menu).base);
		}
	}

	protected abstract ResourceLocation getTextureLocation();

	private record Tooltip(List<Component> text, int x, int y)
		{
		}
}

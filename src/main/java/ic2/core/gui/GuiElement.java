package ic2.core.gui;

import com.google.common.base.Suppliers;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.Ic2Gui;
import ic2.core.gui.dynamic.TextProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;

public abstract class GuiElement<T extends GuiElement<T>>
{
	protected static final int hoverColor = -2130706433;
	public static final ResourceLocation commonTexture = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/common.png");
	private static final Map<Class<?>, Set<GuiElement.ImplementedMethod>> IMPLEMENTED_METHOD_CACHE = new IdentityHashMap<>();
	protected final Ic2Gui<?> gui;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	private IEnableHandler enableHandler;
	private Supplier<String> tooltipProvider;

	protected GuiElement(Ic2Gui<?> gui, int x, int y, int width, int height)
	{
		if (width < 0)
		{
			throw new IllegalArgumentException("negative width");
		}

		if (height < 0)
		{
			throw new IllegalArgumentException("negative height");
		}

		this.gui = gui;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public final boolean isEnabled()
	{
		return this.enableHandler == null || this.enableHandler.isEnabled();
	}

	public boolean contains(int x, int y)
	{
		return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
	}

	public T withEnableHandler(IEnableHandler enableHandler)
	{
		this.enableHandler = enableHandler;
		return (T) this;
	}

	public T withTooltip(String tooltip)
	{
		return this.withTooltip(Suppliers.ofInstance(tooltip));
	}

	public T withTooltip(Supplier<String> tooltipProvider)
	{
		this.tooltipProvider = tooltipProvider;
		return (T) this;
	}

	public void tick()
	{
	}

	public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
	}

	public void drawForeground(GuiGraphics guiGraphics, int mouseX, int mouseY)
	{
		if (this.contains(mouseX, mouseY) && !this.suppressTooltip(mouseX, mouseY))
		{
			List<Component> lines = this.getToolTip();
			if (this.tooltipProvider != null)
			{
				String tooltip = this.tooltipProvider.get();
				if (tooltip != null && !tooltip.isEmpty())
				{
					addLines(lines, tooltip);
				}
			}

			if (!lines.isEmpty())
			{
				this.gui.drawTooltip(mouseX, mouseY, lines);
			}
		}
	}

	private static void addLines(List<Component> list, String str)
	{
		int startPos = 0;

		int pos;
		while ((pos = str.indexOf(10, startPos)) != -1)
		{
			list.add(processText(str.substring(startPos, pos)));
			startPos = pos + 1;
		}

		if (startPos == 0)
		{
			list.add(processText(str));
		} else
		{
			list.add(processText(str.substring(startPos)));
		}
	}

	public boolean onMouseClick(int mouseX, int mouseY, MouseButton button, boolean onThis)
	{
		return onThis && this.onMouseClick(mouseX, mouseY, button);
	}

	protected boolean onMouseClick(int mouseX, int mouseY, MouseButton button)
	{
		return false;
	}

	public boolean onMouseDrag(int mouseX, int mouseY, MouseButton button, boolean onThis)
	{
		return onThis && this.onMouseDrag(mouseX, mouseY, button);
	}

	protected boolean onMouseDrag(int mouseX, int mouseY, MouseButton button)
	{
		return false;
	}

	public boolean onMouseRelease(int mouseX, int mouseY, MouseButton button, boolean onThis)
	{
		return onThis && this.onMouseRelease(mouseX, mouseY, button);
	}

	protected boolean onMouseRelease(int mouseX, int mouseY, MouseButton button)
	{
		return false;
	}

	public void onMouseScroll(int mouseX, int mouseY, ScrollDirection direction)
	{
	}

	public boolean onKeyTyped(char typedChar, int keyCode)
	{
		return false;
	}

	protected boolean suppressTooltip(int mouseX, int mouseY)
	{
		return false;
	}

	protected List<Component> getToolTip()
	{
		return new ArrayList<>();
	}

	protected static Component processText(String text)
	{
		return Component.translatable(text);
	}

	protected final Container getBase()
	{
		return this.gui.getContainer().base;
	}

	protected final Map<String, TextProvider.ITextProvider> getTokens()
	{
		Map<String, TextProvider.ITextProvider> ret = new HashMap<>();
		ret.put("name", TextProvider.of(this.gui.getTitle()));
		return ret;
	}

	protected static void bindTexture(ResourceLocation texture)
	{
		Ic2Gui.bindTexture(texture);
	}

	public static void bindCommonTexture()
	{
		Ic2Gui.bindTexture(commonTexture);
	}

	protected static void bindBlockTexture()
	{
		Ic2Gui.bindTexture(InventoryMenu.BLOCK_ATLAS);
	}

	protected static TextureAtlas getBlockTextureMap()
	{
		return (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS);
	}

	public final Set<GuiElement.ImplementedMethod> getImplementedMethods()
	{
		Class<?> cls = this.getClass();
		Set<GuiElement.ImplementedMethod> ret = IMPLEMENTED_METHOD_CACHE.get(cls);
		if (ret == null)
		{
			ret = EnumSet.noneOf(GuiElement.ImplementedMethod.class);

			for (Class<?> curCls = cls; curCls != GuiElement.class; curCls = curCls.getSuperclass())
			{
				for (Method method : curCls.getDeclaredMethods())
				{
					if ((method.getModifiers() & 10) == 0)
					{
						GuiElement.ImplementedMethod implMethod = GuiElement.ImplementedMethod.LOOKUP.get(method.getName());
						if (implMethod != null && !ret.contains(implMethod) && !method.isAnnotationPresent(GuiElement.SkippedMethod.class))
						{
							ret.add(implMethod);
						}
					}
				}
			}

			IMPLEMENTED_METHOD_CACHE.put(cls, ret);
		}

		return ret;
	}

	public enum ImplementedMethod
	{
		tick,
		drawBackground,
		drawForeground,
		onMouseClick,
		onMouseDrag,
		onMouseRelease,
		onMouseScroll,
		onKeyTyped;

		static final Map<String, GuiElement.ImplementedMethod> LOOKUP = createLookup();

		private static Map<String, GuiElement.ImplementedMethod> createLookup()
		{
			GuiElement.ImplementedMethod[] values = values();
			Map<String, GuiElement.ImplementedMethod> ret = new HashMap<>(values.length);

			for (GuiElement.ImplementedMethod m : values)
			{
				ret.put(m.name(), m);
			}

			return ret;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface SkippedMethod
	{
	}
}

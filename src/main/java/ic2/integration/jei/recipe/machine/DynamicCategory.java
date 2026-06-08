package ic2.integration.jei.recipe.machine;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.dynamic.GuiEnvironment;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.util.Tuple;
import ic2.integration.jeirei.SlotPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class DynamicCategory extends IORecipeCategory
{
	protected final int xOffset;
	protected final int yOffset;
	protected final List<Tuple.T2<IDrawable, SlotPosition>> elements = new ArrayList<>();
	private final List<SlotPosition> inputSlots = new ArrayList<>();
	private final List<SlotPosition> outputSlots = new ArrayList<>();
	private final IDrawable background;
	private final RecipeType<IORecipeWrapper> recipeType;

	public DynamicCategory(Ic2TileEntityBlock teBlock, RecipeType<IORecipeWrapper> recipeType, IGuiHelper guiHelper)
	{
		super(teBlock);
		this.recipeType = recipeType;
		this.initializeWidgets(guiHelper, GuiParser.parse(BuiltInRegistries.BLOCK.getKey(teBlock), teBlock.getDummyTe().getClass()));
		int minX = 1000;
		int minY = 1000;
		int maxX = -1000;
		int maxY = -1000;

		for (Tuple.T2<IDrawable, SlotPosition> element : this.elements)
		{
			minX = Math.min(minX, element.b.x());
			minY = Math.min(minY, element.b.y());
			maxX = Math.max(maxX, element.b.x() + element.a.getWidth());
			maxY = Math.max(maxY, element.b.y() + element.a.getHeight());
		}

		int width = maxX - minX;
		int height = maxY - minY;
		this.xOffset = -minX;
		this.yOffset = -minY;
		this.background = guiHelper.createBlankDrawable(width, height);
	}

	private void initializeWidgets(IGuiHelper guiHelper, GuiParser.ParentNode parentNode)
	{
		label99:
		for (GuiParser.Node rawNode : parentNode.getNodes())
		{
			switch (rawNode.getType())
			{
				case energygauge:
				{
					GuiParser.EnergyGaugeNode node = (GuiParser.EnergyGaugeNode) rawNode;
					SlotPosition pos = new SlotPosition(node.x + node.style.properties.bgXOffset, node.y + node.style.properties.bgYOffset);
					IDrawableStatic energyBackground = guiHelper.createDrawable(
						node.style.properties.texture,
						node.style.properties.uBgInactive,
						node.style.properties.vBgInactive,
						node.style.properties.bgWidth,
						node.style.properties.bgHeight
					);
					this.elements.add(new Tuple.T2<>(energyBackground, pos));
					energyBackground = guiHelper.createDrawable(
						node.style.properties.texture,
						node.style.properties.uInner,
						node.style.properties.vInner,
						node.style.properties.innerWidth,
						node.style.properties.innerHeight
					);
					IDrawableAnimated energyAnimated = guiHelper.createAnimatedDrawable(
						energyBackground,
						300,
						node.style.properties.reverse
							? (node.style.properties.vertical ? StartDirection.TOP : StartDirection.RIGHT)
							: (node.style.properties.vertical ? StartDirection.BOTTOM : StartDirection.LEFT),
						true
					);
					this.elements.add(new Tuple.T2<>(energyAnimated, new SlotPosition(node.x, node.y)));
					break;
				}
				case gauge:
				{
					GuiParser.GaugeNode node = (GuiParser.GaugeNode) rawNode;
					Gauge.GaugeProperties properties = node.style.getProperties();
					SlotPosition pos = new SlotPosition(node.x + properties.bgXOffset, node.y + properties.bgYOffset);
					IDrawableStatic guageBackground = guiHelper.createDrawable(
						properties.texture, properties.uBgActive, properties.vBgActive, properties.bgWidth, properties.bgHeight
					);
					this.elements.add(new Tuple.T2<>(guageBackground, pos));
					guageBackground = guiHelper.createDrawable(
						properties.texture, properties.uInner, properties.vInner, properties.innerWidth, properties.innerHeight
					);
					IDrawable gaugeForeground;
					if (node.style == Gauge.GaugeStyle.HeatCentrifuge)
					{
						gaugeForeground = guageBackground;
					} else
					{
						gaugeForeground = guiHelper.createAnimatedDrawable(
							guageBackground,
							this.getProcessSpeed(node.name),
							properties.reverse
								? (properties.vertical ? StartDirection.BOTTOM : StartDirection.RIGHT)
								: (properties.vertical ? StartDirection.TOP : StartDirection.LEFT),
							false
						);
					}

					this.elements.add(new Tuple.T2<>(gaugeForeground, new SlotPosition(node.x, node.y)));
					break;
				}
				case image:
				{
					GuiParser.ImageNode node = (GuiParser.ImageNode) rawNode;
					SlotPosition pos = new SlotPosition(node.x, node.y);
					IDrawable image = guiHelper.drawableBuilder(node.src, node.u1, node.v1, node.width, node.height)
						.setTextureSize(node.baseWidth, node.baseHeight)
						.build();
					this.elements.add(new Tuple.T2<>(image, pos));
					break;
				}
				case slot:
				{
					GuiParser.SlotNode node = (GuiParser.SlotNode) rawNode;
					SlotPosition pos = new SlotPosition(node.x, node.y, node.style);
					IDrawable drawable = guiHelper.createDrawable(
						GuiElement.commonTexture, pos.style().u, pos.style().v, pos.style().width, pos.style().height
					);
					this.elements.add(new Tuple.T2<>(drawable, pos));
					int extraX = (node.style.width - 16) / 2;
					int extraY = (node.style.height - 16) / 2;
					String slotName = node.name.toLowerCase(Locale.ENGLISH);
					if (!slotName.contains("input") && !slotName.equals("cutterInputSlot"))
					{
						if (slotName.contains("output"))
						{
							this.outputSlots.add(new SlotPosition(pos, extraX, extraY));
						}
						break;
					}

					this.inputSlots.add(new SlotPosition(pos, extraX, extraY));
					break;
				}
				case slotgrid:
				{
					GuiParser.SlotGridNode node = (GuiParser.SlotGridNode) rawNode;
					TileEntityInventory dummyTe = (TileEntityInventory) this.block.getDummyTe();
					if (dummyTe == null)
					{
						throw new NullPointerException("Received null dummy for " + this.block + " in the JeiPlugin.");
					}

					InvSlot slot = dummyTe.getInventorySlot(node.name);
					if (slot == null)
					{
						throw new RuntimeException("invalid invslot name " + node.name + " for base " + dummyTe);
					}

					int size = slot.size();
					if (size > node.offset)
					{
						GuiParser.SlotGridNode.SlotGridDimension dim = node.getDimension(size);
						IDrawable drawable = guiHelper.createDrawable(GuiElement.commonTexture, node.style.u, node.style.v, node.style.width, node.style.height);
						boolean isInput = node.name.toLowerCase().contains("input");
						boolean isOutput = node.name.toLowerCase().contains("output");
						int extraX = (node.style.width - 16) / 2;
						int extraY = (node.style.height - 16) / 2;

						for (int i = 0; i < dim.cols; i++)
						{
							for (int j = 0; j < dim.rows; j++)
							{
								if (i * dim.rows + j > size)
								{
									continue label99;
								}

								SlotPosition posx = new SlotPosition(node.x + i * node.style.width, node.y + j * node.style.height, node.style);
								this.elements.add(new Tuple.T2<>(drawable, posx));
								if (isInput)
								{
									this.inputSlots.add(new SlotPosition(posx, extraX, extraY));
								} else if (isOutput)
								{
									this.outputSlots.add(new SlotPosition(posx, extraX, extraY));
								}
							}
						}
					}
					break;
				}
				case environment:
				{
					GuiParser.EnvironmentNode node = (GuiParser.EnvironmentNode) rawNode;
					if (node.environment == GuiEnvironment.JEI)
					{
						this.initializeWidgets(guiHelper, node);
					}
				}
			}
		}
	}

	public void setRecipe(IRecipeLayoutBuilder builder, IORecipeWrapper recipe, IFocusGroup focuses)
	{
		this.addRecipeSlots(builder, recipe, focuses, this.xOffset, this.yOffset);
	}

	public void draw(IORecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY)
	{
		for (Tuple.T2<IDrawable, SlotPosition> element : this.elements)
		{
			element.a.draw(guiGraphics, element.b.x() + this.xOffset, element.b.y() + this.yOffset);
		}
	}

	public RecipeType<IORecipeWrapper> getRecipeType()
	{
		return this.recipeType;
	}

	@SuppressWarnings("removal")
	public IDrawable getBackground()
	{
		return this.background;
	}

	@Override
	protected List<SlotPosition> getInputSlotPos()
	{
		return this.inputSlots;
	}

	@Override
	protected List<SlotPosition> getOutputSlotPos()
	{
		return this.outputSlots;
	}

	protected int getProcessSpeed(String name)
	{
		if ("progress".equals(name))
		{
			Ic2TileEntity te = this.block.getDummyTe();
			if (te != null && te instanceof TileEntityStandardMachine)
			{
				return ((TileEntityStandardMachine) te).defaultOperationLength / 3;
			}
		}

		return 200;
	}
}

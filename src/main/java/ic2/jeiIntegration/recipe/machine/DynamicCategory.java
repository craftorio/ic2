package ic2.jeiIntegration.recipe.machine;

import ic2.core.block.ITeBlock;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.dynamic.GuiEnvironment;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.util.Tuple;
import ic2.jeiIntegration.SlotPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import net.minecraft.client.Minecraft;

public class DynamicCategory<T> extends IORecipeCategory<T> implements IDrawable
{
	protected static final int xOffset = 0;
	protected static final int yOffset = -16;
	protected final List<Tuple.T2<IDrawable, SlotPosition>> elements = new ArrayList<>();
	private final List<SlotPosition> inputSlots = new ArrayList<>();
	private final List<SlotPosition> outputSlots = new ArrayList<>();

	public DynamicCategory(ITeBlock block, T recipeManager, IGuiHelper guiHelper)
	{
		super(block, recipeManager);
		this.initializeWidgets(guiHelper, GuiParser.parse(block));
	}

	private void initializeWidgets(IGuiHelper guiHelper, GuiParser.ParentNode parentNode)
	{
		label103:
		for (GuiParser.Node rawNode : parentNode.getNodes())
		{
			switch (rawNode.getType())
			{
				case energygauge:
				{
					GuiParser.EnergyGaugeNode node = (GuiParser.EnergyGaugeNode) rawNode;
					SlotPosition pos = new SlotPosition(node.x + node.style.properties.bgXOffset + 0, node.y + node.style.properties.bgYOffset + -16);
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
					this.elements.add(new Tuple.T2<>(energyAnimated, new SlotPosition(node.x + 0, node.y + -16)));
					break;
				}
				case gauge:
				{
					GuiParser.GaugeNode node = (GuiParser.GaugeNode) rawNode;
					Gauge.GaugeProperties properties = node.style.getProperties();
					SlotPosition pos = new SlotPosition(node.x + properties.bgXOffset + 0, node.y + properties.bgYOffset + -16);
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

					this.elements.add(new Tuple.T2<>(gaugeForeground, new SlotPosition(node.x + 0, node.y + -16)));
					break;
				}
				case image:
				{
					GuiParser.ImageNode node = (GuiParser.ImageNode) rawNode;
					SlotPosition pos = new SlotPosition(node.x + 0, node.y + -16);
					IDrawable image = guiHelper.createDrawable(node.src, node.u1, node.v1, node.width, node.height, node.baseWidth, node.baseHeight);
					this.elements.add(new Tuple.T2<>(image, pos));
					break;
				}
				case slot:
				{
					GuiParser.SlotNode node = (GuiParser.SlotNode) rawNode;
					SlotPosition pos = new SlotPosition(node.x + 0, node.y + -16, node.style);
					IDrawable drawable = guiHelper.createDrawable(
						GuiElement.commonTexture, pos.getStyle().u, pos.getStyle().v, pos.getStyle().width, pos.getStyle().height
					);
					this.elements.add(new Tuple.T2<>(drawable, pos));
					int extraX = 0;
					int extraY = 0;
					if (node.style == SlotGrid.SlotStyle.Large)
					{
						extraY = 4;
						extraX = 4;
					}

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

						for (int i = 0; i < dim.cols; i++)
						{
							for (int j = 0; j < dim.rows; j++)
							{
								if (i * dim.rows + j > size)
								{
									continue label103;
								}

								SlotPosition posx = new SlotPosition(node.x + 0 + i * node.style.width, node.y + -16 + j * node.style.height, node.style);
								this.elements.add(new Tuple.T2<>(drawable, posx));
								if (isInput)
								{
									this.inputSlots.add(posx);
								} else if (isOutput)
								{
									this.outputSlots.add(posx);
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

	public IDrawable getBackground()
	{
		return this;
	}

	@Override
	public void drawExtras(Minecraft minecraft)
	{
		for (Tuple.T2<IDrawable, SlotPosition> element : this.elements)
		{
			element.a.draw(minecraft, element.b.getX(), element.b.getY());
		}
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

	public void draw(Minecraft minecraft)
	{
	}

	public void draw(Minecraft minecraft, int xOffset, int yOffset)
	{
	}

	public int getHeight()
	{
		return 60;
	}

	public int getWidth()
	{
		return 160;
	}

	protected int getProcessSpeed(String name)
	{
		if ("progress".equals(name))
		{
			TileEntityBlock te = this.block.getDummyTe();
			if (te != null && te instanceof TileEntityStandardMachine)
			{
				return ((TileEntityStandardMachine) te).defaultOperationLength / 3;
			}
		}

		return 200;
	}
}

package ic2.core.gui.dynamic;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.gui.Button;
import ic2.core.gui.CustomButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiDefaultBackground;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.Image;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.MouseButton;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TankFluidSlot;
import ic2.core.gui.TankGauge;
import ic2.core.gui.TextLabel;
import ic2.core.gui.VanillaButton;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.util.LogCategory;

import java.util.Collections;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

public class DynamicGui<T extends Container> extends GuiDefaultBackground<DynamicContainer<T>>
{
	public static <T extends Container> DynamicGui<T> create(DynamicContainer<T> container, Inventory playerInventory, Component title)
	{
		return new DynamicGui<>(container, playerInventory, title);
	}

	protected DynamicGui(DynamicContainer<T> container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, container.guiNode.width, container.guiNode.height);
		this.initializeWidgets(playerInventory, container.guiNode);
	}

	private void initializeWidgets(Inventory playerInventory, GuiParser.ParentNode parentNode)
	{
		for (GuiParser.Node rawNode : parentNode.getNodes())
		{
			switch (rawNode.getType())
			{
				case environment:
					if (((GuiParser.EnvironmentNode) rawNode).environment != GuiEnvironment.GAME)
					{
						continue;
					}
				case gui:
				case key:
				case only:
				case tooltip:
				default:
					break;
				case button:
				{
					GuiParser.ButtonNode node = (GuiParser.ButtonNode) rawNode;
					if (node.type != GuiParser.ButtonNode.ButtonType.RECIPE
						&& !(this.menu.base instanceof INetworkClientTileEntityEventListener)
						&& !this.isHandHeldGUI())
					{
						throw new RuntimeException("Invalid base " + ((DynamicContainer) this.menu).base + " for button elements");
					}

					Button<?> button = null;
					switch (node.type)
					{
						case VANILLA:
							button = new VanillaButton(this, node.x, node.y, node.width, node.height, this.createEventSender(node.eventID, node.eventName));
						case CUSTOM:
						default:
							break;
						case TRANSPARENT:
							button = new CustomButton(this, node.x, node.y, node.width, node.height, this.createEventSender(node.eventID, node.eventName));
							break;
						case RECIPE:
							if (RecipeButton.canUse() && node.eventName != null)
							{
								button = new RecipeButton(this, node.x, node.y, node.width, node.height, node.eventName.split(",[ ]*"));
								node.text = TextProvider.of("");
							}
					}

					if (button != null)
					{
						String text = node.text.get(this.menu.base, Collections.singletonMap("name", TextProvider.of(this.title)));
						if (node.icon == null)
						{
							button = button.withText(text);
						} else
						{
							button.withIcon(() -> node.icon);
							button.withTooltip(text);
						}

						parentNode.addElement(this, button);
					}
					break;
				}
				case energygauge:
				{
					if (!(this.menu.base instanceof Ic2TileEntity)
						|| !((Ic2TileEntity) this.menu.base).hasComponent(Energy.class))
					{
						throw new RuntimeException("invalid base " + ((DynamicContainer) this.menu).base + " for energygauge elements");
					}

					GuiParser.EnergyGaugeNode node = (GuiParser.EnergyGaugeNode) rawNode;
					parentNode.addElement(this, new EnergyGauge(this, node.x, node.y, (Ic2TileEntity) this.menu.base, node.style));
					break;
				}
				case gauge:
				{
					if (!(this.menu.base instanceof IGuiValueProvider))
					{
						throw new RuntimeException("invalid base " + ((DynamicContainer) this.menu).base + " for gauge elements");
					}

					GuiParser.GaugeNode node = (GuiParser.GaugeNode) rawNode;
					final boolean isActiveLinked = node.activeLinked;
					if (isActiveLinked && !(this.menu.base instanceof IGuiValueProvider.IActiveGuiValueProvider))
					{
						throw new RuntimeException("Invalid base " + ((DynamicContainer) this.menu).base + " for active linked gauge elements");
					}

					parentNode.addElement(
						this,
						new LinkedGauge(this, node.x, node.y, (IGuiValueProvider) this.menu.base, node.name, node.style)
						{
							@Override
							protected boolean isActive(double ratio)
							{
								return isActiveLinked
									? ((IGuiValueProvider.IActiveGuiValueProvider) DynamicGui.this.menu.base).isGuiValueActive(this.name)
									: super.isActive(ratio);
							}
						}
					);
					break;
				}
				case image:
				{
					GuiParser.ImageNode node = (GuiParser.ImageNode) rawNode;
					parentNode.addElement(
						this,
						Image.create(this, node.x, node.y, node.width, node.height, node.src, node.baseWidth, node.baseHeight, node.u1, node.v1, node.u2, node.v2)
					);
					break;
				}
				case playerinventory:
				{
					GuiParser.PlayerInventoryNode node = (GuiParser.PlayerInventoryNode) rawNode;
					parentNode.addElement(this, new SlotGrid(this, node.x, node.y, 9, 3, node.style, 0, node.spacing));
					parentNode.addElement(this, new SlotGrid(this, node.x, node.y + node.hotbarOffset, 9, 1, node.style, 0, node.spacing));
					if (node.showTitle)
					{
						parentNode.addElement(this, TextLabel.create(this, node.x + 1, node.y - 10, TextProvider.of(playerInventory.getName()), 4210752, false));
					}
					break;
				}
				case slot:
				case slothologram:
				{
					GuiParser.SlotNode node = (GuiParser.SlotNode) rawNode;
					parentNode.addElement(this, new SlotGrid(this, node.x, node.y, 1, 1, node.style));
					break;
				}
				case slotgrid:
				{
					if (!(this.menu.base instanceof IInventorySlotHolder))
					{
						throw new RuntimeException("Invalid base " + ((DynamicContainer<?>) this.menu).base + " for slot elements");
					}

					GuiParser.SlotGridNode node = (GuiParser.SlotGridNode) rawNode;
					InvSlot slot = ((IInventorySlotHolder) this.menu.base).getInventorySlot(node.name);
					if (slot == null)
					{
						throw new RuntimeException("Invalid InvSlot name " + node.name + " for base " + ((DynamicContainer<?>) this.menu).base);
					}

					int size = slot.size();
					if (size > node.offset)
					{
						GuiParser.SlotGridNode.SlotGridDimension dim = node.getDimension(size);
						parentNode.addElement(this, new SlotGrid(this, node.x, node.y, dim.cols, dim.rows, node.style, 0, node.spacing));
					}
					break;
				}
				case text:
				{
					GuiParser.TextNode node = (GuiParser.TextNode) rawNode;

					int var18 = switch (node.align)
					{
						case Start -> node.x;
						case Center -> node.x + this.imageWidth / 2;
						case End -> node.x + this.imageWidth;
						default -> throw new IllegalArgumentException("invalid alignment: " + node.align);
					};
					TextLabel text;
					if (node.rightAligned)
					{
						text = TextLabel.createRightAligned(
							this, var18, node.y, node.width, node.height, node.text, node.color, node.shadow, node.xOffset, node.yOffset, node.centerX, node.centerY
						);
					} else
					{
						text = TextLabel.create(
							this, var18, node.y, node.width, node.height, node.text, node.color, node.shadow, node.xOffset, node.yOffset, node.centerX, node.centerY
						);
					}

					parentNode.addElement(this, text);
					break;
				}
				case fluidtank:
				{
					if (!(this.menu.base instanceof Ic2TileEntity)
						|| !((Ic2TileEntity) this.menu.base).hasComponent(Fluids.class))
					{
						throw new RuntimeException("invalid base " + ((DynamicContainer<?>) this.menu).base + " for tank elements");
					}

					GuiParser.FluidTankNode node = (GuiParser.FluidTankNode) rawNode;
					Fluids fluids = ((Ic2TileEntity) this.menu.base).getComponent(Fluids.class);

					parentNode.addElement(this, switch (node.type)
					{
						case NORMAL -> TankGauge.createNormal(this, node.x, node.y, fluids.getFluidTank(node.name));
						case PLAIN ->
							TankGauge.createPlain(this, node.x, node.y, node.width, node.height, fluids.getFluidTank(node.name));
						case BORDERLESS ->
							TankGauge.createBorderless(this, node.x, node.y, fluids.getFluidTank(node.name), node.mirrored);
					});
					break;
				}
				case fluidslot:
				{
					if (!(this.menu.base instanceof Ic2TileEntity)
						|| !((Ic2TileEntity) this.menu.base).hasComponent(Fluids.class))
					{
						throw new RuntimeException("invalid base " + ((DynamicContainer<?>) this.menu).base + " for tank elements");
					}

					GuiParser.FluidSlotNode node = (GuiParser.FluidSlotNode) rawNode;
					parentNode.addElement(
						this,
						TankFluidSlot.createFluidSlot(
							this, node.x, node.y, ((Ic2TileEntity) this.menu.base).getComponent(Fluids.class).getFluidTank(node.name)
						)
					);
				}
			}

			if (rawNode instanceof GuiParser.ParentNode)
			{
				this.initializeWidgets(playerInventory, (GuiParser.ParentNode) rawNode);
			}
		}
	}

	protected IClickHandler createEventSender(int event, String eventString)
	{
		if (this.isHandHeldGUI())
		{
			final String eventName;
			if (eventString == null)
			{
				IC2.log.warn(LogCategory.General, "HandHand inventory given numeric event rather than string");
				eventName = Integer.toString(event);
			} else
			{
				eventName = eventString;
			}

			return button ->
			{
				IC2.network.get(false).sendContainerEvent(DynamicGui.this.menu, eventName);
				((HandHeldInventory) DynamicGui.this.menu.base).onEvent(eventName);
			};
		} else
		{
			assert eventString == null;
			return this.createEventSender(event);
		}
	}

	protected boolean isHandHeldGUI()
	{
		return this.menu.base instanceof HandHeldInventory;
	}

	@Override
	public void addElement(GuiElement<?> element)
	{
		super.addElement(element);
	}
}

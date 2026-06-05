package ic2.core.gui.dynamic;

import com.google.common.base.Suppliers;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.gui.Button;
import ic2.core.gui.CustomButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.FluidSlot;
import ic2.core.gui.GuiDefaultBackground;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.Image;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.MouseButton;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TankGauge;
import ic2.core.gui.Text;
import ic2.core.gui.VanillaButton;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.util.LogCategory;
import java.util.Collections;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class DynamicGui<T extends ContainerBase<? extends IInventory>> extends GuiDefaultBackground<T> {
   public static <T extends IInventory> DynamicGui<ContainerBase<T>> create(T base, EntityPlayer player, GuiParser.GuiNode guiNode) {
      DynamicContainer<T> container = DynamicContainer.create(base, player, guiNode);
      return new DynamicGui(player, (T)container, guiNode);
   }

   public static <T extends HandHeldInventory> DynamicGui<ContainerBase<T>> create(T base, EntityPlayer player, GuiParser.GuiNode guiNode) {
      DynamicHandHeldContainer<T> container = DynamicHandHeldContainer.create(base, player, guiNode);
      return new DynamicGui(player, (T)container, guiNode);
   }

   protected DynamicGui(EntityPlayer player, T container, GuiParser.GuiNode guiNode) {
      super(container, guiNode.width, guiNode.height);
      this.initializeWidgets(player, guiNode);
   }

   private void initializeWidgets(EntityPlayer player, GuiParser.ParentNode parentNode) {
      for (GuiParser.Node rawNode : parentNode.getNodes()) {
         switch (rawNode.getType()) {
            case environment:
               if (((GuiParser.EnvironmentNode)rawNode).environment != GuiEnvironment.GAME) {
                  continue;
               }
            case gui:
            case key:
            case only:
            case tooltip:
            default:
               break;
            case button: {
               GuiParser.ButtonNode node = (GuiParser.ButtonNode)rawNode;
               if (node.type != GuiParser.ButtonNode.ButtonType.RECIPE
                  && !(this.container.base instanceof INetworkClientTileEntityEventListener)
                  && !this.isHandHeldGUI()) {
                  throw new RuntimeException("Invalid base " + this.container.base + " for button elements");
               }

               Button<?> button = null;
               switch (node.type) {
                  case VANILLA:
                     button = new VanillaButton(this, node.x, node.y, node.width, node.height, this.createEventSender(node.eventID, node.eventName));
                  case CUSTOM:
                  default:
                     break;
                  case TRANSPARENT:
                     button = new CustomButton(this, node.x, node.y, node.width, node.height, this.createEventSender(node.eventID, node.eventName));
                     break;
                  case RECIPE:
                     if (RecipeButton.canUse() && node.eventName != null) {
                        button = new RecipeButton(this, node.x, node.y, node.width, node.height, node.eventName.split(",[ ]*"));
                        node.text = TextProvider.of("");
                     }
               }

               if (button != null) {
                  String text = node.text
                     .get(this.container.base, Collections.singletonMap("name", TextProvider.ofTranslated(this.container.base.getName())));
                  if (node.icon == null) {
                     button = button.withText(text);
                  } else {
                     button.withIcon(Suppliers.ofInstance(node.icon));
                     button.withTooltip(text);
                  }

                  parentNode.addElement(this, button);
               }
               break;
            }
            case energygauge: {
               if (!(this.container.base instanceof TileEntityBlock) || !((TileEntityBlock)this.container.base).hasComponent(Energy.class)) {
                  throw new RuntimeException("invalid base " + this.container.base + " for energygauge elements");
               }

               GuiParser.EnergyGaugeNode node = (GuiParser.EnergyGaugeNode)rawNode;
               parentNode.addElement(this, new EnergyGauge(this, node.x, node.y, (TileEntityBlock)this.container.base, node.style));
               break;
            }
            case gauge: {
               if (!(this.container.base instanceof IGuiValueProvider)) {
                  throw new RuntimeException("invalid base " + this.container.base + " for gauge elements");
               }

               GuiParser.GaugeNode node = (GuiParser.GaugeNode)rawNode;
               final boolean isActiveLinked = node.activeLinked;
               if (isActiveLinked && !(this.container.base instanceof IGuiValueProvider.IActiveGuiValueProvider)) {
                  throw new RuntimeException("Invalid base " + this.container.base + " for active linked gauge elements");
               }

               parentNode.addElement(
                  this,
                  new LinkedGauge(this, node.x, node.y, (IGuiValueProvider)this.container.base, node.name, node.style) {
                     @Override
                     protected boolean isActive(double ratio) {
                        return isActiveLinked
                           ? ((IGuiValueProvider.IActiveGuiValueProvider)DynamicGui.this.container.base).isGuiValueActive(this.name)
                           : super.isActive(ratio);
                     }
                  }
               );
               break;
            }
            case image: {
               GuiParser.ImageNode node = (GuiParser.ImageNode)rawNode;
               parentNode.addElement(
                  this,
                  Image.create(this, node.x, node.y, node.width, node.height, node.src, node.baseWidth, node.baseHeight, node.u1, node.v1, node.u2, node.v2)
               );
               break;
            }
            case playerinventory: {
               GuiParser.PlayerInventoryNode node = (GuiParser.PlayerInventoryNode)rawNode;
               parentNode.addElement(this, new SlotGrid(this, node.x, node.y, 9, 3, node.style, 0, node.spacing));
               parentNode.addElement(this, new SlotGrid(this, node.x, node.y + node.hotbarOffset, 9, 1, node.style, 0, node.spacing));
               if (node.showTitle) {
                  parentNode.addElement(
                     this, Text.create(this, node.x + 1, node.y - 10, TextProvider.ofTranslated(player.inventory.getName()), 4210752, false)
                  );
               }
               break;
            }
            case slot:
            case slothologram: {
               GuiParser.SlotNode node = (GuiParser.SlotNode)rawNode;
               parentNode.addElement(this, new SlotGrid(this, node.x, node.y, 1, 1, node.style));
               break;
            }
            case slotgrid: {
               if (!(this.container.base instanceof IInventorySlotHolder)) {
                  throw new RuntimeException("Invalid base " + this.container.base + " for slot elements");
               }

               GuiParser.SlotGridNode node = (GuiParser.SlotGridNode)rawNode;
               InvSlot slot = ((IInventorySlotHolder)this.container.base).getInventorySlot(node.name);
               if (slot == null) {
                  throw new RuntimeException("Invalid InvSlot name " + node.name + " for base " + this.container.base);
               }

               int size = slot.size();
               if (size > node.offset) {
                  GuiParser.SlotGridNode.SlotGridDimension dim = node.getDimension(size);
                  parentNode.addElement(this, new SlotGrid(this, node.x, node.y, dim.cols, dim.rows, node.style, 0, node.spacing));
               }
               break;
            }
            case text:
               GuiParser.TextNode nodex = (GuiParser.TextNode)rawNode;
               int x;
               switch (nodex.align) {
                  case Start:
                     x = nodex.x;
                     break;
                  case Center:
                     x = nodex.x + this.xSize / 2;
                     break;
                  case End:
                     x = nodex.x + this.xSize;
                     break;
                  default:
                     throw new IllegalArgumentException("invalid alignment: " + nodex.align);
               }

               Text text;
               if (nodex.rightAligned) {
                  text = Text.createRightAligned(
                     this,
                     x,
                     nodex.y,
                     nodex.width,
                     nodex.height,
                     nodex.text,
                     nodex.color,
                     nodex.shadow,
                     nodex.xOffset,
                     nodex.yOffset,
                     nodex.centerX,
                     nodex.centerY
                  );
               } else {
                  text = Text.create(
                     this,
                     x,
                     nodex.y,
                     nodex.width,
                     nodex.height,
                     nodex.text,
                     nodex.color,
                     nodex.shadow,
                     nodex.xOffset,
                     nodex.yOffset,
                     nodex.centerX,
                     nodex.centerY
                  );
               }

               parentNode.addElement(this, text);
               break;
            case fluidtank:
               if (!(this.container.base instanceof TileEntityBlock) || !((TileEntityBlock)this.container.base).hasComponent(Fluids.class)) {
                  throw new RuntimeException("invalid base " + this.container.base + " for tank elements");
               }

               GuiParser.FluidTankNode nodex = (GuiParser.FluidTankNode)rawNode;
               Fluids fluids = ((TileEntityBlock)this.container.base).getComponent(Fluids.class);
               TankGauge tankGauge;
               switch (nodex.type) {
                  case NORMAL:
                     tankGauge = TankGauge.createNormal(this, nodex.x, nodex.y, fluids.getFluidTank(nodex.name));
                     break;
                  case PLAIN:
                     tankGauge = TankGauge.createPlain(this, nodex.x, nodex.y, nodex.width, nodex.height, fluids.getFluidTank(nodex.name));
                     break;
                  case BORDERLESS:
                     tankGauge = TankGauge.createBorderless(this, nodex.x, nodex.y, fluids.getFluidTank(nodex.name), nodex.mirrored);
                     break;
                  default:
                     throw new IllegalStateException("Unexpected type " + nodex.type);
               }

               parentNode.addElement(this, tankGauge);
               break;
            case fluidslot: {
               if (!(this.container.base instanceof TileEntityBlock) || !((TileEntityBlock)this.container.base).hasComponent(Fluids.class)) {
                  throw new RuntimeException("invalid base " + this.container.base + " for tank elements");
               }

               GuiParser.FluidSlotNode node = (GuiParser.FluidSlotNode)rawNode;
               parentNode.addElement(
                  this,
                  FluidSlot.createFluidSlot(this, node.x, node.y, ((TileEntityBlock)this.container.base).getComponent(Fluids.class).getFluidTank(node.name))
               );
            }
         }

         if (rawNode instanceof GuiParser.ParentNode) {
            this.initializeWidgets(player, (GuiParser.ParentNode)rawNode);
         }
      }
   }

   protected IClickHandler createEventSender(int event, String eventString) {
      if (this.isHandHeldGUI()) {
         final String eventName;
         if (eventString == null) {
            IC2.log.warn(LogCategory.General, "HandHand inventory given numeric event rather than string");
            eventName = Integer.toString(event);
         } else {
            eventName = eventString;
         }

         return new IClickHandler() {
            @Override
            public void onClick(MouseButton button) {
               IC2.network.get(false).sendContainerEvent(DynamicGui.this.container, eventName);
               ((HandHeldInventory)DynamicGui.this.container.base).onEvent(eventName);
            }
         };
      } else {
         assert eventString == null;
         return this.createEventSender(event);
      }
   }

   protected boolean isHandHeldGUI() {
      return this.container.base instanceof HandHeldInventory;
   }

   @Override
   public void addElement(GuiElement<?> element) {
      super.addElement(element);
   }
}

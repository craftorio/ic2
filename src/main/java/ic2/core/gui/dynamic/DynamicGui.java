package ic2.core.gui.dynamic;

import com.google.common.base.Suppliers;
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
import ic2.core.gui.Gauge;
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
import ic2.core.network.NetworkManager;
import ic2.core.util.LogCategory;
import java.util.Collections;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fluids.IFluidTank;

public class DynamicGui<T extends ContainerBase<? extends IInventory>> extends GuiDefaultBackground<T> {
  public static <T extends IInventory> DynamicGui<ContainerBase<T>> create(T base, EntityPlayer player, GuiParser.GuiNode guiNode) {
    DynamicContainer<T> container = DynamicContainer.create(base, player, guiNode);
    return new DynamicGui<>(player, container, guiNode);
  }
  
  public static <T extends HandHeldInventory> DynamicGui<ContainerBase<T>> create(T base, EntityPlayer player, GuiParser.GuiNode guiNode) {
    DynamicHandHeldContainer<T> container = DynamicHandHeldContainer.create(base, player, guiNode);
    return new DynamicGui<>(player, container, guiNode);
  }
  
  protected DynamicGui(EntityPlayer player, T container, GuiParser.GuiNode guiNode) {
    super((ContainerBase)container, guiNode.width, guiNode.height);
    initializeWidgets(player, guiNode);
  }
  
  private void initializeWidgets(EntityPlayer player, GuiParser.ParentNode parentNode) {
    for (GuiParser.Node rawNode : parentNode.getNodes()) {
      GuiParser.ButtonNode buttonNode;
      GuiParser.EnergyGaugeNode energyGaugeNode;
      GuiParser.GaugeNode gaugeNode;
      GuiParser.ImageNode imageNode;
      GuiParser.PlayerInventoryNode playerInventoryNode;
      GuiParser.SlotNode slotNode;
      GuiParser.SlotGridNode slotGridNode;
      GuiParser.TextNode textNode;
      GuiParser.FluidTankNode fluidTankNode;
      GuiParser.FluidSlotNode node;
      Button<?> button;
      VanillaButton vanillaButton;
      CustomButton customButton;
      RecipeButton recipeButton;
      final boolean isActiveLinked;
      InvSlot slot;
      int x;
      Fluids fluids;
      int size;
      Text text;
      TankGauge tankGauge;
      switch (rawNode.getType()) {
        case environment:
          if (((GuiParser.EnvironmentNode)rawNode).environment != GuiEnvironment.GAME)
            continue; 
          break;
        case button:
          buttonNode = (GuiParser.ButtonNode)rawNode;
          if (buttonNode.type != GuiParser.ButtonNode.ButtonType.RECIPE && !(this.container.base instanceof ic2.api.network.INetworkClientTileEntityEventListener) && !isHandHeldGUI())
            throw new RuntimeException("Invalid base " + this.container.base + " for button elements"); 
          button = null;
          switch (buttonNode.type) {
            case environment:
              vanillaButton = new VanillaButton((GuiIC2)this, buttonNode.x, buttonNode.y, buttonNode.width, buttonNode.height, createEventSender(buttonNode.eventID, buttonNode.eventName));
              break;
            case key:
              customButton = new CustomButton((GuiIC2)this, buttonNode.x, buttonNode.y, buttonNode.width, buttonNode.height, createEventSender(buttonNode.eventID, buttonNode.eventName));
              break;
            case only:
              if (RecipeButton.canUse() && buttonNode.eventName != null) {
                recipeButton = new RecipeButton((GuiIC2)this, buttonNode.x, buttonNode.y, buttonNode.width, buttonNode.height, buttonNode.eventName.split(",[ ]*"));
                buttonNode.text = TextProvider.of("");
              } 
              break;
          } 
          if (recipeButton != null) {
            Button button1;
            String str = buttonNode.text.get(this.container.base, Collections.singletonMap("name", TextProvider.ofTranslated(this.container.base.func_70005_c_())));
            if (buttonNode.icon == null) {
              button1 = recipeButton.withText(str);
            } else {
              button1.withIcon(Suppliers.ofInstance(buttonNode.icon));
              button1.withTooltip(str);
            } 
            parentNode.addElement(this, (GuiElement<?>)button1);
          } 
          break;
        case energygauge:
          if (!(this.container.base instanceof TileEntityBlock) || 
            !((TileEntityBlock)this.container.base).hasComponent(Energy.class))
            throw new RuntimeException("invalid base " + this.container.base + " for energygauge elements"); 
          energyGaugeNode = (GuiParser.EnergyGaugeNode)rawNode;
          parentNode.addElement(this, (GuiElement<?>)new EnergyGauge((GuiIC2)this, energyGaugeNode.x, energyGaugeNode.y, (TileEntityBlock)this.container.base, energyGaugeNode.style));
          break;
        case gauge:
          if (!(this.container.base instanceof IGuiValueProvider))
            throw new RuntimeException("invalid base " + this.container.base + " for gauge elements"); 
          gaugeNode = (GuiParser.GaugeNode)rawNode;
          isActiveLinked = gaugeNode.activeLinked;
          if (isActiveLinked && !(this.container.base instanceof IGuiValueProvider.IActiveGuiValueProvider))
            throw new RuntimeException("Invalid base " + this.container.base + " for active linked gauge elements"); 
          parentNode.addElement(this, (GuiElement<?>)new LinkedGauge((GuiIC2)this, gaugeNode.x, gaugeNode.y, (IGuiValueProvider)this.container.base, gaugeNode.name, gaugeNode.style) {
                protected boolean isActive(double ratio) {
                  return isActiveLinked ? ((IGuiValueProvider.IActiveGuiValueProvider)DynamicGui.this.container.base).isGuiValueActive(this.name) : super.isActive(ratio);
                }
              });
          break;
        case image:
          imageNode = (GuiParser.ImageNode)rawNode;
          parentNode.addElement(this, (GuiElement<?>)Image.create((GuiIC2)this, imageNode.x, imageNode.y, imageNode.width, imageNode.height, imageNode.src, imageNode.baseWidth, imageNode.baseHeight, imageNode.u1, imageNode.v1, imageNode.u2, imageNode.v2));
          break;
        case playerinventory:
          playerInventoryNode = (GuiParser.PlayerInventoryNode)rawNode;
          parentNode.addElement(this, (GuiElement<?>)new SlotGrid((GuiIC2)this, playerInventoryNode.x, playerInventoryNode.y, 9, 3, playerInventoryNode.style, 0, playerInventoryNode.spacing));
          parentNode.addElement(this, (GuiElement<?>)new SlotGrid((GuiIC2)this, playerInventoryNode.x, playerInventoryNode.y + playerInventoryNode.hotbarOffset, 9, 1, playerInventoryNode.style, 0, playerInventoryNode.spacing));
          if (playerInventoryNode.showTitle)
            parentNode.addElement(this, (GuiElement<?>)Text.create((GuiIC2)this, playerInventoryNode.x + 1, playerInventoryNode.y - 10, 
                  TextProvider.ofTranslated(player.inventory.func_70005_c_()), 4210752, false)); 
          break;
        case slot:
        case slothologram:
          slotNode = (GuiParser.SlotNode)rawNode;
          parentNode.addElement(this, (GuiElement<?>)new SlotGrid((GuiIC2)this, slotNode.x, slotNode.y, 1, 1, slotNode.style));
          break;
        case slotgrid:
          if (!(this.container.base instanceof IInventorySlotHolder))
            throw new RuntimeException("Invalid base " + this.container.base + " for slot elements"); 
          slotGridNode = (GuiParser.SlotGridNode)rawNode;
          slot = ((IInventorySlotHolder)this.container.base).getInventorySlot(slotGridNode.name);
          if (slot == null)
            throw new RuntimeException("Invalid InvSlot name " + slotGridNode.name + " for base " + this.container.base); 
          size = slot.size();
          if (size > slotGridNode.offset) {
            GuiParser.SlotGridNode.SlotGridDimension dim = slotGridNode.getDimension(size);
            parentNode.addElement(this, (GuiElement<?>)new SlotGrid((GuiIC2)this, slotGridNode.x, slotGridNode.y, dim.cols, dim.rows, slotGridNode.style, 0, slotGridNode.spacing));
          } 
          break;
        case text:
          textNode = (GuiParser.TextNode)rawNode;
          switch (textNode.align) {
            case environment:
              x = textNode.x;
              break;
            case gui:
              x = textNode.x + this.field_146999_f / 2;
              break;
            case key:
              x = textNode.x + this.field_146999_f;
              break;
            default:
              throw new IllegalArgumentException("invalid alignment: " + textNode.align);
          } 
          if (textNode.rightAligned) {
            text = Text.createRightAligned((GuiIC2)this, x, textNode.y, textNode.width, textNode.height, textNode.text, textNode.color, textNode.shadow, textNode.xOffset, textNode.yOffset, textNode.centerX, textNode.centerY);
          } else {
            text = Text.create((GuiIC2)this, x, textNode.y, textNode.width, textNode.height, textNode.text, textNode.color, textNode.shadow, textNode.xOffset, textNode.yOffset, textNode.centerX, textNode.centerY);
          } 
          parentNode.addElement(this, (GuiElement<?>)text);
          break;
        case fluidtank:
          if (!(this.container.base instanceof TileEntityBlock) || 
            !((TileEntityBlock)this.container.base).hasComponent(Fluids.class))
            throw new RuntimeException("invalid base " + this.container.base + " for tank elements"); 
          fluidTankNode = (GuiParser.FluidTankNode)rawNode;
          fluids = (Fluids)((TileEntityBlock)this.container.base).getComponent(Fluids.class);
          switch (fluidTankNode.type) {
            case environment:
              tankGauge = TankGauge.createNormal((GuiIC2)this, fluidTankNode.x, fluidTankNode.y, (IFluidTank)fluids.getFluidTank(fluidTankNode.name));
              break;
            case gui:
              tankGauge = TankGauge.createPlain((GuiIC2)this, fluidTankNode.x, fluidTankNode.y, fluidTankNode.width, fluidTankNode.height, (IFluidTank)fluids.getFluidTank(fluidTankNode.name));
              break;
            case key:
              tankGauge = TankGauge.createBorderless((GuiIC2)this, fluidTankNode.x, fluidTankNode.y, (IFluidTank)fluids.getFluidTank(fluidTankNode.name), fluidTankNode.mirrored);
              break;
            default:
              throw new IllegalStateException("Unexpected type " + fluidTankNode.type);
          } 
          parentNode.addElement(this, (GuiElement<?>)tankGauge);
          break;
        case fluidslot:
          if (!(this.container.base instanceof TileEntityBlock) || 
            !((TileEntityBlock)this.container.base).hasComponent(Fluids.class))
            throw new RuntimeException("invalid base " + this.container.base + " for tank elements"); 
          node = (GuiParser.FluidSlotNode)rawNode;
          parentNode.addElement(this, (GuiElement<?>)FluidSlot.createFluidSlot((GuiIC2)this, node.x, node.y, (IFluidTank)((Fluids)((TileEntityBlock)this.container.base).getComponent(Fluids.class)).getFluidTank(node.name)));
          break;
      } 
      if (rawNode instanceof GuiParser.ParentNode)
        initializeWidgets(player, (GuiParser.ParentNode)rawNode); 
    } 
  }
  
  protected IClickHandler createEventSender(int event, String eventString) {
    if (isHandHeldGUI()) {
      final String eventName;
      if (eventString == null) {
        IC2.log.warn(LogCategory.General, "HandHand inventory given numeric event rather than string");
        eventName = Integer.toString(event);
      } else {
        eventName = eventString;
      } 
      return new IClickHandler() {
          public void onClick(MouseButton button) {
            ((NetworkManager)IC2.network.get(false)).sendContainerEvent(DynamicGui.this.container, eventName);
            ((HandHeldInventory)DynamicGui.this.container.base).onEvent(eventName);
          }
        };
    } 
    assert eventString == null;
    return createEventSender(event);
  }
  
  protected boolean isHandHeldGUI() {
    return this.container.base instanceof HandHeldInventory;
  }
  
  public void addElement(GuiElement<?> element) {
    super.addElement(element);
  }
}

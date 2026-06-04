// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui.dynamic;

import ic2.core.network.NetworkManager;
import ic2.core.gui.MouseButton;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.gui.IClickHandler;
import ic2.core.block.invslot.InvSlot;
import ic2.core.gui.Button;
import java.util.Iterator;
import ic2.core.gui.FluidSlot;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.comp.Fluids;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.gui.Text;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.Image;
import ic2.core.gui.Gauge;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.comp.Energy;
import ic2.core.block.TileEntityBlock;
import ic2.core.gui.GuiElement;
import net.minecraft.item.ItemStack;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.util.Collections;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.CustomButton;
import ic2.core.GuiIC2;
import ic2.core.gui.VanillaButton;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.item.tool.HandHeldInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.gui.GuiDefaultBackground;
import net.minecraft.inventory.IInventory;
import ic2.core.ContainerBase;

public class DynamicGui<T extends ContainerBase<? extends IInventory>> extends GuiDefaultBackground<T>
{
    public static <T extends IInventory> DynamicGui<ContainerBase<T>> create(final T base, final EntityPlayer player, final GuiParser.GuiNode guiNode) {
        final DynamicContainer<T> container = DynamicContainer.create(base, player, guiNode);
        return new DynamicGui<ContainerBase<T>>(player, container, guiNode);
    }
    
    public static <T extends HandHeldInventory> DynamicGui<ContainerBase<T>> create(final T base, final EntityPlayer player, final GuiParser.GuiNode guiNode) {
        final DynamicHandHeldContainer<T> container = DynamicHandHeldContainer.create(base, player, guiNode);
        return new DynamicGui<ContainerBase<T>>(player, container, guiNode);
    }
    
    protected DynamicGui(final EntityPlayer player, final T container, final GuiParser.GuiNode guiNode) {
        super(container, guiNode.width, guiNode.height);
        this.initializeWidgets(player, guiNode);
    }
    
    private void initializeWidgets(final EntityPlayer player, final GuiParser.ParentNode parentNode) {
        for (final GuiParser.Node rawNode : parentNode.getNodes()) {
            Button<?> button;
            String text;
            final InvSlot slot;
            switch (rawNode.getType()) {
                case environment: {
                    if (((GuiParser.EnvironmentNode)rawNode).environment != GuiEnvironment.GAME) {
                        continue;
                    }
                    break;
                }
                case button: {
                    final GuiParser.ButtonNode node = (GuiParser.ButtonNode)rawNode;
                    if (node.type != GuiParser.ButtonNode.ButtonType.RECIPE && !(this.container.base instanceof INetworkClientTileEntityEventListener) && !this.isHandHeldGUI()) {
                        throw new RuntimeException("Invalid base " + this.container.base + " for button elements");
                    }
                    button = null;
                    switch (node.type) {
                        case VANILLA: {
                            button = new VanillaButton(this, node.x, node.y, node.width, node.height, this.createEventSender(node.eventID, node.eventName));
                        }
                        case TRANSPARENT: {
                            button = new CustomButton(this, node.x, node.y, node.width, node.height, this.createEventSender(node.eventID, node.eventName));
                            break;
                        }
                        case RECIPE: {
                            if (RecipeButton.canUse() && node.eventName != null) {
                                button = new RecipeButton(this, node.x, node.y, node.width, node.height, node.eventName.split(",[ ]*"));
                                node.text = TextProvider.of("");
                                break;
                            }
                            break;
                        }
                    }
                    if (button != null) {
                        text = node.text.get(this.container.base, Collections.singletonMap("name", TextProvider.ofTranslated(this.container.base.getName())));
                        if (node.icon == null) {
                            button = (Button<?>)button.withText(text);
                        }
                        else {
                            button.withIcon((Supplier<ItemStack>)Suppliers.ofInstance((Object)node.icon));
                            button.withTooltip(text);
                        }
                        parentNode.addElement(this, button);
                        break;
                    }
                    break;
                }
                case energygauge: {
                    if (!(this.container.base instanceof TileEntityBlock) || !((TileEntityBlock)this.container.base).hasComponent(Energy.class)) {
                        throw new RuntimeException("invalid base " + this.container.base + " for energygauge elements");
                    }
                    final GuiParser.EnergyGaugeNode node2 = (GuiParser.EnergyGaugeNode)rawNode;
                    parentNode.addElement(this, new EnergyGauge(this, node2.x, node2.y, (TileEntityBlock)this.container.base, node2.style));
                    break;
                }
                case gauge: {
                    if (!(this.container.base instanceof IGuiValueProvider)) {
                        throw new RuntimeException("invalid base " + this.container.base + " for gauge elements");
                    }
                    final GuiParser.GaugeNode node3 = (GuiParser.GaugeNode)rawNode;
                    final boolean isActiveLinked = node3.activeLinked;
                    if (isActiveLinked && !(this.container.base instanceof IGuiValueProvider.IActiveGuiValueProvider)) {
                        throw new RuntimeException("Invalid base " + this.container.base + " for active linked gauge elements");
                    }
                    parentNode.addElement(this, new LinkedGauge(this, node3.x, node3.y, (IGuiValueProvider)this.container.base, node3.name, node3.style) {
                        @Override
                        protected boolean isActive(final double ratio) {
                            return isActiveLinked ? ((IGuiValueProvider.IActiveGuiValueProvider)DynamicGui.this.container.base).isGuiValueActive(this.name) : super.isActive(ratio);
                        }
                    });
                    break;
                }
                case image: {
                    final GuiParser.ImageNode node4 = (GuiParser.ImageNode)rawNode;
                    parentNode.addElement(this, Image.create(this, node4.x, node4.y, node4.width, node4.height, node4.src, node4.baseWidth, node4.baseHeight, node4.u1, node4.v1, node4.u2, node4.v2));
                    break;
                }
                case playerinventory: {
                    final GuiParser.PlayerInventoryNode node5 = (GuiParser.PlayerInventoryNode)rawNode;
                    parentNode.addElement(this, new SlotGrid(this, node5.x, node5.y, 9, 3, node5.style, 0, node5.spacing));
                    parentNode.addElement(this, new SlotGrid(this, node5.x, node5.y + node5.hotbarOffset, 9, 1, node5.style, 0, node5.spacing));
                    if (node5.showTitle) {
                        parentNode.addElement(this, Text.create(this, node5.x + 1, node5.y - 10, TextProvider.ofTranslated(player.inventory.getName()), 4210752, false));
                        break;
                    }
                    break;
                }
                case slot:
                case slothologram: {
                    final GuiParser.SlotNode node6 = (GuiParser.SlotNode)rawNode;
                    parentNode.addElement(this, new SlotGrid(this, node6.x, node6.y, 1, 1, node6.style));
                    break;
                }
                case slotgrid: {
                    if (!(this.container.base instanceof IInventorySlotHolder)) {
                        throw new RuntimeException("Invalid base " + this.container.base + " for slot elements");
                    }
                    final GuiParser.SlotGridNode node7 = (GuiParser.SlotGridNode)rawNode;
                    slot = ((IInventorySlotHolder)this.container.base).getInventorySlot(node7.name);
                    if (slot == null) {
                        throw new RuntimeException("Invalid InvSlot name " + node7.name + " for base " + this.container.base);
                    }
                    final int size = slot.size();
                    if (size > node7.offset) {
                        final GuiParser.SlotGridNode.SlotGridDimension dim = node7.getDimension(size);
                        parentNode.addElement(this, new SlotGrid(this, node7.x, node7.y, dim.cols, dim.rows, node7.style, 0, node7.spacing));
                        break;
                    }
                    break;
                }
                case text: {
                    final GuiParser.TextNode node8 = (GuiParser.TextNode)rawNode;
                    int x = 0;
                    switch (node8.align) {
                        case Start: {
                            x = node8.x;
                            break;
                        }
                        case Center: {
                            x = node8.x + this.xSize / 2;
                            break;
                        }
                        case End: {
                            x = node8.x + this.xSize;
                            break;
                        }
                        default: {
                            throw new IllegalArgumentException("invalid alignment: " + node8.align);
                        }
                    }
                    Text text2;
                    if (node8.rightAligned) {
                        text2 = Text.createRightAligned(this, x, node8.y, node8.width, node8.height, node8.text, node8.color, node8.shadow, node8.xOffset, node8.yOffset, node8.centerX, node8.centerY);
                    }
                    else {
                        text2 = Text.create(this, x, node8.y, node8.width, node8.height, node8.text, node8.color, node8.shadow, node8.xOffset, node8.yOffset, node8.centerX, node8.centerY);
                    }
                    parentNode.addElement(this, text2);
                    break;
                }
                case fluidtank: {
                    if (!(this.container.base instanceof TileEntityBlock) || !((TileEntityBlock)this.container.base).hasComponent(Fluids.class)) {
                        throw new RuntimeException("invalid base " + this.container.base + " for tank elements");
                    }
                    final GuiParser.FluidTankNode node9 = (GuiParser.FluidTankNode)rawNode;
                    final Fluids fluids = ((TileEntityBlock)this.container.base).getComponent(Fluids.class);
                    TankGauge tankGauge = null;
                    switch (node9.type) {
                        case NORMAL: {
                            tankGauge = TankGauge.createNormal(this, node9.x, node9.y, (IFluidTank)fluids.getFluidTank(node9.name));
                            break;
                        }
                        case PLAIN: {
                            tankGauge = TankGauge.createPlain(this, node9.x, node9.y, node9.width, node9.height, (IFluidTank)fluids.getFluidTank(node9.name));
                            break;
                        }
                        case BORDERLESS: {
                            tankGauge = TankGauge.createBorderless(this, node9.x, node9.y, (IFluidTank)fluids.getFluidTank(node9.name), node9.mirrored);
                            break;
                        }
                        default: {
                            throw new IllegalStateException("Unexpected type " + node9.type);
                        }
                    }
                    parentNode.addElement(this, tankGauge);
                    break;
                }
                case fluidslot: {
                    if (!(this.container.base instanceof TileEntityBlock) || !((TileEntityBlock)this.container.base).hasComponent(Fluids.class)) {
                        throw new RuntimeException("invalid base " + this.container.base + " for tank elements");
                    }
                    final GuiParser.FluidSlotNode node10 = (GuiParser.FluidSlotNode)rawNode;
                    parentNode.addElement(this, FluidSlot.createFluidSlot(this, node10.x, node10.y, (IFluidTank)((TileEntityBlock)this.container.base).getComponent(Fluids.class).getFluidTank(node10.name)));
                    break;
                }
            }
            if (rawNode instanceof GuiParser.ParentNode) {
                this.initializeWidgets(player, (GuiParser.ParentNode)rawNode);
            }
        }
    }
    
    protected IClickHandler createEventSender(final int event, final String eventString) {
        if (this.isHandHeldGUI()) {
            String eventName;
            if (eventString == null) {
                IC2.log.warn(LogCategory.General, "HandHand inventory given numeric event rather than string");
                eventName = Integer.toString(event);
            }
            else {
                eventName = eventString;
            }
            return new IClickHandler() {
                @Override
                public void onClick(final MouseButton button) {
                    IC2.network.get(false).sendContainerEvent(DynamicGui.this.container, eventName);
                    ((HandHeldInventory)DynamicGui.this.container.base).onEvent(eventName);
                }
            };
        }
        assert eventString == null;
        return this.createEventSender(event);
    }
    
    protected boolean isHandHeldGUI() {
        return this.container.base instanceof HandHeldInventory;
    }
    
    public void addElement(final GuiElement<?> element) {
        super.addElement(element);
    }
}

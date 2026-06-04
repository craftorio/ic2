// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import net.minecraft.client.Minecraft;
import ic2.core.block.invslot.InvSlot;
import mezz.jei.api.gui.IDrawableStatic;
import java.util.Iterator;
import ic2.core.gui.dynamic.GuiEnvironment;
import ic2.core.block.TileEntityInventory;
import java.util.Locale;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.GuiElement;
import ic2.core.gui.Gauge;
import mezz.jei.api.gui.IDrawableAnimated;
import ic2.core.gui.dynamic.GuiParser;
import java.util.ArrayList;
import mezz.jei.api.IGuiHelper;
import ic2.core.block.ITeBlock;
import ic2.jeiIntegration.SlotPosition;
import ic2.core.util.Tuple;
import java.util.List;
import mezz.jei.api.gui.IDrawable;

public class DynamicCategory<T> extends IORecipeCategory<T> implements IDrawable
{
    protected static final int xOffset = 0;
    protected static final int yOffset = -16;
    protected final List<Tuple.T2<IDrawable, SlotPosition>> elements;
    private final List<SlotPosition> inputSlots;
    private final List<SlotPosition> outputSlots;
    
    public DynamicCategory(final ITeBlock block, final T recipeManager, final IGuiHelper guiHelper) {
        super(block, recipeManager);
        this.elements = new ArrayList<Tuple.T2<IDrawable, SlotPosition>>();
        this.inputSlots = new ArrayList<SlotPosition>();
        this.outputSlots = new ArrayList<SlotPosition>();
        this.initializeWidgets(guiHelper, GuiParser.parse(block));
    }
    
    private void initializeWidgets(final IGuiHelper guiHelper, final GuiParser.ParentNode parentNode) {
        for (final GuiParser.Node rawNode : parentNode.getNodes()) {
            final IDrawable image;
            final InvSlot slot;
            switch (rawNode.getType()) {
                case energygauge: {
                    final GuiParser.EnergyGaugeNode node = (GuiParser.EnergyGaugeNode)rawNode;
                    final SlotPosition pos = new SlotPosition(node.x + node.style.properties.bgXOffset + 0, node.y + node.style.properties.bgYOffset - 16);
                    IDrawableStatic energyBackground = guiHelper.createDrawable(node.style.properties.texture, (int)node.style.properties.uBgInactive, (int)node.style.properties.vBgInactive, (int)node.style.properties.bgWidth, (int)node.style.properties.bgHeight);
                    this.elements.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)energyBackground, pos));
                    energyBackground = guiHelper.createDrawable(node.style.properties.texture, (int)node.style.properties.uInner, (int)node.style.properties.vInner, (int)node.style.properties.innerWidth, (int)node.style.properties.innerHeight);
                    final IDrawableAnimated energyAnimated = guiHelper.createAnimatedDrawable(energyBackground, 300, node.style.properties.reverse ? (node.style.properties.vertical ? IDrawableAnimated.StartDirection.TOP : IDrawableAnimated.StartDirection.RIGHT) : (node.style.properties.vertical ? IDrawableAnimated.StartDirection.BOTTOM : IDrawableAnimated.StartDirection.LEFT), true);
                    this.elements.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)energyAnimated, new SlotPosition(node.x + 0, node.y - 16)));
                    continue;
                }
                case gauge: {
                    final GuiParser.GaugeNode node2 = (GuiParser.GaugeNode)rawNode;
                    final Gauge.GaugeProperties properties = node2.style.getProperties();
                    final SlotPosition pos2 = new SlotPosition(node2.x + properties.bgXOffset + 0, node2.y + properties.bgYOffset - 16);
                    IDrawableStatic guageBackground = guiHelper.createDrawable(properties.texture, (int)properties.uBgActive, (int)properties.vBgActive, (int)properties.bgWidth, (int)properties.bgHeight);
                    this.elements.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)guageBackground, pos2));
                    guageBackground = guiHelper.createDrawable(properties.texture, (int)properties.uInner, (int)properties.vInner, (int)properties.innerWidth, (int)properties.innerHeight);
                    IDrawable gaugeForeground;
                    if (node2.style == Gauge.GaugeStyle.HeatCentrifuge) {
                        gaugeForeground = (IDrawable)guageBackground;
                    }
                    else {
                        gaugeForeground = (IDrawable)guiHelper.createAnimatedDrawable(guageBackground, this.getProcessSpeed(node2.name), properties.reverse ? (properties.vertical ? IDrawableAnimated.StartDirection.BOTTOM : IDrawableAnimated.StartDirection.RIGHT) : (properties.vertical ? IDrawableAnimated.StartDirection.TOP : IDrawableAnimated.StartDirection.LEFT), false);
                    }
                    this.elements.add(new Tuple.T2<IDrawable, SlotPosition>(gaugeForeground, new SlotPosition(node2.x + 0, node2.y - 16)));
                    continue;
                }
                case image: {
                    final GuiParser.ImageNode node3 = (GuiParser.ImageNode)rawNode;
                    final SlotPosition pos = new SlotPosition(node3.x + 0, node3.y - 16);
                    image = (IDrawable)guiHelper.createDrawable(node3.src, node3.u1, node3.v1, node3.width, node3.height, node3.baseWidth, node3.baseHeight);
                    this.elements.add(new Tuple.T2<IDrawable, SlotPosition>(image, pos));
                    continue;
                }
                case slot: {
                    final GuiParser.SlotNode node4 = (GuiParser.SlotNode)rawNode;
                    final SlotPosition pos = new SlotPosition(node4.x + 0, node4.y - 16, node4.style);
                    final IDrawable drawable = (IDrawable)guiHelper.createDrawable(GuiElement.commonTexture, pos.getStyle().u, pos.getStyle().v, pos.getStyle().width, pos.getStyle().height);
                    this.elements.add(new Tuple.T2<IDrawable, SlotPosition>(drawable, pos));
                    int extraY;
                    int extraX = extraY = 0;
                    if (node4.style == SlotGrid.SlotStyle.Large) {
                        extraY = (extraX = 4);
                    }
                    final String slotName = node4.name.toLowerCase(Locale.ENGLISH);
                    if (slotName.contains("input") || slotName.equals("cutterInputSlot")) {
                        this.inputSlots.add(new SlotPosition(pos, extraX, extraY));
                        continue;
                    }
                    if (slotName.contains("output")) {
                        this.outputSlots.add(new SlotPosition(pos, extraX, extraY));
                        continue;
                    }
                    continue;
                }
                case slotgrid: {
                    final GuiParser.SlotGridNode node5 = (GuiParser.SlotGridNode)rawNode;
                    final TileEntityInventory dummyTe = (TileEntityInventory)this.block.getDummyTe();
                    if (dummyTe == null) {
                        throw new NullPointerException("Received null dummy for " + this.block + " in the JeiPlugin.");
                    }
                    slot = dummyTe.getInventorySlot(node5.name);
                    if (slot == null) {
                        throw new RuntimeException("invalid invslot name " + node5.name + " for base " + dummyTe);
                    }
                    final int size = slot.size();
                    if (size > node5.offset) {
                        final GuiParser.SlotGridNode.SlotGridDimension dim = node5.getDimension(size);
                        final IDrawable drawable2 = (IDrawable)guiHelper.createDrawable(GuiElement.commonTexture, node5.style.u, node5.style.v, node5.style.width, node5.style.height);
                        final boolean isInput = node5.name.toLowerCase().contains("input");
                        final boolean isOutput = node5.name.toLowerCase().contains("output");
                    Label_1383:
                        for (int i = 0; i < dim.cols; ++i) {
                            for (int j = 0; j < dim.rows; ++j) {
                                if (i * dim.rows + j > size) {
                                    break Label_1383;
                                }
                                final SlotPosition pos3 = new SlotPosition(node5.x + 0 + i * node5.style.width, node5.y - 16 + j * node5.style.height, node5.style);
                                this.elements.add(new Tuple.T2<IDrawable, SlotPosition>(drawable2, pos3));
                                if (isInput) {
                                    this.inputSlots.add(pos3);
                                }
                                else if (isOutput) {
                                    this.outputSlots.add(pos3);
                                }
                            }
                        }
                        continue;
                    }
                    continue;
                }
                case environment: {
                    final GuiParser.EnvironmentNode node6 = (GuiParser.EnvironmentNode)rawNode;
                    if (node6.environment == GuiEnvironment.JEI) {
                        this.initializeWidgets(guiHelper, node6);
                        continue;
                    }
                    continue;
                }
            }
        }
    }
    
    public IDrawable getBackground() {
        return (IDrawable)this;
    }
    
    @Override
    public void drawExtras(final Minecraft minecraft) {
        for (final Tuple.T2<IDrawable, SlotPosition> element : this.elements) {
            element.a.draw(minecraft, element.b.getX(), element.b.getY());
        }
    }
    
    @Override
    protected List<SlotPosition> getInputSlotPos() {
        return this.inputSlots;
    }
    
    @Override
    protected List<SlotPosition> getOutputSlotPos() {
        return this.outputSlots;
    }
    
    public void draw(final Minecraft minecraft) {
    }
    
    public void draw(final Minecraft minecraft, final int xOffset, final int yOffset) {
    }
    
    public int getHeight() {
        return 60;
    }
    
    public int getWidth() {
        return 160;
    }
    
    protected int getProcessSpeed(final String name) {
        if ("progress".equals(name)) {
            final TileEntityBlock te = this.block.getDummyTe();
            if (te != null && te instanceof TileEntityStandardMachine) {
                return ((TileEntityStandardMachine)te).defaultOperationLength / 3;
            }
        }
        return 200;
    }
}

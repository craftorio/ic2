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
import net.minecraft.client.Minecraft;

public class DynamicCategory<T> extends IORecipeCategory<T> implements IDrawable {
  protected static final int xOffset = 0;
  
  protected static final int yOffset = -16;
  
  protected final List<Tuple.T2<IDrawable, SlotPosition>> elements = new ArrayList<>();
  
  private final List<SlotPosition> inputSlots = new ArrayList<>();
  
  private final List<SlotPosition> outputSlots = new ArrayList<>();
  
  public DynamicCategory(ITeBlock block, T recipeManager, IGuiHelper guiHelper) {
    super(block, recipeManager);
    initializeWidgets(guiHelper, (GuiParser.ParentNode)GuiParser.parse(block));
  }
  
  private void initializeWidgets(IGuiHelper guiHelper, GuiParser.ParentNode parentNode) {
    label75: for (GuiParser.Node rawNode : parentNode.getNodes()) {
      GuiParser.EnergyGaugeNode energyGaugeNode;
      GuiParser.GaugeNode gaugeNode;
      GuiParser.ImageNode imageNode;
      GuiParser.SlotNode slotNode;
      GuiParser.SlotGridNode slotGridNode;
      GuiParser.EnvironmentNode node;
      SlotPosition slotPosition1;
      Gauge.GaugeProperties properties;
      SlotPosition pos;
      TileEntityInventory dummyTe;
      IDrawableStatic energyBackground;
      SlotPosition slotPosition2;
      IDrawableStatic iDrawableStatic2;
      IDrawableStatic iDrawableStatic1;
      InvSlot slot;
      IDrawableAnimated energyAnimated;
      IDrawableStatic guageBackground;
      int extraX;
      int size;
      IDrawableAnimated iDrawableAnimated1;
      int extraY;
      String slotName;
      switch (rawNode.getType()) {
        case energygauge:
          energyGaugeNode = (GuiParser.EnergyGaugeNode)rawNode;
          slotPosition1 = new SlotPosition(energyGaugeNode.x + energyGaugeNode.style.properties.bgXOffset + 0, energyGaugeNode.y + energyGaugeNode.style.properties.bgYOffset + -16);
          energyBackground = guiHelper.createDrawable(energyGaugeNode.style.properties.texture, energyGaugeNode.style.properties.uBgInactive, energyGaugeNode.style.properties.vBgInactive, energyGaugeNode.style.properties.bgWidth, energyGaugeNode.style.properties.bgHeight);
          this.elements.add(new Tuple.T2(energyBackground, slotPosition1));
          energyBackground = guiHelper.createDrawable(energyGaugeNode.style.properties.texture, energyGaugeNode.style.properties.uInner, energyGaugeNode.style.properties.vInner, energyGaugeNode.style.properties.innerWidth, energyGaugeNode.style.properties.innerHeight);
          energyAnimated = guiHelper.createAnimatedDrawable(energyBackground, 300, energyGaugeNode.style.properties.reverse ? (energyGaugeNode.style.properties.vertical ? IDrawableAnimated.StartDirection.TOP : IDrawableAnimated.StartDirection.RIGHT) : (energyGaugeNode.style.properties.vertical ? IDrawableAnimated.StartDirection.BOTTOM : IDrawableAnimated.StartDirection.LEFT), true);
          this.elements.add(new Tuple.T2(energyAnimated, new SlotPosition(energyGaugeNode.x + 0, energyGaugeNode.y + -16)));
        case gauge:
          gaugeNode = (GuiParser.GaugeNode)rawNode;
          properties = gaugeNode.style.getProperties();
          slotPosition2 = new SlotPosition(gaugeNode.x + properties.bgXOffset + 0, gaugeNode.y + properties.bgYOffset + -16);
          guageBackground = guiHelper.createDrawable(properties.texture, properties.uBgActive, properties.vBgActive, properties.bgWidth, properties.bgHeight);
          this.elements.add(new Tuple.T2(guageBackground, slotPosition2));
          guageBackground = guiHelper.createDrawable(properties.texture, properties.uInner, properties.vInner, properties.innerWidth, properties.innerHeight);
          if (gaugeNode.style == Gauge.GaugeStyle.HeatCentrifuge) {
            IDrawableStatic iDrawableStatic = guageBackground;
          } else {
            iDrawableAnimated1 = guiHelper.createAnimatedDrawable(guageBackground, getProcessSpeed(gaugeNode.name), properties.reverse ? (properties.vertical ? IDrawableAnimated.StartDirection.BOTTOM : IDrawableAnimated.StartDirection.RIGHT) : (properties.vertical ? IDrawableAnimated.StartDirection.TOP : IDrawableAnimated.StartDirection.LEFT), false);
          } 
          this.elements.add(new Tuple.T2(iDrawableAnimated1, new SlotPosition(gaugeNode.x + 0, gaugeNode.y + -16)));
        case image:
          imageNode = (GuiParser.ImageNode)rawNode;
          pos = new SlotPosition(imageNode.x + 0, imageNode.y + -16);
          iDrawableStatic2 = guiHelper.createDrawable(imageNode.src, imageNode.u1, imageNode.v1, imageNode.width, imageNode.height, imageNode.baseWidth, imageNode.baseHeight);
          this.elements.add(new Tuple.T2(iDrawableStatic2, pos));
        case slot:
          slotNode = (GuiParser.SlotNode)rawNode;
          pos = new SlotPosition(slotNode.x + 0, slotNode.y + -16, slotNode.style);
          iDrawableStatic1 = guiHelper.createDrawable(GuiElement.commonTexture, (pos.getStyle()).u, (pos.getStyle()).v, (pos.getStyle()).width, (pos.getStyle()).height);
          this.elements.add(new Tuple.T2(iDrawableStatic1, pos));
          extraY = extraX = 0;
          if (slotNode.style == SlotGrid.SlotStyle.Large)
            extraX = extraY = 4; 
          slotName = slotNode.name.toLowerCase(Locale.ENGLISH);
          if (slotName.contains("input") || slotName.equals("cutterInputSlot")) {
            this.inputSlots.add(new SlotPosition(pos, extraX, extraY));
            continue;
          } 
          if (slotName.contains("output"))
            this.outputSlots.add(new SlotPosition(pos, extraX, extraY)); 
        case slotgrid:
          slotGridNode = (GuiParser.SlotGridNode)rawNode;
          dummyTe = (TileEntityInventory)this.block.getDummyTe();
          if (dummyTe == null)
            throw new NullPointerException("Received null dummy for " + this.block + " in the JeiPlugin."); 
          slot = dummyTe.getInventorySlot(slotGridNode.name);
          if (slot == null)
            throw new RuntimeException("invalid invslot name " + slotGridNode.name + " for base " + dummyTe); 
          size = slot.size();
          if (size > slotGridNode.offset) {
            GuiParser.SlotGridNode.SlotGridDimension dim = slotGridNode.getDimension(size);
            IDrawableStatic iDrawableStatic = guiHelper.createDrawable(GuiElement.commonTexture, slotGridNode.style.u, slotGridNode.style.v, slotGridNode.style.width, slotGridNode.style.height);
            boolean isInput = slotGridNode.name.toLowerCase().contains("input");
            boolean isOutput = slotGridNode.name.toLowerCase().contains("output");
            for (int i = 0; i < dim.cols; i++) {
              for (int j = 0; j < dim.rows; j++) {
                if (i * dim.rows + j > size)
                  continue label75; 
                SlotPosition slotPosition = new SlotPosition(slotGridNode.x + 0 + i * slotGridNode.style.width, slotGridNode.y + -16 + j * slotGridNode.style.height, slotGridNode.style);
                this.elements.add(new Tuple.T2(iDrawableStatic, slotPosition));
                if (isInput) {
                  this.inputSlots.add(slotPosition);
                } else if (isOutput) {
                  this.outputSlots.add(slotPosition);
                } 
              } 
            } 
          } 
        case environment:
          node = (GuiParser.EnvironmentNode)rawNode;
          if (node.environment == GuiEnvironment.JEI)
            initializeWidgets(guiHelper, (GuiParser.ParentNode)node); 
      } 
    } 
  }
  
  public IDrawable getBackground() {
    return this;
  }
  
  public void drawExtras(Minecraft minecraft) {
    for (Tuple.T2<IDrawable, SlotPosition> element : this.elements)
      ((IDrawable)element.a).draw(minecraft, ((SlotPosition)element.b).getX(), ((SlotPosition)element.b).getY()); 
  }
  
  protected List<SlotPosition> getInputSlotPos() {
    return this.inputSlots;
  }
  
  protected List<SlotPosition> getoutputSlotPos() {
    return this.outputSlots;
  }
  
  public void draw(Minecraft minecraft) {}
  
  public void draw(Minecraft minecraft, int xOffset, int yOffset) {}
  
  public int getHeight() {
    return 60;
  }
  
  public int getWidth() {
    return 160;
  }
  
  protected int getProcessSpeed(String name) {
    if ("progress".equals(name)) {
      TileEntityBlock te = this.block.getDummyTe();
      if (te != null && te instanceof TileEntityStandardMachine)
        return ((TileEntityStandardMachine)te).defaultOperationLength / 3; 
    } 
    return 200;
  }
}

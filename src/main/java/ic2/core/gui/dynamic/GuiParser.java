package ic2.core.gui.dynamic;

import ic2.core.block.ITeBlock;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.Text;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Tuple;
import ic2.core.util.XmlUtil;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GuiParser {
  public static GuiNode parse(ITeBlock teBlock) {
    ResourceLocation loc = new ResourceLocation(teBlock.getIdentifier().func_110624_b(), "guidef/" + teBlock.getName() + ".xml");
    try {
      return parse(loc, teBlock.getTeClass());
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public static GuiNode parse(ResourceLocation location, Class<?> baseClass) throws IOException, SAXException {
    String fileLoc = "/assets/" + location.func_110624_b() + '/' + location.func_110623_a();
    InputStream is = GuiParser.class.getResourceAsStream(fileLoc);
    if (is == null)
      throw new FileNotFoundException("Could not load " + fileLoc + " from the classpath."); 
    try {
      is = new BufferedInputStream(is);
      return parse(is, baseClass);
    } finally {
      if (is != null)
        is.close(); 
    } 
  }
  
  private static GuiNode parse(InputStream is, Class<?> baseClass) throws SAXException, IOException {
    is = new BufferedInputStream(is);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      SAXParser parser = factory.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      SaxHandler handler = new SaxHandler(baseClass);
      reader.setContentHandler(handler);
      reader.parse(new InputSource(is));
      return handler.getResult();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static class SaxHandler extends DefaultHandler {
    private final Class<?> baseClass;
    
    private GuiParser.ParentNode parentNode;
    
    private GuiParser.Node currentNode;
    
    public SaxHandler(Class<?> baseClass) {
      this.baseClass = baseClass;
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      GuiParser.NodeType type = GuiParser.NodeType.get(qName);
      if (type == null)
        type = GuiParser.NodeType.get(qName.toLowerCase(Locale.ENGLISH)); 
      if (type == null)
        throw new SAXException("invalid element: " + qName); 
      if (type == GuiParser.NodeType.gui) {
        if (this.parentNode != null)
          throw new SAXException("invalid gui element location"); 
      } else if (this.parentNode == null) {
        throw new SAXException("invalid " + qName + " element location");
      } 
      switch (type) {
        case gui:
          this.currentNode = this.parentNode = new GuiParser.GuiNode(attributes, this.baseClass);
          break;
        case environment:
          this.currentNode = new GuiParser.EnvironmentNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          this.parentNode = (GuiParser.ParentNode)this.currentNode;
          break;
        case key:
          this.currentNode = GuiParser.KeyOnlyNode.getForKey(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          this.parentNode = (GuiParser.ParentNode)this.currentNode;
          break;
        case only:
          this.currentNode = GuiParser.LogicalNode.getForValue(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          this.parentNode = (GuiParser.ParentNode)this.currentNode;
          break;
        case tooltip:
          this.currentNode = new GuiParser.TooltipNode(this.parentNode, attributes.getValue("text"));
          this.parentNode.addNode(this.currentNode);
          this.parentNode = (GuiParser.ParentNode)this.currentNode;
          break;
        case button:
          this.currentNode = new GuiParser.ButtonNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case energygauge:
          this.currentNode = new GuiParser.EnergyGaugeNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case gauge:
          this.currentNode = new GuiParser.GaugeNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case image:
          this.currentNode = new GuiParser.ImageNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case playerinventory:
          this.currentNode = new GuiParser.PlayerInventoryNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case slot:
          this.currentNode = new GuiParser.SlotNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case slotgrid:
          this.currentNode = new GuiParser.SlotGridNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case slothologram:
          this.currentNode = new GuiParser.SlotHologramNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case text:
          this.currentNode = new GuiParser.TextNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case fluidtank:
          this.currentNode = new GuiParser.FluidTankNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case fluidslot:
          this.currentNode = new GuiParser.FluidSlotNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
      } 
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException {
      while (length > 0 && Character.isWhitespace(ch[start])) {
        start++;
        length--;
      } 
      while (length > 0 && Character.isWhitespace(ch[start + length - 1]))
        length--; 
      if (length != 0) {
        if (this.currentNode == null)
          throw new SAXException("unexpected characters"); 
        this.currentNode.setContent(new String(ch, start, length));
      } 
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (this.currentNode == this.parentNode) {
        if (this.currentNode.getType() == GuiParser.NodeType.gui) {
          this.currentNode = null;
        } else {
          this.currentNode = this.parentNode = this.parentNode.parent;
        } 
      } else {
        this.currentNode = this.parentNode;
      } 
    }
    
    public GuiParser.GuiNode getResult() {
      return (GuiParser.GuiNode)this.parentNode;
    }
  }
  
  public enum NodeType {
    gui, environment, key, only, tooltip, button, energygauge, gauge, image, playerinventory, slot, slotgrid, slothologram, text, fluidtank, fluidslot;
    
    private static Map<String, NodeType> map = getMap();
    
    public static NodeType get(String name) {
      return map.get(name);
    }
    
    private static Map<String, NodeType> getMap() {
      NodeType[] values = values();
      Map<String, NodeType> ret = new HashMap<>(values.length);
      for (NodeType type : values)
        ret.put(type.name(), type); 
      return ret;
    }
    
    static {
    
    }
  }
  
  public static abstract class Node {
    final GuiParser.ParentNode parent;
    
    Node(GuiParser.ParentNode parent) {
      this.parent = parent;
    }
    
    public abstract GuiParser.NodeType getType();
    
    public void setContent(String content) throws SAXException {
      throw new SAXException("unexpected characters");
    }
  }
  
  public static abstract class ParentNode extends Node {
    final List<GuiParser.Node> children;
    
    ParentNode(ParentNode parent) {
      super(parent);
      this.children = new ArrayList<>();
    }
    
    public void addNode(GuiParser.Node node) {
      this.children.add(node);
    }
    
    public Iterable<GuiParser.Node> getNodes() {
      return this.children;
    }
    
    public Class<?> getBaseClass() {
      return this.parent.getBaseClass();
    }
    
    void addElement(DynamicGui<?> gui, GuiElement<?> element) {
      Collection<IEnableHandler> handlers = addHandlers(gui, element, new ArrayList<>());
      if (!handlers.isEmpty())
        element.withEnableHandler(IEnableHandler.EnableHandlers.and(handlers.<IEnableHandler>toArray(new IEnableHandler[handlers.size()]))); 
      gui.addElement(element);
    }
    
    protected Collection<IEnableHandler> addHandlers(DynamicGui<?> gui, GuiElement<?> element, Collection<IEnableHandler> handlers) {
      return handlers;
    }
  }
  
  public static class GuiNode extends ParentNode {
    private final Class<?> baseClass;
    
    final int width;
    
    final int height;
    
    GuiNode(Attributes attributes, Class<?> baseClass) throws SAXException {
      super(null);
      this.baseClass = baseClass;
      this.width = XmlUtil.getIntAttr(attributes, "width");
      this.height = XmlUtil.getIntAttr(attributes, "height");
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.gui;
    }
    
    public Class<?> getBaseClass() {
      return this.baseClass;
    }
  }
  
  public static class EnvironmentNode extends ParentNode {
    public final GuiEnvironment environment;
    
    EnvironmentNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      String name = XmlUtil.getAttr(attributes, "name");
      this.environment = GuiEnvironment.get(name);
      if (this.environment == null)
        throw new SAXException("invalid environment name: " + name); 
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.environment;
    }
  }
  
  public static abstract class KeyOnlyNode extends ParentNode {
    protected final boolean inverted;
    
    static KeyOnlyNode getForKey(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      String key = XmlUtil.getAttr(attributes, "key");
      boolean inverted = (attributes.getValue("inverted") != null);
      if ("shift".equalsIgnoreCase(key))
        return new GuiParser.ShiftOnlyNode(parent, inverted); 
      if ("control".equalsIgnoreCase(key))
        return new GuiParser.ControlOnlyNode(parent, inverted); 
      if ("alt".equalsIgnoreCase(key))
        return new GuiParser.AltOnlyNode(parent, inverted); 
      throw new SAXException("Invalid/Unsupported key name: " + key);
    }
    
    protected KeyOnlyNode(GuiParser.ParentNode parent, boolean inverted) {
      super(parent);
      this.inverted = inverted;
    }
    
    public final GuiParser.NodeType getType() {
      return GuiParser.NodeType.key;
    }
    
    protected Collection<IEnableHandler> addHandlers(DynamicGui<?> gui, GuiElement<?> element, Collection<IEnableHandler> handlers) {
      handlers.add(this.inverted ? IEnableHandler.EnableHandlers.not(this::isKeyDown) : this::isKeyDown);
      return super.addHandlers(gui, element, handlers);
    }
    
    protected abstract boolean isKeyDown();
  }
  
  static class ShiftOnlyNode extends KeyOnlyNode {
    ShiftOnlyNode(GuiParser.ParentNode parent, boolean inverted) {
      super(parent, inverted);
    }
    
    protected boolean isKeyDown() {
      return GuiScreen.func_146272_n();
    }
  }
  
  static class ControlOnlyNode extends KeyOnlyNode {
    ControlOnlyNode(GuiParser.ParentNode parent, boolean inverted) {
      super(parent, inverted);
    }
    
    protected boolean isKeyDown() {
      return GuiScreen.func_146271_m();
    }
  }
  
  static class AltOnlyNode extends KeyOnlyNode {
    AltOnlyNode(GuiParser.ParentNode parent, boolean inverted) {
      super(parent, inverted);
    }
    
    protected boolean isKeyDown() {
      return GuiScreen.func_175283_s();
    }
  }
  
  public static class LogicalNode extends ParentNode {
    final String condition;
    
    protected final boolean inverted;
    
    static LogicalNode getForValue(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      return new LogicalNode(parent, XmlUtil.getAttr(attributes, "if"), (attributes.getValue("inverted") != null));
    }
    
    protected LogicalNode(GuiParser.ParentNode parent, String condition, boolean inverted) {
      super(parent);
      this.condition = condition;
      this.inverted = inverted;
    }
    
    public final GuiParser.NodeType getType() {
      return GuiParser.NodeType.only;
    }
    
    protected Collection<IEnableHandler> addHandlers(DynamicGui<?> gui, GuiElement<?> element, Collection<IEnableHandler> handlers) {
      if (!((gui.getContainer()).base instanceof IGuiConditionProvider))
        throw new RuntimeException("Invalid base " + (gui.getContainer()).base + " for conditional elements"); 
      IEnableHandler handler = () -> ((IGuiConditionProvider)(gui.getContainer()).base).getGuiState(this.condition);
      handlers.add(this.inverted ? IEnableHandler.EnableHandlers.not(handler) : handler);
      return super.addHandlers(gui, element, handlers);
    }
  }
  
  public static class TooltipNode extends ParentNode {
    final String tooltip;
    
    public TooltipNode(GuiParser.ParentNode parent, String tooltip) {
      super(parent);
      this.tooltip = tooltip;
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.tooltip;
    }
    
    void addElement(DynamicGui<?> gui, GuiElement<?> element) {
      this.parent.addElement(gui, element.withTooltip(this.tooltip));
    }
  }
  
  public static class ButtonNode extends Node {
    final int x;
    
    final int y;
    
    final int width;
    
    final int height;
    
    final int eventID;
    
    final String eventName;
    
    final ItemStack icon;
    
    TextProvider.ITextProvider text;
    
    final ButtonType type;
    
    ButtonNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.x = XmlUtil.getIntAttr(attributes, "x");
      this.y = XmlUtil.getIntAttr(attributes, "y");
      this.width = XmlUtil.getIntAttr(attributes, "width", 16);
      this.height = XmlUtil.getIntAttr(attributes, "height", 16);
      Tuple.T2<Integer, String> event = getEventID(attributes);
      if (event.a == null) {
        this.eventID = 0;
        this.eventName = (String)event.b;
      } else {
        this.eventID = ((Integer)event.a).intValue();
        this.eventName = null;
      } 
      String typeName = XmlUtil.getAttr(attributes, "type", "vanilla");
      this.type = ButtonType.get(typeName);
      if (this.type == null)
        throw new SAXException("Invalid button type: " + typeName); 
      String icon = XmlUtil.getAttr(attributes, "icon", "none");
      if ("none".equals(icon)) {
        this.icon = null;
      } else {
        try {
          this.icon = ConfigUtil.asStack(icon);
        } catch (ParseException e) {
          throw new SAXException("Invalid/Unknown icon requested: " + icon, e);
        } 
      } 
    }
    
    protected Tuple.T2<Integer, String> getEventID(Attributes attributes) throws SAXException {
      Integer ID;
      String name;
      try {
        ID = Integer.valueOf(XmlUtil.getIntAttr(attributes, "event"));
        name = null;
      } catch (NumberFormatException e) {
        ID = null;
        name = XmlUtil.getAttr(attributes, "event");
      } 
      return new Tuple.T2(ID, name);
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.button;
    }
    
    public void setContent(String content) throws SAXException {
      if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        Mouse.setGrabbed(false); 
      this.text = TextProvider.parse(content, this.parent.getBaseClass());
    }
    
    public enum ButtonType {
      VANILLA, CUSTOM, TRANSPARENT, RECIPE;
      
      public static ButtonType get(String name) {
        for (ButtonType button : values()) {
          if (button.name().equalsIgnoreCase(name))
            return button; 
        } 
        return null;
      }
    }
  }
  
  public static class EnergyGaugeNode extends Node {
    public final int x;
    
    public final int y;
    
    public final EnergyGauge.EnergyGaugeStyle style;
    
    EnergyGaugeNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.x = XmlUtil.getIntAttr(attributes, "x");
      this.y = XmlUtil.getIntAttr(attributes, "y");
      String styleName = XmlUtil.getAttr(attributes, "style", "bolt");
      this.style = EnergyGauge.EnergyGaugeStyle.get(styleName);
      if (this.style == null)
        throw new SAXException("invalid gauge style: " + styleName); 
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.energygauge;
    }
  }
  
  public static class GaugeNode extends Node {
    public final int x;
    
    public final int y;
    
    public final String name;
    
    public final Gauge.IGaugeStyle style;
    
    public final boolean activeLinked;
    
    GaugeNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.x = XmlUtil.getIntAttr(attributes, "x");
      this.y = XmlUtil.getIntAttr(attributes, "y");
      this.name = XmlUtil.getAttr(attributes, "name");
      String styleName = XmlUtil.getAttr(attributes, "style", "normal");
      this.style = Gauge.GaugeStyle.get(styleName);
      if (this.style == null)
        throw new SAXException("invalid gauge style: " + styleName); 
      this.activeLinked = XmlUtil.getBoolAttr(attributes, "active", false);
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.gauge;
    }
  }
  
  public static class ImageNode extends Node {
    public final int x;
    
    public final int y;
    
    public final int width;
    
    public final int height;
    
    public final int baseWidth;
    
    public final int baseHeight;
    
    public final int u1;
    
    public final int v1;
    
    public final int u2;
    
    public final int v2;
    
    public final ResourceLocation src;
    
    ImageNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      String domain, file;
      this.x = XmlUtil.getIntAttr(attributes, "x");
      this.y = XmlUtil.getIntAttr(attributes, "y");
      this.width = XmlUtil.getIntAttr(attributes, "width", -1);
      this.height = XmlUtil.getIntAttr(attributes, "height", -1);
      this.baseWidth = XmlUtil.getIntAttr(attributes, "basewidth", "basesize", 0);
      this.baseHeight = XmlUtil.getIntAttr(attributes, "baseheight", "basesize", 0);
      this.u1 = XmlUtil.getIntAttr(attributes, "u", "u1", 0);
      this.v1 = XmlUtil.getIntAttr(attributes, "v", "v1", 0);
      this.u2 = XmlUtil.getIntAttr(attributes, "u2", -1);
      this.v2 = XmlUtil.getIntAttr(attributes, "v2", -1);
      String resLoc = XmlUtil.getAttr(attributes, "src");
      if (resLoc.isEmpty())
        throw new SAXException("empty src"); 
      int pos = resLoc.indexOf(':');
      if (pos == -1) {
        domain = "ic2";
        file = resLoc;
      } else {
        domain = resLoc.substring(0, pos);
        file = resLoc.substring(pos + 1);
      } 
      if (!file.endsWith(".png"))
        file = file + ".png"; 
      this.src = new ResourceLocation(domain, file);
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.image;
    }
  }
  
  protected static class PlayerInventoryNode extends Node {
    final int x;
    
    final int y;
    
    final int spacing;
    
    final int hotbarOffset;
    
    final boolean showTitle;
    
    final SlotGrid.SlotStyle style;
    
    PlayerInventoryNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.x = XmlUtil.getIntAttr(attributes, "x");
      this.y = XmlUtil.getIntAttr(attributes, "y");
      this.spacing = XmlUtil.getIntAttr(attributes, "spacing", 0);
      this.hotbarOffset = XmlUtil.getIntAttr(attributes, "hotbaroffset", 58);
      String styleName = XmlUtil.getAttr(attributes, "style", "normal");
      this.style = SlotGrid.SlotStyle.get(styleName);
      if (this.style == null)
        throw new SAXException("Invalid inventory slot style: " + styleName); 
      this.showTitle = XmlUtil.getBoolAttr(attributes, "title", true);
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.playerinventory;
    }
  }
  
  public static class SlotNode extends Node {
    public final int x;
    
    public final int y;
    
    public final String name;
    
    public final int index;
    
    public final SlotGrid.SlotStyle style;
    
    SlotNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.x = XmlUtil.getIntAttr(attributes, "x");
      this.y = XmlUtil.getIntAttr(attributes, "y");
      this.name = XmlUtil.getAttr(attributes, "name");
      this.index = XmlUtil.getIntAttr(attributes, "index", 0);
      String styleName = XmlUtil.getAttr(attributes, "style", "normal");
      this.style = SlotGrid.SlotStyle.get(styleName);
      if (this.style == null)
        throw new SAXException("invalid slot style: " + styleName); 
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.slot;
    }
  }
  
  public static class SlotGridNode extends Node {
    public final int x;
    
    public final int y;
    
    public final String name;
    
    public final int spacing;
    
    public final int offset;
    
    public final int rows;
    
    public final int cols;
    
    public final boolean vertical;
    
    public final SlotGrid.SlotStyle style;
    
    SlotGridNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.x = XmlUtil.getIntAttr(attributes, "x");
      this.y = XmlUtil.getIntAttr(attributes, "y");
      this.name = XmlUtil.getAttr(attributes, "name");
      this.spacing = XmlUtil.getIntAttr(attributes, "spacing", 0);
      this.offset = XmlUtil.getIntAttr(attributes, "offset", 0);
      this.rows = XmlUtil.getIntAttr(attributes, "rows", -1);
      this.cols = XmlUtil.getIntAttr(attributes, "cols", -1);
      this.vertical = XmlUtil.getBoolAttr(attributes, "vertical", false);
      String styleName = XmlUtil.getAttr(attributes, "style", "normal");
      this.style = SlotGrid.SlotStyle.get(styleName);
      if (this.style == null)
        throw new SAXException("invalid slot style: " + styleName); 
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.slotgrid;
    }
    
    public SlotGridDimension getDimension(int totalSize) {
      totalSize -= this.offset;
      if (!this.vertical) {
        if (this.cols > 0)
          return new SlotGridDimension(Math.max(this.rows, (totalSize + this.cols - 1) / this.cols), this.cols); 
        if (this.rows > 0)
          return new SlotGridDimension(this.rows, (totalSize + this.rows - 1) / this.rows); 
        int cols = (int)Math.floor(Math.sqrt(totalSize));
        return new SlotGridDimension((totalSize + cols - 1) / cols, cols);
      } 
      if (this.rows > 0)
        return new SlotGridDimension(this.rows, Math.max(this.cols, (totalSize + this.rows - 1) / this.rows)); 
      if (this.cols > 0)
        return new SlotGridDimension((totalSize + this.cols - 1) / this.cols, this.cols); 
      int rows = (int)Math.floor(Math.sqrt(totalSize));
      return new SlotGridDimension(rows, (totalSize + rows - 1) / rows);
    }
    
    public static class SlotGridDimension {
      public final int rows;
      
      public final int cols;
      
      public SlotGridDimension(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
      }
    }
  }
  
  public static class SlotHologramNode extends SlotNode {
    public final int index;
    
    public final int stackSizeLimit;
    
    SlotHologramNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent, attributes);
      this.index = XmlUtil.getIntAttr(attributes, "index", 0);
      this.stackSizeLimit = XmlUtil.getIntAttr(attributes, "stack", 64);
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.slothologram;
    }
  }
  
  public static class TextNode extends Node {
    private static final int defaultColor = 4210752;
    
    public final int x;
    
    public final int y;
    
    public final int width;
    
    public final int height;
    
    public final int xOffset;
    
    public final int yOffset;
    
    public final boolean centerX;
    
    public final boolean centerY;
    
    public final boolean rightAligned;
    
    public final Text.TextAlignment align;
    
    public final int color;
    
    public final boolean shadow;
    
    public TextProvider.ITextProvider text;
    
    TextNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.x = XmlUtil.getIntAttr(attributes, "x", 0);
      this.y = XmlUtil.getIntAttr(attributes, "y", 0);
      this.width = XmlUtil.getIntAttr(attributes, "width", -1);
      this.height = XmlUtil.getIntAttr(attributes, "height", -1);
      this.xOffset = XmlUtil.getIntAttr(attributes, "xoffset", 0);
      this.yOffset = XmlUtil.getIntAttr(attributes, "yoffset", 0);
      String alignName = XmlUtil.getAttr(attributes, "align", "start");
      this.align = Text.TextAlignment.get(alignName);
      if (this.align == null)
        throw new SAXException("invalid alignment: " + alignName); 
      String center = XmlUtil.getAttr(attributes, "center", (this.align == Text.TextAlignment.Center) ? "x" : "");
      this.centerX = (center.indexOf('x') != -1);
      this.centerY = (center.indexOf('y') != -1);
      this.rightAligned = XmlUtil.getBoolAttr(attributes, "right", false);
      this.color = XmlUtil.getIntAttr(attributes, "color", 4210752);
      this.shadow = (attributes.getIndex("shadow") != -1);
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.text;
    }
    
    public void setContent(String content) throws SAXException {
      if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        Mouse.setGrabbed(false); 
      this.text = TextProvider.parse(content, this.parent.getBaseClass());
    }
  }
  
  public static class FluidTankNode extends Node {
    final int x;
    
    final int y;
    
    final String name;
    
    final int width;
    
    final int height;
    
    final boolean mirrored;
    
    final TankType type;
    
    FluidTankNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.x = XmlUtil.getIntAttr(attributes, "x");
      this.y = XmlUtil.getIntAttr(attributes, "y");
      this.name = XmlUtil.getAttr(attributes, "name");
      String typeName = XmlUtil.getAttr(attributes, "type", "normal");
      this.type = TankType.get(typeName);
      if (this.type == null)
        throw new SAXException("Invalid type: " + typeName); 
      if (this.type == TankType.PLAIN) {
        this.width = XmlUtil.getIntAttr(attributes, "width");
        this.height = XmlUtil.getIntAttr(attributes, "height");
      } else {
        this.width = this.height = -1;
      } 
      if (this.type == TankType.BORDERLESS) {
        this.mirrored = XmlUtil.getBoolAttr(attributes, "mirrored");
      } else {
        this.mirrored = false;
      } 
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.fluidtank;
    }
    
    public enum TankType {
      NORMAL, PLAIN, BORDERLESS;
      
      public static TankType get(String name) {
        for (TankType type : values()) {
          if (type.name().equalsIgnoreCase(name))
            return type; 
        } 
        return null;
      }
    }
  }
  
  public static class FluidSlotNode extends Node {
    final int x;
    
    final int y;
    
    final String name;
    
    FluidSlotNode(GuiParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.x = XmlUtil.getIntAttr(attributes, "x");
      this.y = XmlUtil.getIntAttr(attributes, "y");
      this.name = XmlUtil.getAttr(attributes, "name");
    }
    
    public GuiParser.NodeType getType() {
      return GuiParser.NodeType.fluidslot;
    }
  }
}

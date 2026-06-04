// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui.dynamic;

import ic2.core.gui.Text;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.Gauge;
import ic2.core.gui.EnergyGauge;
import org.lwjgl.input.Mouse;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.FMLCommonHandler;
import ic2.core.util.Tuple;
import java.text.ParseException;
import ic2.core.util.ConfigUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.util.XmlUtil;
import java.util.Collection;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.GuiElement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.ITeBlock;

public class GuiParser
{
    public static GuiNode parse(final ITeBlock teBlock) {
        final ResourceLocation loc = new ResourceLocation(teBlock.getIdentifier().getResourceDomain(), "guidef/" + teBlock.getName() + ".xml");
        try {
            return parse(loc, teBlock.getTeClass());
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static GuiNode parse(final ResourceLocation location, final Class<?> baseClass) throws IOException, SAXException {
        final String fileLoc = "/assets/" + location.getResourceDomain() + '/' + location.getResourcePath();
        InputStream is = GuiParser.class.getResourceAsStream(fileLoc);
        if (is == null) {
            throw new FileNotFoundException("Could not load " + fileLoc + " from the classpath.");
        }
        try {
            is = new BufferedInputStream(is);
            return parse(is, baseClass);
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
    }
    
    private static GuiNode parse(InputStream is, final Class<?> baseClass) throws SAXException, IOException {
        is = new BufferedInputStream(is);
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            final SAXParser parser = factory.newSAXParser();
            final XMLReader reader = parser.getXMLReader();
            final SaxHandler handler = new SaxHandler(baseClass);
            reader.setContentHandler(handler);
            reader.parse(new InputSource(is));
            return handler.getResult();
        }
        catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static class SaxHandler extends DefaultHandler
    {
        private final Class<?> baseClass;
        private ParentNode parentNode;
        private Node currentNode;
        
        public SaxHandler(final Class<?> baseClass) {
            this.baseClass = baseClass;
        }
        
        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            NodeType type = NodeType.get(qName);
            if (type == null) {
                type = NodeType.get(qName.toLowerCase(Locale.ENGLISH));
            }
            if (type == null) {
                throw new SAXException("invalid element: " + qName);
            }
            if (type == NodeType.gui) {
                if (this.parentNode != null) {
                    throw new SAXException("invalid gui element location");
                }
            }
            else if (this.parentNode == null) {
                throw new SAXException("invalid " + qName + " element location");
            }
            switch (type) {
                case gui: {
                    final GuiNode guiNode = new GuiNode(attributes, this.baseClass);
                    this.parentNode = guiNode;
                    this.currentNode = guiNode;
                    break;
                }
                case environment: {
                    this.currentNode = new EnvironmentNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    this.parentNode = (ParentNode)this.currentNode;
                    break;
                }
                case key: {
                    this.currentNode = KeyOnlyNode.getForKey(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    this.parentNode = (ParentNode)this.currentNode;
                    break;
                }
                case only: {
                    this.currentNode = LogicalNode.getForValue(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    this.parentNode = (ParentNode)this.currentNode;
                    break;
                }
                case tooltip: {
                    this.currentNode = new TooltipNode(this.parentNode, attributes.getValue("text"));
                    this.parentNode.addNode(this.currentNode);
                    this.parentNode = (ParentNode)this.currentNode;
                    break;
                }
                case button: {
                    this.currentNode = new ButtonNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case energygauge: {
                    this.currentNode = new EnergyGaugeNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case gauge: {
                    this.currentNode = new GaugeNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case image: {
                    this.currentNode = new ImageNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case playerinventory: {
                    this.currentNode = new PlayerInventoryNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case slot: {
                    this.currentNode = new SlotNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case slotgrid: {
                    this.currentNode = new SlotGridNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case slothologram: {
                    this.currentNode = new SlotHologramNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case text: {
                    this.currentNode = new TextNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case fluidtank: {
                    this.currentNode = new FluidTankNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case fluidslot: {
                    this.currentNode = new FluidSlotNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
            }
        }
        
        @Override
        public void characters(final char[] ch, int start, int length) throws SAXException {
            while (length > 0 && Character.isWhitespace(ch[start])) {
                ++start;
                --length;
            }
            while (length > 0 && Character.isWhitespace(ch[start + length - 1])) {
                --length;
            }
            if (length != 0) {
                if (this.currentNode == null) {
                    throw new SAXException("unexpected characters");
                }
                this.currentNode.setContent(new String(ch, start, length));
            }
        }
        
        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (this.currentNode == this.parentNode) {
                if (this.currentNode.getType() == NodeType.gui) {
                    this.currentNode = null;
                }
                else {
                    final ParentNode parent = this.parentNode.parent;
                    this.parentNode = parent;
                    this.currentNode = parent;
                }
            }
            else {
                this.currentNode = this.parentNode;
            }
        }
        
        public GuiNode getResult() {
            return (GuiNode)this.parentNode;
        }
    }
    
    public enum NodeType
    {
        gui, 
        environment, 
        key, 
        only, 
        tooltip, 
        button, 
        energygauge, 
        gauge, 
        image, 
        playerinventory, 
        slot, 
        slotgrid, 
        slothologram, 
        text, 
        fluidtank, 
        fluidslot;
        
        private static Map<String, NodeType> map;
        
        public static NodeType get(final String name) {
            return NodeType.map.get(name);
        }
        
        private static Map<String, NodeType> getMap() {
            final NodeType[] values = values();
            final Map<String, NodeType> ret = new HashMap<String, NodeType>(values.length);
            for (final NodeType type : values) {
                ret.put(type.name(), type);
            }
            return ret;
        }
        
        static {
            NodeType.map = getMap();
        }
    }
    
    public abstract static class Node
    {
        final ParentNode parent;
        
        Node(final ParentNode parent) {
            this.parent = parent;
        }
        
        public abstract NodeType getType();
        
        public void setContent(final String content) throws SAXException {
            throw new SAXException("unexpected characters");
        }
    }
    
    public abstract static class ParentNode extends Node
    {
        final List<Node> children;
        
        ParentNode(final ParentNode parent) {
            super(parent);
            this.children = new ArrayList<Node>();
        }
        
        public void addNode(final Node node) {
            this.children.add(node);
        }
        
        public Iterable<Node> getNodes() {
            return this.children;
        }
        
        public Class<?> getBaseClass() {
            return this.parent.getBaseClass();
        }
        
        void addElement(final DynamicGui<?> gui, final GuiElement<?> element) {
            final Collection<IEnableHandler> handlers = this.addHandlers(gui, element, new ArrayList<IEnableHandler>());
            if (!handlers.isEmpty()) {
                element.withEnableHandler(IEnableHandler.EnableHandlers.and((IEnableHandler[])handlers.toArray(new IEnableHandler[handlers.size()])));
            }
            gui.addElement(element);
        }
        
        protected Collection<IEnableHandler> addHandlers(final DynamicGui<?> gui, final GuiElement<?> element, final Collection<IEnableHandler> handlers) {
            return handlers;
        }
    }
    
    public static class GuiNode extends ParentNode
    {
        private final Class<?> baseClass;
        final int width;
        final int height;
        
        GuiNode(final Attributes attributes, final Class<?> baseClass) throws SAXException {
            super(null);
            this.baseClass = baseClass;
            this.width = XmlUtil.getIntAttr(attributes, "width");
            this.height = XmlUtil.getIntAttr(attributes, "height");
        }
        
        @Override
        public NodeType getType() {
            return NodeType.gui;
        }
        
        @Override
        public Class<?> getBaseClass() {
            return this.baseClass;
        }
    }
    
    public static class EnvironmentNode extends ParentNode
    {
        public final GuiEnvironment environment;
        
        EnvironmentNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            final String name = XmlUtil.getAttr(attributes, "name");
            this.environment = GuiEnvironment.get(name);
            if (this.environment == null) {
                throw new SAXException("invalid environment name: " + name);
            }
        }
        
        @Override
        public NodeType getType() {
            return NodeType.environment;
        }
    }
    
    public abstract static class KeyOnlyNode extends ParentNode
    {
        protected final boolean inverted;
        
        static KeyOnlyNode getForKey(final ParentNode parent, final Attributes attributes) throws SAXException {
            final String key = XmlUtil.getAttr(attributes, "key");
            final boolean inverted = attributes.getValue("inverted") != null;
            if ("shift".equalsIgnoreCase(key)) {
                return new ShiftOnlyNode(parent, inverted);
            }
            if ("control".equalsIgnoreCase(key)) {
                return new ControlOnlyNode(parent, inverted);
            }
            if ("alt".equalsIgnoreCase(key)) {
                return new AltOnlyNode(parent, inverted);
            }
            throw new SAXException("Invalid/Unsupported key name: " + key);
        }
        
        protected KeyOnlyNode(final ParentNode parent, final boolean inverted) {
            super(parent);
            this.inverted = inverted;
        }
        
        @Override
        public final NodeType getType() {
            return NodeType.key;
        }
        
        @Override
        protected Collection<IEnableHandler> addHandlers(final DynamicGui<?> gui, final GuiElement<?> element, final Collection<IEnableHandler> handlers) {
            handlers.add(this.inverted ? IEnableHandler.EnableHandlers.not(this::isKeyDown) : this::isKeyDown);
            return super.addHandlers(gui, element, handlers);
        }
        
        protected abstract boolean isKeyDown();
    }
    
    static class ShiftOnlyNode extends KeyOnlyNode
    {
        ShiftOnlyNode(final ParentNode parent, final boolean inverted) {
            super(parent, inverted);
        }
        
        @Override
        protected boolean isKeyDown() {
            return GuiScreen.isShiftKeyDown();
        }
    }
    
    static class ControlOnlyNode extends KeyOnlyNode
    {
        ControlOnlyNode(final ParentNode parent, final boolean inverted) {
            super(parent, inverted);
        }
        
        @Override
        protected boolean isKeyDown() {
            return GuiScreen.isCtrlKeyDown();
        }
    }
    
    static class AltOnlyNode extends KeyOnlyNode
    {
        AltOnlyNode(final ParentNode parent, final boolean inverted) {
            super(parent, inverted);
        }
        
        @Override
        protected boolean isKeyDown() {
            return GuiScreen.isAltKeyDown();
        }
    }
    
    public static class LogicalNode extends ParentNode
    {
        final String condition;
        protected final boolean inverted;
        
        static LogicalNode getForValue(final ParentNode parent, final Attributes attributes) throws SAXException {
            return new LogicalNode(parent, XmlUtil.getAttr(attributes, "if"), attributes.getValue("inverted") != null);
        }
        
        protected LogicalNode(final ParentNode parent, final String condition, final boolean inverted) {
            super(parent);
            this.condition = condition;
            this.inverted = inverted;
        }
        
        @Override
        public final NodeType getType() {
            return NodeType.only;
        }
        
        @Override
        protected Collection<IEnableHandler> addHandlers(final DynamicGui<?> gui, final GuiElement<?> element, final Collection<IEnableHandler> handlers) {
            if (!(gui.getContainer().base instanceof IGuiConditionProvider)) {
                throw new RuntimeException("Invalid base " + gui.getContainer().base + " for conditional elements");
            }
            final IEnableHandler handler = () -> ((IGuiConditionProvider)gui.getContainer().base).getGuiState(this.condition);
            handlers.add(this.inverted ? IEnableHandler.EnableHandlers.not(handler) : handler);
            return super.addHandlers(gui, element, handlers);
        }
    }
    
    public static class TooltipNode extends ParentNode
    {
        final String tooltip;
        
        public TooltipNode(final ParentNode parent, final String tooltip) {
            super(parent);
            this.tooltip = tooltip;
        }
        
        @Override
        public NodeType getType() {
            return NodeType.tooltip;
        }
        
        @Override
        void addElement(final DynamicGui<?> gui, final GuiElement<?> element) {
            this.parent.addElement(gui, (GuiElement<?>)element.withTooltip(this.tooltip));
        }
    }
    
    public static class ButtonNode extends Node
    {
        final int x;
        final int y;
        final int width;
        final int height;
        final int eventID;
        final String eventName;
        final ItemStack icon;
        TextProvider.ITextProvider text;
        final ButtonType type;
        
        ButtonNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.x = XmlUtil.getIntAttr(attributes, "x");
            this.y = XmlUtil.getIntAttr(attributes, "y");
            this.width = XmlUtil.getIntAttr(attributes, "width", 16);
            this.height = XmlUtil.getIntAttr(attributes, "height", 16);
            final Tuple.T2<Integer, String> event = this.getEventID(attributes);
            if (event.a == null) {
                this.eventID = 0;
                this.eventName = event.b;
            }
            else {
                this.eventID = event.a;
                this.eventName = null;
            }
            final String typeName = XmlUtil.getAttr(attributes, "type", "vanilla");
            this.type = ButtonType.get(typeName);
            if (this.type == null) {
                throw new SAXException("Invalid button type: " + typeName);
            }
            final String icon = XmlUtil.getAttr(attributes, "icon", "none");
            if ("none".equals(icon)) {
                this.icon = null;
            }
            else {
                try {
                    this.icon = ConfigUtil.asStack(icon);
                }
                catch (final ParseException e) {
                    throw new SAXException("Invalid/Unknown icon requested: " + icon, e);
                }
            }
        }
        
        protected Tuple.T2<Integer, String> getEventID(final Attributes attributes) throws SAXException {
            Integer ID;
            String name;
            try {
                ID = XmlUtil.getIntAttr(attributes, "event");
                name = null;
            }
            catch (final NumberFormatException e) {
                ID = null;
                name = XmlUtil.getAttr(attributes, "event");
            }
            return new Tuple.T2<Integer, String>(ID, name);
        }
        
        @Override
        public NodeType getType() {
            return NodeType.button;
        }
        
        @Override
        public void setContent(final String content) throws SAXException {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                Mouse.setGrabbed(false);
            }
            this.text = TextProvider.parse(content, this.parent.getBaseClass());
        }
        
        public enum ButtonType
        {
            VANILLA, 
            CUSTOM, 
            TRANSPARENT, 
            RECIPE;
            
            public static ButtonType get(final String name) {
                for (final ButtonType button : values()) {
                    if (button.name().equalsIgnoreCase(name)) {
                        return button;
                    }
                }
                return null;
            }
        }
    }
    
    public static class EnergyGaugeNode extends Node
    {
        public final int x;
        public final int y;
        public final EnergyGauge.EnergyGaugeStyle style;
        
        EnergyGaugeNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.x = XmlUtil.getIntAttr(attributes, "x");
            this.y = XmlUtil.getIntAttr(attributes, "y");
            final String styleName = XmlUtil.getAttr(attributes, "style", "bolt");
            this.style = EnergyGauge.EnergyGaugeStyle.get(styleName);
            if (this.style == null) {
                throw new SAXException("invalid gauge style: " + styleName);
            }
        }
        
        @Override
        public NodeType getType() {
            return NodeType.energygauge;
        }
    }
    
    public static class GaugeNode extends Node
    {
        public final int x;
        public final int y;
        public final String name;
        public final Gauge.IGaugeStyle style;
        public final boolean activeLinked;
        
        GaugeNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.x = XmlUtil.getIntAttr(attributes, "x");
            this.y = XmlUtil.getIntAttr(attributes, "y");
            this.name = XmlUtil.getAttr(attributes, "name");
            final String styleName = XmlUtil.getAttr(attributes, "style", "normal");
            this.style = Gauge.GaugeStyle.get(styleName);
            if (this.style == null) {
                throw new SAXException("invalid gauge style: " + styleName);
            }
            this.activeLinked = XmlUtil.getBoolAttr(attributes, "active", false);
        }
        
        @Override
        public NodeType getType() {
            return NodeType.gauge;
        }
    }
    
    public static class ImageNode extends Node
    {
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
        
        ImageNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
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
            final String resLoc = XmlUtil.getAttr(attributes, "src");
            if (resLoc.isEmpty()) {
                throw new SAXException("empty src");
            }
            final int pos = resLoc.indexOf(58);
            String domain;
            String file;
            if (pos == -1) {
                domain = "ic2";
                file = resLoc;
            }
            else {
                domain = resLoc.substring(0, pos);
                file = resLoc.substring(pos + 1);
            }
            if (!file.endsWith(".png")) {
                file += ".png";
            }
            this.src = new ResourceLocation(domain, file);
        }
        
        @Override
        public NodeType getType() {
            return NodeType.image;
        }
    }
    
    protected static class PlayerInventoryNode extends Node
    {
        final int x;
        final int y;
        final int spacing;
        final int hotbarOffset;
        final boolean showTitle;
        final SlotGrid.SlotStyle style;
        
        PlayerInventoryNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.x = XmlUtil.getIntAttr(attributes, "x");
            this.y = XmlUtil.getIntAttr(attributes, "y");
            this.spacing = XmlUtil.getIntAttr(attributes, "spacing", 0);
            this.hotbarOffset = XmlUtil.getIntAttr(attributes, "hotbaroffset", 58);
            final String styleName = XmlUtil.getAttr(attributes, "style", "normal");
            this.style = SlotGrid.SlotStyle.get(styleName);
            if (this.style == null) {
                throw new SAXException("Invalid inventory slot style: " + styleName);
            }
            this.showTitle = XmlUtil.getBoolAttr(attributes, "title", true);
        }
        
        @Override
        public NodeType getType() {
            return NodeType.playerinventory;
        }
    }
    
    public static class SlotNode extends Node
    {
        public final int x;
        public final int y;
        public final String name;
        public final int index;
        public final SlotGrid.SlotStyle style;
        
        SlotNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.x = XmlUtil.getIntAttr(attributes, "x");
            this.y = XmlUtil.getIntAttr(attributes, "y");
            this.name = XmlUtil.getAttr(attributes, "name");
            this.index = XmlUtil.getIntAttr(attributes, "index", 0);
            final String styleName = XmlUtil.getAttr(attributes, "style", "normal");
            this.style = SlotGrid.SlotStyle.get(styleName);
            if (this.style == null) {
                throw new SAXException("invalid slot style: " + styleName);
            }
        }
        
        @Override
        public NodeType getType() {
            return NodeType.slot;
        }
    }
    
    public static class SlotGridNode extends Node
    {
        public final int x;
        public final int y;
        public final String name;
        public final int spacing;
        public final int offset;
        public final int rows;
        public final int cols;
        public final boolean vertical;
        public final SlotGrid.SlotStyle style;
        
        SlotGridNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.x = XmlUtil.getIntAttr(attributes, "x");
            this.y = XmlUtil.getIntAttr(attributes, "y");
            this.name = XmlUtil.getAttr(attributes, "name");
            this.spacing = XmlUtil.getIntAttr(attributes, "spacing", 0);
            this.offset = XmlUtil.getIntAttr(attributes, "offset", 0);
            this.rows = XmlUtil.getIntAttr(attributes, "rows", -1);
            this.cols = XmlUtil.getIntAttr(attributes, "cols", -1);
            this.vertical = XmlUtil.getBoolAttr(attributes, "vertical", false);
            final String styleName = XmlUtil.getAttr(attributes, "style", "normal");
            this.style = SlotGrid.SlotStyle.get(styleName);
            if (this.style == null) {
                throw new SAXException("invalid slot style: " + styleName);
            }
        }
        
        @Override
        public NodeType getType() {
            return NodeType.slotgrid;
        }
        
        public SlotGridDimension getDimension(int totalSize) {
            totalSize -= this.offset;
            if (!this.vertical) {
                if (this.cols > 0) {
                    return new SlotGridDimension(Math.max(this.rows, (totalSize + this.cols - 1) / this.cols), this.cols);
                }
                if (this.rows > 0) {
                    return new SlotGridDimension(this.rows, (totalSize + this.rows - 1) / this.rows);
                }
                final int cols = (int)Math.floor(Math.sqrt(totalSize));
                return new SlotGridDimension((totalSize + cols - 1) / cols, cols);
            }
            else {
                if (this.rows > 0) {
                    return new SlotGridDimension(this.rows, Math.max(this.cols, (totalSize + this.rows - 1) / this.rows));
                }
                if (this.cols > 0) {
                    return new SlotGridDimension((totalSize + this.cols - 1) / this.cols, this.cols);
                }
                final int rows = (int)Math.floor(Math.sqrt(totalSize));
                return new SlotGridDimension(rows, (totalSize + rows - 1) / rows);
            }
        }
        
        public static class SlotGridDimension
        {
            public final int rows;
            public final int cols;
            
            public SlotGridDimension(final int rows, final int cols) {
                this.rows = rows;
                this.cols = cols;
            }
        }
    }
    
    public static class SlotHologramNode extends SlotNode
    {
        public final int index;
        public final int stackSizeLimit;
        
        SlotHologramNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent, attributes);
            this.index = XmlUtil.getIntAttr(attributes, "index", 0);
            this.stackSizeLimit = XmlUtil.getIntAttr(attributes, "stack", 64);
        }
        
        @Override
        public NodeType getType() {
            return NodeType.slothologram;
        }
    }
    
    public static class TextNode extends Node
    {
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
        
        TextNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.x = XmlUtil.getIntAttr(attributes, "x", 0);
            this.y = XmlUtil.getIntAttr(attributes, "y", 0);
            this.width = XmlUtil.getIntAttr(attributes, "width", -1);
            this.height = XmlUtil.getIntAttr(attributes, "height", -1);
            this.xOffset = XmlUtil.getIntAttr(attributes, "xoffset", 0);
            this.yOffset = XmlUtil.getIntAttr(attributes, "yoffset", 0);
            final String alignName = XmlUtil.getAttr(attributes, "align", "start");
            this.align = Text.TextAlignment.get(alignName);
            if (this.align == null) {
                throw new SAXException("invalid alignment: " + alignName);
            }
            final String center = XmlUtil.getAttr(attributes, "center", (this.align == Text.TextAlignment.Center) ? "x" : "");
            this.centerX = (center.indexOf(120) != -1);
            this.centerY = (center.indexOf(121) != -1);
            this.rightAligned = XmlUtil.getBoolAttr(attributes, "right", false);
            this.color = XmlUtil.getIntAttr(attributes, "color", 4210752);
            this.shadow = (attributes.getIndex("shadow") != -1);
        }
        
        @Override
        public NodeType getType() {
            return NodeType.text;
        }
        
        @Override
        public void setContent(final String content) throws SAXException {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                Mouse.setGrabbed(false);
            }
            this.text = TextProvider.parse(content, this.parent.getBaseClass());
        }
    }
    
    public static class FluidTankNode extends Node
    {
        final int x;
        final int y;
        final String name;
        final int width;
        final int height;
        final boolean mirrored;
        final TankType type;
        
        FluidTankNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.x = XmlUtil.getIntAttr(attributes, "x");
            this.y = XmlUtil.getIntAttr(attributes, "y");
            this.name = XmlUtil.getAttr(attributes, "name");
            final String typeName = XmlUtil.getAttr(attributes, "type", "normal");
            this.type = TankType.get(typeName);
            if (this.type == null) {
                throw new SAXException("Invalid type: " + typeName);
            }
            if (this.type == TankType.PLAIN) {
                this.width = XmlUtil.getIntAttr(attributes, "width");
                this.height = XmlUtil.getIntAttr(attributes, "height");
            }
            else {
                final int n = -1;
                this.height = n;
                this.width = n;
            }
            if (this.type == TankType.BORDERLESS) {
                this.mirrored = XmlUtil.getBoolAttr(attributes, "mirrored");
            }
            else {
                this.mirrored = false;
            }
        }
        
        @Override
        public NodeType getType() {
            return NodeType.fluidtank;
        }
        
        public enum TankType
        {
            NORMAL, 
            PLAIN, 
            BORDERLESS;
            
            public static TankType get(final String name) {
                for (final TankType type : values()) {
                    if (type.name().equalsIgnoreCase(name)) {
                        return type;
                    }
                }
                return null;
            }
        }
    }
    
    public static class FluidSlotNode extends Node
    {
        final int x;
        final int y;
        final String name;
        
        FluidSlotNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.x = XmlUtil.getIntAttr(attributes, "x");
            this.y = XmlUtil.getIntAttr(attributes, "y");
            this.name = XmlUtil.getAttr(attributes, "name");
        }
        
        @Override
        public NodeType getType() {
            return NodeType.fluidslot;
        }
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.profile;

import java.io.File;
import org.apache.commons.io.FilenameUtils;
import java.util.zip.ZipEntry;
import java.io.FilenameFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.IOCase;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.JsonToNBT;
import ic2.core.util.Util;
import java.text.ParseException;
import ic2.core.util.ConfigUtil;
import net.minecraft.item.ItemStack;
import ic2.core.util.XmlUtil;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.EnumSet;
import java.util.Arrays;
import java.util.Collections;
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
import java.io.BufferedInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import org.xml.sax.SAXException;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.io.InputStream;
import java.io.IOException;

public class ProfileParser
{
    public static Profile parse(final ProfileTarget root) throws IOException {
        return parse(root, root.offset("profile.xml").asStream());
    }
    
    public static Profile parse(final ProfileTarget root, final InputStream is) throws IOException {
        ProfileNode parent;
        try {
            parent = create(is);
        }
        catch (final SAXException e) {
            IC2.log.error(LogCategory.Resource, e, "Error reading profile XML");
            return null;
        }
        finally {
            is.close();
        }
        String name = null;
        Version style = null;
        final Set<TextureStyle> textures = new HashSet<TextureStyle>();
        final List<RecipeChange> recipeChanges = new ArrayList<RecipeChange>();
        for (final Node rawNode : parent.getNodes()) {
            switch (rawNode.getType()) {
                case name: {
                    if (name != null) {
                        throw new RuntimeException("Duplicate profile names: " + name + " and " + ((NameNode)rawNode).name);
                    }
                    name = ((NameNode)rawNode).name;
                    continue;
                }
                case style: {
                    if (style != null) {
                        throw new RuntimeException("Duplicate profile styles: " + style + " and " + ((StyleNode)rawNode).style);
                    }
                    style = ((StyleNode)rawNode).style;
                    continue;
                }
                case textures: {
                    textures.add(((TextureNode)rawNode).style.apply(root));
                    continue;
                }
                case blocks:
                case items: {
                    continue;
                }
                case crafting: {
                    for (final Node cookedNode : ((ParentNode)rawNode).getNodes()) {
                        switch (cookedNode.getType()) {
                            case shaped:
                            case shapeless: {
                                recipeChanges.addAll(parseChanges(root, cookedNode.getType().name() + "_recipes", (ParentNode)cookedNode));
                                continue;
                            }
                            default: {
                                assert NodeType.crafting.validChildren.contains(cookedNode.getType());
                                throw new IllegalStateException("Unexpected child element in crafting node: " + cookedNode);
                            }
                        }
                    }
                    continue;
                }
                case furnace:
                case macerator:
                case compressor:
                case extractor:
                case ore_washer:
                case thermal_centrifuge:
                case blast_furnace:
                case block_cutter: {
                    recipeChanges.addAll(parseChanges(root, (ParentNode)rawNode));
                    continue;
                }
                case metal_former: {
                    for (final Node cookedNode : ((ParentNode)rawNode).getNodes()) {
                        switch (cookedNode.getType()) {
                            case cutting:
                            case extruding:
                            case rolling: {
                                recipeChanges.addAll(parseChanges(root, "metal_former_" + cookedNode.getType().name(), (ParentNode)cookedNode));
                                continue;
                            }
                            default: {
                                assert NodeType.metal_former.validChildren.contains(cookedNode.getType());
                                throw new IllegalStateException("Unexpected child element in metal former node: " + cookedNode);
                            }
                        }
                    }
                    continue;
                }
                default: {
                    assert parent.getType().validChildren.contains(rawNode.getType());
                    throw new IllegalStateException("Unexpected child element in " + parent.getType() + ": " + rawNode);
                }
            }
        }
        if (name == null) {
            throw new RuntimeException("Missing name for profile at " + root + "/profile.xml!");
        }
        if (style == null) {
            style = Version.NEW;
        }
        if (textures.isEmpty()) {
            textures.add(TextureStyle.EXPERIMENTAL);
        }
        return new Profile(name, textures, style, (RecipeChange[])recipeChanges.toArray(new RecipeChange[0]));
    }
    
    private static List<RecipeChange> parseChanges(final ProfileTarget root, final ParentNode parent) {
        return parseChanges(root, parent.getType().name(), parent);
    }
    
    private static List<RecipeChange> parseChanges(final ProfileTarget root, final String name, final ParentNode parent) {
        final List<RecipeChange> ret = new ArrayList<RecipeChange>();
        boolean madeReplacement = false;
        for (final Node rawNode : parent.getNodes()) {
            switch (rawNode.getType()) {
                case extend: {
                    if (madeReplacement) {
                        throw new RuntimeException("Non-replacement changes made alongside replacement: " + rawNode);
                    }
                    if (ret.stream().anyMatch(change -> change.type == RecipeChange.ChangeType.EXTENSION)) {
                        throw new RuntimeException("Duplicate profile extension!");
                    }
                    ret.add(new RecipeChange.RecipeExtension(name, ((ExtensionNode)rawNode).profile));
                    continue;
                }
                case replacements: {
                    if (!madeReplacement && !ret.isEmpty()) {
                        throw new RuntimeException("Non-replacement changes made alongside replacement: " + ret);
                    }
                    assert ret.stream().allMatch(change -> change.type == RecipeChange.ChangeType.REPLACEMENT);
                    madeReplacement = true;
                    final List<ProfileTarget> targets = new ArrayList<ProfileTarget>();
                    for (final Node cookedNode : ((ParentNode)rawNode).getNodes()) {
                        switch (cookedNode.getType()) {
                            case file: {
                                targets.add(root.offset(((FileNode)cookedNode).path));
                                continue;
                            }
                            case folder: {
                                targets.addAll(((FolderNode)cookedNode).getFiles(root));
                                continue;
                            }
                            default: {
                                assert NodeType.replacements.validChildren.contains(cookedNode.getType());
                                throw new IllegalStateException("Unexpected child element in replacements node: " + cookedNode);
                            }
                        }
                    }
                    ret.add(new RecipeChange.RecipeReplacement(name, (ProfileTarget[])targets.toArray(new ProfileTarget[0])));
                    continue;
                }
                case additions: {
                    if (madeReplacement) {
                        throw new RuntimeException("Non-replacement changes made alongside replacement: " + rawNode);
                    }
                    final List<ProfileTarget> targets = new ArrayList<ProfileTarget>();
                    for (final Node cookedNode : ((ParentNode)rawNode).getNodes()) {
                        switch (cookedNode.getType()) {
                            case file: {
                                targets.add(root.offset(((FileNode)cookedNode).path));
                                continue;
                            }
                            case folder: {
                                targets.addAll(((FolderNode)cookedNode).getFiles(root));
                                continue;
                            }
                            default: {
                                assert NodeType.additions.validChildren.contains(cookedNode.getType());
                                throw new IllegalStateException("Unexpected child element in additions node: " + cookedNode);
                            }
                        }
                    }
                    ret.add(new RecipeChange.RecipeAddition(name, (ProfileTarget[])targets.toArray(new ProfileTarget[0])));
                    continue;
                }
                case removals: {
                    if (madeReplacement) {
                        throw new RuntimeException("Non-replacement changes made alongside replacement: " + rawNode);
                    }
                    ret.add(null);
                    continue;
                }
                default: {
                    assert parent.getType().validChildren.contains(rawNode.getType());
                    throw new IllegalStateException("Unexpected child element in " + parent.getType() + ": " + rawNode);
                }
            }
        }
        return ret;
    }
    
    private static ProfileNode create(InputStream is) throws SAXException, IOException {
        is = new BufferedInputStream(is);
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            final SAXParser parser = factory.newSAXParser();
            final XMLReader reader = parser.getXMLReader();
            final SaxHandler handler = new SaxHandler();
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
        private ParentNode parentNode;
        private Node currentNode;
        
        public SaxHandler() {
        }
        
        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            NodeType type = NodeType.get(qName);
            if (type == null) {
                type = NodeType.get(qName.toLowerCase(Locale.ENGLISH));
            }
            if (type == null) {
                throw new SAXException("Invalid element: " + qName);
            }
            if (type == NodeType.profile) {
                if (this.parentNode != null) {
                    throw new SAXException("Invalid profile element location");
                }
            }
            else if (this.parentNode == null) {
                throw new SAXException("invalid " + qName + " element location");
            }
            switch (type) {
                case profile: {
                    final ProfileNode profileNode = new ProfileNode();
                    this.parentNode = profileNode;
                    this.currentNode = profileNode;
                    break;
                }
                case shaped:
                case shapeless:
                case cutting:
                case extruding:
                case rolling:
                case blocks:
                case items:
                case crafting:
                case furnace:
                case macerator:
                case compressor:
                case extractor:
                case ore_washer:
                case thermal_centrifuge:
                case blast_furnace:
                case block_cutter:
                case metal_former:
                case replacements:
                case additions:
                case removals:
                case whitelist:
                case blacklist: {
                    this.currentNode = new GenericParentNode(this.parentNode, type);
                    this.parentNode.addNode(this.currentNode);
                    this.parentNode = (ParentNode)this.currentNode;
                    break;
                }
                case name: {
                    this.currentNode = new NameNode(this.parentNode);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case textures: {
                    this.currentNode = new TextureNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case style: {
                    this.currentNode = new StyleNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case stack: {
                    this.currentNode = new StackNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case ore_dict: {
                    this.currentNode = new OreDictionaryNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case file: {
                    this.currentNode = new FileNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case folder: {
                    this.currentNode = new FolderNode(this.parentNode, attributes);
                    this.parentNode.addNode(this.currentNode);
                    break;
                }
                case extend: {
                    this.currentNode = new ExtensionNode(this.parentNode, attributes);
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
                if (this.currentNode.getType() == NodeType.profile) {
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
        
        public ProfileNode getResult() {
            return (ProfileNode)this.parentNode;
        }
    }
    
    public enum NodeType
    {
        name(new NodeType[0]), 
        textures(new NodeType[0]), 
        style(new NodeType[0]), 
        stack(new NodeType[0]), 
        ore_dict(new NodeType[0]), 
        file(new NodeType[0]), 
        folder(new NodeType[0]), 
        extend(new NodeType[0]), 
        whitelist(new NodeType[] { NodeType.stack }), 
        blacklist(new NodeType[] { NodeType.stack }), 
        blocks(new NodeType[] { NodeType.whitelist, NodeType.blacklist }), 
        items(new NodeType[] { NodeType.whitelist, NodeType.blacklist }), 
        replacements(new NodeType[] { NodeType.file, NodeType.folder }), 
        additions(new NodeType[] { NodeType.file, NodeType.folder }), 
        removals(new NodeType[] { NodeType.stack, NodeType.ore_dict, NodeType.file, NodeType.folder }), 
        shaped(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        shapeless(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        crafting(new NodeType[] { NodeType.shaped, NodeType.shapeless }), 
        furnace(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        macerator(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        compressor(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        extractor(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        ore_washer(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        thermal_centrifuge(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        blast_furnace(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        block_cutter(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        cutting(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        extruding(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        rolling(new NodeType[] { NodeType.extend, NodeType.replacements, NodeType.additions, NodeType.removals }), 
        metal_former(new NodeType[] { NodeType.cutting, NodeType.extruding, NodeType.rolling }), 
        profile(new NodeType[] { NodeType.name, NodeType.textures, NodeType.style, NodeType.blocks, NodeType.items, NodeType.crafting, NodeType.furnace, NodeType.macerator, NodeType.compressor, NodeType.extractor, NodeType.ore_washer, NodeType.thermal_centrifuge, NodeType.blast_furnace, NodeType.block_cutter, NodeType.metal_former });
        
        private NodeType[] types;
        Set<NodeType> validChildren;
        private static final Map<String, NodeType> MAP;
        
        private NodeType(final NodeType[] types) {
            this.types = types;
        }
        
        public static NodeType get(final String name) {
            return NodeType.MAP.get(name);
        }
        
        static {
            for (final NodeType type : values()) {
                type.validChildren = ((type.types.length == 0) ? Collections.emptySet() : EnumSet.copyOf(Arrays.asList(type.types)));
                type.types = null;
            }
            MAP = Arrays.stream(values()).collect(Collectors.toMap((Function<? super NodeType, ? extends String>)Enum::name, (Function<? super NodeType, ? extends NodeType>)Function.identity()));
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
            throw new SAXException("Unexpected characters: " + content);
        }
        
        @Override
        public String toString() {
            return "Node<" + this.getType() + '>';
        }
    }
    
    public abstract static class ParentNode extends Node
    {
        final List<Node> children;
        
        ParentNode(final ParentNode parent) {
            super(parent);
            this.children = new ArrayList<Node>();
        }
        
        public void addNode(final Node node) throws SAXException {
            if (!this.getType().validChildren.contains(node.getType())) {
                throw new SAXException("Invalid child: " + node);
            }
            this.children.add(node);
        }
        
        public Iterable<Node> getNodes() {
            return this.children;
        }
        
        @Override
        public String toString() {
            return "ParentNode<" + this.getType() + ": " + this.children + '>';
        }
    }
    
    public static class ProfileNode extends ParentNode
    {
        ProfileNode() {
            super(null);
        }
        
        @Override
        public NodeType getType() {
            return NodeType.profile;
        }
    }
    
    public static class GenericParentNode extends ParentNode
    {
        private final NodeType type;
        
        GenericParentNode(final ParentNode parent, final NodeType type) {
            super(parent);
            this.type = type;
        }
        
        @Override
        public NodeType getType() {
            return this.type;
        }
    }
    
    public static class NameNode extends Node
    {
        public String name;
        
        NameNode(final ParentNode parent) {
            super(parent);
        }
        
        @Override
        public NodeType getType() {
            return NodeType.name;
        }
        
        @Override
        public void setContent(final String content) throws SAXException {
            this.name = content;
        }
    }
    
    public static class TextureNode extends Node
    {
        private final String mod;
        public Function<ProfileTarget, TextureStyle> style;
        
        TextureNode(final ParentNode parent, final Attributes attributes) {
            super(parent);
            this.mod = XmlUtil.getAttr(attributes, "mod", "ic2");
        }
        
        @Override
        public NodeType getType() {
            return NodeType.textures;
        }
        
        @Override
        public void setContent(final String content) throws SAXException {
            switch (content) {
                case "NEW": {
                    this.style = (Function<ProfileTarget, TextureStyle>)(root -> TextureStyle.EXPERIMENTAL);
                    break;
                }
                case "OLD": {
                    this.style = (Function<ProfileTarget, TextureStyle>)(root -> TextureStyle.CLASSIC);
                    break;
                }
                default: {
                    this.style = (Function<ProfileTarget, TextureStyle>)(root -> new TextureStyle(this.mod, root.offset(content)));
                    break;
                }
            }
        }
    }
    
    public static class StyleNode extends Node
    {
        public final Version style;
        
        StyleNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.style = Version.valueOf(XmlUtil.getAttr(attributes, "type"));
        }
        
        @Override
        public NodeType getType() {
            return NodeType.style;
        }
    }
    
    public static class StackNode extends Node
    {
        public final ItemStack stack;
        
        StackNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            final String combined = attributes.getValue("combined");
            if (combined != null) {
                try {
                    this.stack = ConfigUtil.asStack(combined);
                    return;
                }
                catch (final ParseException e) {
                    throw new SAXException("Invalid/Unknown stack requested: " + combined, e);
                }
            }
            final Item id = Util.getItem(XmlUtil.getAttr(attributes, "id"));
            if (id == null) {
                throw new SAXException("Invalid/Unknown item requested: " + id);
            }
            final int meta = XmlUtil.getIntAttr(attributes, "meta", 32767);
            this.stack = new ItemStack(id, 1, meta);
            final String nbt = attributes.getValue("nbt");
            if (nbt != null) {
                try {
                    this.stack.setTagCompound(JsonToNBT.getTagFromJson(nbt));
                }
                catch (final NBTException e2) {
                    throw new SAXException("Invalid stack NBT: " + nbt, (Exception)e2);
                }
            }
        }
        
        @Override
        public NodeType getType() {
            return NodeType.stack;
        }
    }
    
    public static class OreDictionaryNode extends Node
    {
        public final String tag;
        
        OreDictionaryNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.tag = XmlUtil.getAttr(attributes, "tag");
        }
        
        @Override
        public NodeType getType() {
            return NodeType.ore_dict;
        }
    }
    
    public static class FileNode extends Node
    {
        public final String path;
        
        FileNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.path = XmlUtil.getAttr(attributes, "path");
        }
        
        @Override
        public NodeType getType() {
            return NodeType.file;
        }
    }
    
    public static class FolderNode extends Node
    {
        public final String path;
        
        FolderNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.path = XmlUtil.getAttr(attributes, "path");
        }
        
        @Override
        public NodeType getType() {
            return NodeType.folder;
        }
        
        Set<ProfileTarget> getFiles(final ProfileTarget root) {
            final ProfileTarget folder = root.offset(this.path);
            final Set<ProfileTarget> files = new HashSet<ProfileTarget>();
            if (!folder.isFile()) {
                for (final File file : folder.asFile().listFiles((FilenameFilter)new WildcardFileFilter("*.INI", IOCase.INSENSITIVE))) {
                    if (file.isFile()) {
                        files.add(folder.offset(file.getName()));
                    }
                }
            }
            else {
                for (final ZipEntry entry : Collections.list(folder.asZip().entries())) {
                    if (!entry.isDirectory()) {
                        final String name = entry.getName();
                        if (!FilenameUtils.equals(this.path, FilenameUtils.getPathNoEndSeparator(name)) || !FilenameUtils.isExtension(name, "ini")) {
                            continue;
                        }
                        files.add(folder.offset(FilenameUtils.getName(name)));
                    }
                }
            }
            return files;
        }
    }
    
    public static class ExtensionNode extends Node
    {
        public final String profile;
        
        ExtensionNode(final ParentNode parent, final Attributes attributes) throws SAXException {
            super(parent);
            this.profile = XmlUtil.getAttr(attributes, "profile");
        }
        
        @Override
        public NodeType getType() {
            return NodeType.extend;
        }
    }
}

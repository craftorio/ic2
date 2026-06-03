package ic2.core.profile;

import ic2.core.IC2;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import ic2.core.util.XmlUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class ProfileParser {
  public static Profile parse(ProfileTarget root) throws IOException {
    return parse(root, root.offset("profile.xml").asStream());
  }
  
  public static Profile parse(ProfileTarget root, InputStream is) throws IOException {
    ProfileNode parent;
    try {
      parent = create(is);
    } catch (SAXException e) {
      IC2.log.error(LogCategory.Resource, e, "Error reading profile XML");
      return null;
    } finally {
      is.close();
    } 
    String name = null;
    Version style = null;
    Set<TextureStyle> textures = new HashSet<>();
    List<RecipeChange> recipeChanges = new ArrayList<>();
    for (Node rawNode : parent.getNodes()) {
      switch (rawNode.getType()) {
        case name:
          if (name != null)
            throw new RuntimeException("Duplicate profile names: " + name + " and " + ((NameNode)rawNode).name); 
          name = ((NameNode)rawNode).name;
          continue;
        case style:
          if (style != null)
            throw new RuntimeException("Duplicate profile styles: " + style + " and " + ((StyleNode)rawNode).style); 
          style = ((StyleNode)rawNode).style;
          continue;
        case textures:
          textures.add(((TextureNode)rawNode).style.apply(root));
          continue;
        case blocks:
        case items:
          continue;
        case crafting:
          for (Node cookedNode : ((ParentNode)rawNode).getNodes()) {
            switch (cookedNode.getType()) {
              case shaped:
              case shapeless:
                recipeChanges.addAll(parseChanges(root, cookedNode.getType().name() + "_recipes", (ParentNode)cookedNode));
                continue;
            } 
            assert NodeType.crafting.validChildren.contains(cookedNode.getType());
            throw new IllegalStateException("Unexpected child element in crafting node: " + cookedNode);
          } 
          continue;
        case furnace:
        case macerator:
        case compressor:
        case extractor:
        case ore_washer:
        case thermal_centrifuge:
        case blast_furnace:
        case block_cutter:
          recipeChanges.addAll(parseChanges(root, (ParentNode)rawNode));
          continue;
        case metal_former:
          for (Node cookedNode : ((ParentNode)rawNode).getNodes()) {
            switch (cookedNode.getType()) {
              case cutting:
              case extruding:
              case rolling:
                recipeChanges.addAll(parseChanges(root, "metal_former_" + cookedNode.getType().name(), (ParentNode)cookedNode));
                continue;
            } 
            assert NodeType.metal_former.validChildren.contains(cookedNode.getType());
            throw new IllegalStateException("Unexpected child element in metal former node: " + cookedNode);
          } 
          continue;
      } 
      assert (parent.getType()).validChildren.contains(rawNode.getType());
      throw new IllegalStateException("Unexpected child element in " + parent.getType() + ": " + rawNode);
    } 
    if (name == null)
      throw new RuntimeException("Missing name for profile at " + root + "/profile.xml!"); 
    if (style == null)
      style = Version.NEW; 
    if (textures.isEmpty())
      textures.add(TextureStyle.EXPERIMENTAL); 
    return new Profile(name, textures, style, recipeChanges.<RecipeChange>toArray(new RecipeChange[0]));
  }
  
  private static List<RecipeChange> parseChanges(ProfileTarget root, ParentNode parent) {
    return parseChanges(root, parent.getType().name(), parent);
  }
  
  private static List<RecipeChange> parseChanges(ProfileTarget root, String name, ParentNode parent) {
    List<RecipeChange> ret = new ArrayList<>();
    boolean madeReplacement = false;
    for (Node rawNode : parent.getNodes()) {
      List<ProfileTarget> targets;
      switch (rawNode.getType()) {
        case extend:
          if (madeReplacement)
            throw new RuntimeException("Non-replacement changes made alongside replacement: " + rawNode); 
          if (ret.stream().anyMatch(change -> (change.type == RecipeChange.ChangeType.EXTENSION)))
            throw new RuntimeException("Duplicate profile extension!"); 
          ret.add(new RecipeChange.RecipeExtension(name, ((ExtensionNode)rawNode).profile));
          continue;
        case replacements:
          if (!madeReplacement && !ret.isEmpty())
            throw new RuntimeException("Non-replacement changes made alongside replacement: " + ret); 
          assert ret.stream().allMatch(change -> (change.type == RecipeChange.ChangeType.REPLACEMENT));
          madeReplacement = true;
          targets = new ArrayList<>();
          for (Node cookedNode : ((ParentNode)rawNode).getNodes()) {
            switch (cookedNode.getType()) {
              case file:
                targets.add(root.offset(((FileNode)cookedNode).path));
                continue;
              case folder:
                targets.addAll(((FolderNode)cookedNode).getFiles(root));
                continue;
            } 
            assert NodeType.replacements.validChildren.contains(cookedNode.getType());
            throw new IllegalStateException("Unexpected child element in replacements node: " + cookedNode);
          } 
          ret.add(new RecipeChange.RecipeReplacement(name, targets.<ProfileTarget>toArray(new ProfileTarget[0])));
          continue;
        case additions:
          if (madeReplacement)
            throw new RuntimeException("Non-replacement changes made alongside replacement: " + rawNode); 
          targets = new ArrayList<>();
          for (Node cookedNode : ((ParentNode)rawNode).getNodes()) {
            switch (cookedNode.getType()) {
              case file:
                targets.add(root.offset(((FileNode)cookedNode).path));
                continue;
              case folder:
                targets.addAll(((FolderNode)cookedNode).getFiles(root));
                continue;
            } 
            assert NodeType.additions.validChildren.contains(cookedNode.getType());
            throw new IllegalStateException("Unexpected child element in additions node: " + cookedNode);
          } 
          ret.add(new RecipeChange.RecipeAddition(name, targets.<ProfileTarget>toArray(new ProfileTarget[0])));
          continue;
        case removals:
          if (madeReplacement)
            throw new RuntimeException("Non-replacement changes made alongside replacement: " + rawNode); 
          ret.add((RecipeChange)null);
          continue;
      } 
      assert (parent.getType()).validChildren.contains(rawNode.getType());
      throw new IllegalStateException("Unexpected child element in " + parent.getType() + ": " + rawNode);
    } 
    return ret;
  }
  
  private static ProfileNode create(InputStream is) throws SAXException, IOException {
    is = new BufferedInputStream(is);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      SAXParser parser = factory.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      SaxHandler handler = new SaxHandler();
      reader.setContentHandler(handler);
      reader.parse(new InputSource(is));
      return handler.getResult();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private static class SaxHandler extends DefaultHandler {
    private ProfileParser.ParentNode parentNode;
    
    private ProfileParser.Node currentNode;
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      ProfileParser.NodeType type = ProfileParser.NodeType.get(qName);
      if (type == null)
        type = ProfileParser.NodeType.get(qName.toLowerCase(Locale.ENGLISH)); 
      if (type == null)
        throw new SAXException("Invalid element: " + qName); 
      if (type == ProfileParser.NodeType.profile) {
        if (this.parentNode != null)
          throw new SAXException("Invalid profile element location"); 
      } else if (this.parentNode == null) {
        throw new SAXException("invalid " + qName + " element location");
      } 
      switch (type) {
        case profile:
          this.currentNode = this.parentNode = new ProfileParser.ProfileNode();
          break;
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
        case blacklist:
          this.currentNode = new ProfileParser.GenericParentNode(this.parentNode, type);
          this.parentNode.addNode(this.currentNode);
          this.parentNode = (ProfileParser.ParentNode)this.currentNode;
          break;
        case name:
          this.currentNode = new ProfileParser.NameNode(this.parentNode);
          this.parentNode.addNode(this.currentNode);
          break;
        case textures:
          this.currentNode = new ProfileParser.TextureNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case style:
          this.currentNode = new ProfileParser.StyleNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case stack:
          this.currentNode = new ProfileParser.StackNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case ore_dict:
          this.currentNode = new ProfileParser.OreDictionaryNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case file:
          this.currentNode = new ProfileParser.FileNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case folder:
          this.currentNode = new ProfileParser.FolderNode(this.parentNode, attributes);
          this.parentNode.addNode(this.currentNode);
          break;
        case extend:
          this.currentNode = new ProfileParser.ExtensionNode(this.parentNode, attributes);
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
        if (this.currentNode.getType() == ProfileParser.NodeType.profile) {
          this.currentNode = null;
        } else {
          this.currentNode = this.parentNode = this.parentNode.parent;
        } 
      } else {
        this.currentNode = this.parentNode;
      } 
    }
    
    public ProfileParser.ProfileNode getResult() {
      return (ProfileParser.ProfileNode)this.parentNode;
    }
  }
  
  public enum NodeType {
    name((String)new NodeType[0]),
    textures((String)new NodeType[0]),
    style((String)new NodeType[0]),
    stack((String)new NodeType[0]),
    ore_dict((String)new NodeType[0]),
    file((String)new NodeType[0]),
    folder((String)new NodeType[0]),
    extend((String)new NodeType[0]),
    whitelist((String)new NodeType[] { stack }),
    blacklist((String)new NodeType[] { stack }),
    blocks((String)new NodeType[] { whitelist, blacklist }),
    items((String)new NodeType[] { whitelist, blacklist }),
    replacements((String)new NodeType[] { file, folder }),
    additions((String)new NodeType[] { file, folder }),
    removals((String)new NodeType[] { stack, ore_dict, file, folder }),
    shaped((String)new NodeType[] { extend, replacements, additions, removals }),
    shapeless((String)new NodeType[] { extend, replacements, additions, removals }),
    crafting((String)new NodeType[] { shaped, shapeless }),
    furnace((String)new NodeType[] { extend, replacements, additions, removals }),
    macerator((String)new NodeType[] { extend, replacements, additions, removals }),
    compressor((String)new NodeType[] { extend, replacements, additions, removals }),
    extractor((String)new NodeType[] { extend, replacements, additions, removals }),
    ore_washer((String)new NodeType[] { extend, replacements, additions, removals }),
    thermal_centrifuge((String)new NodeType[] { extend, replacements, additions, removals }),
    blast_furnace((String)new NodeType[] { extend, replacements, additions, removals }),
    block_cutter((String)new NodeType[] { extend, replacements, additions, removals }),
    cutting((String)new NodeType[] { extend, replacements, additions, removals }),
    extruding((String)new NodeType[] { extend, replacements, additions, removals }),
    rolling((String)new NodeType[] { extend, replacements, additions, removals }),
    metal_former((String)new NodeType[] { cutting, extruding, rolling }),
    profile((String)new NodeType[] { 
        name, textures, style, blocks, items, crafting, furnace, macerator, compressor, extractor, 
        ore_washer, thermal_centrifuge, blast_furnace, block_cutter, metal_former });
    
    private NodeType[] types;
    
    Set<NodeType> validChildren;
    
    private static final Map<String, NodeType> MAP = (Map<String, NodeType>)Arrays.<NodeType>stream(values()).collect(Collectors.toMap(Enum::name, Function.identity()));
    
    NodeType(NodeType... types) {
      this.types = types;
    }
    
    public static NodeType get(String name) {
      return MAP.get(name);
    }
    
    static {
    
    }
  }
  
  public static abstract class Node {
    final ProfileParser.ParentNode parent;
    
    Node(ProfileParser.ParentNode parent) {
      this.parent = parent;
    }
    
    public abstract ProfileParser.NodeType getType();
    
    public void setContent(String content) throws SAXException {
      throw new SAXException("Unexpected characters: " + content);
    }
    
    public String toString() {
      return "Node<" + getType() + '>';
    }
  }
  
  public static abstract class ParentNode extends Node {
    final List<ProfileParser.Node> children;
    
    ParentNode(ParentNode parent) {
      super(parent);
      this.children = new ArrayList<>();
    }
    
    public void addNode(ProfileParser.Node node) throws SAXException {
      if (!(getType()).validChildren.contains(node.getType()))
        throw new SAXException("Invalid child: " + node); 
      this.children.add(node);
    }
    
    public Iterable<ProfileParser.Node> getNodes() {
      return this.children;
    }
    
    public String toString() {
      return "ParentNode<" + getType() + ": " + this.children + '>';
    }
  }
  
  public static class ProfileNode extends ParentNode {
    ProfileNode() {
      super((ProfileParser.ParentNode)null);
    }
    
    public ProfileParser.NodeType getType() {
      return ProfileParser.NodeType.profile;
    }
  }
  
  public static class GenericParentNode extends ParentNode {
    private final ProfileParser.NodeType type;
    
    GenericParentNode(ProfileParser.ParentNode parent, ProfileParser.NodeType type) {
      super(parent);
      this.type = type;
    }
    
    public ProfileParser.NodeType getType() {
      return this.type;
    }
  }
  
  public static class NameNode extends Node {
    public String name;
    
    NameNode(ProfileParser.ParentNode parent) {
      super(parent);
    }
    
    public ProfileParser.NodeType getType() {
      return ProfileParser.NodeType.name;
    }
    
    public void setContent(String content) throws SAXException {
      this.name = content;
    }
  }
  
  public static class TextureNode extends Node {
    private final String mod;
    
    public Function<ProfileTarget, TextureStyle> style;
    
    TextureNode(ProfileParser.ParentNode parent, Attributes attributes) {
      super(parent);
      this.mod = XmlUtil.getAttr(attributes, "mod", "ic2");
    }
    
    public ProfileParser.NodeType getType() {
      return ProfileParser.NodeType.textures;
    }
    
    public void setContent(String content) throws SAXException {
      switch (content) {
        case "NEW":
          this.style = (root -> TextureStyle.EXPERIMENTAL);
          return;
        case "OLD":
          this.style = (root -> TextureStyle.CLASSIC);
          return;
      } 
      this.style = (root -> new TextureStyle(this.mod, root.offset(content)));
    }
  }
  
  public static class StyleNode extends Node {
    public final Version style;
    
    StyleNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.style = Version.valueOf(XmlUtil.getAttr(attributes, "type"));
    }
    
    public ProfileParser.NodeType getType() {
      return ProfileParser.NodeType.style;
    }
  }
  
  public static class StackNode extends Node {
    public final ItemStack stack;
    
    StackNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      String combined = attributes.getValue("combined");
      if (combined != null) {
        try {
          this.stack = ConfigUtil.asStack(combined);
        } catch (ParseException e) {
          throw new SAXException("Invalid/Unknown stack requested: " + combined, e);
        } 
      } else {
        Item id = Util.getItem(XmlUtil.getAttr(attributes, "id"));
        if (id == null)
          throw new SAXException("Invalid/Unknown item requested: " + id); 
        int meta = XmlUtil.getIntAttr(attributes, "meta", 32767);
        this.stack = new ItemStack(id, 1, meta);
        String nbt = attributes.getValue("nbt");
        if (nbt != null)
          try {
            this.stack.func_77982_d(JsonToNBT.func_180713_a(nbt));
          } catch (NBTException e) {
            throw new SAXException("Invalid stack NBT: " + nbt, e);
          }  
      } 
    }
    
    public ProfileParser.NodeType getType() {
      return ProfileParser.NodeType.stack;
    }
  }
  
  public static class OreDictionaryNode extends Node {
    public final String tag;
    
    OreDictionaryNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.tag = XmlUtil.getAttr(attributes, "tag");
    }
    
    public ProfileParser.NodeType getType() {
      return ProfileParser.NodeType.ore_dict;
    }
  }
  
  public static class FileNode extends Node {
    public final String path;
    
    FileNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.path = XmlUtil.getAttr(attributes, "path");
    }
    
    public ProfileParser.NodeType getType() {
      return ProfileParser.NodeType.file;
    }
  }
  
  public static class FolderNode extends Node {
    public final String path;
    
    FolderNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.path = XmlUtil.getAttr(attributes, "path");
    }
    
    public ProfileParser.NodeType getType() {
      return ProfileParser.NodeType.folder;
    }
    
    Set<ProfileTarget> getFiles(ProfileTarget root) {
      ProfileTarget folder = root.offset(this.path);
      Set<ProfileTarget> files = new HashSet<>();
      if (!folder.isFile()) {
        for (File file : folder.asFile().listFiles((FilenameFilter)new WildcardFileFilter("*.INI", IOCase.INSENSITIVE))) {
          if (file.isFile())
            files.add(folder.offset(file.getName())); 
        } 
      } else {
        for (ZipEntry entry : Collections.<ZipEntry>list(folder.asZip().entries())) {
          if (!entry.isDirectory()) {
            String name = entry.getName();
            if (FilenameUtils.equals(this.path, FilenameUtils.getPathNoEndSeparator(name)) && FilenameUtils.isExtension(name, "ini"))
              files.add(folder.offset(FilenameUtils.getName(name))); 
          } 
        } 
      } 
      return files;
    }
  }
  
  public static class ExtensionNode extends Node {
    public final String profile;
    
    ExtensionNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
      super(parent);
      this.profile = XmlUtil.getAttr(attributes, "profile");
    }
    
    public ProfileParser.NodeType getType() {
      return ProfileParser.NodeType.extend;
    }
  }
}

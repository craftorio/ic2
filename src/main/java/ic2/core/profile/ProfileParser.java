package ic2.core.profile;

import ic2.core.IC2;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import ic2.core.util.XmlUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
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
      ProfileParser.ProfileNode parent;
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
      HashSet textures = new HashSet();
      ArrayList recipeChanges = new ArrayList();

      for (ProfileParser.Node rawNode : parent.getNodes()) {
         switch (rawNode.getType()) {
            case name:
               if (name != null) {
                  throw new RuntimeException("Duplicate profile names: " + name + " and " + ((ProfileParser.NameNode)rawNode).name);
               }

               name = ((ProfileParser.NameNode)rawNode).name;
               break;
            case style:
               if (style != null) {
                  throw new RuntimeException("Duplicate profile styles: " + style + " and " + ((ProfileParser.StyleNode)rawNode).style);
               }

               style = ((ProfileParser.StyleNode)rawNode).style;
               break;
            case textures:
               textures.add(((ProfileParser.TextureNode)rawNode).style.apply(root));
            case blocks:
            case items:
               break;
            case crafting:
               for (ProfileParser.Node cookedNode : ((ProfileParser.ParentNode)rawNode).getNodes()) {
                  switch (cookedNode.getType()) {
                     case shaped:
                     case shapeless:
                        recipeChanges.addAll(parseChanges(root, cookedNode.getType().name() + "_recipes", (ProfileParser.ParentNode)cookedNode));
                        break;
                     default:
                        assert ProfileParser.NodeType.crafting.validChildren.contains(cookedNode.getType());
                        throw new IllegalStateException("Unexpected child element in crafting node: " + cookedNode);
                  }
               }
               break;
            case furnace:
            case macerator:
            case compressor:
            case extractor:
            case ore_washer:
            case thermal_centrifuge:
            case blast_furnace:
            case block_cutter:
               recipeChanges.addAll(parseChanges(root, (ProfileParser.ParentNode)rawNode));
               break;
            case metal_former:
               for (ProfileParser.Node cookedNode : ((ProfileParser.ParentNode)rawNode).getNodes()) {
                  switch (cookedNode.getType()) {
                     case cutting:
                     case extruding:
                     case rolling:
                        recipeChanges.addAll(parseChanges(root, "metal_former_" + cookedNode.getType().name(), (ProfileParser.ParentNode)cookedNode));
                        break;
                     default:
                        assert ProfileParser.NodeType.metal_former.validChildren.contains(cookedNode.getType());
                        throw new IllegalStateException("Unexpected child element in metal former node: " + cookedNode);
                  }
               }
               break;
            default:
               assert parent.getType().validChildren.contains(rawNode.getType());
               throw new IllegalStateException("Unexpected child element in " + parent.getType() + ": " + rawNode);
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

      return new Profile(name, textures, style, recipeChanges.toArray(new RecipeChange[0]));
   }

   private static List<RecipeChange> parseChanges(ProfileTarget root, ProfileParser.ParentNode parent) {
      return parseChanges(root, parent.getType().name(), parent);
   }

   private static List<RecipeChange> parseChanges(ProfileTarget root, String name, ProfileParser.ParentNode parent) {
      List<RecipeChange> ret = new ArrayList<>();
      boolean madeReplacement = false;

      for (ProfileParser.Node rawNode : parent.getNodes()) {
         switch (rawNode.getType()) {
            case extend:
               if (madeReplacement) {
                  throw new RuntimeException("Non-replacement changes made alongside replacement: " + rawNode);
               }

               if (ret.stream().anyMatch(change -> change.type == RecipeChange.ChangeType.EXTENSION)) {
                  throw new RuntimeException("Duplicate profile extension!");
               }

               ret.add(new RecipeChange.RecipeExtension(name, ((ProfileParser.ExtensionNode)rawNode).profile));
               break;
            case replacements:
               if (!madeReplacement && !ret.isEmpty()) {
                  throw new RuntimeException("Non-replacement changes made alongside replacement: " + ret);
               }

               assert ret.stream().allMatch(change -> change.type == RecipeChange.ChangeType.REPLACEMENT);
               madeReplacement = true;
               List<ProfileTarget> targets = new ArrayList<>();

               for (ProfileParser.Node cookedNode : ((ProfileParser.ParentNode)rawNode).getNodes()) {
                  switch (cookedNode.getType()) {
                     case file:
                        targets.add(root.offset(((ProfileParser.FileNode)cookedNode).path));
                        break;
                     case folder:
                        targets.addAll(((ProfileParser.FolderNode)cookedNode).getFiles(root));
                        break;
                     default:
                        assert ProfileParser.NodeType.replacements.validChildren.contains(cookedNode.getType());
                        throw new IllegalStateException("Unexpected child element in replacements node: " + cookedNode);
                  }
               }

               ret.add(new RecipeChange.RecipeReplacement(name, targets.toArray(new ProfileTarget[0])));
               break;
            case additions:
               if (madeReplacement) {
                  throw new RuntimeException("Non-replacement changes made alongside replacement: " + rawNode);
               }

               List<ProfileTarget> targets = new ArrayList<>();

               for (ProfileParser.Node cookedNode : ((ProfileParser.ParentNode)rawNode).getNodes()) {
                  switch (cookedNode.getType()) {
                     case file:
                        targets.add(root.offset(((ProfileParser.FileNode)cookedNode).path));
                        break;
                     case folder:
                        targets.addAll(((ProfileParser.FolderNode)cookedNode).getFiles(root));
                        break;
                     default:
                        assert ProfileParser.NodeType.additions.validChildren.contains(cookedNode.getType());
                        throw new IllegalStateException("Unexpected child element in additions node: " + cookedNode);
                  }
               }

               ret.add(new RecipeChange.RecipeAddition(name, targets.toArray(new ProfileTarget[0])));
               break;
            case removals:
               if (madeReplacement) {
                  throw new RuntimeException("Non-replacement changes made alongside replacement: " + rawNode);
               }

               ret.add(null);
               break;
            default:
               assert parent.getType().validChildren.contains(rawNode.getType());
               throw new IllegalStateException("Unexpected child element in " + parent.getType() + ": " + rawNode);
         }
      }

      return ret;
   }

   private static ProfileParser.ProfileNode create(InputStream is) throws SAXException, IOException {
      is = new BufferedInputStream(is);
      SAXParserFactory factory = SAXParserFactory.newInstance();

      try {
         SAXParser parser = factory.newSAXParser();
         XMLReader reader = parser.getXMLReader();
         ProfileParser.SaxHandler handler = new ProfileParser.SaxHandler();
         reader.setContentHandler(handler);
         reader.parse(new InputSource(is));
         return handler.getResult();
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(e);
      }
   }

   public static class ExtensionNode extends ProfileParser.Node {
      public final String profile;

      ExtensionNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
         super(parent);
         this.profile = XmlUtil.getAttr(attributes, "profile");
      }

      @Override
      public ProfileParser.NodeType getType() {
         return ProfileParser.NodeType.extend;
      }
   }

   public static class FileNode extends ProfileParser.Node {
      public final String path;

      FileNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
         super(parent);
         this.path = XmlUtil.getAttr(attributes, "path");
      }

      @Override
      public ProfileParser.NodeType getType() {
         return ProfileParser.NodeType.file;
      }
   }

   public static class FolderNode extends ProfileParser.Node {
      public final String path;

      FolderNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
         super(parent);
         this.path = XmlUtil.getAttr(attributes, "path");
      }

      @Override
      public ProfileParser.NodeType getType() {
         return ProfileParser.NodeType.folder;
      }

      Set<ProfileTarget> getFiles(ProfileTarget root) {
         ProfileTarget folder = root.offset(this.path);
         Set<ProfileTarget> files = new HashSet<>();
         if (!folder.isFile()) {
            for (File file : folder.asFile().listFiles(new WildcardFileFilter("*.INI", IOCase.INSENSITIVE))) {
               if (file.isFile()) {
                  files.add(folder.offset(file.getName()));
               }
            }
         } else {
            for (ZipEntry entry : Collections.list(folder.asZip().entries())) {
               if (!entry.isDirectory()) {
                  String name = entry.getName();
                  if (FilenameUtils.equals(this.path, FilenameUtils.getPathNoEndSeparator(name)) && FilenameUtils.isExtension(name, "ini")) {
                     files.add(folder.offset(FilenameUtils.getName(name)));
                  }
               }
            }
         }

         return files;
      }
   }

   public static class GenericParentNode extends ProfileParser.ParentNode {
      private final ProfileParser.NodeType type;

      GenericParentNode(ProfileParser.ParentNode parent, ProfileParser.NodeType type) {
         super(parent);
         this.type = type;
      }

      @Override
      public ProfileParser.NodeType getType() {
         return this.type;
      }
   }

   public static class NameNode extends ProfileParser.Node {
      public String name;

      NameNode(ProfileParser.ParentNode parent) {
         super(parent);
      }

      @Override
      public ProfileParser.NodeType getType() {
         return ProfileParser.NodeType.name;
      }

      @Override
      public void setContent(String content) throws SAXException {
         this.name = content;
      }
   }

   public abstract static class Node {
      final ProfileParser.ParentNode parent;

      Node(ProfileParser.ParentNode parent) {
         this.parent = parent;
      }

      public abstract ProfileParser.NodeType getType();

      public void setContent(String content) throws SAXException {
         throw new SAXException("Unexpected characters: " + content);
      }

      @Override
      public String toString() {
         return "Node<" + this.getType() + '>';
      }
   }

   public enum NodeType {
      name(),
      textures(),
      style(),
      stack(),
      ore_dict(),
      file(),
      folder(),
      extend(),
      whitelist(stack),
      blacklist(stack),
      blocks(whitelist, blacklist),
      items(whitelist, blacklist),
      replacements(file, folder),
      additions(file, folder),
      removals(stack, ore_dict, file, folder),
      shaped(extend, replacements, additions, removals),
      shapeless(extend, replacements, additions, removals),
      crafting(shaped, shapeless),
      furnace(extend, replacements, additions, removals),
      macerator(extend, replacements, additions, removals),
      compressor(extend, replacements, additions, removals),
      extractor(extend, replacements, additions, removals),
      ore_washer(extend, replacements, additions, removals),
      thermal_centrifuge(extend, replacements, additions, removals),
      blast_furnace(extend, replacements, additions, removals),
      block_cutter(extend, replacements, additions, removals),
      cutting(extend, replacements, additions, removals),
      extruding(extend, replacements, additions, removals),
      rolling(extend, replacements, additions, removals),
      metal_former(cutting, extruding, rolling),
      profile(
         name,
         textures,
         style,
         blocks,
         items,
         crafting,
         furnace,
         macerator,
         compressor,
         extractor,
         ore_washer,
         thermal_centrifuge,
         blast_furnace,
         block_cutter,
         metal_former
      );

      private ProfileParser.NodeType[] types;
      Set<ProfileParser.NodeType> validChildren;
      private static final Map<String, ProfileParser.NodeType> MAP;

      NodeType(ProfileParser.NodeType... types) {
         this.types = types;
      }

      public static ProfileParser.NodeType get(String name) {
         return MAP.get(name);
      }

      static {
         for (ProfileParser.NodeType type : values()) {
            type.validChildren = type.types.length == 0 ? Collections.emptySet() : EnumSet.copyOf(Arrays.asList(type.types));
            type.types = null;
         }

         MAP = Arrays.stream(values()).collect(Collectors.toMap(Enum::name, Function.identity()));
      }
   }

   public static class OreDictionaryNode extends ProfileParser.Node {
      public final String tag;

      OreDictionaryNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
         super(parent);
         this.tag = XmlUtil.getAttr(attributes, "tag");
      }

      @Override
      public ProfileParser.NodeType getType() {
         return ProfileParser.NodeType.ore_dict;
      }
   }

   public abstract static class ParentNode extends ProfileParser.Node {
      final List<ProfileParser.Node> children = new ArrayList<>();

      ParentNode(ProfileParser.ParentNode parent) {
         super(parent);
      }

      public void addNode(ProfileParser.Node node) throws SAXException {
         if (!this.getType().validChildren.contains(node.getType())) {
            throw new SAXException("Invalid child: " + node);
         }

         this.children.add(node);
      }

      public Iterable<ProfileParser.Node> getNodes() {
         return this.children;
      }

      @Override
      public String toString() {
         return "ParentNode<" + this.getType() + ": " + this.children + '>';
      }
   }

   public static class ProfileNode extends ProfileParser.ParentNode {
      ProfileNode() {
         super(null);
      }

      @Override
      public ProfileParser.NodeType getType() {
         return ProfileParser.NodeType.profile;
      }
   }

   private static class SaxHandler extends DefaultHandler {
      private ProfileParser.ParentNode parentNode;
      private ProfileParser.Node currentNode;

      public SaxHandler() {
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         ProfileParser.NodeType type = ProfileParser.NodeType.get(qName);
         if (type == null) {
            type = ProfileParser.NodeType.get(qName.toLowerCase(Locale.ENGLISH));
         }

         if (type == null) {
            throw new SAXException("Invalid element: " + qName);
         }

         if (type == ProfileParser.NodeType.profile) {
            if (this.parentNode != null) {
               throw new SAXException("Invalid profile element location");
            }
         } else if (this.parentNode == null) {
            throw new SAXException("invalid " + qName + " element location");
         }

         switch (type) {
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
            case style:
               this.currentNode = new ProfileParser.StyleNode(this.parentNode, attributes);
               this.parentNode.addNode(this.currentNode);
               break;
            case textures:
               this.currentNode = new ProfileParser.TextureNode(this.parentNode, attributes);
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
            case profile:
               this.currentNode = this.parentNode = new ProfileParser.ProfileNode();
               break;
            case stack:
               this.currentNode = new ProfileParser.StackNode(this.parentNode, attributes);
               this.parentNode.addNode(this.currentNode);
               break;
            case ore_dict:
               this.currentNode = new ProfileParser.OreDictionaryNode(this.parentNode, attributes);
               this.parentNode.addNode(this.currentNode);
         }
      }

      @Override
      public void characters(char[] ch, int start, int length) throws SAXException {
         while (length > 0 && Character.isWhitespace(ch[start])) {
            start++;
            length--;
         }

         while (length > 0 && Character.isWhitespace(ch[start + length - 1])) {
            length--;
         }

         if (length != 0) {
            if (this.currentNode == null) {
               throw new SAXException("unexpected characters");
            }

            this.currentNode.setContent(new String(ch, start, length));
         }
      }

      @Override
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

   public static class StackNode extends ProfileParser.Node {
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
            if (id == null) {
               throw new SAXException("Invalid/Unknown item requested: " + id);
            }

            int meta = XmlUtil.getIntAttr(attributes, "meta", 32767);
            this.stack = new ItemStack(id, 1, meta);
            String nbt = attributes.getValue("nbt");
            if (nbt != null) {
               try {
                  this.stack.setTagCompound(JsonToNBT.getTagFromJson(nbt));
               } catch (NBTException e) {
                  throw new SAXException("Invalid stack NBT: " + nbt, e);
               }
            }
         }
      }

      @Override
      public ProfileParser.NodeType getType() {
         return ProfileParser.NodeType.stack;
      }
   }

   public static class StyleNode extends ProfileParser.Node {
      public final Version style;

      StyleNode(ProfileParser.ParentNode parent, Attributes attributes) throws SAXException {
         super(parent);
         this.style = Version.valueOf(XmlUtil.getAttr(attributes, "type"));
      }

      @Override
      public ProfileParser.NodeType getType() {
         return ProfileParser.NodeType.style;
      }
   }

   public static class TextureNode extends ProfileParser.Node {
      private final String mod;
      public Function<ProfileTarget, TextureStyle> style;

      TextureNode(ProfileParser.ParentNode parent, Attributes attributes) {
         super(parent);
         this.mod = XmlUtil.getAttr(attributes, "mod", "ic2");
      }

      @Override
      public ProfileParser.NodeType getType() {
         return ProfileParser.NodeType.textures;
      }

      @Override
      public void setContent(String content) throws SAXException {
         switch (content) {
            case "NEW":
               this.style = root -> TextureStyle.EXPERIMENTAL;
               break;
            case "OLD":
               this.style = root -> TextureStyle.CLASSIC;
               break;
            default:
               this.style = root -> new TextureStyle(this.mod, root.offset(content));
         }
      }
   }
}

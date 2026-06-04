package ic2.core.profile;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackFileNotFoundException;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TextureStyle {
  public static final TextureStyle EXPERIMENTAL = new TextureStyle("ic2", ProfileTarget.fromJar("assets/ic2")) {
      @SideOnly(Side.CLIENT)
      public IResourcePack applyChanges() {
        return null;
      }
    };
  
  public static final TextureStyle CLASSIC = new TextureStyle("ic2", ProfileTarget.fromJar("ic2/profiles/classic/ic2"));
  
  protected final ProfileTarget target;
  
  public final String mod;
  
  public TextureStyle(String mod, ProfileTarget target) {
    this.mod = mod;
    this.target = target;
  }
  
  @SideOnly(Side.CLIENT)
  public IResourcePack applyChanges() {
    if (this.target.isFile())
      return new IResourcePack() {
          public String getPackName() {
            return "IC2 Profile Pack for " + TextureStyle.this.mod;
          }
          
          public Set<String> getResourceDomains() {
            return Collections.singleton(TextureStyle.this.mod);
          }
          
          public boolean resourceExists(ResourceLocation location) {
            if (!TextureStyle.this.mod.equals(location.getResourceDomain()))
              return false; 
            return (TextureStyle.this.target.asZip().getEntry(location.getResourcePath()) != null);
          }
          
          public InputStream getInputStream(ResourceLocation location) throws IOException {
            if (!TextureStyle.this.mod.equals(location.getResourceDomain()))
              return null; 
            ZipFile zip = TextureStyle.this.target.asZip();
            ZipEntry entry = zip.getEntry(location.getResourcePath());
            if (entry == null)
              throw new ResourcePackFileNotFoundException(TextureStyle.this.target.root, location.getResourcePath()); 
            return zip.getInputStream(entry);
          }
          
          public <T extends net.minecraft.client.resources.data.IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
            return null;
          }
          
          public BufferedImage getPackImage() throws IOException {
            throw new IOException();
          }
        }; 
    return (IResourcePack)new FolderResourcePack(this.target.asFile()) {
        public String getPackName() {
          return "IC2 Profile Pack for " + TextureStyle.this.mod;
        }
        
        public Set<String> getResourceDomains() {
          return Collections.singleton(TextureStyle.this.mod);
        }
        
        public boolean resourceExists(ResourceLocation location) {
          if (!TextureStyle.this.mod.equals(location.getResourceDomain()))
            return false; 
          return (findFile(location.getResourcePath()) != null);
        }
        
        public InputStream getInputStream(ResourceLocation location) throws IOException {
          if (!TextureStyle.this.mod.equals(location.getResourceDomain()))
            return null; 
          File file = findFile(location.getResourcePath());
          if (file == null)
            throw new ResourcePackFileNotFoundException(this.resourcePackFile, location.getResourcePath()); 
          return new BufferedInputStream(new FileInputStream(file));
        }
        
        protected File findFile(String path) {
          try {
            File file = new File(this.resourcePackFile, path);
            if (file.isFile() && validatePath(file, path))
              return file; 
          } catch (IOException iOException) {}
          return null;
        }
        
        public <T extends net.minecraft.client.resources.data.IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
          return null;
        }
        
        public BufferedImage getPackImage() throws IOException {
          throw new IOException();
        }
      };
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof TextureStyle))
      return false; 
    TextureStyle other = (TextureStyle)obj;
    return (this.mod.equals(other.mod) && this.target.equals(other.target));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.mod, this.target.root, this.target.offset });
  }
  
  public String toString() {
    return "TextureStyle<" + this.mod + ": " + this.target + '>';
  }
}

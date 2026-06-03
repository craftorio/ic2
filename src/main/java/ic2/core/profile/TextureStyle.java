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
          public String func_130077_b() {
            return "IC2 Profile Pack for " + TextureStyle.this.mod;
          }
          
          public Set<String> func_110587_b() {
            return Collections.singleton(TextureStyle.this.mod);
          }
          
          public boolean func_110589_b(ResourceLocation location) {
            if (!TextureStyle.this.mod.equals(location.func_110624_b()))
              return false; 
            return (TextureStyle.this.target.asZip().getEntry(location.func_110623_a()) != null);
          }
          
          public InputStream func_110590_a(ResourceLocation location) throws IOException {
            if (!TextureStyle.this.mod.equals(location.func_110624_b()))
              return null; 
            ZipFile zip = TextureStyle.this.target.asZip();
            ZipEntry entry = zip.getEntry(location.func_110623_a());
            if (entry == null)
              throw new ResourcePackFileNotFoundException(TextureStyle.this.target.root, location.func_110623_a()); 
            return zip.getInputStream(entry);
          }
          
          public <T extends net.minecraft.client.resources.data.IMetadataSection> T func_135058_a(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
            return null;
          }
          
          public BufferedImage func_110586_a() throws IOException {
            throw new IOException();
          }
        }; 
    return (IResourcePack)new FolderResourcePack(this.target.asFile()) {
        public String func_130077_b() {
          return "IC2 Profile Pack for " + TextureStyle.this.mod;
        }
        
        public Set<String> func_110587_b() {
          return Collections.singleton(TextureStyle.this.mod);
        }
        
        public boolean func_110589_b(ResourceLocation location) {
          if (!TextureStyle.this.mod.equals(location.func_110624_b()))
            return false; 
          return (findFile(location.func_110623_a()) != null);
        }
        
        public InputStream func_110590_a(ResourceLocation location) throws IOException {
          if (!TextureStyle.this.mod.equals(location.func_110624_b()))
            return null; 
          File file = findFile(location.func_110623_a());
          if (file == null)
            throw new ResourcePackFileNotFoundException(this.field_110597_b, location.func_110623_a()); 
          return new BufferedInputStream(new FileInputStream(file));
        }
        
        protected File findFile(String path) {
          try {
            File file = new File(this.field_110597_b, path);
            if (file.isFile() && func_191384_a(file, path))
              return file; 
          } catch (IOException iOException) {}
          return null;
        }
        
        public <T extends net.minecraft.client.resources.data.IMetadataSection> T func_135058_a(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
          return null;
        }
        
        public BufferedImage func_110586_a() throws IOException {
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

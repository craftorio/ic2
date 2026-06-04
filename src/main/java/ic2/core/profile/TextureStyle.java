// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.profile;

import java.util.Objects;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import net.minecraft.client.resources.FolderResourcePack;
import java.awt.image.BufferedImage;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.client.resources.ResourcePackFileNotFoundException;
import java.io.InputStream;
import net.minecraft.util.ResourceLocation;
import java.util.Collections;
import java.util.Set;
import net.minecraft.client.resources.IResourcePack;

public class TextureStyle
{
    public static final TextureStyle EXPERIMENTAL;
    public static final TextureStyle CLASSIC;
    protected final ProfileTarget target;
    public final String mod;
    
    public TextureStyle(final String mod, final ProfileTarget target) {
        this.mod = mod;
        this.target = target;
    }
    
    @SideOnly(Side.CLIENT)
    public IResourcePack applyChanges() {
        if (this.target.isFile()) {
            return (IResourcePack)new IResourcePack() {
                public String getPackName() {
                    return "IC2 Profile Pack for " + TextureStyle.this.mod;
                }
                
                public Set<String> getResourceDomains() {
                    return Collections.singleton(TextureStyle.this.mod);
                }
                
                public boolean resourceExists(final ResourceLocation location) {
                    return TextureStyle.this.mod.equals(location.getResourceDomain()) && TextureStyle.this.target.asZip().getEntry(location.getResourcePath()) != null;
                }
                
                public InputStream getInputStream(final ResourceLocation location) throws IOException {
                    if (!TextureStyle.this.mod.equals(location.getResourceDomain())) {
                        return null;
                    }
                    final ZipFile zip = TextureStyle.this.target.asZip();
                    final ZipEntry entry = zip.getEntry(location.getResourcePath());
                    if (entry == null) {
                        throw new ResourcePackFileNotFoundException(TextureStyle.this.target.root, location.getResourcePath());
                    }
                    return zip.getInputStream(entry);
                }
                
                public <T extends IMetadataSection> T getPackMetadata(final MetadataSerializer metadataSerializer, final String metadataSectionName) throws IOException {
                    return null;
                }
                
                public BufferedImage getPackImage() throws IOException {
                    throw new IOException();
                }
            };
        }
        return (IResourcePack)new FolderResourcePack(this.target.asFile()) {
            public String getPackName() {
                return "IC2 Profile Pack for " + TextureStyle.this.mod;
            }
            
            public Set<String> getResourceDomains() {
                return Collections.singleton(TextureStyle.this.mod);
            }
            
            public boolean resourceExists(final ResourceLocation location) {
                return TextureStyle.this.mod.equals(location.getResourceDomain()) && this.findFile(location.getResourcePath()) != null;
            }
            
            public InputStream getInputStream(final ResourceLocation location) throws IOException {
                if (!TextureStyle.this.mod.equals(location.getResourceDomain())) {
                    return null;
                }
                final File file = this.findFile(location.getResourcePath());
                if (file == null) {
                    throw new ResourcePackFileNotFoundException(this.resourcePackFile, location.getResourcePath());
                }
                return new BufferedInputStream(new FileInputStream(file));
            }
            
            protected File findFile(final String path) {
                try {
                    final File file = new File(this.resourcePackFile, path);
                    if (file.isFile() && validatePath(file, path)) {
                        return file;
                    }
                }
                catch (final IOException ex) {}
                return null;
            }
            
            public <T extends IMetadataSection> T getPackMetadata(final MetadataSerializer metadataSerializer, final String metadataSectionName) throws IOException {
                return null;
            }
            
            public BufferedImage getPackImage() throws IOException {
                throw new IOException();
            }
        };
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TextureStyle)) {
            return false;
        }
        final TextureStyle other = (TextureStyle)obj;
        return this.mod.equals(other.mod) && this.target.equals(other.target);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.mod, this.target.root, this.target.offset);
    }
    
    @Override
    public String toString() {
        return "TextureStyle<" + this.mod + ": " + this.target + '>';
    }
    
    static {
        EXPERIMENTAL = new TextureStyle("ic2", ProfileTarget.fromJar("assets/ic2")) {
            @SideOnly(Side.CLIENT)
            @Override
            public IResourcePack applyChanges() {
                return null;
            }
        };
        CLASSIC = new TextureStyle("ic2", ProfileTarget.fromJar("ic2/profiles/classic/ic2"));
    }
}

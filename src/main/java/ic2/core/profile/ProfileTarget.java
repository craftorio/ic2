// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.profile;

import java.util.Objects;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;
import java.util.zip.ZipEntry;
import java.io.IOException;
import java.util.HashMap;
import net.minecraftforge.fml.common.Loader;
import java.util.zip.ZipFile;
import java.util.Map;
import java.io.File;

public class ProfileTarget
{
    public final File root;
    public final String offset;
    private static final Map<File, Map<String, ZipFile>> ZIP_HOLDER;
    
    public static ProfileTarget fromJar(final String offset) {
        assert "ic2".equals(Loader.instance().activeModContainer().getModId());
        return new ProfileTarget(Loader.instance().activeModContainer().getSource(), offset);
    }
    
    public ProfileTarget(final File root, final String offset) {
        this.root = root;
        this.offset = offset;
    }
    
    public boolean isFile() {
        return !this.root.isDirectory();
    }
    
    public File asFile() {
        return new File(this.root, this.offset);
    }
    
    public ZipFile asZip() {
        final Map<String, ZipFile> offsetMap = ProfileTarget.ZIP_HOLDER.computeIfAbsent(this.root, k -> new HashMap());
        ZipFile ret = offsetMap.get(this.offset);
        if (ret == null) {
            try {
                offsetMap.put(this.offset, ret = this.makeZip());
            }
            catch (final IOException e) {
                throw new RuntimeException("Failed to get zip!", e);
            }
        }
        return ret;
    }
    
    protected ZipFile makeZip() throws IOException {
        return new ZipFile(this.root) {
            @Override
            public ZipEntry getEntry(final String name) {
                return super.getEntry(StringUtils.isNotBlank((CharSequence)name) ? (ProfileTarget.this.offset + '/' + name) : ProfileTarget.this.offset);
            }
        };
    }
    
    public InputStream asStream() throws IOException {
        if (this.isFile()) {
            final ZipFile zip = this.asZip();
            final ZipEntry entry = zip.getEntry(null);
            if (entry == null) {
                return null;
            }
            return zip.getInputStream(entry);
        }
        else {
            final File file = this.asFile();
            if (!file.canRead() || !file.isFile()) {
                return null;
            }
            return new BufferedInputStream(new FileInputStream(file));
        }
    }
    
    public ProfileTarget offset(final String extra) {
        return new ProfileTarget(this.root, this.offset + '/' + extra);
    }
    
    @Override
    public String toString() {
        return "ProfileTarget<" + this.root + " -> " + this.offset + '>';
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProfileTarget)) {
            return false;
        }
        final ProfileTarget other = (ProfileTarget)obj;
        return Objects.equals(this.root, other.root) && Objects.equals(this.offset, other.offset);
    }
    
    static {
        ZIP_HOLDER = new HashMap<File, Map<String, ZipFile>>();
    }
}

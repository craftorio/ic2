package ic2.core.profile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.StringUtils;

public class ProfileTarget {
   public final File root;
   public final String offset;
   private static final Map<File, Map<String, ZipFile>> ZIP_HOLDER = new HashMap<>();

   public static ProfileTarget fromJar(String offset) {
      assert "ic2".equals(Loader.instance().activeModContainer().getModId());
      return new ProfileTarget(Loader.instance().activeModContainer().getSource(), offset);
   }

   public ProfileTarget(File root, String offset) {
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
      Map<String, ZipFile> offsetMap = ZIP_HOLDER.computeIfAbsent(this.root, k -> new HashMap<>());
      ZipFile ret = offsetMap.get(this.offset);
      if (ret == null) {
         try {
            offsetMap.put(this.offset, ret = this.makeZip());
         } catch (IOException e) {
            throw new RuntimeException("Failed to get zip!", e);
         }
      }

      return ret;
   }

   protected ZipFile makeZip() throws IOException {
      return new ZipFile(this.root) {
         @Override
         public ZipEntry getEntry(String name) {
            return super.getEntry(StringUtils.isNotBlank(name) ? ProfileTarget.this.offset + '/' + name : ProfileTarget.this.offset);
         }
      };
   }

   public InputStream asStream() throws IOException {
      if (this.isFile()) {
         ZipFile zip = this.asZip();
         ZipEntry entry = zip.getEntry(null);
         return entry == null ? null : zip.getInputStream(entry);
      } else {
         File file = this.asFile();
         return file.canRead() && file.isFile() ? new BufferedInputStream(new FileInputStream(file)) : null;
      }
   }

   public ProfileTarget offset(String extra) {
      return new ProfileTarget(this.root, this.offset + '/' + extra);
   }

   @Override
   public String toString() {
      return "ProfileTarget<" + this.root + " -> " + this.offset + '>';
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof ProfileTarget)) {
         return false;
      }

      ProfileTarget other = (ProfileTarget)obj;
      return Objects.equals(this.root, other.root) && Objects.equals(this.offset, other.offset);
   }
}

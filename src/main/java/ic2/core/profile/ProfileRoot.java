package ic2.core.profile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class ProfileRoot extends ProfileTarget {
   public ProfileRoot(File root) {
      super(root, "");
   }

   @Override
   public File asFile() {
      return this.root;
   }

   @Override
   protected ZipFile makeZip() throws IOException {
      return new ZipFile(this.root);
   }

   @Override
   public InputStream asStream() throws IOException {
      return null;
   }

   @Override
   public ProfileTarget offset(String extra) {
      return new ProfileTarget(this.root, extra);
   }

   @Override
   public String toString() {
      return "ProfileRoot<" + this.root + '>';
   }
}

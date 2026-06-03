package ic2.core.profile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class ProfileRoot extends ProfileTarget {
  public ProfileRoot(File root) {
    super(root, "");
  }
  
  public File asFile() {
    return this.root;
  }
  
  protected ZipFile makeZip() throws IOException {
    return new ZipFile(this.root);
  }
  
  public InputStream asStream() throws IOException {
    return null;
  }
  
  public ProfileTarget offset(String extra) {
    return new ProfileTarget(this.root, extra);
  }
  
  public String toString() {
    return "ProfileRoot<" + this.root + '>';
  }
}

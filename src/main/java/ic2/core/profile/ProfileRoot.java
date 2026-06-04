// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.profile;

import java.io.InputStream;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.io.File;

public class ProfileRoot extends ProfileTarget
{
    public ProfileRoot(final File root) {
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
    public ProfileTarget offset(final String extra) {
        return new ProfileTarget(this.root, extra);
    }
    
    @Override
    public String toString() {
        return "ProfileRoot<" + this.root + '>';
    }
}

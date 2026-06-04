// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

import ic2.core.block.ITeBlock;

public class MetaTeBlock implements Comparable<MetaTeBlock>
{
    public final ITeBlock teBlock;
    public final boolean active;
    
    MetaTeBlock(final ITeBlock teBlock, final boolean active) {
        this.teBlock = teBlock;
        this.active = active;
    }
    
    @Override
    public int compareTo(final MetaTeBlock o) {
        final int ret = this.teBlock.getId() - o.teBlock.getId();
        if (ret != 0) {
            return ret;
        }
        return Boolean.compare(this.active, o.active);
    }
    
    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder("MetaTeBlock{").append(this.teBlock.getName());
        if (this.active) {
            ret.append("_active");
        }
        return ret.append('}').toString();
    }
}

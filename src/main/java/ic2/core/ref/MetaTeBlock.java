package ic2.core.ref;

import ic2.core.block.ITeBlock;

public class MetaTeBlock implements Comparable<MetaTeBlock> {
   public final ITeBlock teBlock;
   public final boolean active;

   MetaTeBlock(ITeBlock teBlock, boolean active) {
      this.teBlock = teBlock;
      this.active = active;
   }

   public int compareTo(MetaTeBlock o) {
      int ret = this.teBlock.getId() - o.teBlock.getId();
      return ret != 0 ? ret : Boolean.compare(this.active, o.active);
   }

   @Override
   public String toString() {
      StringBuilder ret = new StringBuilder("MetaTeBlock{").append(this.teBlock.getName());
      if (this.active) {
         ret.append("_active");
      }

      return ret.append('}').toString();
   }
}

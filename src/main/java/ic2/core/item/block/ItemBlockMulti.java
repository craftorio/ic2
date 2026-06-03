package ic2.core.item.block;

import ic2.core.block.BlockMultiID;
import ic2.core.block.state.IIdProvider;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.ItemStack;

public class ItemBlockMulti extends ItemBlockIC2 {
  public ItemBlockMulti(Block block) {
    super(block);
    func_77627_a(true);
  }
  
  public int func_77647_b(int damage) {
    return damage;
  }
  
  public String func_77667_c(ItemStack stack) {
    String name = ((IIdProvider)this.field_150939_a.func_176203_a(stack.func_77960_j()).func_177229_b((IProperty)((BlockMultiID)this.field_150939_a).getTypeProperty())).getName();
    return super.func_77667_c(stack) + "." + name;
  }
}

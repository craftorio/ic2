package ic2.core.item.block;

import ic2.core.block.Ic2Leaves;
import ic2.core.init.Localization;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.ItemLeaves;
import net.minecraft.item.ItemStack;

public class ItemIc2Leaves extends ItemLeaves {
  public ItemIc2Leaves(Block block) {
    super((BlockLeaves)block);
    func_77627_a(false);
  }
  
  public String func_77658_a() {
    return "ic2." + super.func_77658_a().substring(5);
  }
  
  public String func_77667_c(ItemStack stack) {
    return func_77658_a() + "." + ((Ic2Leaves.LeavesType)this.field_150939_a.func_176203_a(stack.func_77960_j()).func_177229_b((IProperty)Ic2Leaves.typeProperty)).getName();
  }
  
  public String func_77653_i(ItemStack stack) {
    return Localization.translate(func_77667_c(stack));
  }
}

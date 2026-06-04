package ic2.core.item.block;

import ic2.core.block.BlockBase;
import ic2.core.block.BlockScaffold;
import ic2.core.init.Localization;
import ic2.core.ref.BlockName;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockIC2 extends ItemBlock {
  public ItemBlockIC2(Block block) {
    super(block);
  }
  
  public String func_77667_c(ItemStack stack) {
    return func_77658_a();
  }
  
  public String func_77653_i(ItemStack stack) {
    return Localization.translate(func_77667_c(stack));
  }
  
  public boolean canHarvestBlock(IBlockState block, ItemStack stack) {
    return (block.getBlock() == BlockName.scaffold.getInstance());
  }
  
  public int getItemBurnTime(ItemStack stack) {
    if (this.field_150939_a == BlockName.scaffold.getInstance()) {
      BlockScaffold scaffold = (BlockScaffold)this.field_150939_a;
      IBlockState state = scaffold.getState(scaffold.getVariant(stack));
      return (state.getMaterial() == Material.field_151575_d) ? 300 : 0;
    } 
    return -1;
  }
  
  public EnumRarity func_77613_e(ItemStack stack) {
    if (this.field_150939_a instanceof BlockBase)
      return ((BlockBase)this.field_150939_a).getRarity(stack); 
    return super.func_77613_e(stack);
  }
  
  public static Function<Block, Item> supplier = ItemBlockIC2::new;
}

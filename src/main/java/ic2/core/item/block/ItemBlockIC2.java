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
  
  public String getUnlocalizedName(ItemStack stack) {
    return getUnlocalizedName();
  }
  
  public String getItemStackDisplayName(ItemStack stack) {
    return Localization.translate(getUnlocalizedName(stack));
  }
  
  public boolean canHarvestBlock(IBlockState block, ItemStack stack) {
    return (block.getBlock() == BlockName.scaffold.getInstance());
  }
  
  public int getItemBurnTime(ItemStack stack) {
    if (this.block == BlockName.scaffold.getInstance()) {
      BlockScaffold scaffold = (BlockScaffold)this.block;
      IBlockState state = scaffold.getState(scaffold.getVariant(stack));
      return (state.getMaterial() == Material.WOOD) ? 300 : 0;
    } 
    return -1;
  }
  
  public EnumRarity getRarity(ItemStack stack) {
    if (this.block instanceof BlockBase)
      return ((BlockBase)this.block).getRarity(stack); 
    return super.getRarity(stack);
  }
  
  public static Function<Block, Item> supplier = ItemBlockIC2::new;
}

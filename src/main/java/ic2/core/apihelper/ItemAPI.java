package ic2.core.apihelper;

import ic2.api.item.IItemAPI;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.IMultiBlock;
import ic2.core.ref.ItemName;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemAPI implements IItemAPI {
  public ItemStack getItemStack(String name, String variant) {
    if (name == null)
      return null; 
    if (variant == null) {
      int idx = name.indexOf('#');
      if (idx != -1) {
        variant = name.substring(idx + 1);
        name = name.substring(0, idx);
      } 
    } 
    BlockName blockName = getBlockName(name);
    if (blockName != null)
      return blockName.getItemStack(variant); 
    ItemName itemName = getItemName(name);
    if (itemName != null)
      return itemName.getItemStack(variant); 
    return null;
  }
  
  public Block getBlock(String name) {
    if (name == null)
      return null; 
    BlockName blockName = getBlockName(name);
    if (blockName != null)
      return blockName.getInstance(); 
    FluidName fluidName = getFluidName(name);
    if (fluidName != null)
      return fluidName.getInstance().getBlock(); 
    return null;
  }
  
  public Item getItem(String name) {
    if (name == null)
      return null; 
    ItemName itemName = getItemName(name);
    if (itemName != null)
      return itemName.getInstance(); 
    Block block = getBlock(name);
    if (block != null) {
      Item ret = Item.func_150898_a(block);
      if (ret != Items.field_190931_a || block == Blocks.field_150350_a)
        return ret; 
    } 
    return null;
  }
  
  public IBlockState getBlockState(String name, String variant) {
    if (variant == null) {
      int idx = name.indexOf('#');
      if (idx != -1) {
        variant = name.substring(idx + 1);
        name = name.substring(0, idx);
      } 
    } 
    BlockName blockName = getBlockName(name);
    if (blockName != null) {
      Block block = blockName.getInstance();
      if (block instanceof IMultiBlock)
        return ((IMultiBlock)block).getState(variant); 
      assert variant == null;
      return block.func_176223_P();
    } 
    FluidName fluidName = getFluidName(name);
    if (fluidName != null) {
      assert variant == null;
      return fluidName.getInstance().getBlock().func_176223_P();
    } 
    return null;
  }
  
  private ItemName getItemName(String itemName) {
    for (ItemName name : ItemName.values) {
      if (name.name().equalsIgnoreCase(itemName))
        return name; 
    } 
    return null;
  }
  
  private BlockName getBlockName(String blockName) {
    for (BlockName name : BlockName.values) {
      if (name.name().equalsIgnoreCase(blockName))
        return name; 
    } 
    return null;
  }
  
  private FluidName getFluidName(String fluidName) {
    for (FluidName name : FluidName.values) {
      if (name.name().equalsIgnoreCase(fluidName))
        return name; 
    } 
    return null;
  }
}

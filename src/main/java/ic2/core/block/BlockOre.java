package ic2.core.block;

import ic2.core.block.type.ResourceBlock;
import ic2.core.ref.BlockName;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockOre extends BlockMultiID<ResourceBlock> {
  public static BlockOre create() {
    return BlockMultiID.<ResourceBlock, BlockOre>create(BlockOre.class, ResourceBlock.class, new Object[0]);
  }
  
  public BlockOre() {
    super(BlockName.resource, Material.ROCK);
  }
  
  public int damageDropped(IBlockState state) {
    return getMetaFromState(state);
  }
}

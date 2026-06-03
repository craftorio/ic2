package ic2.core.block.steam;

import ic2.core.block.BlockBase;
import ic2.core.ref.BlockName;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockRefractoryBricks extends BlockBase {
  public BlockRefractoryBricks() {
    super(BlockName.refractory_bricks, Material.field_151576_e);
    func_149711_c(2.0F);
    func_149752_b(10.0F);
    setHarvestLevel("pickaxe", 0);
  }
  
  protected BlockStateContainer func_180661_e() {
    return new BlockStateContainer((Block)this, new net.minecraft.block.properties.IProperty[0]);
  }
  
  public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
    return false;
  }
}

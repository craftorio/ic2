package ic2.core.block.steam;

import ic2.core.block.BlockBase;
import ic2.core.ref.BlockName;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockRefractoryBricks extends BlockBase {
   public BlockRefractoryBricks() {
      super(BlockName.refractory_bricks, Material.ROCK);
      this.setHardness(2.0F);
      this.setResistance(10.0F);
      this.setHarvestLevel("pickaxe", 0);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[0]);
   }

   public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
      return false;
   }
}

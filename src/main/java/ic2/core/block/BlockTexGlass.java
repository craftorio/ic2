package ic2.core.block;

import ic2.core.block.state.IIdProvider;
import ic2.core.ref.BlockName;
import java.util.Random;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTexGlass extends BlockMultiID<BlockTexGlass.GlassType> {
   public static BlockTexGlass create() {
      return BlockMultiID.create(BlockTexGlass.class, BlockTexGlass.GlassType.class);
   }

   private BlockTexGlass() {
      super(BlockName.glass, Material.GLASS);
      this.setHardness(5.0F);
      this.setResistance(180.0F);
      this.setSoundType(SoundType.GLASS);
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean isFullBlock(IBlockState state) {
      return true;
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public boolean isTopSolid(IBlockState state) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
      return false;
   }

   public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return world.getBlockState(pos.offset(side)).getBlock() == this ? false : super.shouldSideBeRendered(state, world, pos, side);
   }

   public enum GlassType implements IIdProvider {
      reinforced;

      @Override
      public String getName() {
         return this.name();
      }

      @Override
      public int getId() {
         return this.ordinal();
      }
   }
}

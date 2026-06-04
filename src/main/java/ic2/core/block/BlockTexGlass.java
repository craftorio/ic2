package ic2.core.block;

import ic2.core.block.state.IIdProvider;
import ic2.core.ref.BlockName;
import java.util.Random;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTexGlass extends BlockMultiID<BlockTexGlass.GlassType> {
  public static BlockTexGlass create() {
    return BlockMultiID.<GlassType, BlockTexGlass>create(BlockTexGlass.class, GlassType.class, new Object[0]);
  }
  
  private BlockTexGlass() {
    super(BlockName.glass, Material.field_151592_s);
    func_149711_c(5.0F);
    func_149752_b(180.0F);
    func_149672_a(SoundType.field_185853_f);
  }
  
  public int func_149745_a(Random random) {
    return 0;
  }
  
  public boolean func_149662_c(IBlockState state) {
    return false;
  }
  
  public boolean func_149730_j(IBlockState state) {
    return true;
  }
  
  public boolean func_149686_d(IBlockState state) {
    return false;
  }
  
  public boolean func_185481_k(IBlockState state) {
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer func_180664_k() {
    return BlockRenderLayer.CUTOUT;
  }
  
  public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
    return false;
  }
  
  public boolean func_176225_a(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    if (world.getBlockState(pos.func_177972_a(side)).getBlock() == this)
      return false; 
    return super.func_176225_a(state, world, pos, side);
  }
  
  public enum GlassType implements IIdProvider {
    reinforced;
    
    public String getName() {
      return name();
    }
    
    public int getId() {
      return ordinal();
    }
  }
}

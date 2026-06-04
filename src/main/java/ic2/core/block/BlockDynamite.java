package ic2.core.block;

import ic2.core.ref.BlockName;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDynamite extends BlockBase {
  public BlockDynamite() {
    super(BlockName.dynamite, MaterialIC2TNT.instance, (Function<Block, Item>)null);
    func_149675_a(true);
    func_149711_c(0.0F);
    func_149672_a(SoundType.field_185850_c);
    func_149647_a(null);
    func_180632_j(getDefaultState().func_177226_a(linked, Boolean.valueOf(false)).func_177226_a((IProperty)BlockTorch.field_176596_a, (Comparable)EnumFacing.UP));
  }
  
  protected BlockStateContainer func_180661_e() {
    return new BlockStateContainer(this, new IProperty[] { (IProperty)BlockTorch.field_176596_a, linked });
  }
  
  public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
    return Blocks.field_150478_aa.getDefaultState().func_177226_a((IProperty)BlockTorch.field_176596_a, state.func_177229_b((IProperty)BlockTorch.field_176596_a)).func_185900_c(source, pos);
  }
  
  public boolean func_149662_c(IBlockState state) {
    return false;
  }
  
  public boolean func_149686_d(IBlockState state) {
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer func_180664_k() {
    return BlockRenderLayer.CUTOUT;
  }
  
  public boolean func_176196_c(World world, BlockPos pos) {
    for (EnumFacing dir : BlockTorch.field_176596_a.func_177700_c()) {
      if (world.func_175677_d(pos.offset(dir.getOpposite()), false))
        return true; 
    } 
    return false;
  }
  
  public IBlockState func_180642_a(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    if (facing == EnumFacing.DOWN || !world.func_175677_d(pos.offset(facing.getOpposite()), false))
      for (EnumFacing facing2 : BlockTorch.field_176596_a.func_177700_c()) {
        if (world.func_175677_d(pos.offset(facing2.getOpposite()), false)) {
          facing = facing2;
          break;
        } 
      }  
    return getDefaultState().func_177226_a((IProperty)BlockTorch.field_176596_a, (Comparable)facing);
  }
  
  public int func_176201_c(IBlockState state) {
    return ((EnumFacing)state.func_177229_b((IProperty)BlockTorch.field_176596_a)).ordinal() << 1 | (((Boolean)state.func_177229_b(linked)).booleanValue() ? 1 : 0);
  }
  
  public IBlockState func_176203_a(int meta) {
    return getDefaultState().func_177226_a(linked, Boolean.valueOf(((meta & 0x1) != 0))).func_177226_a((IProperty)BlockTorch.field_176596_a, (Comparable)EnumFacing.VALUES[meta >> 1]);
  }
  
  public void func_180633_a(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    checkPlacement(world, pos, state);
  }
  
  public void func_180645_a(World world, BlockPos pos, IBlockState state, Random random) {
    checkPlacement(world, pos, state);
  }
  
  public void func_189540_a(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
    checkPlacement(world, pos, state);
  }
  
  public int func_149745_a(Random random) {
    return 0;
  }
  
  public int func_180651_a(IBlockState state) {
    return 0;
  }
  
  public void func_180652_a(World world, BlockPos pos, Explosion explosion) {
    explode(world, pos, (explosion != null) ? explosion.func_94613_c() : null, true);
  }
  
  public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
    if (!world.isRemote)
      explode(world, pos, (EntityLivingBase)player, false); 
    return false;
  }
  
  private void checkPlacement(World world, BlockPos pos, IBlockState state) {
    if (world.isRemote)
      return; 
    if (world.isBlockPowered(pos)) {
      explode(world, pos, (EntityLivingBase)null, false);
    } else if (!world.func_175677_d(pos.offset(((EnumFacing)state.func_177229_b((IProperty)BlockTorch.field_176596_a)).getOpposite()), false)) {
      world.func_175698_g(pos);
      func_176226_b(world, pos, state, 0);
    } 
  }
  
  private void explode(World world, BlockPos pos, EntityLivingBase player, boolean byExplosion) {
    world.func_175698_g(pos);
    EntityDynamite entity = new EntityStickyDynamite(world, pos.getX() + 0.5D, pos.getY() + 0.5D, (pos.getZ() + 0.5F));
    entity.owner = player;
    entity.fuse = byExplosion ? 5 : 40;
    world.spawnEntity(entity);
    world.func_184133_a(null, pos, SoundEvents.field_187904_gd, SoundCategory.BLOCKS, 1.0F, 1.0F);
  }
  
  public RayTraceResult func_180636_a(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
    return Blocks.field_150478_aa.func_180636_a(state, world, pos, start, end);
  }
  
  public static final IProperty<Boolean> linked = (IProperty<Boolean>)PropertyBool.func_177716_a("linked");
}

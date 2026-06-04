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
    setTickRandomly(true);
    setHardness(0.0F);
    setSoundType(SoundType.PLANT);
    setCreativeTab(null);
    setDefaultState(getDefaultState().withProperty(linked, Boolean.valueOf(false)).withProperty((IProperty)BlockTorch.FACING, (Comparable)EnumFacing.UP));
  }
  
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, new IProperty[] { (IProperty)BlockTorch.FACING, linked });
  }
  
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    return Blocks.TORCH.getDefaultState().withProperty((IProperty)BlockTorch.FACING, state.getValue((IProperty)BlockTorch.FACING)).getBoundingBox(source, pos);
  }
  
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }
  
  public boolean isFullCube(IBlockState state) {
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getBlockLayer() {
    return BlockRenderLayer.CUTOUT;
  }
  
  public boolean canPlaceBlockAt(World world, BlockPos pos) {
    for (EnumFacing dir : BlockTorch.FACING.getAllowedValues()) {
      if (world.isBlockNormalCube(pos.offset(dir.getOpposite()), false))
        return true; 
    } 
    return false;
  }
  
  public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    if (facing == EnumFacing.DOWN || !world.isBlockNormalCube(pos.offset(facing.getOpposite()), false))
      for (EnumFacing facing2 : BlockTorch.FACING.getAllowedValues()) {
        if (world.isBlockNormalCube(pos.offset(facing2.getOpposite()), false)) {
          facing = facing2;
          break;
        } 
      }  
    return getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)facing);
  }
  
  public int getMetaFromState(IBlockState state) {
    return ((EnumFacing)state.getValue((IProperty)BlockTorch.FACING)).ordinal() << 1 | (((Boolean)state.getValue(linked)).booleanValue() ? 1 : 0);
  }
  
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty(linked, Boolean.valueOf(((meta & 0x1) != 0))).withProperty((IProperty)BlockTorch.FACING, (Comparable)EnumFacing.VALUES[meta >> 1]);
  }
  
  public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    checkPlacement(world, pos, state);
  }
  
  public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
    checkPlacement(world, pos, state);
  }
  
  public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
    checkPlacement(world, pos, state);
  }
  
  public int quantityDropped(Random random) {
    return 0;
  }
  
  public int damageDropped(IBlockState state) {
    return 0;
  }
  
  public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
    explode(world, pos, (explosion != null) ? explosion.getExplosivePlacedBy() : null, true);
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
    } else if (!world.isBlockNormalCube(pos.offset(((EnumFacing)state.getValue((IProperty)BlockTorch.FACING)).getOpposite()), false)) {
      world.setBlockToAir(pos);
      dropBlockAsItem(world, pos, state, 0);
    } 
  }
  
  private void explode(World world, BlockPos pos, EntityLivingBase player, boolean byExplosion) {
    world.setBlockToAir(pos);
    EntityDynamite entity = new EntityStickyDynamite(world, pos.getX() + 0.5D, pos.getY() + 0.5D, (pos.getZ() + 0.5F));
    entity.owner = player;
    entity.fuse = byExplosion ? 5 : 40;
    world.spawnEntity(entity);
    world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
  }
  
  public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
    return Blocks.TORCH.collisionRayTrace(state, world, pos, start, end);
  }
  
  public static final IProperty<Boolean> linked = (IProperty<Boolean>)PropertyBool.create("linked");
}

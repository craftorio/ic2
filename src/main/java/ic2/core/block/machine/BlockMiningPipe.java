package ic2.core.block.machine;

import ic2.core.block.BlockMultiID;
import ic2.core.block.state.IIdProvider;
import ic2.core.ref.BlockName;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMiningPipe extends BlockMultiID<BlockMiningPipe.MiningPipeType> {
  public static BlockMiningPipe create() {
    return (BlockMiningPipe)BlockMultiID.create(BlockMiningPipe.class, MiningPipeType.class, new Object[0]);
  }
  
  public BlockMiningPipe() {
    super(BlockName.mining_pipe, Material.IRON);
    setHardness(6.0F);
    setResistance(10.0F);
  }
  
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    return false;
  }
  
  public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
    MiningPipeType type = (MiningPipeType)getType(state);
    if (type == null)
      return true; 
    return (type != MiningPipeType.pipe);
  }
  
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
    MiningPipeType type = (MiningPipeType)getType(state);
    if (type == null)
      return super.getBoundingBox(state, world, pos); 
    return getAabb(type);
  }
  
  private AxisAlignedBB getAabb(MiningPipeType type) {
    switch (type) {
      case pipe:
        return pipeAabb;
    } 
    return FULL_BLOCK_AABB;
  }
  
  public int getLightOpacity(IBlockState state) {
    return state.isFullCube() ? 255 : 0;
  }
  
  public boolean isFullCube(IBlockState state) {
    MiningPipeType type = (MiningPipeType)getType(state);
    if (type == null)
      return super.isFullCube(state); 
    switch (type) {
      case pipe:
        return false;
    } 
    return true;
  }
  
  public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
    MiningPipeType type = (MiningPipeType)getType(state);
    if (type == null)
      return true; 
    switch (type) {
      case pipe:
        return false;
      case tip:
        return true;
    } 
    return true;
  }
  
  public ItemStack getItemStack(IBlockState state) {
    MiningPipeType type = (MiningPipeType)getType(state);
    if (type == MiningPipeType.tip)
      return getItemStack(MiningPipeType.pipe); 
    return super.getItemStack(state);
  }
  
  public void getSubBlocks(CreativeTabs tabs, NonNullList<ItemStack> itemList) {
    for (MiningPipeType type : this.typeProperty.getShownValues()) {
      if (type == MiningPipeType.tip)
        continue; 
      itemList.add(getItemStack(type));
    } 
  }
  
  public enum MiningPipeType implements IIdProvider {
    pipe, tip;
    
    public String getName() {
      return name();
    }
    
    public int getId() {
      return ordinal();
    }
  }
  
  private static final AxisAlignedBB pipeAabb = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D);
}

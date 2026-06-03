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
    super(BlockName.mining_pipe, Material.field_151573_f);
    func_149711_c(6.0F);
    func_149752_b(10.0F);
  }
  
  public boolean func_176196_c(World worldIn, BlockPos pos) {
    return false;
  }
  
  public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
    MiningPipeType type = (MiningPipeType)getType(state);
    if (type == null)
      return true; 
    return (type != MiningPipeType.pipe);
  }
  
  public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess world, BlockPos pos) {
    MiningPipeType type = (MiningPipeType)getType(state);
    if (type == null)
      return super.func_185496_a(state, world, pos); 
    return getAabb(type);
  }
  
  private AxisAlignedBB getAabb(MiningPipeType type) {
    switch (type) {
      case pipe:
        return pipeAabb;
    } 
    return field_185505_j;
  }
  
  public int func_149717_k(IBlockState state) {
    return state.func_185917_h() ? 255 : 0;
  }
  
  public boolean func_149686_d(IBlockState state) {
    MiningPipeType type = (MiningPipeType)getType(state);
    if (type == null)
      return super.func_149686_d(state); 
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
  
  public void func_149666_a(CreativeTabs tabs, NonNullList<ItemStack> itemList) {
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

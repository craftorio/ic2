package ic2.core.block;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.item.block.ItemIc2Leaves;
import ic2.core.ref.BlockName;
import ic2.core.ref.IBlockModelProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Ic2Leaves extends BlockLeaves implements IBlockModelProvider {
  public Ic2Leaves() {
    setUnlocalizedName(BlockName.leaves.name());
    setCreativeTab((CreativeTabs)IC2.tabIC2);
    ResourceLocation name = IC2.getIdentifier(BlockName.leaves.name());
    BlocksItems.registerBlock((Block)this, name);
    BlocksItems.registerItem((Item)new ItemIc2Leaves((Block)this), name);
    BlockName.leaves.setInstance((Block)this);
    setDefaultState(this.blockState.getBaseState()
        .withProperty((IProperty)CHECK_DECAY, Boolean.valueOf(true))
        .withProperty((IProperty)DECAYABLE, Boolean.valueOf(true))
        .withProperty((IProperty)typeProperty, LeavesType.rubber));
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(BlockName name) {
    StateMap stateMap = (new StateMap.Builder()).ignore(new IProperty[] { (IProperty)CHECK_DECAY, (IProperty)DECAYABLE }).build();
    ModelLoader.setCustomStateMapper((Block)this, (IStateMapper)stateMap);
    List<IBlockState> states = new ArrayList<>(typeProperty.getAllowedValues().size());
    for (LeavesType type : LeavesType.values)
      states.add(getDropState(getDefaultState().withProperty((IProperty)typeProperty, type))); 
    BlockBase.registerItemModels((Block)this, states, (IStateMapper)stateMap);
  }
  
  private static IBlockState getDropState(IBlockState state) {
    return state.withProperty((IProperty)CHECK_DECAY, Boolean.valueOf(false))
      .withProperty((IProperty)DECAYABLE, Boolean.valueOf(false));
  }
  
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer((Block)this, new IProperty[] { (IProperty)CHECK_DECAY, (IProperty)DECAYABLE, (IProperty)typeProperty });
  }
  
  public IBlockState getStateFromMeta(int meta) {
    boolean checkDecay = ((meta & 0x8) != 0);
    boolean decayable = ((meta & 0x4) != 0);
    meta &= 0x3;
    IBlockState ret = getDefaultState().withProperty((IProperty)CHECK_DECAY, Boolean.valueOf(checkDecay)).withProperty((IProperty)DECAYABLE, Boolean.valueOf(decayable));
    if (meta < LeavesType.values.length)
      ret = ret.withProperty((IProperty)typeProperty, LeavesType.values[meta]); 
    return ret;
  }
  
  public int getMetaFromState(IBlockState state) {
    int ret = 0;
    if (((Boolean)state.getValue((IProperty)CHECK_DECAY)).booleanValue())
      ret |= 0x8; 
    if (((Boolean)state.getValue((IProperty)DECAYABLE)).booleanValue())
      ret |= 0x4; 
    ret |= ((LeavesType)state.getValue((IProperty)typeProperty)).ordinal();
    return ret;
  }
  
  public boolean isOpaqueCube(IBlockState state) {
    return Blocks.LEAVES.isOpaqueCube(state);
  }
  
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getBlockLayer() {
    return Blocks.LEAVES.getBlockLayer();
  }
  
  public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    BlockPos nPos = pos.offset(side);
    return ((!isOpaqueCube(state) || world.getBlockState(nPos) != state) && 
      !world.getBlockState(nPos).doesSideBlockRendering(world, nPos, side.getOpposite()));
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return ((LeavesType)state.getValue((IProperty)typeProperty)).getSapling().getItem();
  }
  
  public int damageDropped(IBlockState state) {
    return ((LeavesType)state.getValue((IProperty)typeProperty)).getSapling().getMetadata();
  }
  
  protected int getSaplingDropChance(IBlockState state) {
    return ((LeavesType)state.getValue((IProperty)typeProperty)).saplingDropChance;
  }
  
  public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
    IBlockState state = getDropState(world.getBlockState(pos));
    return Arrays.asList(new ItemStack[] { new ItemStack((Block)this, 1, getMetaFromState(state)) });
  }
  
  public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
    IBlockState state = getDropState(getDefaultState());
    for (LeavesType type : LeavesType.values)
      list.add(new ItemStack((Block)this, 1, getMetaFromState(state.withProperty((IProperty)typeProperty, type)))); 
  }
  
  public BlockPlanks.EnumType getWoodType(int meta) {
    return null;
  }
  
  public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
    return true;
  }
  
  public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
    return true;
  }
  
  public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
    return true;
  }
  
  public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
    return 30;
  }
  
  public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
    return 20;
  }
  
  public enum LeavesType implements IStringSerializable {
    rubber(35);
    
    private static final LeavesType[] values = values();
    
    public final int saplingDropChance;
    
    LeavesType(int saplingDropChance) {
      this.saplingDropChance = saplingDropChance;
    }
    
    public String getName() {
      return name();
    }
    
    public ItemStack getSapling() {
      return new ItemStack(BlockName.sapling.getInstance());
    }
    
    static {
    
    }
  }
  
  public static final PropertyEnum<LeavesType> typeProperty = PropertyEnum.create("type", LeavesType.class);
  
  private static final int checkDecayFlag = 8;
  
  private static final int decayableFlag = 4;
}

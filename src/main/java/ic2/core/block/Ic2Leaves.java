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
    func_149663_c(BlockName.leaves.name());
    func_149647_a((CreativeTabs)IC2.tabIC2);
    ResourceLocation name = IC2.getIdentifier(BlockName.leaves.name());
    BlocksItems.registerBlock((Block)this, name);
    BlocksItems.registerItem((Item)new ItemIc2Leaves((Block)this), name);
    BlockName.leaves.setInstance((Block)this);
    func_180632_j(this.field_176227_L.func_177621_b()
        .func_177226_a((IProperty)field_176236_b, Boolean.valueOf(true))
        .func_177226_a((IProperty)field_176237_a, Boolean.valueOf(true))
        .func_177226_a((IProperty)typeProperty, LeavesType.rubber));
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(BlockName name) {
    StateMap stateMap = (new StateMap.Builder()).func_178442_a(new IProperty[] { (IProperty)field_176236_b, (IProperty)field_176237_a }).func_178441_a();
    ModelLoader.setCustomStateMapper((Block)this, (IStateMapper)stateMap);
    List<IBlockState> states = new ArrayList<>(typeProperty.func_177700_c().size());
    for (LeavesType type : LeavesType.values)
      states.add(getDropState(getDefaultState().func_177226_a((IProperty)typeProperty, type))); 
    BlockBase.registerItemModels((Block)this, states, (IStateMapper)stateMap);
  }
  
  private static IBlockState getDropState(IBlockState state) {
    return state.func_177226_a((IProperty)field_176236_b, Boolean.valueOf(false))
      .func_177226_a((IProperty)field_176237_a, Boolean.valueOf(false));
  }
  
  protected BlockStateContainer func_180661_e() {
    return new BlockStateContainer((Block)this, new IProperty[] { (IProperty)field_176236_b, (IProperty)field_176237_a, (IProperty)typeProperty });
  }
  
  public IBlockState func_176203_a(int meta) {
    boolean checkDecay = ((meta & 0x8) != 0);
    boolean decayable = ((meta & 0x4) != 0);
    meta &= 0x3;
    IBlockState ret = getDefaultState().func_177226_a((IProperty)field_176236_b, Boolean.valueOf(checkDecay)).func_177226_a((IProperty)field_176237_a, Boolean.valueOf(decayable));
    if (meta < LeavesType.values.length)
      ret = ret.func_177226_a((IProperty)typeProperty, LeavesType.values[meta]); 
    return ret;
  }
  
  public int func_176201_c(IBlockState state) {
    int ret = 0;
    if (((Boolean)state.func_177229_b((IProperty)field_176236_b)).booleanValue())
      ret |= 0x8; 
    if (((Boolean)state.func_177229_b((IProperty)field_176237_a)).booleanValue())
      ret |= 0x4; 
    ret |= ((LeavesType)state.func_177229_b((IProperty)typeProperty)).ordinal();
    return ret;
  }
  
  public boolean func_149662_c(IBlockState state) {
    return Blocks.field_150362_t.func_149662_c(state);
  }
  
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer func_180664_k() {
    return Blocks.field_150362_t.func_180664_k();
  }
  
  public boolean func_176225_a(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    BlockPos nPos = pos.offset(side);
    return ((!func_149662_c(state) || world.getBlockState(nPos) != state) && 
      !world.getBlockState(nPos).doesSideBlockRendering(world, nPos, side.getOpposite()));
  }
  
  public Item func_180660_a(IBlockState state, Random rand, int fortune) {
    return ((LeavesType)state.func_177229_b((IProperty)typeProperty)).getSapling().getItem();
  }
  
  public int func_180651_a(IBlockState state) {
    return ((LeavesType)state.func_177229_b((IProperty)typeProperty)).getSapling().func_77960_j();
  }
  
  protected int func_176232_d(IBlockState state) {
    return ((LeavesType)state.func_177229_b((IProperty)typeProperty)).saplingDropChance;
  }
  
  public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
    IBlockState state = getDropState(world.getBlockState(pos));
    return Arrays.asList(new ItemStack[] { new ItemStack((Block)this, 1, func_176201_c(state)) });
  }
  
  public void func_149666_a(CreativeTabs tab, NonNullList<ItemStack> list) {
    IBlockState state = getDropState(getDefaultState());
    for (LeavesType type : LeavesType.values)
      list.add(new ItemStack((Block)this, 1, func_176201_c(state.func_177226_a((IProperty)typeProperty, type)))); 
  }
  
  public BlockPlanks.EnumType func_176233_b(int meta) {
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
  
  public static final PropertyEnum<LeavesType> typeProperty = PropertyEnum.func_177709_a("type", LeavesType.class);
  
  private static final int checkDecayFlag = 8;
  
  private static final int decayableFlag = 4;
}

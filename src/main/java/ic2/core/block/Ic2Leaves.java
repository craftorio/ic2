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
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap.Builder;
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
   public static final PropertyEnum<Ic2Leaves.LeavesType> typeProperty = PropertyEnum.create("type", Ic2Leaves.LeavesType.class);
   private static final int checkDecayFlag = 8;
   private static final int decayableFlag = 4;

   public Ic2Leaves() {
      this.setUnlocalizedName(BlockName.leaves.name());
      this.setCreativeTab(IC2.tabIC2);
      ResourceLocation name = IC2.getIdentifier(BlockName.leaves.name());
      BlocksItems.registerBlock(this, name);
      BlocksItems.registerItem(new ItemIc2Leaves(this), name);
      BlockName.leaves.setInstance(this);
      this.setDefaultState(
         this.blockState
            .getBaseState()
            .withProperty(CHECK_DECAY, true)
            .withProperty(DECAYABLE, true)
            .withProperty(typeProperty, Ic2Leaves.LeavesType.rubber)
      );
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerModels(BlockName name) {
      IStateMapper mapper = new Builder().ignore(new IProperty[]{CHECK_DECAY, DECAYABLE}).build();
      ModelLoader.setCustomStateMapper(this, mapper);
      List<IBlockState> states = new ArrayList<>(typeProperty.getAllowedValues().size());

      for (Ic2Leaves.LeavesType type : Ic2Leaves.LeavesType.values) {
         states.add(getDropState(this.getDefaultState().withProperty(typeProperty, type)));
      }

      BlockBase.registerItemModels(this, states, mapper);
   }

   private static IBlockState getDropState(IBlockState state) {
      return state.withProperty(CHECK_DECAY, false).withProperty(DECAYABLE, false);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{CHECK_DECAY, DECAYABLE, typeProperty});
   }

   public IBlockState getStateFromMeta(int meta) {
      boolean checkDecay = (meta & 8) != 0;
      boolean decayable = (meta & 4) != 0;
      meta &= 3;
      IBlockState ret = this.getDefaultState().withProperty(CHECK_DECAY, checkDecay).withProperty(DECAYABLE, decayable);
      if (meta < Ic2Leaves.LeavesType.values.length) {
         ret = ret.withProperty(typeProperty, Ic2Leaves.LeavesType.values[meta]);
      }

      return ret;
   }

   public int getMetaFromState(IBlockState state) {
      int ret = 0;
      if ((Boolean)state.getValue(CHECK_DECAY)) {
         ret |= 8;
      }

      if ((Boolean)state.getValue(DECAYABLE)) {
         ret |= 4;
      }

      return ret | ((Ic2Leaves.LeavesType)state.getValue(typeProperty)).ordinal();
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
      return (!this.isOpaqueCube(state) || world.getBlockState(nPos) != state)
         && !world.getBlockState(nPos).doesSideBlockRendering(world, nPos, side.getOpposite());
   }

   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return ((Ic2Leaves.LeavesType)state.getValue(typeProperty)).getSapling().getItem();
   }

   public int damageDropped(IBlockState state) {
      return ((Ic2Leaves.LeavesType)state.getValue(typeProperty)).getSapling().getMetadata();
   }

   protected int getSaplingDropChance(IBlockState state) {
      return ((Ic2Leaves.LeavesType)state.getValue(typeProperty)).saplingDropChance;
   }

   public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
      IBlockState state = getDropState(world.getBlockState(pos));
      return Arrays.asList(new ItemStack(this, 1, this.getMetaFromState(state)));
   }

   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      IBlockState state = getDropState(this.getDefaultState());

      for (Ic2Leaves.LeavesType type : Ic2Leaves.LeavesType.values) {
         list.add(new ItemStack(this, 1, this.getMetaFromState(state.withProperty(typeProperty, type))));
      }
   }

   public EnumType getWoodType(int meta) {
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

      public final int saplingDropChance;
      private static final Ic2Leaves.LeavesType[] values = values();

      LeavesType(int saplingDropChance) {
         this.saplingDropChance = saplingDropChance;
      }

      public String getName() {
         return this.name();
      }

      public ItemStack getSapling() {
         return new ItemStack(BlockName.sapling.getInstance());
      }
   }
}

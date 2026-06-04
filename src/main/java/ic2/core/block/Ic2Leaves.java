// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.util.IStringSerializable;
import net.minecraft.block.BlockPlanks;
import net.minecraft.util.NonNullList;
import java.util.Arrays;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import java.util.Random;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.block.state.IBlockState;
import java.util.ArrayList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.Block;
import ic2.core.item.block.ItemIc2Leaves;
import ic2.core.init.BlocksItems;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import ic2.core.ref.BlockName;
import net.minecraft.block.properties.PropertyEnum;
import ic2.core.ref.IBlockModelProvider;
import net.minecraft.block.BlockLeaves;

public class Ic2Leaves extends BlockLeaves implements IBlockModelProvider
{
    public static final PropertyEnum<LeavesType> typeProperty;
    private static final int checkDecayFlag = 8;
    private static final int decayableFlag = 4;
    
    public Ic2Leaves() {
        this.setUnlocalizedName(BlockName.leaves.name());
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
        final ResourceLocation name = IC2.getIdentifier(BlockName.leaves.name());
        BlocksItems.registerBlock(this, name);
        BlocksItems.registerItem(new ItemIc2Leaves((Block)this), name);
        BlockName.leaves.setInstance(this);
        this.setDefaultState(this.blockState.getBaseState().withProperty((IProperty)Ic2Leaves.CHECK_DECAY, (Comparable)true).withProperty((IProperty)Ic2Leaves.DECAYABLE, (Comparable)true).withProperty((IProperty)Ic2Leaves.typeProperty, (Comparable)LeavesType.rubber));
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final BlockName name) {
        final IStateMapper mapper = (IStateMapper)new StateMap.Builder().ignore(new IProperty[] { (IProperty)Ic2Leaves.CHECK_DECAY, (IProperty)Ic2Leaves.DECAYABLE }).build();
        ModelLoader.setCustomStateMapper((Block)this, mapper);
        final List<IBlockState> states = new ArrayList<IBlockState>(Ic2Leaves.typeProperty.getAllowedValues().size());
        for (final LeavesType type : LeavesType.values) {
            states.add(getDropState(this.getDefaultState().withProperty((IProperty)Ic2Leaves.typeProperty, (Comparable)type)));
        }
        BlockBase.registerItemModels((Block)this, states, mapper);
    }
    
    private static IBlockState getDropState(final IBlockState state) {
        return state.withProperty((IProperty)Ic2Leaves.CHECK_DECAY, (Comparable)false).withProperty((IProperty)Ic2Leaves.DECAYABLE, (Comparable)false);
    }
    
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer((Block)this, new IProperty[] { (IProperty)Ic2Leaves.CHECK_DECAY, (IProperty)Ic2Leaves.DECAYABLE, (IProperty)Ic2Leaves.typeProperty });
    }
    
    public IBlockState getStateFromMeta(int meta) {
        final boolean checkDecay = (meta & 0x8) != 0x0;
        final boolean decayable = (meta & 0x4) != 0x0;
        meta &= 0x3;
        IBlockState ret = this.getDefaultState().withProperty((IProperty)Ic2Leaves.CHECK_DECAY, (Comparable)checkDecay).withProperty((IProperty)Ic2Leaves.DECAYABLE, (Comparable)decayable);
        if (meta < LeavesType.values.length) {
            ret = ret.withProperty((IProperty)Ic2Leaves.typeProperty, (Comparable)LeavesType.values[meta]);
        }
        return ret;
    }
    
    public int getMetaFromState(final IBlockState state) {
        int ret = 0;
        if (state.getValue((IProperty)Ic2Leaves.CHECK_DECAY)) {
            ret |= 0x8;
        }
        if (state.getValue((IProperty)Ic2Leaves.DECAYABLE)) {
            ret |= 0x4;
        }
        ret |= ((LeavesType)state.getValue((IProperty)Ic2Leaves.typeProperty)).ordinal();
        return ret;
    }
    
    public boolean isOpaqueCube(final IBlockState state) {
        return Blocks.LEAVES.isOpaqueCube(state);
    }
    
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return Blocks.LEAVES.getBlockLayer();
    }
    
    public boolean shouldSideBeRendered(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        final BlockPos nPos = pos.offset(side);
        return (!this.isOpaqueCube(state) || world.getBlockState(nPos) != state) && !world.getBlockState(nPos).doesSideBlockRendering(world, nPos, side.getOpposite());
    }
    
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return ((LeavesType)state.getValue((IProperty)Ic2Leaves.typeProperty)).getSapling().getItem();
    }
    
    public int damageDropped(final IBlockState state) {
        return ((LeavesType)state.getValue((IProperty)Ic2Leaves.typeProperty)).getSapling().getMetadata();
    }
    
    protected int getSaplingDropChance(final IBlockState state) {
        return ((LeavesType)state.getValue((IProperty)Ic2Leaves.typeProperty)).saplingDropChance;
    }
    
    public List<ItemStack> onSheared(final ItemStack item, final IBlockAccess world, final BlockPos pos, final int fortune) {
        final IBlockState state = getDropState(world.getBlockState(pos));
        return Arrays.asList(new ItemStack((Block)this, 1, this.getMetaFromState(state)));
    }
    
    public void getSubBlocks(final CreativeTabs tab, final NonNullList<ItemStack> list) {
        final IBlockState state = getDropState(this.getDefaultState());
        for (final LeavesType type : LeavesType.values) {
            list.add((Object)new ItemStack((Block)this, 1, this.getMetaFromState(state.withProperty((IProperty)Ic2Leaves.typeProperty, (Comparable)type))));
        }
    }
    
    public BlockPlanks.EnumType getWoodType(final int meta) {
        return null;
    }
    
    public boolean canBeReplacedByLeaves(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return true;
    }
    
    public boolean isShearable(final ItemStack item, final IBlockAccess world, final BlockPos pos) {
        return true;
    }
    
    public boolean isLeaves(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return true;
    }
    
    public int getFireSpreadSpeed(final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        return 30;
    }
    
    public int getFlammability(final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        return 20;
    }
    
    static {
        typeProperty = PropertyEnum.create("type", (Class)LeavesType.class);
    }
    
    public enum LeavesType implements IStringSerializable
    {
        rubber(35);
        
        public final int saplingDropChance;
        private static final LeavesType[] values;
        
        private LeavesType(final int saplingDropChance) {
            this.saplingDropChance = saplingDropChance;
        }
        
        public String getName() {
            return this.name();
        }
        
        public ItemStack getSapling() {
            return new ItemStack(BlockName.sapling.getInstance());
        }
        
        static {
            values = values();
        }
    }
}

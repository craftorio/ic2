// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine;

import ic2.core.block.state.IIdProvider;
import java.util.Iterator;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import net.minecraft.util.math.AxisAlignedBB;
import ic2.core.block.BlockMultiID;

public class BlockMiningPipe extends BlockMultiID<MiningPipeType>
{
    private static final AxisAlignedBB pipeAabb;
    
    public static BlockMiningPipe create() {
        return BlockMultiID.create(BlockMiningPipe.class, MiningPipeType.class, new Object[0]);
    }
    
    public BlockMiningPipe() {
        super(BlockName.mining_pipe, Material.IRON);
        this.setHardness(6.0f);
        this.setResistance(10.0f);
    }
    
    public boolean canPlaceBlockAt(final World worldIn, final BlockPos pos) {
        return false;
    }
    
    public boolean doesSideBlockRendering(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        final MiningPipeType type = this.getType(state);
        return type == null || type != MiningPipeType.pipe;
    }
    
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final MiningPipeType type = this.getType(state);
        if (type == null) {
            return super.getBoundingBox(state, world, pos);
        }
        return this.getAabb(type);
    }
    
    private AxisAlignedBB getAabb(final MiningPipeType type) {
        switch (type) {
            case pipe: {
                return BlockMiningPipe.pipeAabb;
            }
            default: {
                return BlockMiningPipe.FULL_BLOCK_AABB;
            }
        }
    }
    
    public int getLightOpacity(final IBlockState state) {
        return state.isFullCube() ? 255 : 0;
    }
    
    public boolean isFullCube(final IBlockState state) {
        final MiningPipeType type = this.getType(state);
        if (type == null) {
            return super.isFullCube(state);
        }
        switch (type) {
            case pipe: {
                return false;
            }
            default: {
                return true;
            }
        }
    }
    
    public boolean isNormalCube(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final MiningPipeType type = this.getType(state);
        if (type == null) {
            return true;
        }
        switch (type) {
            case pipe: {
                return false;
            }
            case tip: {
                return true;
            }
            default: {
                return true;
            }
        }
    }
    
    @Override
    public ItemStack getItemStack(final IBlockState state) {
        final MiningPipeType type = this.getType(state);
        if (type == MiningPipeType.tip) {
            return this.getItemStack(MiningPipeType.pipe);
        }
        return super.getItemStack(state);
    }
    
    @Override
    public void getSubBlocks(final CreativeTabs tabs, final NonNullList<ItemStack> itemList) {
        for (final MiningPipeType type : this.typeProperty.getShownValues()) {
            if (type == MiningPipeType.tip) {
                continue;
            }
            itemList.add((Object)this.getItemStack(type));
        }
    }
    
    static {
        pipeAabb = new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 1.0, 0.625);
    }
    
    public enum MiningPipeType implements IIdProvider
    {
        pipe, 
        tip;
        
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

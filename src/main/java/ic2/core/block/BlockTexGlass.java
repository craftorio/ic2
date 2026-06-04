// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.block.state.IIdProvider;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.block.state.IBlockState;
import java.util.Random;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;

public class BlockTexGlass extends BlockMultiID<GlassType>
{
    public static BlockTexGlass create() {
        return BlockMultiID.create(BlockTexGlass.class, GlassType.class, new Object[0]);
    }
    
    private BlockTexGlass() {
        super(BlockName.glass, Material.GLASS);
        this.setHardness(5.0f);
        this.setResistance(180.0f);
        this.setSoundType(SoundType.GLASS);
    }
    
    public int quantityDropped(final Random random) {
        return 0;
    }
    
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }
    
    public boolean isFullBlock(final IBlockState state) {
        return true;
    }
    
    public boolean isFullCube(final IBlockState state) {
        return false;
    }
    
    public boolean isTopSolid(final IBlockState state) {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    public boolean canCreatureSpawn(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EntityLiving.SpawnPlacementType type) {
        return false;
    }
    
    public boolean shouldSideBeRendered(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        return world.getBlockState(pos.offset(side)).getBlock() != this && super.shouldSideBeRendered(state, world, pos, side);
    }
    
    public enum GlassType implements IIdProvider
    {
        reinforced;
        
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

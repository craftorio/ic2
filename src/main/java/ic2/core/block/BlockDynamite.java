// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.Explosion;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import java.util.Iterator;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.BlockTorch;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import com.google.common.base.Function;
import ic2.core.ref.BlockName;
import net.minecraft.block.properties.IProperty;

public class BlockDynamite extends BlockBase
{
    public static final IProperty<Boolean> linked;
    
    public BlockDynamite() {
        super(BlockName.dynamite, MaterialIC2TNT.instance, (java.util.function.Function<Block, Item>)null);
        this.setTickRandomly(true);
        this.setHardness(0.0f);
        this.setSoundType(SoundType.PLANT);
        this.setCreativeTab((CreativeTabs)null);
        this.setDefaultState(this.getDefaultState().withProperty((IProperty)BlockDynamite.linked, (Comparable)false).withProperty((IProperty)BlockTorch.FACING, (Comparable)EnumFacing.UP));
    }
    
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer((Block)this, new IProperty[] { (IProperty)BlockTorch.FACING, BlockDynamite.linked });
    }
    
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return Blocks.TORCH.getDefaultState().withProperty((IProperty)BlockTorch.FACING, state.getValue((IProperty)BlockTorch.FACING)).getBoundingBox(source, pos);
    }
    
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }
    
    public boolean isFullCube(final IBlockState state) {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    public boolean canPlaceBlockAt(final World world, final BlockPos pos) {
        for (final EnumFacing dir : BlockTorch.FACING.getAllowedValues()) {
            if (world.isBlockNormalCube(pos.offset(dir.getOpposite()), false)) {
                return true;
            }
        }
        return false;
    }
    
    public IBlockState getStateForPlacement(final World world, final BlockPos pos, EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        if (facing == EnumFacing.DOWN || !world.isBlockNormalCube(pos.offset(facing.getOpposite()), false)) {
            for (final EnumFacing facing2 : BlockTorch.FACING.getAllowedValues()) {
                if (world.isBlockNormalCube(pos.offset(facing2.getOpposite()), false)) {
                    facing = facing2;
                    break;
                }
            }
        }
        return this.getDefaultState().withProperty((IProperty)BlockTorch.FACING, (Comparable)facing);
    }
    
    public int getMetaFromState(final IBlockState state) {
        return ((EnumFacing)state.getValue((IProperty)BlockTorch.FACING)).ordinal() << 1 | (((boolean)state.getValue((IProperty)BlockDynamite.linked)) ? 1 : 0);
    }
    
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty((IProperty)BlockDynamite.linked, (Comparable)((meta & 0x1) != 0x0)).withProperty((IProperty)BlockTorch.FACING, (Comparable)EnumFacing.VALUES[meta >> 1]);
    }
    
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack) {
        this.checkPlacement(world, pos, state);
    }
    
    public void randomTick(final World world, final BlockPos pos, final IBlockState state, final Random random) {
        this.checkPlacement(world, pos, state);
    }
    
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos) {
        this.checkPlacement(world, pos, state);
    }
    
    public int quantityDropped(final Random random) {
        return 0;
    }
    
    public int damageDropped(final IBlockState state) {
        return 0;
    }
    
    public void onBlockDestroyedByExplosion(final World world, final BlockPos pos, final Explosion explosion) {
        this.explode(world, pos, (explosion != null) ? explosion.getExplosivePlacedBy() : null, true);
    }
    
    public boolean removedByPlayer(final IBlockState state, final World world, final BlockPos pos, final EntityPlayer player, final boolean willHarvest) {
        if (!world.isRemote) {
            this.explode(world, pos, (EntityLivingBase)player, false);
        }
        return false;
    }
    
    private void checkPlacement(final World world, final BlockPos pos, final IBlockState state) {
        if (world.isRemote) {
            return;
        }
        if (world.isBlockPowered(pos)) {
            this.explode(world, pos, null, false);
        }
        else if (!world.isBlockNormalCube(pos.offset(((EnumFacing)state.getValue((IProperty)BlockTorch.FACING)).getOpposite()), false)) {
            world.setBlockToAir(pos);
            this.dropBlockAsItem(world, pos, state, 0);
        }
    }
    
    private void explode(final World world, final BlockPos pos, final EntityLivingBase player, final boolean byExplosion) {
        world.setBlockToAir(pos);
        final EntityDynamite entity = new EntityStickyDynamite(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5f);
        entity.owner = player;
        entity.fuse = (byExplosion ? 5 : 40);
        world.spawnEntity((Entity)entity);
        world.playSound((EntityPlayer)null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }
    
    public RayTraceResult collisionRayTrace(final IBlockState state, final World world, final BlockPos pos, final Vec3d start, final Vec3d end) {
        return Blocks.TORCH.collisionRayTrace(state, world, pos, start, end);
    }
    
    static {
        linked = (IProperty)PropertyBool.create("linked");
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.block.type.ResourceBlock;
import java.util.ArrayList;
import ic2.core.block.state.IIdProvider;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import java.util.List;
import net.minecraft.block.Block;
import ic2.core.util.StackUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.properties.IProperty;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import javax.annotation.Nullable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;

public class BlockFoam extends BlockMultiID<FoamType>
{
    public static BlockFoam create() {
        return BlockMultiID.create(BlockFoam.class, FoamType.class, new Object[0]);
    }
    
    private BlockFoam() {
        super(BlockName.foam, Material.CLOTH);
        this.setTickRandomly(true);
        this.setHardness(0.01f);
        this.setResistance(10.0f);
        this.setSoundType(SoundType.CLOTH);
    }
    
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }
    
    public boolean isNormalCube(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return true;
    }
    
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, final IBlockAccess world, final BlockPos pos) {
        return null;
    }
    
    public boolean isSideSolid(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        return false;
    }
    
    public void randomTick(final World world, final BlockPos pos, final IBlockState state, final Random random) {
        final int tickSpeed = world.getGameRules().getInt("randomTickSpeed");
        if (tickSpeed <= 0) {
            throw new IllegalStateException("Foam was randomly ticked when world " + world + " isn't ticking?");
        }
        final FoamType type = (FoamType)state.getValue((IProperty)this.typeProperty);
        final float chance = getHardenChance(world, pos, state, type) * 4096.0f / tickSpeed;
        if (random.nextFloat() < chance) {
            world.setBlockState(pos, ((FoamType)state.getValue((IProperty)this.typeProperty)).getResult());
        }
    }
    
    public static float getHardenChance(final World world, final BlockPos pos, final IBlockState state, final FoamType type) {
        int light = world.getLightFromNeighbors(pos);
        if (!state.useNeighborBrightness() && state.getBlock().getLightOpacity(state, (IBlockAccess)world, pos) == 0) {
            for (final EnumFacing side : EnumFacing.VALUES) {
                light = Math.max(light, world.getLight(pos.offset(side), false));
            }
        }
        final int avgTime = type.hardenTime * (16 - light);
        return 1.0f / (avgTime * 20);
    }
    
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (StackUtil.consume(player, hand, StackUtil.sameItem((Block)Blocks.SAND), 1)) {
            world.setBlockState(pos, ((FoamType)state.getValue((IProperty)this.typeProperty)).getResult());
            return true;
        }
        return false;
    }
    
    @Override
    public List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
        return ((FoamType)state.getValue((IProperty)this.typeProperty)).getDrops();
    }
    
    public boolean canCreatureSpawn(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EntityLiving.SpawnPlacementType type) {
        return false;
    }
    
    public enum FoamType implements IIdProvider
    {
        normal(300), 
        reinforced(600);
        
        public final int hardenTime;
        
        private FoamType(final int hardenTime) {
            this.hardenTime = hardenTime;
        }
        
        @Override
        public String getName() {
            return this.name();
        }
        
        @Override
        public int getId() {
            return this.ordinal();
        }
        
        public List<ItemStack> getDrops() {
            switch (this) {
                case normal: {
                    return new ArrayList<ItemStack>();
                }
                case reinforced: {
                    final List<ItemStack> ret = new ArrayList<ItemStack>();
                    ret.add(BlockName.scaffold.getItemStack(BlockScaffold.ScaffoldType.iron));
                    return ret;
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
        
        public IBlockState getResult() {
            switch (this) {
                case normal: {
                    return BlockName.wall.getBlockState(BlockWall.defaultColor);
                }
                case reinforced: {
                    return BlockName.resource.getBlockState(ResourceBlock.reinforced_stone);
                }
                default: {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }
}

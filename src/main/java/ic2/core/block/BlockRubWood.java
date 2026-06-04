// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.util.IStringSerializable;
import net.minecraft.block.material.EnumPushReaction;
import java.util.Random;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.item.Item;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import net.minecraft.block.properties.PropertyEnum;

public class BlockRubWood extends BlockBase
{
    public static final PropertyEnum<RubberWoodState> stateProperty;
    
    public BlockRubWood() {
        super(BlockName.rubber_wood, Material.WOOD);
        this.setTickRandomly(true);
        this.setHardness(1.0f);
        this.setSoundType(SoundType.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty((IProperty)BlockRubWood.stateProperty, (Comparable)RubberWoodState.plain_y));
    }
    
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer((Block)this, new IProperty[] { (IProperty)BlockRubWood.stateProperty });
    }
    
    public IBlockState getStateFromMeta(final int meta) {
        if (meta >= 0 && meta < RubberWoodState.values.length) {
            return this.getDefaultState().withProperty((IProperty)BlockRubWood.stateProperty, (Comparable)RubberWoodState.values[meta]);
        }
        return this.getDefaultState();
    }
    
    public int getMetaFromState(final IBlockState state) {
        return ((RubberWoodState)state.getValue((IProperty)BlockRubWood.stateProperty)).ordinal();
    }
    
    public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer) {
        final IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
        return state.withProperty((IProperty)BlockRubWood.stateProperty, (Comparable)getPlainAxisState(facing.getAxis()));
    }
    
    private static RubberWoodState getPlainAxisState(final EnumFacing.Axis axis) {
        switch (axis) {
            case X: {
                return RubberWoodState.plain_x;
            }
            case Y: {
                return RubberWoodState.plain_y;
            }
            case Z: {
                return RubberWoodState.plain_z;
            }
            default: {
                throw new IllegalArgumentException("invalid axis: " + axis);
            }
        }
    }
    
    public void dropBlockAsItemWithChance(final World world, final BlockPos pos, final IBlockState state, final float chance, final int fortune) {
        if (world.isRemote) {
            return;
        }
        for (int count = this.quantityDropped(world.rand), j1 = 0; j1 < count; ++j1) {
            if (world.rand.nextFloat() <= chance) {
                final Item item = this.getItemDropped(state, world.rand, fortune);
                if (item != null) {
                    spawnAsEntity(world, pos, new ItemStack(item, 1, 0));
                }
                if (!((RubberWoodState)state.getValue((IProperty)BlockRubWood.stateProperty)).isPlain() && world.rand.nextInt(6) == 0) {
                    spawnAsEntity(world, pos, ItemName.misc_resource.getItemStack(MiscResourceType.resin));
                }
            }
        }
    }
    
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        final int range = 4;
        final BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
        for (int y = -range; y <= range; ++y) {
            for (int z = -range; z <= range; ++z) {
                for (int x = -range; x <= range; ++x) {
                    cPos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    final IBlockState cState = world.getBlockState((BlockPos)cPos);
                    final Block cBlock = cState.getBlock();
                    if (cBlock.isLeaves(cState, (IBlockAccess)world, (BlockPos)cPos)) {
                        cBlock.beginLeavesDecay(cState, world, new BlockPos((Vec3i)cPos));
                    }
                }
            }
        }
    }
    
    public void randomTick(final World world, final BlockPos pos, final IBlockState state, final Random random) {
        if (random.nextInt(7) == 0) {
            final RubberWoodState rwState = (RubberWoodState)state.getValue((IProperty)BlockRubWood.stateProperty);
            if (!rwState.canRegenerate()) {
                return;
            }
            world.setBlockState(pos, state.withProperty((IProperty)BlockRubWood.stateProperty, (Comparable)rwState.getWet()));
        }
    }
    
    public EnumPushReaction getMobilityFlag(final IBlockState state) {
        final RubberWoodState rstate = (RubberWoodState)state.getValue((IProperty)BlockRubWood.stateProperty);
        if (rstate == RubberWoodState.plain_x || rstate == RubberWoodState.plain_y || rstate == RubberWoodState.plain_z) {
            return EnumPushReaction.NORMAL;
        }
        return EnumPushReaction.BLOCK;
    }
    
    public boolean canSustainLeaves(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return true;
    }
    
    public boolean isWood(final IBlockAccess world, final BlockPos pos) {
        return true;
    }
    
    public int getFireSpreadSpeed(final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        return 4;
    }
    
    public int getFlammability(final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        return 20;
    }
    
    static {
        stateProperty = PropertyEnum.create("state", (Class)RubberWoodState.class);
    }
    
    public enum RubberWoodState implements IStringSerializable
    {
        plain_y(EnumFacing.Axis.Y, (EnumFacing)null, false), 
        plain_x(EnumFacing.Axis.X, (EnumFacing)null, false), 
        plain_z(EnumFacing.Axis.Z, (EnumFacing)null, false), 
        dry_north(EnumFacing.Axis.Y, EnumFacing.NORTH, false), 
        dry_south(EnumFacing.Axis.Y, EnumFacing.SOUTH, false), 
        dry_west(EnumFacing.Axis.Y, EnumFacing.WEST, false), 
        dry_east(EnumFacing.Axis.Y, EnumFacing.EAST, false), 
        wet_north(EnumFacing.Axis.Y, EnumFacing.NORTH, true), 
        wet_south(EnumFacing.Axis.Y, EnumFacing.SOUTH, true), 
        wet_west(EnumFacing.Axis.Y, EnumFacing.WEST, true), 
        wet_east(EnumFacing.Axis.Y, EnumFacing.EAST, true);
        
        public final EnumFacing.Axis axis;
        public final EnumFacing facing;
        public final boolean wet;
        private static final RubberWoodState[] values;
        
        private RubberWoodState(final EnumFacing.Axis axis, final EnumFacing facing, final boolean wet) {
            this.axis = axis;
            this.facing = facing;
            this.wet = wet;
        }
        
        public String getName() {
            return this.name();
        }
        
        public boolean isPlain() {
            return this.facing == null;
        }
        
        public boolean canRegenerate() {
            return !this.isPlain() && !this.wet;
        }
        
        public RubberWoodState getWet() {
            if (this.isPlain()) {
                return null;
            }
            if (this.wet) {
                return this;
            }
            return RubberWoodState.values[this.ordinal() + 4];
        }
        
        public RubberWoodState getDry() {
            if (this.isPlain() || !this.wet) {
                return this;
            }
            return RubberWoodState.values[this.ordinal() - 4];
        }
        
        public static RubberWoodState getWet(final EnumFacing facing) {
            switch (facing) {
                case NORTH: {
                    return RubberWoodState.wet_north;
                }
                case SOUTH: {
                    return RubberWoodState.wet_south;
                }
                case WEST: {
                    return RubberWoodState.wet_west;
                }
                case EAST: {
                    return RubberWoodState.wet_east;
                }
                default: {
                    throw new IllegalArgumentException("incompatible facing: " + facing);
                }
            }
        }
        
        static {
            values = values();
        }
    }
}

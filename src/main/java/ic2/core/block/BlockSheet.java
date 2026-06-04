// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.profile.NotClassic;
import ic2.core.block.type.IExtBlockType;
import ic2.core.block.state.IIdProvider;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import java.util.ArrayList;
import net.minecraft.util.math.Vec3i;
import ic2.core.util.Ic2BlockPos;
import ic2.core.IC2;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import ic2.core.item.block.ItemBlockSheet;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public class BlockSheet extends BlockMultiID<SheetType>
{
    private static final AxisAlignedBB aabb;
    private static final EnumFacing[] positiveHorizontalFacings;
    
    public static BlockSheet create() {
        return BlockMultiID.create(BlockSheet.class, SheetType.class, new Object[0]);
    }
    
    public BlockSheet() {
        super(BlockName.sheet, Material.CIRCUITS, ItemBlockSheet.class);
    }
    
    public boolean isFullCube(final IBlockState state) {
        return false;
    }
    
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }
    
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return BlockSheet.aabb;
    }
    
    public void addCollisionBoxToList(final IBlockState state, final World worldIn, final BlockPos pos, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, @Nullable final Entity entityIn, final boolean isActualState) {
        if (this.getType(state) == SheetType.wool && entityIn instanceof EntityPlayer && (entityIn.isSneaking() || entityIn.posY < pos.getY() + BlockSheet.aabb.maxY - entityIn.stepHeight)) {
            return;
        }
        super.addCollisionBoxToList(state, worldIn, pos, entityBox, (List)collidingBoxes, entityIn, isActualState);
    }
    
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final AxisAlignedBB aabb = super.getCollisionBoundingBox(state, world, pos);
        switch (this.getType(state)) {
            case resin: {
                return null;
            }
            default: {
                return aabb;
            }
        }
    }
    
    public boolean canReplace(final World world, final BlockPos pos, final EnumFacing side, final ItemStack stack) {
        return this.isValidPosition(world, pos, this.getStateFromMeta(stack.getItemDamage()));
    }
    
    private boolean isValidPosition(final World world, final BlockPos pos, IBlockState state) {
        switch (this.getType(state)) {
            case resin: {
                return this.isNormalCubeBelow(world, pos);
            }
            case rubber: {
                for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
                    state = world.getBlockState(pos.offset(facing));
                    if (state == BlockName.sheet.getBlockState(SheetType.rubber) || state.getBlock().isNormalCube(state, (IBlockAccess)world, pos)) {
                        return true;
                    }
                }
                return this.isNormalCubeBelow(world, pos);
            }
            case wool: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private boolean isNormalCubeBelow(final World world, BlockPos pos) {
        pos = pos.down();
        final IBlockState state = world.getBlockState(pos);
        return state.getBlock().isNormalCube(state, (IBlockAccess)world, pos);
    }
    
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos) {
        if (!this.isValidPosition(world, pos, state)) {
            world.setBlockToAir(pos);
            this.dropBlockAsItem(world, pos, state, 0);
        }
    }
    
    public void onEntityCollidedWithBlock(final World world, final BlockPos pos, final IBlockState state, final Entity entity) {
        switch (this.getType(state)) {
            case resin: {
                entity.fallDistance *= 0.75;
                entity.motionX *= 0.6;
                entity.motionY *= 0.85;
                entity.motionZ *= 0.6;
                break;
            }
            case rubber: {
                if (world.isBlockNormalCube(pos.down(), false)) {
                    return;
                }
                if (entity instanceof EntityLivingBase && !canSupportWeight(world, pos)) {
                    world.setBlockToAir(pos);
                    return;
                }
                if (entity.motionY > -0.4) {
                    break;
                }
                entity.fallDistance = 0.0f;
                entity.motionX *= 1.1;
                entity.motionZ *= 1.1;
                if (!(entity instanceof EntityLivingBase)) {
                    entity.motionY *= -0.8;
                    break;
                }
                if (entity instanceof EntityPlayer && IC2.keyboard.isJumpKeyDown((EntityPlayer)entity)) {
                    entity.motionY *= -1.3;
                    break;
                }
                if (entity instanceof EntityPlayer && ((EntityPlayer)entity).isSneaking()) {
                    entity.motionY *= -0.1;
                    break;
                }
                entity.motionY *= -0.8;
                break;
            }
            case wool: {
                entity.fallDistance *= (float)0.95;
                break;
            }
        }
    }
    
    private static boolean canSupportWeight(final World world, final BlockPos pos) {
        final int maxRange = 16;
        final Ic2BlockPos cPos = new Ic2BlockPos();
        for (final EnumFacing axis : BlockSheet.positiveHorizontalFacings) {
            for (int dir = -1; dir <= 1; dir += 2) {
                cPos.set((Vec3i)pos);
                boolean supported = false;
                for (int i = 0; i < 16; ++i) {
                    cPos.move(axis, dir);
                    final IBlockState state = cPos.getBlockState((IBlockAccess)world);
                    if (state.getBlock().isNormalCube(state, (IBlockAccess)world, (BlockPos)cPos)) {
                        supported = true;
                        break;
                    }
                    if (state != BlockName.sheet.getBlockState(SheetType.rubber)) {
                        break;
                    }
                    cPos.moveDown();
                    final IBlockState baseState = cPos.getBlockState((IBlockAccess)world);
                    if (baseState.getBlock().isNormalCube(baseState, (IBlockAccess)world, (BlockPos)cPos)) {
                        supported = true;
                        break;
                    }
                    cPos.moveUp();
                }
                if (!supported) {
                    break;
                }
                if (dir == 1) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
        switch (this.getType(state)) {
            case resin: {
                if (IC2.random.nextInt(5) != 0) {
                    final List<ItemStack> ret = new ArrayList<ItemStack>();
                    ret.add(ItemName.misc_resource.getItemStack(MiscResourceType.resin));
                    return ret;
                }
                return new ArrayList<ItemStack>();
            }
            default: {
                return super.getDrops(world, pos, state, fortune);
            }
        }
    }
    
    static {
        aabb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0);
        positiveHorizontalFacings = new EnumFacing[] { EnumFacing.EAST, EnumFacing.SOUTH };
    }
    
    public enum SheetType implements IIdProvider, IExtBlockType
    {
        resin(1.6f, 0.5f), 
        rubber(0.8f, 2.0f), 
        @NotClassic
        wool(0.8f, 0.8f);
        
        public static SheetType[] values;
        private final float hardness;
        private final float explosionResistance;
        
        private SheetType(final float hardness, final float explosionResistance) {
            this.hardness = hardness;
            this.explosionResistance = explosionResistance;
        }
        
        @Override
        public String getName() {
            return this.name();
        }
        
        @Override
        public int getId() {
            return this.ordinal();
        }
        
        @Override
        public float getHardness() {
            return this.hardness;
        }
        
        @Override
        public float getExplosionResistance() {
            return this.explosionResistance;
        }
        
        static {
            SheetType.values = values();
        }
    }
}

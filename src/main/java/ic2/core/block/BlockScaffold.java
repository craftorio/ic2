// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.block.SoundType;
import ic2.core.block.type.IBlockSound;
import ic2.core.block.type.IExtBlockType;
import ic2.core.block.state.IIdProvider;
import ic2.api.recipe.Recipes;
import java.util.Set;
import java.util.Queue;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.init.Blocks;
import java.util.Random;
import ic2.core.IC2;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumHand;
import net.minecraft.init.Items;
import net.minecraft.block.properties.IProperty;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import ic2.api.recipe.IRecipeInput;

public class BlockScaffold extends BlockMultiID<ScaffoldType>
{
    private static final IRecipeInput stickInput;
    private static final EnumFacing[] supportedFacings;
    private static final double border = 0.03125;
    private static final AxisAlignedBB aabb;
    
    public static BlockScaffold create() {
        return BlockMultiID.create(BlockScaffold.class, ScaffoldType.class, new Object[0]);
    }
    
    private BlockScaffold() {
        super(BlockName.scaffold, Material.WOOD);
        this.setTickRandomly(true);
    }
    
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    public Material getMaterial(final IBlockState state) {
        final ScaffoldType type = this.getType(state);
        if (type == null) {
            return super.getMaterial(state);
        }
        switch (type) {
            case wood:
            case reinforced_wood: {
                return Material.WOOD;
            }
            case iron:
            case reinforced_iron: {
                return Material.IRON;
            }
            default: {
                throw new IllegalStateException("Invalid scaffold type: " + type);
            }
        }
    }
    
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }
    
    public boolean isNormalCube(final IBlockState state) {
        return false;
    }
    
    public boolean isLadder(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EntityLivingBase entity) {
        return true;
    }
    
    public void onEntityCollidedWithBlock(final World world, final BlockPos pos, final IBlockState state, final Entity rawEntity) {
        if (rawEntity instanceof EntityLivingBase) {
            final EntityLivingBase entity = (EntityLivingBase)rawEntity;
            entity.fallDistance = 0.0f;
            final double limit = 0.15;
            entity.motionX = Util.limit(entity.motionX, -limit, limit);
            entity.motionZ = Util.limit(entity.motionZ, -limit, limit);
            if (entity.isSneaking() && entity instanceof EntityPlayer) {
                if (entity.isInWater()) {
                    entity.motionY = 0.02;
                }
                else {
                    entity.motionY = 0.08;
                }
            }
            else if (entity.collidedHorizontally) {
                entity.motionY = 0.2;
            }
            else {
                entity.motionY = Math.max(entity.motionY, -0.07);
            }
        }
    }
    
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return BlockScaffold.aabb;
    }
    
    public AxisAlignedBB getSelectedBoundingBox(final IBlockState state, final World worldIn, final BlockPos pos) {
        return BlockScaffold.FULL_BLOCK_AABB.offset(pos);
    }
    
    public boolean isSideSolid(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        return side.getAxis() == EnumFacing.Axis.Y;
    }
    
    @Override
    public List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
        if (state.getBlock() != this) {
            return Collections.emptyList();
        }
        final List<ItemStack> ret = new ArrayList<ItemStack>();
        final ScaffoldType type = (ScaffoldType)state.getValue((IProperty)this.typeProperty);
        switch (type) {
            case wood:
            case iron: {
                ret.add(this.getItemStack(type));
                break;
            }
            case reinforced_wood: {
                ret.add(this.getItemStack(ScaffoldType.wood));
                ret.add(new ItemStack(Items.STICK, 2));
                break;
            }
            case reinforced_iron: {
                ret.add(this.getItemStack(ScaffoldType.iron));
                ret.add(BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron));
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        return ret;
    }
    
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        final ItemStack stack = player.getHeldItem(hand);
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        ScaffoldType type = this.getType(state);
        if (type == null) {
            return false;
        }
        final int stickCount = 2;
        final int fenceCount = 1;
        switch (type) {
            case wood: {
                if (!BlockScaffold.stickInput.matches(stack) || StackUtil.getSize(stack) < 2) {
                    return false;
                }
                break;
            }
            case iron: {
                if (!StackUtil.checkItemEquality(stack, BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron)) || StackUtil.getSize(stack) < 1) {
                    return false;
                }
                break;
            }
            case reinforced_wood:
            case reinforced_iron: {
                return false;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        if (!this.isPillar(world, pos)) {
            return false;
        }
        switch (type) {
            case wood: {
                StackUtil.consumeOrError(player, hand, StackUtil.recipeInput(BlockScaffold.stickInput), 2);
                type = ScaffoldType.reinforced_wood;
                break;
            }
            case iron: {
                StackUtil.consumeOrError(player, hand, StackUtil.sameStack(BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron)), 1);
                type = ScaffoldType.reinforced_iron;
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        world.setBlockState(pos, state.withProperty((IProperty)this.typeProperty, (Comparable)type));
        return true;
    }
    
    public void onBlockClicked(final World world, BlockPos pos, final EntityPlayer player) {
        final EnumHand hand = EnumHand.MAIN_HAND;
        final ItemStack stack = player.getHeldItem(hand);
        if (StackUtil.isEmpty(stack)) {
            return;
        }
        if (StackUtil.checkItemEquality(stack, Item.getItemFromBlock((Block)this))) {
            while (world.getBlockState(pos).getBlock() == this) {
                pos = pos.up();
            }
            if (this.canPlaceBlockAt(world, pos) && pos.getY() < IC2.getWorldHeight(world)) {
                final boolean isCreative = player.capabilities.isCreativeMode;
                final ItemStack prev = isCreative ? StackUtil.copy(stack) : null;
                stack.onItemUse(player, world, pos.down(), hand, EnumFacing.UP, 0.5f, 1.0f, 0.5f);
                if (!isCreative) {
                    StackUtil.clearEmpty(player, hand);
                }
                else {
                    StackUtil.set(player, hand, prev);
                }
            }
        }
    }
    
    public boolean canPlaceBlockAt(final World world, final BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && this.hasSupport((IBlockAccess)world, pos, ScaffoldType.wood);
    }
    
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos) {
        this.checkSupport(world, pos);
    }
    
    public void randomTick(final World world, final BlockPos pos, final IBlockState state, final Random random) {
        if (random.nextInt(8) == 0) {
            this.checkSupport(world, pos);
        }
    }
    
    private boolean isPillar(final World world, BlockPos pos) {
        while (world.getBlockState(pos).getBlock() == this) {
            pos = pos.down();
        }
        return world.isBlockNormalCube(pos, false);
    }
    
    public int getFireSpreadSpeed(final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        final ScaffoldType type = this.getType(world, pos);
        if (type == null) {
            return 0;
        }
        switch (type) {
            case wood:
            case reinforced_wood: {
                return 8;
            }
            case iron:
            case reinforced_iron: {
                return 0;
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }
    
    public int getFlammability(final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        final ScaffoldType type = this.getType(world, pos);
        if (type == null) {
            return 0;
        }
        switch (type) {
            case wood:
            case reinforced_wood: {
                return 20;
            }
            case iron:
            case reinforced_iron: {
                return 0;
            }
            default: {
                throw new IllegalStateException();
            }
        }
    }
    
    private boolean hasSupport(final IBlockAccess world, final BlockPos start, final ScaffoldType type) {
        return this.calculateSupport(world, start, type).get(start).strength >= 0;
    }
    
    private void checkSupport(final World world, final BlockPos start) {
        final IBlockState state = world.getBlockState(start);
        if (state.getBlock() != this) {
            return;
        }
        final Map<BlockPos, Support> results = this.calculateSupport((IBlockAccess)world, start, (ScaffoldType)state.getValue((IProperty)this.typeProperty));
        boolean droppedAny = false;
        for (final Support support : results.values()) {
            if (support.strength >= 0) {
                continue;
            }
            world.setBlockState(support.pos, Blocks.AIR.getDefaultState(), 2);
            this.dropBlockAsItem(world, support.pos, this.getDefaultState().withProperty((IProperty)this.typeProperty, (Comparable)support.type), 0);
            droppedAny = true;
        }
        if (droppedAny) {
            for (final Support support : results.values()) {
                if (support.strength < 0) {
                    world.notifyNeighborsRespectDebug(support.pos, (Block)this, true);
                }
            }
        }
    }
    
    private Map<BlockPos, Support> calculateSupport(final IBlockAccess world, final BlockPos start, ScaffoldType type) {
        final Map<BlockPos, Support> results = new HashMap<BlockPos, Support>();
        final Queue<Support> queue = new ArrayDeque<Support>();
        final Set<BlockPos> groundSupports = new HashSet<BlockPos>();
        Support support = new Support(start, type, -1);
        results.put(start, support);
        queue.add(support);
        while ((support = queue.poll()) != null) {
            for (final EnumFacing dir : EnumFacing.VALUES) {
                final BlockPos pos = support.pos.offset(dir);
                if (!results.containsKey(pos)) {
                    final IBlockState state = world.getBlockState(pos);
                    final Block block = state.getBlock();
                    if (block == this) {
                        type = (ScaffoldType)state.getValue((IProperty)this.typeProperty);
                        final Support cSupport = new Support(pos, type, -1);
                        results.put(pos, cSupport);
                        queue.add(cSupport);
                    }
                    else if (block.isNormalCube(state, world, pos)) {
                        groundSupports.add(pos);
                    }
                }
            }
        }
        for (final BlockPos groundPos : groundSupports) {
            BlockPos pos2 = groundPos.up();
            int propagatedStrength = 0;
            while (true) {
                support = results.get(pos2);
                if (support == null) {
                    break;
                }
                int strength;
                if (support.type.strength >= propagatedStrength) {
                    strength = support.type.strength;
                    propagatedStrength = strength - 1;
                }
                else {
                    strength = propagatedStrength;
                    --propagatedStrength;
                }
                if (support.strength < strength) {
                    support.strength = strength;
                    for (final EnumFacing dir2 : EnumFacing.HORIZONTALS) {
                        final BlockPos nPos = pos2.offset(dir2);
                        final Support nSupport = results.get(nPos);
                        if (nSupport != null) {
                            if (nSupport.strength < strength) {
                                nSupport.strength = strength - 1;
                                queue.add(nSupport);
                            }
                        }
                    }
                }
                pos2 = pos2.up();
            }
        }
        while ((support = queue.poll()) != null) {
            for (final EnumFacing dir : BlockScaffold.supportedFacings) {
                final BlockPos pos = support.pos.offset(dir);
                final Support nSupport2 = results.get(pos);
                if (nSupport2 != null) {
                    if (nSupport2.strength < support.strength) {
                        nSupport2.strength = support.strength - 1;
                        if (nSupport2.strength > 0) {
                            queue.add(nSupport2);
                        }
                    }
                }
            }
        }
        return results;
    }
    
    static {
        stickInput = Recipes.inputFactory.forOreDict("stickWood");
        supportedFacings = new EnumFacing[] { EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
        aabb = new AxisAlignedBB(0.03125, 0.0, 0.03125, 0.96875, 1.0, 0.96875);
    }
    
    private static class Support
    {
        final BlockPos pos;
        final ScaffoldType type;
        int strength;
        
        Support(final BlockPos pos, final ScaffoldType type, final int strength) {
            this.pos = pos;
            this.type = type;
            this.strength = strength;
        }
    }
    
    public enum ScaffoldType implements IIdProvider, IExtBlockType, IBlockSound
    {
        wood(2, 0.5f, 0.12f, SoundType.WOOD), 
        reinforced_wood(5, 0.6f, 0.24f, SoundType.WOOD), 
        iron(5, 0.8f, 6.0f, SoundType.METAL), 
        reinforced_iron(12, 1.0f, 8.0f, SoundType.METAL);
        
        public final int strength;
        private final float hardness;
        private final float explosionResistance;
        private final SoundType sound;
        
        private ScaffoldType(final int strength, final float hardness, final float explosionResistance, final SoundType sound) {
            if (strength < 1) {
                throw new IllegalArgumentException();
            }
            this.strength = strength;
            this.hardness = hardness;
            this.explosionResistance = explosionResistance;
            this.sound = sound;
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
        
        @Override
        public SoundType getSound() {
            return this.sound;
        }
    }
}

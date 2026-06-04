// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.block.type.IExtBlockType;
import ic2.core.block.state.IIdProvider;
import java.util.IdentityHashMap;
import net.minecraft.block.properties.PropertyBool;
import java.util.EnumMap;
import java.util.Collections;
import net.minecraft.util.math.Vec3i;
import ic2.core.util.Ic2BlockPos;
import ic2.api.item.ItemWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockFence;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.IC2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import ic2.core.util.Util;
import net.minecraft.init.Items;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import java.util.Iterator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.EnumFacing;
import java.util.Map;

public class BlockIC2Fence extends BlockMultiID<IC2FenceType>
{
    public static final Map<EnumFacing, IProperty<Boolean>> connectProperties;
    private static final double halfThickness = 0.125;
    private static final double height = 1.5;
    private static final Map<IProperty<Boolean>, AxisAlignedBB> aabbs;
    
    public static BlockIC2Fence create() {
        return BlockMultiID.create(BlockIC2Fence.class, IC2FenceType.class, new Object[0]);
    }
    
    private BlockIC2Fence() {
        super(BlockName.fence, Material.IRON);
        IBlockState defaultState = this.blockState.getBaseState().withProperty((IProperty)this.typeProperty, (Comparable)this.typeProperty.getDefault());
        for (final IProperty<Boolean> property : BlockIC2Fence.connectProperties.values()) {
            defaultState = defaultState.withProperty((IProperty)property, (Comparable)false);
        }
        this.setDefaultState(defaultState);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final BlockName name) {
        final Item item = Item.getItemFromBlock((Block)this);
        if (item == null || item == Items.AIR) {
            return;
        }
        final ResourceLocation loc = Util.getName(item);
        if (loc == null) {
            return;
        }
        for (final IBlockState state : this.getTypeStates()) {
            ModelLoader.setCustomModelResourceLocation(item, this.getMetaFromState(state), new ModelResourceLocation(loc.toString() + "/" + ((IC2FenceType)state.getValue((IProperty)this.typeProperty)).getName(), (String)null));
        }
    }
    
    public boolean isFullCube(final IBlockState state) {
        return false;
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        final List<IProperty<?>> properties = new ArrayList<IProperty<?>>();
        properties.add((IProperty<?>)this.getTypeProperty());
        properties.addAll(BlockIC2Fence.connectProperties.values());
        return new BlockStateContainer((Block)this, (IProperty[])properties.toArray(new IProperty[0]));
    }
    
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        boolean isPole = true;
        boolean magnetizerConnected = false;
        IBlockState ret = state;
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final IBlockState neighborState = world.getBlockState(pos.offset(facing));
            if (isFence(neighborState)) {
                isPole = false;
                if (magnetizerConnected) {
                    break;
                }
                ret = ret.withProperty((IProperty)BlockIC2Fence.connectProperties.get(facing), (Comparable)true);
            }
            else if (isPole && getMagnetizer(world, pos.offset(facing), facing, world.getBlockState(pos.offset(facing)), false) != null) {
                magnetizerConnected = true;
                ret = ret.withProperty((IProperty)BlockIC2Fence.connectProperties.get(facing), (Comparable)true);
            }
        }
        if (!isPole && magnetizerConnected) {
            ret = state;
            for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
                final IBlockState neighborState = world.getBlockState(pos.offset(facing));
                if (isFence(neighborState)) {
                    ret = ret.withProperty((IProperty)BlockIC2Fence.connectProperties.get(facing), (Comparable)true);
                }
            }
        }
        return ret;
    }
    
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }
    
    public boolean isNormalCube(final IBlockState state) {
        return false;
    }
    
    public boolean isSideSolid(final IBlockState state, final IBlockAccess world, final BlockPos blockPos, final EnumFacing side) {
        return side.getAxis() == EnumFacing.Axis.Y;
    }
    
    public boolean canPlaceTorchOnTop(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return true;
    }
    
    public BlockFaceShape getBlockFaceShape(final IBlockAccess world, final IBlockState state, final BlockPos pos, final EnumFacing face) {
        return (face.getAxis() != EnumFacing.Axis.Y) ? BlockFaceShape.MIDDLE_POLE : BlockFaceShape.CENTER;
    }
    
    public void onEntityCollidedWithBlock(final World world, final BlockPos pos, final IBlockState state, final Entity rawEntity) {
        if (!(rawEntity instanceof EntityPlayer)) {
            return;
        }
        final boolean powered = this.isPowered(world, pos, (IC2FenceType)state.getValue((IProperty)this.typeProperty));
        final EntityPlayer player = (EntityPlayer)rawEntity;
        final boolean metalShoes = hasMetalShoes(player);
        final boolean descending = player.isSneaking();
        final boolean slow = player.motionY >= -0.25 || player.motionY < 1.6;
        if (slow) {
            player.fallDistance = 0.0f;
        }
        if (!powered) {
            if (descending && !slow && metalShoes) {
                final EntityPlayer entityPlayer = player;
                entityPlayer.motionY *= 0.9;
            }
        }
        else if (descending) {
            if (!slow) {
                final EntityPlayer entityPlayer2 = player;
                entityPlayer2.motionY *= 0.8;
            }
        }
        else {
            final EntityPlayer entityPlayer3 = player;
            entityPlayer3.motionY += 0.075;
            if (player.motionY > 0.0) {
                final EntityPlayer entityPlayer4 = player;
                entityPlayer4.motionY *= 1.03;
            }
            final double maxSpeed = IC2.keyboard.isAltKeyDown(player) ? 0.1 : (metalShoes ? 1.5 : 0.5);
            player.motionY = Math.min(player.motionY, maxSpeed);
        }
        if (!world.isRemote) {
            final List<TileEntityMagnetizer> magnetizers = this.getMagnetizers((IBlockAccess)world, pos, false);
            for (final TileEntityMagnetizer magnetizer : magnetizers) {
                IC2.network.get(true).updateTileEntityField(magnetizer, "energy");
            }
        }
    }
    
    public void addCollisionBoxToList(IBlockState state, final World world, final BlockPos pos, final AxisAlignedBB mask, final List<AxisAlignedBB> result, final Entity collidingEntity, final boolean isActualState) {
        if (!isActualState) {
            state = this.getActualState(state, (IBlockAccess)world, pos);
        }
        addCollisionBoxToList(pos, mask, (List)result, (AxisAlignedBB)BlockIC2Fence.aabbs.get(null));
        for (final IProperty<Boolean> property : BlockIC2Fence.connectProperties.values()) {
            if (state.getValue((IProperty)property)) {
                addCollisionBoxToList(pos, mask, (List)result, (AxisAlignedBB)BlockIC2Fence.aabbs.get(property));
            }
        }
    }
    
    public AxisAlignedBB getBoundingBox(IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final AxisAlignedBB ret = BlockIC2Fence.aabbs.get(null);
        double xS = ret.minX;
        final double yS = 0.0;
        double zS = ret.minZ;
        double xE = ret.maxX;
        final double yE = 1.0;
        double zE = ret.maxZ;
        state = this.getActualState(state, world, pos);
        for (final IProperty<Boolean> property : BlockIC2Fence.connectProperties.values()) {
            if (state.getValue((IProperty)property)) {
                final AxisAlignedBB aabb = BlockIC2Fence.aabbs.get(property);
                xS = Math.min(xS, aabb.minX);
                zS = Math.min(zS, aabb.minZ);
                xE = Math.max(xE, aabb.maxX);
                zE = Math.max(zE, aabb.maxZ);
            }
        }
        return new AxisAlignedBB(xS, 0.0, zS, xE, 1.0, zE);
    }
    
    private static boolean isFence(final IBlockState state) {
        return state.getBlock() instanceof BlockIC2Fence || state.getBlock() instanceof BlockFence;
    }
    
    private static TileEntityMagnetizer getMagnetizer(final IBlockAccess world, final BlockPos pos, final EnumFacing side, final IBlockState state, final boolean checkPower) {
        if (state.getBlock() != BlockName.te.getInstance()) {
            return null;
        }
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityMagnetizer) {
            final TileEntityMagnetizer ret = (TileEntityMagnetizer)te;
            if (side != null && !side.getOpposite().equals((Object)ret.getFacing())) {
                return null;
            }
            if (!checkPower || ret.canBoost()) {
                return ret;
            }
        }
        return null;
    }
    
    public static boolean hasMetalShoes(final EntityPlayer player) {
        final ItemStack shoes = (ItemStack)player.inventory.armorInventory.get(0);
        if (shoes != null) {
            final Item item = shoes.getItem();
            if (item == Items.IRON_BOOTS || item == Items.GOLDEN_BOOTS || item == Items.CHAINMAIL_BOOTS || ItemWrapper.isMetalArmor(shoes, player)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isPowered(final World world, final BlockPos start, final IC2FenceType type) {
        if (!type.canBoost) {
            return false;
        }
        final List<TileEntityMagnetizer> magnetizers = this.getMagnetizers((IBlockAccess)world, start, true);
        if (magnetizers.isEmpty()) {
            return false;
        }
        final double multiplier = 1.0 / magnetizers.size();
        for (final TileEntityMagnetizer magnetizer : magnetizers) {
            magnetizer.boost(multiplier);
        }
        return true;
    }
    
    private List<TileEntityMagnetizer> getMagnetizers(final IBlockAccess world, final BlockPos start, final boolean checkPower) {
        final int maxRange = 20;
        final List<TileEntityMagnetizer> ret = new ArrayList<TileEntityMagnetizer>();
        final Ic2BlockPos center = new Ic2BlockPos((Vec3i)start);
        final Ic2BlockPos tmp = new Ic2BlockPos();
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final Ic2BlockPos nPos = tmp.set((Vec3i)center).move(facing);
            final IBlockState state = nPos.getBlockState(world);
            if (isFence(state)) {
                return Collections.emptyList();
            }
            final TileEntityMagnetizer te;
            if ((te = getMagnetizer(world, nPos, facing, state, checkPower)) != null) {
                ret.add(te);
            }
        }
        if (!ret.isEmpty()) {
            return ret;
        }
        int minDir = 0;
        int maxDir = 2;
        for (int dy = 1; dy <= 20; ++dy) {
            boolean abort = false;
            int dir = minDir;
            while (dir < maxDir) {
                final int offset = dir * 2 - 1;
                center.setY(start.getY() + offset * dy);
                final IBlockState centerState = center.getBlockState(world);
                if (!(centerState.getBlock() instanceof BlockIC2Fence) || !((IC2FenceType)centerState.getValue((IProperty)this.typeProperty)).canBoost) {
                    if (dir == 0) {
                        minDir = 1;
                    }
                    else {
                        maxDir = 1;
                    }
                    if (minDir == maxDir) {
                        abort = true;
                        break;
                    }
                    break;
                }
                else {
                    final int oldSize = ret.size();
                    for (final EnumFacing facing2 : EnumFacing.HORIZONTALS) {
                        final Ic2BlockPos nPos2 = tmp.set((Vec3i)center).move(facing2);
                        final IBlockState state2 = nPos2.getBlockState(world);
                        if (isFence(state2)) {
                            if (dir == 0) {
                                minDir = 1;
                            }
                            else {
                                maxDir = 1;
                            }
                            if (minDir == maxDir) {
                                abort = true;
                            }
                            while (ret.size() > oldSize) {
                                ret.remove(ret.size() - 1);
                            }
                            break;
                        }
                        final TileEntityMagnetizer te2;
                        if ((te2 = getMagnetizer(world, nPos2, facing2, state2, checkPower)) != null) {
                            abort = true;
                            ret.add(te2);
                        }
                    }
                    ++dir;
                }
            }
            if (abort) {
                break;
            }
        }
        return ret;
    }
    
    private static Map<EnumFacing, IProperty<Boolean>> getConnectProperties() {
        final Map<EnumFacing, IProperty<Boolean>> ret = new EnumMap<EnumFacing, IProperty<Boolean>>(EnumFacing.class);
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            ret.put(facing, (IProperty<Boolean>)PropertyBool.create(facing.getName()));
        }
        return ret;
    }
    
    private static Map<IProperty<Boolean>, AxisAlignedBB> getAabbs() {
        final Map<IProperty<Boolean>, AxisAlignedBB> ret = new IdentityHashMap<IProperty<Boolean>, AxisAlignedBB>(BlockIC2Fence.connectProperties.size() + 1);
        final double spaceL = 0.375;
        final double spaceR = 0.625;
        ret.put(null, new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 1.5, 0.625));
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            double start;
            double end;
            if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                start = 0.0;
                end = 0.375;
            }
            else {
                start = 0.625;
                end = 1.0;
            }
            AxisAlignedBB aabb;
            if (facing.getAxis() == EnumFacing.Axis.X) {
                aabb = new AxisAlignedBB(start, 0.0, 0.375, end, 1.5, 0.625);
            }
            else {
                aabb = new AxisAlignedBB(0.375, 0.0, start, 0.625, 1.5, end);
            }
            ret.put(BlockIC2Fence.connectProperties.get(facing), aabb);
        }
        return ret;
    }
    
    static {
        connectProperties = getConnectProperties();
        aabbs = getAabbs();
    }
    
    public enum IC2FenceType implements IIdProvider, IExtBlockType
    {
        iron(true, 5.0f, 10.0f);
        
        public final boolean canBoost;
        private final float hardness;
        private final float explosionResistance;
        
        private IC2FenceType(final boolean canBoost, final float hardness, final float explosionResistance) {
            this.canBoost = canBoost;
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
    }
}

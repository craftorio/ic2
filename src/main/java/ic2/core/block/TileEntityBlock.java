// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import java.util.HashMap;
import net.minecraftforge.fml.common.FMLCommonHandler;
import java.util.IdentityHashMap;
import ic2.core.init.Localization;
import ic2.core.block.comp.Energy;
import net.minecraft.client.util.ITooltipFlag;
import ic2.core.block.type.ResourceBlock;
import ic2.core.ref.BlockName;
import java.util.Set;
import net.minecraft.block.SoundType;
import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;
import net.minecraft.world.Explosion;
import net.minecraft.item.EnumDyeColor;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.block.comp.BasicRedstoneComponent;
import net.minecraft.block.Block;
import ic2.core.IHasGui;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import ic2.core.util.AabbUtil;
import org.apache.commons.lang3.mutable.MutableObject;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.model.ModelComparator;
import ic2.core.ref.MetaTeBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import ic2.core.network.NetworkManager;
import net.minecraft.nbt.NBTBase;
import ic2.core.block.comp.Components;
import java.util.Iterator;
import java.util.ArrayList;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import ic2.core.IWorldTickCallback;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.ref.MetaTeBlockProperty;
import net.minecraft.block.properties.IProperty;
import ic2.core.block.state.MaterialProperty;
import net.minecraft.block.state.IBlockState;
import ic2.core.ref.TeBlock;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import java.util.Map;
import ic2.core.block.comp.TileEntityComponent;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.EnumPlantType;
import ic2.core.gui.dynamic.IGuiConditionProvider;
import net.minecraft.util.ITickable;
import ic2.api.network.INetworkUpdateListener;
import ic2.api.network.INetworkDataProvider;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityBlock extends TileEntity implements INetworkDataProvider, INetworkUpdateListener, ITickable, IGuiConditionProvider
{
    public static final String teBlockName = "teBlk";
    public static final String oldMarker = "Old-";
    protected static final int lightOpacityTranslucent = 0;
    protected static final int lightOpacityOpaque = 255;
    protected static final EnumPlantType noCrop;
    private static final NBTTagCompound emptyNbt;
    private static final List<AxisAlignedBB> defaultAabbs;
    private static final List<TileEntityComponent> emptyComponents;
    private static final Map<Class<?>, TickSubscription> tickSubscriptions;
    private static final byte loadStateInitial = 0;
    private static final byte loadStateQueued = 1;
    private static final byte loadStateLoaded = 2;
    private static final byte loadStateUnloaded = 3;
    private static final boolean debugLoad;
    protected final ITeBlock teBlock;
    private final BlockTileEntity block;
    private Map<Class<? extends TileEntityComponent>, TileEntityComponent> components;
    private Map<Capability<?>, TileEntityComponent> capabilityComponents;
    private List<TileEntityComponent> updatableComponents;
    private boolean active;
    private byte facing;
    private byte loadState;
    private boolean enableWorldTick;
    
    public static <T extends TileEntityBlock> T instantiate(final Class<T> cls) {
        try {
            return cls.newInstance();
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public TileEntityBlock() {
        this.active = false;
        this.facing = (byte)EnumFacing.DOWN.ordinal();
        this.loadState = 0;
        final ITeBlock teb = TeBlockRegistry.get(this.getClass());
        this.teBlock = ((teb == null) ? TeBlock.invalid : teb);
        this.block = TeBlockRegistry.get(this.teBlock.getIdentifier());
    }
    
    public final BlockTileEntity getBlockType() {
        return this.block;
    }
    
    public final IBlockState getBlockState() {
        return this.block.getDefaultState().withProperty((IProperty)this.block.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(this.teBlock.getMaterial())).withProperty((IProperty)this.block.typeProperty, (Comparable)MetaTeBlockProperty.getState(this.teBlock, this.getActive())).withProperty((IProperty)BlockTileEntity.facingProperty, (Comparable)this.getFacing()).withProperty((IProperty)BlockTileEntity.transparentProperty, (Comparable)this.teBlock.isTransparent());
    }
    
    public final void invalidate() {
        if (this.loadState == 2) {
            if (TileEntityBlock.debugLoad) {
                IC2.log.debug(LogCategory.Block, "TE onUnloaded (invalidate) for %s at %s.", this, Util.formatPosition(this));
            }
            this.onUnloaded();
        }
        else {
            if (TileEntityBlock.debugLoad) {
                IC2.log.debug(LogCategory.Block, "Skipping TE onUnloaded (invalidate) for %s at %s, state: %d.", this, Util.formatPosition(this), this.loadState);
            }
            this.loadState = 3;
        }
        super.invalidate();
    }
    
    public final void onChunkUnload() {
        if (this.loadState == 2) {
            if (TileEntityBlock.debugLoad) {
                IC2.log.debug(LogCategory.Block, "TE onUnloaded (chunk unload) for %s at %s.", this, Util.formatPosition(this));
            }
            this.onUnloaded();
        }
        else {
            if (TileEntityBlock.debugLoad) {
                IC2.log.debug(LogCategory.Block, "Skipping TE onUnloaded (chunk unload) for %s at %s, state: %d.", this, Util.formatPosition(this), this.loadState);
            }
            this.loadState = 3;
        }
        super.onChunkUnload();
    }
    
    public final void validate() {
        super.validate();
        final World world = this.getWorld();
        if (world == null || this.pos == null) {
            throw new IllegalStateException("no world/pos");
        }
        if (this.loadState != 0 && this.loadState != 3) {
            throw new IllegalStateException("invalid load state: " + this.loadState);
        }
        this.loadState = 1;
        IC2.tickHandler.requestSingleWorldTick(world, new IWorldTickCallback() {
            @Override
            public void onTick(final World world) {
                final IBlockState state;
                if (world == TileEntityBlock.this.getWorld() && TileEntityBlock.this.pos != null && !TileEntityBlock.this.isInvalid() && TileEntityBlock.this.loadState == 1 && world.isBlockLoaded(TileEntityBlock.this.pos) && (state = world.getBlockState(TileEntityBlock.this.pos)).getBlock() == TileEntityBlock.this.block && world.getTileEntity(TileEntityBlock.this.pos) == TileEntityBlock.this) {
                    final Material expectedMaterial = TileEntityBlock.this.teBlock.getMaterial();
                    if (((MaterialProperty.WrappedMaterial)state.getValue((IProperty)TileEntityBlock.this.block.materialProperty)).getMaterial() != expectedMaterial) {
                        if (TileEntityBlock.debugLoad) {
                            IC2.log.debug(LogCategory.Block, "Adjusting material for %s at %s.", TileEntityBlock.this, Util.formatPosition(TileEntityBlock.this));
                        }
                        world.setBlockState(TileEntityBlock.this.pos, state.withProperty((IProperty)TileEntityBlock.this.block.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(expectedMaterial)), 0);
                        assert world.getTileEntity(TileEntityBlock.this.pos) == TileEntityBlock.this;
                    }
                    if (TileEntityBlock.debugLoad) {
                        IC2.log.debug(LogCategory.Block, "TE onLoaded for %s at %s.", TileEntityBlock.this, Util.formatPosition(TileEntityBlock.this));
                    }
                    TileEntityBlock.this.onLoaded();
                }
                else if (TileEntityBlock.debugLoad) {
                    IC2.log.debug(LogCategory.Block, "Skipping TE init for %s at %s.", TileEntityBlock.this, Util.formatPosition(TileEntityBlock.this));
                }
            }
        });
    }
    
    public final void onLoad() {
    }
    
    protected void onLoaded() {
        if (this.loadState != 1) {
            throw new IllegalStateException("invalid load state: " + this.loadState);
        }
        this.loadState = 2;
        this.enableWorldTick = this.requiresWorldTick();
        if (this.components != null) {
            for (final TileEntityComponent component : this.components.values()) {
                component.onLoaded();
                if (component.enableWorldTick()) {
                    if (this.updatableComponents == null) {
                        this.updatableComponents = new ArrayList<TileEntityComponent>(4);
                    }
                    this.updatableComponents.add(component);
                }
            }
        }
        if (!this.enableWorldTick && this.updatableComponents == null) {
            this.getWorld().tickableTileEntities.remove(this);
        }
    }
    
    protected void onUnloaded() {
        if (this.loadState == 3) {
            throw new IllegalStateException("invalid load state: " + this.loadState);
        }
        this.loadState = 3;
        if (this.components != null) {
            for (final TileEntityComponent component : this.components.values()) {
                component.onUnloaded();
            }
        }
    }
    
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (!this.getSupportedFacings().isEmpty()) {
            final byte facingValue = nbt.getByte("facing");
            if (facingValue >= 0 && facingValue < EnumFacing.VALUES.length && this.getSupportedFacings().contains(EnumFacing.VALUES[facingValue])) {
                this.facing = facingValue;
            }
            else if (!this.getSupportedFacings().isEmpty()) {
                this.facing = (byte)this.getSupportedFacings().iterator().next().ordinal();
            }
            else {
                this.facing = (byte)EnumFacing.DOWN.ordinal();
            }
        }
        this.active = nbt.getBoolean("active");
        if (this.components != null && nbt.hasKey("components", 10)) {
            final NBTTagCompound componentsNbt = nbt.getCompoundTag("components");
            for (final String name : componentsNbt.getKeySet()) {
                final Class<? extends TileEntityComponent> cls = Components.getClass(name);
                final TileEntityComponent component;
                if (cls == null || (component = this.getComponent(cls)) == null) {
                    IC2.log.warn(LogCategory.Block, "Can't find component %s while loading %s.", name, this);
                }
                else {
                    final NBTTagCompound componentNbt = componentsNbt.getCompoundTag(name);
                    component.readFromNbt(componentNbt);
                }
            }
        }
    }
    
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (!this.getSupportedFacings().isEmpty()) {
            nbt.setByte("facing", this.facing);
        }
        nbt.setBoolean("active", this.active);
        if (this.components != null) {
            NBTTagCompound componentsNbt = null;
            for (final TileEntityComponent component : this.components.values()) {
                final NBTTagCompound componentNbt = component.writeToNbt();
                if (componentNbt == null) {
                    continue;
                }
                if (componentsNbt == null) {
                    componentsNbt = new NBTTagCompound();
                    nbt.setTag("components", (NBTBase)componentsNbt);
                }
                componentsNbt.setTag(Components.getId(component.getClass()), (NBTBase)componentNbt);
            }
        }
        return nbt;
    }
    
    public NBTTagCompound getUpdateTag() {
        IC2.network.get(true).sendInitialData(this);
        return TileEntityBlock.emptyNbt;
    }
    
    public SPacketUpdateTileEntity getUpdatePacket() {
        IC2.network.get(true).sendInitialData(this);
        return null;
    }
    
    public final void update() {
        if (this.loadState != 2) {
            return;
        }
        if (this.updatableComponents != null) {
            for (final TileEntityComponent component : this.updatableComponents) {
                component.onWorldTick();
            }
        }
        if (this.enableWorldTick) {
            if (this.getWorld().isRemote) {
                this.updateEntityClient();
            }
            else {
                this.updateEntityServer();
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    protected void updateEntityClient() {
    }
    
    protected void updateEntityServer() {
    }
    
    public List<String> getNetworkedFields() {
        final List<String> ret = new ArrayList<String>(3);
        ret.add("teBlk=" + (this.isOldVersion() ? "Old-" : "") + this.teBlock.getName());
        ret.add("active");
        ret.add("facing");
        return ret;
    }
    
    private boolean isOldVersion() {
        assert this.hasWorld() && !this.getWorld().isRemote;
        return this.teBlock.getTeClass() != this.getClass();
    }
    
    public void onNetworkUpdate(final String field) {
        if ((field.equals("active") && this.hasActiveTexture()) || field.equals("facing")) {
            this.rerender();
        }
    }
    
    @SideOnly(Side.CLIENT)
    private boolean hasActiveTexture() {
        if (!this.teBlock.hasActive()) {
            return false;
        }
        final IBlockState stateA = this.getBlockState();
        final IBlockState stateB = stateA.withProperty((IProperty)this.block.typeProperty, (Comparable)MetaTeBlockProperty.getState(this.teBlock, !((MetaTeBlock)stateA.getValue((IProperty)this.block.typeProperty)).active));
        return !ModelComparator.isEqual(stateA, stateB, this.getWorld(), this.getPos());
    }
    
    protected Ic2BlockState.Ic2BlockStateInstance getExtendedState(final Ic2BlockState.Ic2BlockStateInstance state) {
        return state;
    }
    
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, EnumFacing facing) {
        final World world = this.getWorld();
        if (!world.isRemote) {}
        facing = this.getPlacementFacing(placer, facing);
        if (facing != this.getFacing()) {
            this.setFacing(facing);
        }
        if (world.isRemote) {
            this.rerender();
        }
    }
    
    protected RayTraceResult collisionRayTrace(final Vec3d start, final Vec3d end) {
        final Vec3d startNormalized = start.subtract((double)this.pos.getX(), (double)this.pos.getY(), (double)this.pos.getZ());
        final double lengthSq = Util.square(end.x - start.x) + Util.square(end.y - start.y) + Util.square(end.z - start.z);
        final double lengthInv = 1.0 / Math.sqrt(lengthSq);
        final Vec3d direction = new Vec3d((end.x - start.x) * lengthInv, (end.y - start.y) * lengthInv, (end.z - start.z) * lengthInv);
        double minDistanceSq = lengthSq;
        Vec3d minIntersection = null;
        EnumFacing minIntersectionSide = null;
        final MutableObject<Vec3d> intersectionOut = (MutableObject<Vec3d>)new MutableObject();
        for (final AxisAlignedBB aabb : this.getAabbs(false)) {
            final EnumFacing side = AabbUtil.getIntersection(startNormalized, direction, aabb, intersectionOut);
            if (side == null) {
                continue;
            }
            final Vec3d intersection = (Vec3d)intersectionOut.getValue();
            final double distanceSq = Util.square(intersection.x - startNormalized.x) + Util.square(intersection.y - startNormalized.y) + Util.square(intersection.z - startNormalized.z);
            if (distanceSq >= minDistanceSq) {
                continue;
            }
            minDistanceSq = distanceSq;
            minIntersection = intersection;
            minIntersectionSide = side;
        }
        if (minIntersection == null) {
            return null;
        }
        return new RayTraceResult(minIntersection.addVector((double)this.pos.getX(), (double)this.pos.getY(), (double)this.pos.getZ()), minIntersectionSide, this.pos);
    }
    
    public AxisAlignedBB getVisualBoundingBox() {
        return this.getAabb(false);
    }
    
    protected AxisAlignedBB getPhysicsBoundingBox() {
        return this.getAabb(true);
    }
    
    protected AxisAlignedBB getOutlineBoundingBox() {
        return this.getVisualBoundingBox();
    }
    
    protected void addCollisionBoxesToList(final AxisAlignedBB mask, final List<AxisAlignedBB> list, final Entity collidingEntity) {
        final AxisAlignedBB maskNormalized = mask.offset((double)(-this.pos.getX()), (double)(-this.pos.getY()), (double)(-this.pos.getZ()));
        for (final AxisAlignedBB aabb : this.getAabbs(true)) {
            if (!aabb.intersects(maskNormalized)) {
                continue;
            }
            list.add(aabb.offset(this.pos));
        }
    }
    
    private AxisAlignedBB getAabb(final boolean forCollision) {
        final List<AxisAlignedBB> aabbs = this.getAabbs(forCollision);
        if (aabbs.isEmpty()) {
            throw new RuntimeException("No AABBs for " + this);
        }
        if (aabbs.size() == 1) {
            return aabbs.get(0);
        }
        double zS;
        double xS;
        double yS = xS = (zS = Double.POSITIVE_INFINITY);
        double zE;
        double xE;
        double yE = xE = (zE = Double.NEGATIVE_INFINITY);
        for (final AxisAlignedBB aabb : aabbs) {
            xS = Math.min(xS, aabb.minX);
            yS = Math.min(yS, aabb.minY);
            zS = Math.min(zS, aabb.minZ);
            xE = Math.max(xE, aabb.maxX);
            yE = Math.max(yE, aabb.maxY);
            zE = Math.max(zE, aabb.maxZ);
        }
        return new AxisAlignedBB(xS, yS, zS, xE, yE, zE);
    }
    
    protected void onEntityCollision(final Entity entity) {
    }
    
    @SideOnly(Side.CLIENT)
    protected boolean shouldSideBeRendered(final EnumFacing side, final BlockPos otherPos) {
        final AxisAlignedBB aabb = this.getVisualBoundingBox();
        if (aabb != TileEntityBlock.defaultAabbs) {
            switch (side) {
                case DOWN: {
                    if (aabb.minY > 0.0) {
                        return true;
                    }
                    break;
                }
                case UP: {
                    if (aabb.maxY < 1.0) {
                        return true;
                    }
                    break;
                }
                case NORTH: {
                    if (aabb.minZ > 0.0) {
                        return true;
                    }
                    break;
                }
                case SOUTH: {
                    if (aabb.maxZ < 1.0) {
                        return true;
                    }
                    break;
                }
                case WEST: {
                    if (aabb.minX > 0.0) {
                        return true;
                    }
                    break;
                }
                case EAST: {
                    if (aabb.maxX < 1.0) {
                        return true;
                    }
                    break;
                }
            }
        }
        final World world = this.getWorld();
        return !world.getBlockState(otherPos).doesSideBlockRendering((IBlockAccess)world, otherPos, side.getOpposite());
    }
    
    protected boolean doesSideBlockRendering(final EnumFacing side) {
        return checkSide(this.getAabbs(false), side, false);
    }
    
    private static boolean checkSide(final List<AxisAlignedBB> aabbs, final EnumFacing side, final boolean strict) {
        if (aabbs == TileEntityBlock.defaultAabbs) {
            return true;
        }
        final int dx = side.getFrontOffsetX();
        final int dy = side.getFrontOffsetY();
        final int dz = side.getFrontOffsetZ();
        final int xS = (dx + 1) / 2;
        final int yS = (dy + 1) / 2;
        final int zS = (dz + 1) / 2;
        final int xE = (dx + 2) / 2;
        final int yE = (dy + 2) / 2;
        final int zE = (dz + 2) / 2;
        if (strict) {
            for (final AxisAlignedBB aabb : aabbs) {
                switch (side) {
                    case DOWN: {
                        if (aabb.minY < 0.0) {
                            return false;
                        }
                        continue;
                    }
                    case UP: {
                        if (aabb.maxY > 1.0) {
                            return false;
                        }
                        continue;
                    }
                    case NORTH: {
                        if (aabb.minZ < 0.0) {
                            return false;
                        }
                        continue;
                    }
                    case SOUTH: {
                        if (aabb.maxZ > 1.0) {
                            return false;
                        }
                        continue;
                    }
                    case WEST: {
                        if (aabb.minX < 0.0) {
                            return false;
                        }
                        continue;
                    }
                    case EAST: {
                        if (aabb.maxX > 1.0) {
                            return false;
                        }
                        continue;
                    }
                }
            }
        }
        for (final AxisAlignedBB aabb : aabbs) {
            if (aabb.minX <= xS && aabb.minY <= yS && aabb.minZ <= zS && aabb.maxX >= xE && aabb.maxY >= yE && aabb.maxZ >= zE) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean isNormalCube() {
        final List<AxisAlignedBB> aabbs = this.getAabbs(false);
        if (aabbs == TileEntityBlock.defaultAabbs) {
            return true;
        }
        if (aabbs.size() != 1) {
            return false;
        }
        final AxisAlignedBB aabb = aabbs.get(0);
        return aabb.minX <= 0.0 && aabb.minY <= 0.0 && aabb.minZ <= 0.0 && aabb.maxX >= 1.0 && aabb.maxY >= 1.0 && aabb.maxZ >= 1.0;
    }
    
    protected boolean isSideSolid(final EnumFacing side) {
        return checkSide(this.getAabbs(false), side, true);
    }
    
    protected BlockFaceShape getFaceShape(final EnumFacing face) {
        return this.isSideSolid(face) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
    
    protected int getLightOpacity() {
        return this.isNormalCube() ? 255 : 0;
    }
    
    protected int getLightValue() {
        return 0;
    }
    
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        return this instanceof IHasGui && (this.getWorld().isRemote || IC2.platform.launchGui(player, (IHasGui)this));
    }
    
    protected void onClicked(final EntityPlayer player) {
    }
    
    protected void onNeighborChange(final Block neighbor, final BlockPos neighborPos) {
        if (this.components != null) {
            for (final TileEntityComponent component : this.components.values()) {
                component.onNeighborChange(neighbor, neighborPos);
            }
        }
    }
    
    protected int getWeakPower(final EnumFacing side) {
        final BasicRedstoneComponent component = this.getComponent(RedstoneEmitter.class);
        return (component == null) ? 0 : component.getLevel();
    }
    
    protected boolean canConnectRedstone(final EnumFacing side) {
        return this.hasComponent(RedstoneEmitter.class) || this.hasComponent(Redstone.class);
    }
    
    protected int getComparatorInputOverride() {
        final BasicRedstoneComponent component = this.getComponent(ComparatorEmitter.class);
        return (component == null) ? 0 : component.getLevel();
    }
    
    protected boolean recolor(final EnumFacing side, final EnumDyeColor mcColor) {
        return false;
    }
    
    protected void onExploded(final Explosion explosion) {
    }
    
    protected void onBlockBreak() {
    }
    
    protected boolean onRemovedByPlayer(final EntityPlayer player, final boolean willHarvest) {
        return true;
    }
    
    protected boolean isFlammable(final EnumFacing face) {
        return true;
    }
    
    protected ItemStack getPickBlock(final EntityPlayer player, final RayTraceResult target) {
        return this.block.getItemStack(this.teBlock);
    }
    
    protected boolean canHarvest(final EntityPlayer player, final boolean defaultValue) {
        return defaultValue;
    }
    
    protected List<ItemStack> getSelfDrops(final int fortune, final boolean wrench) {
        ItemStack drop = this.getPickBlock(null, null);
        drop = this.adjustDrop(drop, wrench);
        if (drop == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(drop);
    }
    
    protected List<ItemStack> getAuxDrops(final int fortune) {
        return Collections.emptyList();
    }
    
    protected float getHardness() {
        return this.teBlock.getHardness();
    }
    
    protected float getExplosionResistance(final Entity exploder, final Explosion explosion) {
        return this.teBlock.getExplosionResistance();
    }
    
    protected boolean canEntityDestroy(final Entity entity) {
        return true;
    }
    
    public EnumFacing getFacing() {
        return EnumFacing.VALUES[this.facing];
    }
    
    protected boolean canSetFacingWrench(final EnumFacing facing, final EntityPlayer player) {
        return this.teBlock.allowWrenchRotating() && facing != this.getFacing() && this.getSupportedFacings().contains(facing);
    }
    
    protected boolean setFacingWrench(final EnumFacing facing, final EntityPlayer player) {
        if (!this.canSetFacingWrench(facing, player)) {
            return false;
        }
        this.setFacing(facing);
        return true;
    }
    
    protected boolean wrenchCanRemove(final EntityPlayer player) {
        return true;
    }
    
    protected List<ItemStack> getWrenchDrops(final EntityPlayer player, final int fortune) {
        final List<ItemStack> ret = new ArrayList<ItemStack>();
        ret.addAll(this.getSelfDrops(fortune, true));
        ret.addAll(this.getAuxDrops(fortune));
        return ret;
    }
    
    protected EnumPlantType getPlantType() {
        return TileEntityBlock.noCrop;
    }
    
    protected SoundType getBlockSound(final Entity entity) {
        return SoundType.STONE;
    }
    
    protected EnumFacing getPlacementFacing(final EntityLivingBase placer, final EnumFacing facing) {
        final Set<EnumFacing> supportedFacings = this.getSupportedFacings();
        if (supportedFacings.isEmpty()) {
            return EnumFacing.DOWN;
        }
        if (placer == null) {
            return (facing != null && supportedFacings.contains(facing.getOpposite())) ? facing.getOpposite() : this.getSupportedFacings().iterator().next();
        }
        final Vec3d dir = placer.getLookVec();
        EnumFacing bestFacing = null;
        double maxMatch = Double.NEGATIVE_INFINITY;
        for (final EnumFacing cFacing : supportedFacings) {
            final double match = dir.dotProduct(new Vec3d(cFacing.getOpposite().getDirectionVec()));
            if (match > maxMatch) {
                maxMatch = match;
                bestFacing = cFacing;
            }
        }
        return bestFacing;
    }
    
    protected List<AxisAlignedBB> getAabbs(final boolean forCollision) {
        return TileEntityBlock.defaultAabbs;
    }
    
    protected ItemStack adjustDrop(ItemStack drop, final boolean wrench) {
        if (!wrench) {
            switch (this.teBlock.getDefaultDrop()) {
                case None: {
                    drop = null;
                    break;
                }
                case Generator: {
                    drop = BlockName.te.getItemStack(TeBlock.generator);
                    break;
                }
                case Machine: {
                    drop = BlockName.resource.getItemStack(ResourceBlock.machine);
                    break;
                }
                case AdvMachine: {
                    drop = BlockName.resource.getItemStack(ResourceBlock.advanced_machine);
                    break;
                }
            }
        }
        return drop;
    }
    
    protected Set<EnumFacing> getSupportedFacings() {
        return this.teBlock.getSupportedFacings();
    }
    
    protected void setFacing(final EnumFacing facing) {
        if (facing == null) {
            throw new NullPointerException("null facing");
        }
        if (this.facing == facing.ordinal()) {
            throw new IllegalArgumentException("unchanged facing");
        }
        if (!this.getSupportedFacings().contains(facing)) {
            throw new IllegalArgumentException("invalid facing: " + facing + ", supported: " + this.getSupportedFacings());
        }
        this.facing = (byte)facing.ordinal();
        if (!this.getWorld().isRemote) {
            IC2.network.get(true).updateTileEntityField(this, "facing");
        }
    }
    
    public boolean getActive() {
        return this.active;
    }
    
    public void setActive(final boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        IC2.network.get(true).updateTileEntityField(this, "active");
    }
    
    public boolean getGuiState(final String name) {
        if ("active".equals(name)) {
            return this.getActive();
        }
        throw new IllegalArgumentException("Unexpected GUI value requested: " + name);
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final List<String> tooltip, final ITooltipFlag advanced) {
        if (this.hasComponent(Energy.class)) {
            final Energy energy = this.getComponent(Energy.class);
            if (!energy.getSourceDirs().isEmpty()) {
                tooltip.add(Localization.translate("ic2.item.tooltip.PowerTier", energy.getSourceTier()));
            }
            else if (!energy.getSinkDirs().isEmpty()) {
                tooltip.add(Localization.translate("ic2.item.tooltip.PowerTier", energy.getSinkTier()));
            }
        }
    }
    
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        if (super.hasCapability((Capability)capability, facing)) {
            return true;
        }
        if (this.capabilityComponents == null) {
            return false;
        }
        final TileEntityComponent comp = this.capabilityComponents.get(capability);
        return comp != null && comp.getProvidedCapabilities(facing).contains(capability);
    }
    
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (this.capabilityComponents == null) {
            return (T)super.getCapability((Capability)capability, facing);
        }
        final TileEntityComponent comp = this.capabilityComponents.get(capability);
        if (comp == null) {
            return (T)super.getCapability((Capability)capability, facing);
        }
        return comp.getCapability(capability, facing);
    }
    
    protected final <T extends TileEntityComponent> T addComponent(final T component) {
        if (component == null) {
            throw new NullPointerException("null component");
        }
        if (this.components == null) {
            this.components = new IdentityHashMap<Class<? extends TileEntityComponent>, TileEntityComponent>(4);
        }
        final TileEntityComponent prev = this.components.put(component.getClass(), component);
        if (prev != null) {
            throw new RuntimeException("conflicting component while adding " + component + ", already used by " + prev + ".");
        }
        for (final Capability<?> cap : component.getProvidedCapabilities(null)) {
            this.addComponentCapability(cap, component);
        }
        return component;
    }
    
    public boolean hasComponent(final Class<? extends TileEntityComponent> cls) {
        return this.components != null && this.components.containsKey(cls);
    }
    
    public <T extends TileEntityComponent> T getComponent(final Class<T> cls) {
        if (this.components == null) {
            return null;
        }
        return (T)this.components.get(cls);
    }
    
    public final Iterable<? extends TileEntityComponent> getComponents() {
        if (this.components == null) {
            return TileEntityBlock.emptyComponents;
        }
        return this.components.values();
    }
    
    private void addComponentCapability(final Capability<?> cap, final TileEntityComponent component) {
        if (this.capabilityComponents == null) {
            this.capabilityComponents = new IdentityHashMap<Capability<?>, TileEntityComponent>();
        }
        final TileEntityComponent prev = this.capabilityComponents.put(cap, component);
        assert prev == null;
    }
    
    protected final void rerender() {
        final IBlockState state = this.getBlockState();
        this.getWorld().notifyBlockUpdate(this.pos, state, state, 2);
    }
    
    protected boolean clientNeedsExtraModelInfo() {
        return false;
    }
    
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState, final IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
    
    private final synchronized boolean requiresWorldTick() {
        Class<?> cls = this.getClass();
        TickSubscription subscription = TileEntityBlock.tickSubscriptions.get(cls);
        if (subscription == null) {
            boolean hasUpdateClient = false;
            boolean hasUpdateServer = false;
            for (boolean isClient = FMLCommonHandler.instance().getSide().isClient(); cls != TileEntityBlock.class && ((!hasUpdateClient && isClient) || !hasUpdateServer); cls = cls.getSuperclass()) {
                if (!hasUpdateClient && isClient) {
                    boolean found = true;
                    try {
                        cls.getDeclaredMethod("updateEntityClient", (Class<?>[])new Class[0]);
                    }
                    catch (final NoSuchMethodException e) {
                        found = false;
                    }
                    if (found) {
                        hasUpdateClient = true;
                    }
                }
                if (!hasUpdateServer) {
                    boolean found = true;
                    try {
                        cls.getDeclaredMethod("updateEntityServer", (Class<?>[])new Class[0]);
                    }
                    catch (final NoSuchMethodException e) {
                        found = false;
                    }
                    if (found) {
                        hasUpdateServer = true;
                    }
                }
            }
            if (hasUpdateClient) {
                if (hasUpdateServer) {
                    subscription = TickSubscription.Both;
                }
                else {
                    subscription = TickSubscription.Client;
                }
            }
            else if (hasUpdateServer) {
                subscription = TickSubscription.Server;
            }
            else {
                subscription = TickSubscription.None;
            }
            TileEntityBlock.tickSubscriptions.put(this.getClass(), subscription);
        }
        if (this.getWorld().isRemote) {
            return subscription == TickSubscription.Both || subscription == TickSubscription.Client;
        }
        return subscription == TickSubscription.Both || subscription == TickSubscription.Server;
    }
    
    static {
        noCrop = EnumPlantType.getPlantType("IC2_NO_CROP");
        emptyNbt = new NBTTagCompound();
        defaultAabbs = Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
        emptyComponents = Collections.emptyList();
        tickSubscriptions = new HashMap<Class<?>, TickSubscription>();
        debugLoad = (System.getProperty("ic2.te.debugload") != null);
    }
    
    private enum TickSubscription
    {
        None, 
        Client, 
        Server, 
        Both;
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.block.state.SkippedBooleanProperty;
import net.minecraft.block.properties.PropertyDirection;
import ic2.core.block.state.IIdProvider;
import net.minecraft.block.SoundType;
import net.minecraftforge.common.EnumPlantType;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import java.util.ArrayList;
import net.minecraft.world.Explosion;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumHand;
import net.minecraft.block.state.BlockFaceShape;
import java.util.List;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.EnumPushReaction;
import ic2.core.util.ParticleUtil;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import ic2.core.network.NetworkManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.util.StackUtil;
import net.minecraft.world.World;
import java.util.Collections;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.BlockRenderLayer;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Iterator;
import net.minecraft.client.renderer.block.model.ModelBakery;
import ic2.api.item.ITeBlockSpecialItem;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraftforge.client.model.ModelLoader;
import com.google.common.collect.UnmodifiableIterator;
import java.util.IdentityHashMap;
import net.minecraft.block.state.IBlockState;
import java.util.Map;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import ic2.core.model.ModelUtil;
import ic2.core.util.Util;
import net.minecraftforge.fml.common.ModContainer;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import com.google.common.base.Function;
import java.util.function.Consumer;
import net.minecraftforge.fml.common.Loader;
import ic2.core.ref.MetaTeBlockProperty;
import net.minecraft.util.ResourceLocation;
import ic2.core.ref.TeBlock;
import net.minecraft.block.material.Material;
import java.util.Collection;
import ic2.core.ref.BlockName;
import java.lang.ref.WeakReference;
import ic2.core.item.block.ItemBlockTileEntity;
import net.minecraft.util.EnumFacing;
import ic2.core.block.state.MaterialProperty;
import ic2.core.ref.MetaTeBlock;
import net.minecraft.block.properties.IProperty;
import net.minecraftforge.common.IPlantable;
import ic2.api.tile.IWrenchable;
import ic2.core.ref.IMultiBlock;

public final class BlockTileEntity extends BlockBase implements IMultiBlock<ITeBlock>, IWrenchable, IPlantable
{
    public final IProperty<MetaTeBlock> typeProperty;
    public final MaterialProperty materialProperty;
    public static final IProperty<EnumFacing> facingProperty;
    private static final ThreadLocal<IProperty<MetaTeBlock>> currentTypeProperty;
    private static final ThreadLocal<MaterialProperty> currentMaterialProperty;
    public static final IProperty<Boolean> transparentProperty;
    private final ItemBlockTileEntity item;
    private static final int removedTesToKeep = 4;
    private static final WeakReference<TileEntityBlock>[] removedTes;
    private static int nextRemovedTeIndex;
    
    static BlockTileEntity create(final BlockName name, final Collection<Material> materials) {
        final BlockTileEntity ret = create(name.name(), TeBlock.invalid.getIdentifier(), materials);
        name.setInstance(ret);
        return ret;
    }
    
    static BlockTileEntity create(final String name, final ResourceLocation identifier, final Collection<Material> materials) {
        BlockTileEntity.currentTypeProperty.set((IProperty<MetaTeBlock>)new MetaTeBlockProperty(identifier));
        BlockTileEntity.currentMaterialProperty.set(new MaterialProperty(materials));
        final BlockTileEntity ret = new BlockTileEntity(name, identifier);
        BlockTileEntity.currentMaterialProperty.remove();
        BlockTileEntity.currentTypeProperty.remove();
        return ret;
    }
    
    private BlockTileEntity(final String name, final ResourceLocation identifier) {
        super(null, TeBlockRegistry.getInfo(identifier).getDefaultMaterial());
        this.typeProperty = this.getTypeProperty();
        this.materialProperty = this.getMaterialProperty();
        final ModContainer ic2 = Loader.instance().activeModContainer();
        Loader.instance().getActiveModList().stream().filter(mod -> identifier.getResourceDomain().equals(mod.getModId())).findFirst().ifPresent(Loader.instance()::setActiveModContainer);
        this.register(name, identifier, (java.util.function.Function<Block, Item>)new Function<Block, Item>() {
            public Item apply(final Block input) {
                return (Item)new ItemBlockTileEntity(input, identifier);
            }
        });
        Loader.instance().setActiveModContainer(ic2);
        this.setDefaultState(this.blockState.getBaseState().withProperty((IProperty)this.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(this.blockMaterial)).withProperty((IProperty)this.typeProperty, (Comparable)MetaTeBlockProperty.invalid).withProperty((IProperty)BlockTileEntity.facingProperty, (Comparable)EnumFacing.DOWN).withProperty((IProperty)BlockTileEntity.transparentProperty, (Comparable)Boolean.FALSE));
        this.item = (ItemBlockTileEntity)Item.getItemFromBlock((Block)this);
        IC2.log.debug(LogCategory.Block, "Successfully built BlockTileEntity for identity " + identifier + '.');
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final BlockName name) {
        final ModelResourceLocation invalidLocation = ModelUtil.getTEBlockModelLocation(Util.getName(BlockName.te.getInstance()), this.blockState.getBaseState().withProperty((IProperty)this.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(this.blockMaterial)).withProperty((IProperty)this.typeProperty, (Comparable)MetaTeBlockProperty.invalid).withProperty((IProperty)BlockTileEntity.facingProperty, (Comparable)EnumFacing.NORTH).withProperty((IProperty)BlockTileEntity.transparentProperty, (Comparable)Boolean.FALSE));
        IC2.log.debug(LogCategory.Block, "Preparing to set models for " + this.item.identifier + '.');
        IC2.log.debug(LogCategory.Block, "Mapping " + this.getBlockState().getValidStates().size() + " states.");
        ModelLoader.setCustomStateMapper((Block)this, (IStateMapper)new IStateMapper() {
            public Map<IBlockState, ModelResourceLocation> putStateModelLocations(final Block block) {
                final Map<IBlockState, ModelResourceLocation> ret = new IdentityHashMap<IBlockState, ModelResourceLocation>();
                for (final IBlockState state : block.getBlockState().getValidStates()) {
                    final MetaTeBlock metaTeBlock = (MetaTeBlock)state.getValue((IProperty)BlockTileEntity.this.typeProperty);
                    final EnumFacing facing = (EnumFacing)state.getValue((IProperty)BlockTileEntity.facingProperty);
                    if (metaTeBlock.teBlock.getSupportedFacings().contains(facing) || (facing == EnumFacing.DOWN && metaTeBlock.teBlock.getSupportedFacings().isEmpty())) {
                        ret.put(state, ModelUtil.getTEBlockModelLocation(metaTeBlock.teBlock.getIdentifier(), state));
                    }
                    else {
                        ret.put(state, invalidLocation);
                    }
                }
                return ret;
            }
        });
        ModelLoader.setCustomMeshDefinition((Item)this.item, (ItemMeshDefinition)new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                final ITeBlock teBlock = TeBlockRegistry.get(BlockTileEntity.this.item.identifier, stack.getItemDamage());
                if (teBlock == null) {
                    return invalidLocation;
                }
                if (teBlock instanceof ITeBlockSpecialItem && ((ITeBlockSpecialItem)teBlock).doesOverrideDefault(stack)) {
                    final ModelResourceLocation location = ((ITeBlockSpecialItem)teBlock).getModelLocation(stack);
                    return (location == null) ? invalidLocation : location;
                }
                final IBlockState state = BlockTileEntity.this.getDefaultState().withProperty((IProperty)BlockTileEntity.this.typeProperty, (Comparable)MetaTeBlockProperty.getState(teBlock)).withProperty((IProperty)BlockTileEntity.facingProperty, (Comparable)getItemFacing(teBlock));
                return ModelUtil.getTEBlockModelLocation(teBlock.getIdentifier(), state);
            }
        });
        final boolean checkSpecialModels = TeBlockRegistry.getInfo(this.item.identifier).hasSpecialModels();
        for (final MetaTeBlockProperty.MetaTePair block : MetaTeBlockProperty.getAllStates(this.item.identifier)) {
            if (block.hasItem()) {
                ModelResourceLocation model = checkSpecialModels ? this.getSpecialModel(block) : null;
                if (model == null) {
                    final IBlockState state = this.blockState.getBaseState().withProperty((IProperty)this.typeProperty, (Comparable)block.inactive).withProperty((IProperty)BlockTileEntity.facingProperty, (Comparable)getItemFacing(block.getBlock()));
                    model = ModelUtil.getTEBlockModelLocation(block.getIdentifier(), state);
                }
                assert model != null;
                ModelBakery.registerItemVariants((Item)this.item, new ResourceLocation[] { (ResourceLocation)model });
            }
            IC2.log.debug(LogCategory.Block, "Done item for " + this.item.identifier + ':' + block.getName() + '.');
        }
    }
    
    private static EnumFacing getItemFacing(final ITeBlock teBlock) {
        final Set<EnumFacing> supported = teBlock.getSupportedFacings();
        if (supported.contains(EnumFacing.NORTH)) {
            return EnumFacing.NORTH;
        }
        if (!supported.isEmpty()) {
            return supported.iterator().next();
        }
        return EnumFacing.DOWN;
    }
    
    @SideOnly(Side.CLIENT)
    private ModelResourceLocation getSpecialModel(final MetaTeBlockProperty.MetaTePair blockTextures) {
        assert blockTextures.getBlock() instanceof ITeBlockSpecialItem;
        final ITeBlockSpecialItem block = (ITeBlockSpecialItem)blockTextures.getBlock();
        final ItemStack stack = new ItemStack((Item)this.item, 1, blockTextures.getBlock().getId());
        return block.doesOverrideDefault(stack) ? block.getModelLocation(stack) : null;
    }
    
    public boolean canRenderInLayer(final IBlockState state, final BlockRenderLayer layer) {
        return state.getValue((IProperty)BlockTileEntity.transparentProperty) ? (layer == BlockRenderLayer.CUTOUT) : (layer == BlockRenderLayer.SOLID);
    }
    
    public boolean hasTileEntity() {
        return true;
    }
    
    public boolean hasTileEntity(final IBlockState state) {
        return true;
    }
    
    protected BlockStateContainer createBlockState() {
        return new Ic2BlockState(this, (IProperty<?>[])new IProperty[] { this.getTypeProperty(), (IProperty)this.getMaterialProperty(), BlockTileEntity.facingProperty, BlockTileEntity.transparentProperty });
    }
    
    public int getMetaFromState(final IBlockState state) {
        int ret = this.materialProperty.getId((MaterialProperty.WrappedMaterial)state.getValue((IProperty)this.materialProperty));
        if (ret < 0 || ret >= 8) {
            throw new IllegalStateException("invalid material id: " + ret);
        }
        ret |= (state.getValue((IProperty)BlockTileEntity.transparentProperty) ? 8 : 0);
        return ret;
    }
    
    public IBlockState getStateFromMeta(final int meta) {
        final boolean isTransparent = (meta & 0x8) != 0x0;
        final int materialId = meta & 0x7;
        return this.getDefaultState().withProperty((IProperty)this.materialProperty, (Comparable)this.materialProperty.getMaterial(materialId)).withProperty((IProperty)BlockTileEntity.transparentProperty, (Comparable)isTransparent);
    }
    
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return state;
        }
        return te.getBlockState();
    }
    
    @Override
    public String getUnlocalizedName() {
        if (!this.isIC2()) {
            return this.item.identifier.getResourceDomain() + '.' + this.item.identifier.getResourcePath();
        }
        return super.getUnlocalizedName();
    }
    
    public void getSubBlocks(final CreativeTabs tabs, final NonNullList<ItemStack> list) {
        final TeBlockRegistry.TeBlockInfo<?> info = TeBlockRegistry.getInfo(this.item.identifier);
        if (info.hasCreativeRegisterer()) {
            info.getCreativeRegisterer().addSubBlocks(list, this, this.item, tabs);
        }
        else if (tabs == IC2.tabIC2 || tabs == CreativeTabs.SEARCH) {
            for (final ITeBlock type : info.getTeBlocks()) {
                if (type.hasItem()) {
                    list.add((Object)this.getItemStack(type));
                }
            }
        }
    }
    
    public Set<ITeBlock> getAllTypes() {
        return Collections.unmodifiableSet(TeBlockRegistry.getAll(this.item.identifier));
    }
    
    public ItemStack getItem(final World world, final BlockPos pos, final IBlockState state) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return StackUtil.emptyStack;
        }
        return te.getPickBlock(null, null);
    }
    
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world, final BlockPos pos, final EntityPlayer player) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return StackUtil.emptyStack;
        }
        return te.getPickBlock(player, target);
    }
    
    @Override
    public IBlockState getState(final ITeBlock variant) {
        if (variant == null) {
            throw new IllegalArgumentException("invalid type: " + variant);
        }
        final Set<EnumFacing> supportedFacings = variant.getSupportedFacings();
        EnumFacing facing;
        if (supportedFacings.isEmpty()) {
            facing = EnumFacing.DOWN;
        }
        else if (supportedFacings.contains(EnumFacing.NORTH)) {
            facing = EnumFacing.NORTH;
        }
        else {
            facing = supportedFacings.iterator().next();
        }
        return this.getDefaultState().withProperty((IProperty)this.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(variant.getMaterial())).withProperty((IProperty)this.typeProperty, (Comparable)MetaTeBlockProperty.getState(variant)).withProperty((IProperty)BlockTileEntity.facingProperty, (Comparable)facing).withProperty((IProperty)BlockTileEntity.transparentProperty, (Comparable)variant.isTransparent());
    }
    
    @Override
    public IBlockState getState(final String variant) {
        return this.getState(TeBlockRegistry.get(variant));
    }
    
    public ItemStack getItemStack(final ITeBlock type) {
        if (type == null) {
            throw new IllegalArgumentException("invalid type: null");
        }
        final int id = type.getId();
        if (id != -1) {
            return new ItemStack((Item)this.item, 1, id);
        }
        return null;
    }
    
    public ItemStack getItemStack(final String variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Invalid ITeBlock type: null");
        }
        final ITeBlock type = TeBlockRegistry.get(variant);
        if (type == null) {
            throw new IllegalArgumentException("Invalid ITeBlock type: " + variant);
        }
        return this.getItemStack(type);
    }
    
    public String getVariant(final ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null stack");
        }
        if (stack.getItem() != this.item) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this.item + " (" + this + ")");
        }
        final ITeBlock type = TeBlockRegistry.get(this.item.identifier, stack.getMetadata());
        if (type == null) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't reference any valid subtype");
        }
        return type.getName();
    }
    
    public boolean isFullCube(final IBlockState state) {
        return false;
    }
    
    public boolean isOpaqueCube(final IBlockState state) {
        return !(boolean)state.getValue((IProperty)BlockTileEntity.transparentProperty);
    }
    
    public boolean canReplace(final World world, final BlockPos pos, final EnumFacing side, final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return true;
        }
        if (stack.getItem() != this.item) {
            return false;
        }
        final ITeBlock type = TeBlockRegistry.get(this.item.identifier, stack.getMetadata());
        if (type == null) {
            return false;
        }
        final TeBlock.ITePlaceHandler handler = type.getPlaceHandler();
        return handler == null || handler.canReplace(world, pos, side, stack);
    }
    
    public boolean addLandingEffects(final IBlockState state, final WorldServer world, final BlockPos pos, final IBlockState state2, final EntityLivingBase entity, final int numberOfParticles) {
        if (world.isRemote) {
            throw new IllegalStateException();
        }
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return super.addLandingEffects(state, world, pos, state2, entity, numberOfParticles);
        }
        if (te.clientNeedsExtraModelInfo()) {
            IC2.network.get(true).initiateTeblockLandEffect((World)world, pos, entity.posX, entity.posY, entity.posZ, numberOfParticles, te.teBlock);
        }
        else {
            IC2.network.get(true).initiateTeblockLandEffect((World)world, entity.posX, entity.posY, entity.posZ, numberOfParticles, te.teBlock);
        }
        return true;
    }
    
    public boolean addRunningEffects(final IBlockState state, final World world, final BlockPos pos, final Entity entity) {
        if (world.isRemote) {
            return true;
        }
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return super.addRunningEffects(state, world, pos, entity);
        }
        if (te.clientNeedsExtraModelInfo()) {
            IC2.network.get(true).initiateTeblockRunEffect(world, pos, entity, te.teBlock);
        }
        else {
            IC2.network.get(true).initiateTeblockRunEffect(world, entity, te.teBlock);
        }
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(final IBlockState state, final World world, final RayTraceResult target, final ParticleManager manager) {
        final BlockPos pos = target.getBlockPos();
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return super.addHitEffects(state, world, target, manager);
        }
        ParticleUtil.spawnBlockHitParticles(te, target.sideHit, te.clientNeedsExtraModelInfo());
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(final World world, final BlockPos pos, final ParticleManager manager) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te != null && te.clientNeedsExtraModelInfo()) {
            ParticleUtil.spawnBlockBreakParticles(te);
            return true;
        }
        return super.addDestroyEffects(world, pos, manager);
    }
    
    public Material getMaterial(final IBlockState state) {
        return ((MaterialProperty.WrappedMaterial)state.getValue((IProperty)this.materialProperty)).getMaterial();
    }
    
    public boolean causesSuffocation(final IBlockState state) {
        return this.getMaterial(state).blocksMovement() && this.getDefaultState().isFullCube();
    }
    
    public boolean isPassable(final IBlockAccess world, final BlockPos pos) {
        return !this.getMaterial(world.getBlockState(pos)).blocksMovement();
    }
    
    public boolean canSpawnInBlock() {
        return super.canSpawnInBlock();
    }
    
    public EnumPushReaction getMobilityFlag(final IBlockState state) {
        return this.getMaterial(state).getMobilityFlag();
    }
    
    public boolean isTranslucent(final IBlockState state) {
        return !this.getMaterial(state).blocksLight();
    }
    
    public MapColor getMapColor(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return this.getMaterial(state).getMaterialMapColor();
    }
    
    public IBlockState getExtendedState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return state;
        }
        return (IBlockState)te.getExtendedState((Ic2BlockState.Ic2BlockStateInstance)state);
    }
    
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return;
        }
        te.onPlaced(stack, placer, EnumFacing.UP);
    }
    
    public RayTraceResult collisionRayTrace(final IBlockState state, final World world, final BlockPos pos, final Vec3d start, final Vec3d end) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return super.collisionRayTrace(state, world, pos, start, end);
        }
        return te.collisionRayTrace(start, end);
    }
    
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return super.getBoundingBox(state, world, pos);
        }
        return te.getVisualBoundingBox();
    }
    
    public AxisAlignedBB getSelectedBoundingBox(final IBlockState state, final World world, final BlockPos pos) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return super.getSelectedBoundingBox(state, world, pos);
        }
        return te.getOutlineBoundingBox().offset(pos);
    }
    
    public AxisAlignedBB getCollisionBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return super.getCollisionBoundingBox(state, world, pos);
        }
        return te.getPhysicsBoundingBox();
    }
    
    public void addCollisionBoxToList(final IBlockState state, final World world, final BlockPos pos, final AxisAlignedBB mask, final List<AxisAlignedBB> list, final Entity collidingEntity, final boolean isActualState) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            super.addCollisionBoxToList(state, world, pos, mask, (List)list, collidingEntity, isActualState);
        }
        else {
            te.addCollisionBoxesToList(mask, list, collidingEntity);
        }
    }
    
    public void onEntityCollidedWithBlock(final World world, final BlockPos pos, final IBlockState state, final Entity entity) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return;
        }
        te.onEntityCollision(entity);
    }
    
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return super.shouldSideBeRendered(state, world, pos, side);
        }
        return te.shouldSideBeRendered(side, pos.offset(side));
    }
    
    public boolean doesSideBlockRendering(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        final TileEntityBlock te = getTe(world, pos);
        return te != null && te.doesSideBlockRendering(face);
    }
    
    public boolean isNormalCube(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntityBlock te = getTe(world, pos);
        return te != null && te.isNormalCube();
    }
    
    public boolean isSideSolid(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        final TileEntityBlock te = getTe(world, pos);
        return te != null && te.isSideSolid(side);
    }
    
    public BlockFaceShape getBlockFaceShape(final IBlockAccess world, final IBlockState state, final BlockPos pos, final EnumFacing face) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return super.getBlockFaceShape(world, state, pos, face);
        }
        return te.getFaceShape(face);
    }
    
    public int getLightOpacity(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return this.getLightOpacity(state);
        }
        return te.getLightOpacity();
    }
    
    public int getLightValue(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return 0;
        }
        return te.getLightValue();
    }
    
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        return te != null && te.onActivated(player, hand, side, hitX, hitY, hitZ);
    }
    
    public void onBlockClicked(final World world, final BlockPos pos, final EntityPlayer player) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return;
        }
        te.onClicked(player);
    }
    
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return;
        }
        te.onNeighborChange(neighborBlock, neighborPos);
    }
    
    public int getWeakPower(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return 0;
        }
        return te.getWeakPower(side);
    }
    
    public boolean canConnectRedstone(final IBlockState state, final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
        final TileEntityBlock te = getTe(world, pos);
        return te != null && te.canConnectRedstone(side);
    }
    
    public boolean hasComparatorInputOverride(final IBlockState state) {
        return true;
    }
    
    public int getComparatorInputOverride(final IBlockState state, final World world, final BlockPos pos) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return 0;
        }
        return te.getComparatorInputOverride();
    }
    
    public boolean recolorBlock(final World world, final BlockPos pos, final EnumFacing side, final EnumDyeColor color) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        return te != null && te.recolor(side, color);
    }
    
    public void onBlockExploded(final World world, final BlockPos pos, final Explosion explosion) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te != null) {
            te.onExploded(explosion);
        }
        super.onBlockExploded(world, pos, explosion);
    }
    
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te != null) {
            te.onBlockBreak();
        }
        super.breakBlock(world, pos, state);
    }
    
    public boolean removedByPlayer(final IBlockState state, final World world, final BlockPos pos, final EntityPlayer player, final boolean willHarvest) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te != null) {
            if (!te.onRemovedByPlayer(player, willHarvest)) {
                return false;
            }
            if (willHarvest && !world.isRemote) {
                BlockTileEntity.removedTes[BlockTileEntity.nextRemovedTeIndex] = new WeakReference<TileEntityBlock>(te);
                BlockTileEntity.nextRemovedTeIndex = (BlockTileEntity.nextRemovedTeIndex + 1) % 4;
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    public boolean isFlammable(final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        final TileEntityBlock tile = getTe(world, pos);
        return tile != null && tile.isFlammable(face);
    }
    
    public float getPlayerRelativeBlockHardness(final IBlockState state, final EntityPlayer player, final World world, final BlockPos pos) {
        float ret = super.getPlayerRelativeBlockHardness(state, player, world, pos);
        if (!player.canHarvestBlock(state)) {
            final TileEntityBlock te = getTe((IBlockAccess)world, pos);
            if (te != null && te.teBlock.getHarvestTool() == TeBlock.HarvestTool.None) {
                ret *= 3.3333333f;
            }
        }
        return ret;
    }
    
    public boolean canHarvestBlock(final IBlockAccess world, final BlockPos pos, final EntityPlayer player) {
        final boolean ret = super.canHarvestBlock(world, pos, player);
        if (ret) {
            return ret;
        }
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return false;
        }
        switch (te.teBlock.getHarvestTool()) {
            case None: {
                return true;
            }
            case Wrench: {
                final ItemStack stack = player.getHeldItemMainhand();
                if (!stack.isEmpty()) {
                    final String tool = TeBlock.HarvestTool.Pickaxe.toolClass;
                    return stack.getItem().getHarvestLevel(stack, tool, player, world.getBlockState(pos)) >= TeBlock.HarvestTool.Pickaxe.level;
                }
                break;
            }
        }
        return false;
    }
    
    public String getHarvestTool(final IBlockState state) {
        if (state.getBlock() != this) {
            return null;
        }
        return ((MetaTeBlock)state.getValue((IProperty)this.typeProperty)).teBlock.getHarvestTool().toolClass;
    }
    
    public int getHarvestLevel(final IBlockState state) {
        if (state.getBlock() != this) {
            return 0;
        }
        return ((MetaTeBlock)state.getValue((IProperty)this.typeProperty)).teBlock.getHarvestTool().level;
    }
    
    public void getDrops(final NonNullList<ItemStack> list, final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
        list.addAll((Collection)this.getDrops(world, pos, state, fortune));
    }
    
    public List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
        TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            final World realWorld = Util.getWorld(world);
            if ((realWorld != null && realWorld.isRemote) || (realWorld == null && !IC2.platform.isSimulating())) {
                return new ArrayList<ItemStack>();
            }
            int idx = BlockTileEntity.nextRemovedTeIndex;
            do {
                final int checkIdx = (idx + 4 - 1) % 4;
                final WeakReference<TileEntityBlock> ref = BlockTileEntity.removedTes[checkIdx];
                final TileEntityBlock cTe;
                if (ref != null && (cTe = ref.get()) != null && (realWorld == null || cTe.getWorld() == realWorld) && cTe.getPos().equals((Object)pos)) {
                    te = cTe;
                    BlockTileEntity.removedTes[checkIdx] = null;
                    break;
                }
                idx = checkIdx;
            } while (idx != BlockTileEntity.nextRemovedTeIndex);
            if (te == null) {
                return new ArrayList<ItemStack>();
            }
        }
        final List<ItemStack> ret = new ArrayList<ItemStack>();
        boolean wasWrench = ConfigUtil.getBool(MainConfig.get(), "balance/ignoreWrenchRequirement");
        if (!wasWrench) {
            final EntityPlayer player = this.harvesters.get();
            if (player != null) {
                final ItemStack stack = player.getHeldItemMainhand();
                if (!stack.isEmpty()) {
                    final String tool = TeBlock.HarvestTool.Wrench.toolClass;
                    wasWrench |= (stack.getItem().getHarvestLevel(stack, tool, player, state) >= TeBlock.HarvestTool.Wrench.level);
                }
            }
        }
        ret.addAll(te.getSelfDrops(fortune, wasWrench));
        ret.addAll(te.getAuxDrops(fortune));
        return ret;
    }
    
    public float getBlockHardness(final IBlockState state, final World world, final BlockPos pos) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return 5.0f;
        }
        return te.getHardness();
    }
    
    public float getExplosionResistance(final World world, final BlockPos pos, final Entity exploder, final Explosion explosion) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return 10.0f;
        }
        return te.getExplosionResistance(exploder, explosion);
    }
    
    public boolean canEntityDestroy(final IBlockState state, final IBlockAccess world, final BlockPos pos, final Entity entity) {
        final TileEntityBlock te = getTe(world, pos);
        return te == null || te.canEntityDestroy(entity);
    }
    
    @Override
    public EnumFacing getFacing(final World world, final BlockPos pos) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return EnumFacing.DOWN;
        }
        return te.getFacing();
    }
    
    @Override
    public boolean canSetFacing(final World world, final BlockPos pos, final EnumFacing newDirection, final EntityPlayer player) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        return te != null && te.canSetFacingWrench(newDirection, player);
    }
    
    @Override
    public boolean setFacing(final World world, final BlockPos pos, final EnumFacing newDirection, final EntityPlayer player) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        return te != null && te.setFacingWrench(newDirection, player);
    }
    
    @Override
    public boolean wrenchCanRemove(final World world, final BlockPos pos, final EntityPlayer player) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        return te != null && te.wrenchCanRemove(player);
    }
    
    @Override
    public List<ItemStack> getWrenchDrops(final World world, final BlockPos pos, final IBlockState state, final TileEntity te, final EntityPlayer player, final int fortune) {
        if (!(te instanceof TileEntityBlock)) {
            return Collections.emptyList();
        }
        return ((TileEntityBlock)te).getWrenchDrops(player, fortune);
    }
    
    public EnumPlantType getPlantType(final IBlockAccess world, final BlockPos pos) {
        final TileEntityBlock te = getTe(world, pos);
        if (te == null) {
            return TileEntityBlock.noCrop;
        }
        return te.getPlantType();
    }
    
    public SoundType getSoundType(final IBlockState state, final World world, final BlockPos pos, final Entity entity) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return super.getSoundType(state, world, pos, entity);
        }
        return te.getBlockSound(entity);
    }
    
    private static TileEntityBlock getTe(final IBlockAccess world, final BlockPos pos) {
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityBlock) {
            return (TileEntityBlock)te;
        }
        return null;
    }
    
    public IBlockState getPlant(final IBlockAccess world, final BlockPos pos) {
        return world.getBlockState(pos);
    }
    
    public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing axis) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te != null) {
            final EnumFacing target = te.getFacing().rotateAround(axis.getAxis());
            if (te.getSupportedFacings().contains(target) && te.getFacing() != target) {
                te.setFacing(target);
                return true;
            }
        }
        return false;
    }
    
    public EnumFacing[] getValidRotations(final World world, final BlockPos pos) {
        final TileEntityBlock te = getTe((IBlockAccess)world, pos);
        if (te == null) {
            return null;
        }
        final Set<EnumFacing> facings = te.getSupportedFacings();
        return (EnumFacing[])(facings.isEmpty() ? null : ((EnumFacing[])facings.toArray(new EnumFacing[facings.size()])));
    }
    
    public boolean isIC2() {
        return this.item.identifier == TeBlock.invalid.getIdentifier();
    }
    
    public ItemBlockTileEntity getItem() {
        return this.item;
    }
    
    public final IProperty<MetaTeBlock> getTypeProperty() {
        final IProperty<MetaTeBlock> ret = (IProperty<MetaTeBlock>)((this.typeProperty != null) ? this.typeProperty : ((IProperty)BlockTileEntity.currentTypeProperty.get()));
        assert ret != null : "The type property can't be obtained.";
        return ret;
    }
    
    public final MaterialProperty getMaterialProperty() {
        final MaterialProperty ret = (this.materialProperty != null) ? this.materialProperty : BlockTileEntity.currentMaterialProperty.get();
        assert ret != null : "The matieral property can't be obtained.";
        return ret;
    }
    
    static {
        facingProperty = (IProperty)PropertyDirection.create("facing");
        currentTypeProperty = new UnstartingThreadLocal<IProperty<MetaTeBlock>>();
        currentMaterialProperty = new UnstartingThreadLocal<MaterialProperty>();
        transparentProperty = (IProperty)new SkippedBooleanProperty("transparent");
        removedTes = new WeakReference[4];
    }
}

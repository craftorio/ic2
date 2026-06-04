package ic2.core.block;

import com.google.common.base.Function;
import com.google.common.collect.UnmodifiableIterator;
import ic2.api.item.ITeBlockSpecialItem;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.MaterialProperty;
import ic2.core.block.state.SkippedBooleanProperty;
import ic2.core.init.MainConfig;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.model.ModelUtil;
import ic2.core.network.NetworkManager;
import ic2.core.ref.BlockName;
import ic2.core.ref.IMultiBlock;
import ic2.core.ref.MetaTeBlock;
import ic2.core.ref.MetaTeBlockProperty;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.ParticleUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class BlockTileEntity extends BlockBase implements IMultiBlock<ITeBlock>, IWrenchable, IPlantable {
  public final IProperty<MetaTeBlock> typeProperty;
  
  public final MaterialProperty materialProperty;
  
  static BlockTileEntity create(BlockName name, Collection<Material> materials) {
    BlockTileEntity ret = create(name.name(), TeBlock.invalid.getIdentifier(), materials);
    name.setInstance(ret);
    return ret;
  }
  
  static BlockTileEntity create(String name, ResourceLocation identifier, Collection<Material> materials) {
    currentTypeProperty.set(new MetaTeBlockProperty(identifier));
    currentMaterialProperty.set(new MaterialProperty(materials));
    BlockTileEntity ret = new BlockTileEntity(name, identifier);
    currentMaterialProperty.remove();
    currentTypeProperty.remove();
    return ret;
  }
  
  private BlockTileEntity(String name, final ResourceLocation identifier) {
    super((BlockName)null, TeBlockRegistry.getInfo(identifier).getDefaultMaterial());
    this.typeProperty = getTypeProperty();
    this.materialProperty = getMaterialProperty();
    ModContainer ic2 = Loader.instance().activeModContainer();
    Loader.instance().getActiveModList().stream().filter(mod -> identifier.func_110624_b().equals(mod.getModId())).findFirst().ifPresent(Loader.instance()::setActiveModContainer);
    register(name, identifier, (Function<Block, Item>)new Function<Block, Item>() {
          public Item apply(Block input) {
            return (Item)new ItemBlockTileEntity(input, identifier);
          }
        });
    Loader.instance().setActiveModContainer(ic2);
    func_180632_j(this.field_176227_L.func_177621_b()
        .func_177226_a((IProperty)this.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(this.field_149764_J))
        .func_177226_a(this.typeProperty, (Comparable)MetaTeBlockProperty.invalid)
        .func_177226_a(facingProperty, (Comparable)EnumFacing.DOWN)
        .func_177226_a(transparentProperty, Boolean.FALSE));
    this.item = (ItemBlockTileEntity)Item.getItemFromBlock(this);
    IC2.log.debug(LogCategory.Block, "Successfully built BlockTileEntity for identity " + identifier + '.');
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(BlockName name) {
    final ModelResourceLocation invalidLocation = ModelUtil.getTEBlockModelLocation(Util.getName(BlockName.te.getInstance()), this.field_176227_L.func_177621_b()
        .func_177226_a((IProperty)this.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(this.field_149764_J))
        .func_177226_a(this.typeProperty, (Comparable)MetaTeBlockProperty.invalid)
        .func_177226_a(facingProperty, (Comparable)EnumFacing.NORTH)
        .func_177226_a(transparentProperty, Boolean.FALSE));
    IC2.log.debug(LogCategory.Block, "Preparing to set models for " + this.item.identifier + '.');
    IC2.log.debug(LogCategory.Block, "Mapping " + func_176194_O().func_177619_a().size() + " states.");
    ModelLoader.setCustomStateMapper(this, new IStateMapper() {
          public Map<IBlockState, ModelResourceLocation> func_178130_a(Block block) {
            Map<IBlockState, ModelResourceLocation> ret = new IdentityHashMap<>();
            for (UnmodifiableIterator<IBlockState> unmodifiableIterator = block.func_176194_O().func_177619_a().iterator(); unmodifiableIterator.hasNext(); ) {
              IBlockState state = unmodifiableIterator.next();
              MetaTeBlock metaTeBlock = (MetaTeBlock)state.func_177229_b(BlockTileEntity.this.typeProperty);
              EnumFacing facing = (EnumFacing)state.func_177229_b(BlockTileEntity.facingProperty);
              if (metaTeBlock.teBlock.getSupportedFacings().contains(facing) || (facing == EnumFacing.DOWN && metaTeBlock.teBlock
                .getSupportedFacings().isEmpty())) {
                ret.put(state, ModelUtil.getTEBlockModelLocation(metaTeBlock.teBlock.getIdentifier(), state));
                continue;
              } 
              ret.put(state, invalidLocation);
            } 
            return ret;
          }
        });
    ModelLoader.setCustomMeshDefinition((Item)this.item, new ItemMeshDefinition() {
          public ModelResourceLocation func_178113_a(ItemStack stack) {
            ITeBlock teBlock = TeBlockRegistry.get(BlockTileEntity.this.item.identifier, stack.getItemDamage());
            if (teBlock == null)
              return invalidLocation; 
            if (teBlock instanceof ITeBlockSpecialItem && ((ITeBlockSpecialItem)teBlock).doesOverrideDefault(stack)) {
              ModelResourceLocation location = ((ITeBlockSpecialItem)teBlock).getModelLocation(stack);
              return (location == null) ? invalidLocation : location;
            } 
            IBlockState state = BlockTileEntity.this.getDefaultState().func_177226_a(BlockTileEntity.this.typeProperty, (Comparable)MetaTeBlockProperty.getState(teBlock)).func_177226_a(BlockTileEntity.facingProperty, (Comparable)BlockTileEntity.getItemFacing(teBlock));
            return ModelUtil.getTEBlockModelLocation(teBlock.getIdentifier(), state);
          }
        });
    boolean checkSpecialModels = TeBlockRegistry.getInfo(this.item.identifier).hasSpecialModels();
    for (MetaTeBlockProperty.MetaTePair block : MetaTeBlockProperty.getAllStates(this.item.identifier)) {
      if (block.hasItem()) {
        ModelResourceLocation model = checkSpecialModels ? getSpecialModel(block) : null;
        if (model == null) {
          IBlockState state = this.field_176227_L.func_177621_b().func_177226_a(this.typeProperty, (Comparable)block.inactive).func_177226_a(facingProperty, (Comparable)getItemFacing(block.getBlock()));
          model = ModelUtil.getTEBlockModelLocation(block.getIdentifier(), state);
        } 
        assert model != null;
        ModelBakery.registerItemVariants((Item)this.item, new ResourceLocation[] { (ResourceLocation)model });
      } 
      IC2.log.debug(LogCategory.Block, "Done item for " + this.item.identifier + ':' + block.getName() + '.');
    } 
  }
  
  private static EnumFacing getItemFacing(ITeBlock teBlock) {
    Set<EnumFacing> supported = teBlock.getSupportedFacings();
    if (supported.contains(EnumFacing.NORTH))
      return EnumFacing.NORTH; 
    if (!supported.isEmpty())
      return supported.iterator().next(); 
    return EnumFacing.DOWN;
  }
  
  @SideOnly(Side.CLIENT)
  private ModelResourceLocation getSpecialModel(MetaTeBlockProperty.MetaTePair blockTextures) {
    assert blockTextures.getBlock() instanceof ITeBlockSpecialItem;
    ITeBlockSpecialItem block = (ITeBlockSpecialItem)blockTextures.getBlock();
    ItemStack stack = new ItemStack((Item)this.item, 1, blockTextures.getBlock().getId());
    return block.doesOverrideDefault(stack) ? block.getModelLocation(stack) : null;
  }
  
  public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
    return ((Boolean)state.func_177229_b(transparentProperty)).booleanValue() ? ((layer == BlockRenderLayer.CUTOUT)) : ((layer == BlockRenderLayer.SOLID));
  }
  
  public boolean func_149716_u() {
    return true;
  }
  
  public boolean hasTileEntity(IBlockState state) {
    return true;
  }
  
  protected BlockStateContainer func_180661_e() {
    return (BlockStateContainer)new Ic2BlockState(this, new IProperty[] { getTypeProperty(), (IProperty)getMaterialProperty(), facingProperty, transparentProperty });
  }
  
  public int func_176201_c(IBlockState state) {
    int ret = this.materialProperty.getId((MaterialProperty.WrappedMaterial)state.func_177229_b((IProperty)this.materialProperty));
    if (ret < 0 || ret >= 8)
      throw new IllegalStateException("invalid material id: " + ret); 
    ret |= ((Boolean)state.func_177229_b(transparentProperty)).booleanValue() ? 8 : 0;
    return ret;
  }
  
  public IBlockState func_176203_a(int meta) {
    boolean isTransparent = ((meta & 0x8) != 0);
    int materialId = meta & 0x7;
    return getDefaultState().func_177226_a((IProperty)this.materialProperty, (Comparable)this.materialProperty.getMaterial(materialId)).func_177226_a(transparentProperty, Boolean.valueOf(isTransparent));
  }
  
  public IBlockState func_176221_a(IBlockState state, IBlockAccess world, BlockPos pos) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return state; 
    return te.getBlockState();
  }
  
  public String func_149739_a() {
    if (!isIC2())
      return this.item.identifier.func_110624_b() + '.' + this.item.identifier.func_110623_a(); 
    return super.func_149739_a();
  }
  
  public void func_149666_a(CreativeTabs tabs, NonNullList<ItemStack> list) {
    TeBlockRegistry.TeBlockInfo<?> info = TeBlockRegistry.getInfo(this.item.identifier);
    if (info.hasCreativeRegisterer()) {
      info.getCreativeRegisterer().addSubBlocks(list, this, this.item, tabs);
    } else if (tabs == IC2.tabIC2 || tabs == CreativeTabs.field_78027_g) {
      for (ITeBlock type : info.getTeBlocks()) {
        if (type.hasItem())
          list.add(getItemStack(type)); 
      } 
    } 
  }
  
  public Set<ITeBlock> getAllTypes() {
    return Collections.unmodifiableSet(TeBlockRegistry.getAll(this.item.identifier));
  }
  
  public ItemStack func_185473_a(World world, BlockPos pos, IBlockState state) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return StackUtil.emptyStack; 
    return te.getPickBlock((EntityPlayer)null, (RayTraceResult)null);
  }
  
  public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return StackUtil.emptyStack; 
    return te.getPickBlock(player, target);
  }
  
  public IBlockState getState(ITeBlock variant) {
    EnumFacing facing;
    if (variant == null)
      throw new IllegalArgumentException("invalid type: " + variant); 
    Set<EnumFacing> supportedFacings = variant.getSupportedFacings();
    if (supportedFacings.isEmpty()) {
      facing = EnumFacing.DOWN;
    } else if (supportedFacings.contains(EnumFacing.NORTH)) {
      facing = EnumFacing.NORTH;
    } else {
      facing = supportedFacings.iterator().next();
    } 
    return getDefaultState()
      .func_177226_a((IProperty)this.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(variant.getMaterial()))
      .func_177226_a(this.typeProperty, (Comparable)MetaTeBlockProperty.getState(variant))
      .func_177226_a(facingProperty, (Comparable)facing)
      .func_177226_a(transparentProperty, Boolean.valueOf(variant.isTransparent()));
  }
  
  public IBlockState getState(String variant) {
    return getState(TeBlockRegistry.get(variant));
  }
  
  public ItemStack getItemStack(ITeBlock type) {
    if (type == null)
      throw new IllegalArgumentException("invalid type: null"); 
    int id = type.getId();
    if (id != -1)
      return new ItemStack((Item)this.item, 1, id); 
    return null;
  }
  
  public ItemStack getItemStack(String variant) {
    if (variant == null)
      throw new IllegalArgumentException("Invalid ITeBlock type: null"); 
    ITeBlock type = TeBlockRegistry.get(variant);
    if (type == null)
      throw new IllegalArgumentException("Invalid ITeBlock type: " + variant); 
    return getItemStack(type);
  }
  
  public String getVariant(ItemStack stack) {
    if (stack == null)
      throw new NullPointerException("null stack"); 
    if (stack.getItem() != this.item)
      throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this.item + " (" + this + ")"); 
    ITeBlock type = TeBlockRegistry.get(this.item.identifier, stack.func_77960_j());
    if (type == null)
      throw new IllegalArgumentException("The stack " + stack + " doesn't reference any valid subtype"); 
    return type.getName();
  }
  
  public boolean func_149686_d(IBlockState state) {
    return false;
  }
  
  public boolean func_149662_c(IBlockState state) {
    return !((Boolean)state.func_177229_b(transparentProperty)).booleanValue();
  }
  
  public boolean canReplace(World world, BlockPos pos, EnumFacing side, ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return true; 
    if (stack.getItem() != this.item)
      return false; 
    ITeBlock type = TeBlockRegistry.get(this.item.identifier, stack.func_77960_j());
    if (type == null)
      return false; 
    TeBlock.ITePlaceHandler handler = type.getPlaceHandler();
    return (handler == null || handler.canReplace(world, pos, side, stack));
  }
  
  public boolean addLandingEffects(IBlockState state, WorldServer world, BlockPos pos, IBlockState state2, EntityLivingBase entity, int numberOfParticles) {
    if (world.isRemote)
      throw new IllegalStateException(); 
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return super.addLandingEffects(state, world, pos, state2, entity, numberOfParticles); 
    if (te.clientNeedsExtraModelInfo()) {
      ((NetworkManager)IC2.network.get(true)).initiateTeblockLandEffect((World)world, pos, entity.posX, entity.posY, entity.posZ, numberOfParticles, te.teBlock);
    } else {
      ((NetworkManager)IC2.network.get(true)).initiateTeblockLandEffect((World)world, entity.posX, entity.posY, entity.posZ, numberOfParticles, te.teBlock);
    } 
    return true;
  }
  
  public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
    if (world.isRemote)
      return true; 
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return super.addRunningEffects(state, world, pos, entity); 
    if (te.clientNeedsExtraModelInfo()) {
      ((NetworkManager)IC2.network.get(true)).initiateTeblockRunEffect(world, pos, entity, te.teBlock);
    } else {
      ((NetworkManager)IC2.network.get(true)).initiateTeblockRunEffect(world, entity, te.teBlock);
    } 
    return true;
  }
  
  @SideOnly(Side.CLIENT)
  public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
    BlockPos pos = target.getBlockPos();
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return super.addHitEffects(state, world, target, manager); 
    ParticleUtil.spawnBlockHitParticles(te, target.field_178784_b, te.clientNeedsExtraModelInfo());
    return true;
  }
  
  @SideOnly(Side.CLIENT)
  public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te != null && te.clientNeedsExtraModelInfo()) {
      ParticleUtil.spawnBlockBreakParticles(te);
      return true;
    } 
    return super.addDestroyEffects(world, pos, manager);
  }
  
  public Material func_149688_o(IBlockState state) {
    return ((MaterialProperty.WrappedMaterial)state.func_177229_b((IProperty)this.materialProperty)).getMaterial();
  }
  
  public boolean func_176214_u(IBlockState state) {
    return (func_149688_o(state).func_76230_c() && getDefaultState().func_185917_h());
  }
  
  public boolean func_176205_b(IBlockAccess world, BlockPos pos) {
    return !func_149688_o(world.getBlockState(pos)).func_76230_c();
  }
  
  public boolean func_181623_g() {
    return super.func_181623_g();
  }
  
  public EnumPushReaction func_149656_h(IBlockState state) {
    return func_149688_o(state).func_186274_m();
  }
  
  public boolean func_149751_l(IBlockState state) {
    return !func_149688_o(state).func_76228_b();
  }
  
  public MapColor func_180659_g(IBlockState state, IBlockAccess world, BlockPos pos) {
    return func_149688_o(state).func_151565_r();
  }
  
  public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return state; 
    return (IBlockState)te.getExtendedState((Ic2BlockState.Ic2BlockStateInstance)state);
  }
  
  public void func_180633_a(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return; 
    te.onPlaced(stack, placer, EnumFacing.UP);
  }
  
  public RayTraceResult func_180636_a(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return super.func_180636_a(state, world, pos, start, end); 
    return te.collisionRayTrace(start, end);
  }
  
  public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess world, BlockPos pos) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return super.func_185496_a(state, world, pos); 
    return te.getVisualBoundingBox();
  }
  
  public AxisAlignedBB func_180640_a(IBlockState state, World world, BlockPos pos) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return super.func_180640_a(state, world, pos); 
    return te.getOutlineBoundingBox().func_186670_a(pos);
  }
  
  public AxisAlignedBB func_180646_a(IBlockState state, IBlockAccess world, BlockPos pos) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return super.func_180646_a(state, world, pos); 
    return te.getPhysicsBoundingBox();
  }
  
  public void func_185477_a(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean isActualState) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null) {
      super.func_185477_a(state, world, pos, mask, list, collidingEntity, isActualState);
    } else {
      te.addCollisionBoxesToList(mask, list, collidingEntity);
    } 
  }
  
  public void func_180634_a(World world, BlockPos pos, IBlockState state, Entity entity) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return; 
    te.onEntityCollision(entity);
  }
  
  @SideOnly(Side.CLIENT)
  public boolean func_176225_a(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return super.func_176225_a(state, world, pos, side); 
    return te.shouldSideBeRendered(side, pos.offset(side));
  }
  
  public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return false; 
    return te.doesSideBlockRendering(face);
  }
  
  public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return false; 
    return te.isNormalCube();
  }
  
  public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return false; 
    return te.isSideSolid(side);
  }
  
  public BlockFaceShape func_193383_a(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return super.func_193383_a(world, state, pos, face); 
    return te.getFaceShape(face);
  }
  
  public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return func_149717_k(state); 
    return te.getLightOpacity();
  }
  
  public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return 0; 
    return te.getLightValue();
  }
  
  public boolean func_180639_a(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (player.func_70093_af())
      return false; 
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return false; 
    return te.onActivated(player, hand, side, hitX, hitY, hitZ);
  }
  
  public void func_180649_a(World world, BlockPos pos, EntityPlayer player) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return; 
    te.onClicked(player);
  }
  
  public void func_189540_a(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return; 
    te.onNeighborChange(neighborBlock, neighborPos);
  }
  
  public int func_180656_a(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return 0; 
    return te.getWeakPower(side);
  }
  
  public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return false; 
    return te.canConnectRedstone(side);
  }
  
  public boolean func_149740_M(IBlockState state) {
    return true;
  }
  
  public int func_180641_l(IBlockState state, World world, BlockPos pos) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return 0; 
    return te.getComparatorInputOverride();
  }
  
  public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return false; 
    return te.recolor(side, color);
  }
  
  public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te != null)
      te.onExploded(explosion); 
    super.onBlockExploded(world, pos, explosion);
  }
  
  public void func_180663_b(World world, BlockPos pos, IBlockState state) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te != null)
      te.onBlockBreak(); 
    super.func_180663_b(world, pos, state);
  }
  
  public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te != null) {
      if (!te.onRemovedByPlayer(player, willHarvest))
        return false; 
      if (willHarvest && !world.isRemote) {
        removedTes[nextRemovedTeIndex] = new WeakReference<>(te);
        nextRemovedTeIndex = (nextRemovedTeIndex + 1) % 4;
      } 
    } 
    return super.removedByPlayer(state, world, pos, player, willHarvest);
  }
  
  public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
    TileEntityBlock tile = getTe(world, pos);
    if (tile == null)
      return false; 
    return tile.isFlammable(face);
  }
  
  public float func_180647_a(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
    float ret = super.func_180647_a(state, player, world, pos);
    if (!player.func_184823_b(state)) {
      TileEntityBlock te = getTe((IBlockAccess)world, pos);
      if (te != null && te.teBlock.getHarvestTool() == TeBlock.HarvestTool.None)
        ret *= 3.3333333F; 
    } 
    return ret;
  }
  
  public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
    ItemStack stack;
    boolean ret = super.canHarvestBlock(world, pos, player);
    if (ret)
      return ret; 
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return false; 
    switch (te.teBlock.getHarvestTool()) {
      case None:
        return true;
      case Wrench:
        stack = player.func_184614_ca();
        if (!stack.func_190926_b()) {
          String tool = TeBlock.HarvestTool.Pickaxe.toolClass;
          return (stack.getItem().getHarvestLevel(stack, tool, player, world.getBlockState(pos)) >= TeBlock.HarvestTool.Pickaxe.level);
        } 
        break;
    } 
    return false;
  }
  
  public String getHarvestTool(IBlockState state) {
    if (state.getBlock() != this)
      return null; 
    return (((MetaTeBlock)state.func_177229_b(this.typeProperty)).teBlock.getHarvestTool()).toolClass;
  }
  
  public int getHarvestLevel(IBlockState state) {
    if (state.getBlock() != this)
      return 0; 
    return (((MetaTeBlock)state.func_177229_b(this.typeProperty)).teBlock.getHarvestTool()).level;
  }
  
  public void getDrops(NonNullList<ItemStack> list, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    list.addAll(getDrops(world, pos, state, fortune));
  }
  
  public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    int i;
    TileEntityBlock te = getTe(world, pos);
    if (te == null) {
      World realWorld = Util.getWorld(world);
      if ((realWorld != null && realWorld.isRemote) || (realWorld == null && 
        !IC2.platform.isSimulating()))
        return new ArrayList<>(); 
      i = nextRemovedTeIndex;
      do {
        int checkIdx = (i + 4 - 1) % 4;
        WeakReference<TileEntityBlock> ref = removedTes[checkIdx];
        TileEntityBlock cTe;
        if (ref != null && (
          cTe = ref.get()) != null && (realWorld == null || cTe
          .getWorld() == realWorld) && cTe
          .getPos().equals(pos)) {
          te = cTe;
          removedTes[checkIdx] = null;
          break;
        } 
        i = checkIdx;
      } while (i != nextRemovedTeIndex);
      if (te == null)
        return new ArrayList<>(); 
    } 
    List<ItemStack> ret = new ArrayList<>();
    boolean wasWrench = ConfigUtil.getBool(MainConfig.get(), "balance/ignoreWrenchRequirement");
    if (!wasWrench) {
      EntityPlayer player = this.harvesters.get();
      if (player != null) {
        ItemStack stack = player.func_184614_ca();
        if (!stack.func_190926_b()) {
          String tool = TeBlock.HarvestTool.Wrench.toolClass;
          i = wasWrench | ((stack.getItem().getHarvestLevel(stack, tool, player, state) >= TeBlock.HarvestTool.Wrench.level) ? 1 : 0);
        } 
      } 
    } 
    ret.addAll(te.getSelfDrops(fortune, i));
    ret.addAll(te.getAuxDrops(fortune));
    return ret;
  }
  
  public float func_176195_g(IBlockState state, World world, BlockPos pos) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return 5.0F; 
    return te.getHardness();
  }
  
  public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return 10.0F; 
    return te.getExplosionResistance(exploder, explosion);
  }
  
  public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return true; 
    return te.canEntityDestroy(entity);
  }
  
  public EnumFacing getFacing(World world, BlockPos pos) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return EnumFacing.DOWN; 
    return te.getFacing();
  }
  
  public boolean canSetFacing(World world, BlockPos pos, EnumFacing newDirection, EntityPlayer player) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return false; 
    return te.canSetFacingWrench(newDirection, player);
  }
  
  public boolean setFacing(World world, BlockPos pos, EnumFacing newDirection, EntityPlayer player) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return false; 
    return te.setFacingWrench(newDirection, player);
  }
  
  public boolean wrenchCanRemove(World world, BlockPos pos, EntityPlayer player) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return false; 
    return te.wrenchCanRemove(player);
  }
  
  public List<ItemStack> getWrenchDrops(World world, BlockPos pos, IBlockState state, TileEntity te, EntityPlayer player, int fortune) {
    if (!(te instanceof TileEntityBlock))
      return Collections.emptyList(); 
    return ((TileEntityBlock)te).getWrenchDrops(player, fortune);
  }
  
  public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
    TileEntityBlock te = getTe(world, pos);
    if (te == null)
      return TileEntityBlock.noCrop; 
    return te.getPlantType();
  }
  
  public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return super.getSoundType(state, world, pos, entity); 
    return te.getBlockSound(entity);
  }
  
  private static TileEntityBlock getTe(IBlockAccess world, BlockPos pos) {
    TileEntity te = world.getTileEntity(pos);
    if (te instanceof TileEntityBlock)
      return (TileEntityBlock)te; 
    return null;
  }
  
  public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
    return world.getBlockState(pos);
  }
  
  public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te != null) {
      EnumFacing target = te.getFacing().rotateAround(axis.getAxis());
      if (te.getSupportedFacings().contains(target) && te.getFacing() != target) {
        te.setFacing(target);
        return true;
      } 
    } 
    return false;
  }
  
  public EnumFacing[] getValidRotations(World world, BlockPos pos) {
    TileEntityBlock te = getTe((IBlockAccess)world, pos);
    if (te == null)
      return null; 
    Set<EnumFacing> facings = te.getSupportedFacings();
    return !facings.isEmpty() ? facings.<EnumFacing>toArray(new EnumFacing[facings.size()]) : null;
  }
  
  public boolean isIC2() {
    return (this.item.identifier == TeBlock.invalid.getIdentifier());
  }
  
  public ItemBlockTileEntity getItem() {
    return this.item;
  }
  
  public final IProperty<MetaTeBlock> getTypeProperty() {
    IProperty<MetaTeBlock> ret = (this.typeProperty != null) ? this.typeProperty : currentTypeProperty.get();
    assert ret != null : "The type property can't be obtained.";
    return ret;
  }
  
  public final MaterialProperty getMaterialProperty() {
    MaterialProperty ret = (this.materialProperty != null) ? this.materialProperty : currentMaterialProperty.get();
    assert ret != null : "The matieral property can't be obtained.";
    return ret;
  }
  
  public static final IProperty<EnumFacing> facingProperty = (IProperty<EnumFacing>)PropertyDirection.func_177714_a("facing");
  
  private static final ThreadLocal<IProperty<MetaTeBlock>> currentTypeProperty = new UnstartingThreadLocal<>();
  
  private static final ThreadLocal<MaterialProperty> currentMaterialProperty = new UnstartingThreadLocal<>();
  
  public static final IProperty<Boolean> transparentProperty = (IProperty<Boolean>)new SkippedBooleanProperty("transparent");
  
  private final ItemBlockTileEntity item;
  
  private static final int removedTesToKeep = 4;
  
  private static final WeakReference<TileEntityBlock>[] removedTes = (WeakReference<TileEntityBlock>[])new WeakReference[4];
  
  private static int nextRemovedTeIndex;
}

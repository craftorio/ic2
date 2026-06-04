package ic2.core.block;

import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkUpdateListener;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.IWorldTickCallback;
import ic2.core.block.comp.BasicRedstoneComponent;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.Components;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.MaterialProperty;
import ic2.core.block.type.ResourceBlock;
import ic2.core.gui.dynamic.IGuiConditionProvider;
import ic2.core.init.Localization;
import ic2.core.model.ModelComparator;
import ic2.core.network.NetworkManager;
import ic2.core.ref.BlockName;
import ic2.core.ref.MetaTeBlock;
import ic2.core.ref.MetaTeBlockProperty;
import ic2.core.ref.TeBlock;
import ic2.core.util.AabbUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.mutable.MutableObject;

public abstract class TileEntityBlock extends TileEntity implements INetworkDataProvider, INetworkUpdateListener, ITickable, IGuiConditionProvider {
  public static final String teBlockName = "teBlk";
  
  public static final String oldMarker = "Old-";
  
  protected static final int lightOpacityTranslucent = 0;
  
  protected static final int lightOpacityOpaque = 255;
  
  public static <T extends TileEntityBlock> T instantiate(Class<T> cls) {
    try {
      return cls.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public TileEntityBlock() {
    this.active = false;
    this.facing = (byte)EnumFacing.DOWN.ordinal();
    this.loadState = 0;
    ITeBlock teb = TeBlockRegistry.get((Class)getClass());
    this.teBlock = (teb == null) ? (ITeBlock)TeBlock.invalid : teb;
    this.block = TeBlockRegistry.get(this.teBlock.getIdentifier());
  }
  
  public final BlockTileEntity func_145838_q() {
    return this.block;
  }
  
  public final IBlockState getBlockState() {
    return this.block.getDefaultState().func_177226_a((IProperty)this.block.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(this.teBlock.getMaterial())).func_177226_a(this.block.typeProperty, (Comparable)MetaTeBlockProperty.getState(this.teBlock, getActive())).func_177226_a(BlockTileEntity.facingProperty, (Comparable)getFacing()).func_177226_a(BlockTileEntity.transparentProperty, Boolean.valueOf(this.teBlock.isTransparent()));
  }
  
  public final void invalidate() {
    if (this.loadState == 2) {
      if (debugLoad)
        IC2.log.debug(LogCategory.Block, "TE onUnloaded (invalidate) for %s at %s.", new Object[] { this, Util.formatPosition(this) }); 
      onUnloaded();
    } else {
      if (debugLoad)
        IC2.log.debug(LogCategory.Block, "Skipping TE onUnloaded (invalidate) for %s at %s, state: %d.", new Object[] { this, Util.formatPosition(this), Byte.valueOf(this.loadState) }); 
      this.loadState = 3;
    } 
    super.invalidate();
  }
  
  public final void onChunkUnload() {
    if (this.loadState == 2) {
      if (debugLoad)
        IC2.log.debug(LogCategory.Block, "TE onUnloaded (chunk unload) for %s at %s.", new Object[] { this, Util.formatPosition(this) }); 
      onUnloaded();
    } else {
      if (debugLoad)
        IC2.log.debug(LogCategory.Block, "Skipping TE onUnloaded (chunk unload) for %s at %s, state: %d.", new Object[] { this, Util.formatPosition(this), Byte.valueOf(this.loadState) }); 
      this.loadState = 3;
    } 
    super.onChunkUnload();
  }
  
  public final void func_145829_t() {
    super.func_145829_t();
    World world = getWorld();
    if (world == null || this.field_174879_c == null)
      throw new IllegalStateException("no world/pos"); 
    if (this.loadState != 0 && this.loadState != 3)
      throw new IllegalStateException("invalid load state: " + this.loadState); 
    this.loadState = 1;
    IC2.tickHandler.requestSingleWorldTick(world, new IWorldTickCallback() {
          public void onTick(World world) {
            IBlockState state;
            if (world == TileEntityBlock.this.getWorld() && TileEntityBlock.this.field_174879_c != null && !TileEntityBlock.this.func_145837_r() && TileEntityBlock.this.loadState == 1 && world.func_175667_e(TileEntityBlock.this.field_174879_c) && (state = world.getBlockState(TileEntityBlock.this.field_174879_c)).getBlock() == TileEntityBlock.this.block && world.func_175625_s(TileEntityBlock.this.field_174879_c) == TileEntityBlock.this) {
              Material expectedMaterial = TileEntityBlock.this.teBlock.getMaterial();
              if (((MaterialProperty.WrappedMaterial)state.func_177229_b((IProperty)TileEntityBlock.this.block.materialProperty)).getMaterial() != expectedMaterial) {
                if (TileEntityBlock.debugLoad)
                  IC2.log.debug(LogCategory.Block, "Adjusting material for %s at %s.", new Object[] { this.this$0, Util.formatPosition(this.this$0) }); 
                world.func_180501_a(TileEntityBlock.this.field_174879_c, state.func_177226_a((IProperty)TileEntityBlock.this.block.materialProperty, (Comparable)MaterialProperty.WrappedMaterial.get(expectedMaterial)), 0);
                assert world.func_175625_s(TileEntityBlock.this.field_174879_c) == TileEntityBlock.this;
              } 
              if (TileEntityBlock.debugLoad)
                IC2.log.debug(LogCategory.Block, "TE onLoaded for %s at %s.", new Object[] { this.this$0, Util.formatPosition(this.this$0) }); 
              TileEntityBlock.this.onLoaded();
            } else if (TileEntityBlock.debugLoad) {
              IC2.log.debug(LogCategory.Block, "Skipping TE init for %s at %s.", new Object[] { this.this$0, Util.formatPosition(this.this$0) });
            } 
          }
        });
  }
  
  public final void onLoad() {}
  
  protected void onLoaded() {
    if (this.loadState != 1)
      throw new IllegalStateException("invalid load state: " + this.loadState); 
    this.loadState = 2;
    this.enableWorldTick = requiresWorldTick();
    if (this.components != null)
      for (TileEntityComponent component : this.components.values()) {
        component.onLoaded();
        if (component.enableWorldTick()) {
          if (this.updatableComponents == null)
            this.updatableComponents = new ArrayList<>(4); 
          this.updatableComponents.add(component);
        } 
      }  
    if (!this.enableWorldTick && this.updatableComponents == null)
      (getWorld()).field_175730_i.remove(this); 
  }
  
  protected void onUnloaded() {
    if (this.loadState == 3)
      throw new IllegalStateException("invalid load state: " + this.loadState); 
    this.loadState = 3;
    if (this.components != null)
      for (TileEntityComponent component : this.components.values())
        component.onUnloaded();  
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    if (!getSupportedFacings().isEmpty()) {
      byte facingValue = nbt.func_74771_c("facing");
      if (facingValue >= 0 && facingValue < EnumFacing.field_82609_l.length && getSupportedFacings().contains(EnumFacing.field_82609_l[facingValue])) {
        this.facing = facingValue;
      } else if (!getSupportedFacings().isEmpty()) {
        this.facing = (byte)((EnumFacing)getSupportedFacings().iterator().next()).ordinal();
      } else {
        this.facing = (byte)EnumFacing.DOWN.ordinal();
      } 
    } 
    this.active = nbt.func_74767_n("active");
    if (this.components != null && nbt.func_150297_b("components", 10)) {
      NBTTagCompound componentsNbt = nbt.getCompoundTag("components");
      for (String name : componentsNbt.func_150296_c()) {
        Class<? extends TileEntityComponent> cls = Components.getClass(name);
        TileEntityComponent component;
        if (cls == null || (component = getComponent((Class)cls)) == null) {
          IC2.log.warn(LogCategory.Block, "Can't find component %s while loading %s.", new Object[] { name, this });
          continue;
        } 
        NBTTagCompound componentNbt = componentsNbt.getCompoundTag(name);
        component.readFromNbt(componentNbt);
      } 
    } 
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    if (!getSupportedFacings().isEmpty())
      nbt.func_74774_a("facing", this.facing); 
    nbt.func_74757_a("active", this.active);
    if (this.components != null) {
      NBTTagCompound componentsNbt = null;
      for (TileEntityComponent component : this.components.values()) {
        NBTTagCompound componentNbt = component.writeToNbt();
        if (componentNbt == null)
          continue; 
        if (componentsNbt == null) {
          componentsNbt = new NBTTagCompound();
          nbt.setTag("components", (NBTBase)componentsNbt);
        } 
        componentsNbt.setTag(Components.getId(component.getClass()), (NBTBase)componentNbt);
      } 
    } 
    return nbt;
  }
  
  public NBTTagCompound func_189517_E_() {
    ((NetworkManager)IC2.network.get(true)).sendInitialData(this);
    return emptyNbt;
  }
  
  public SPacketUpdateTileEntity func_189518_D_() {
    ((NetworkManager)IC2.network.get(true)).sendInitialData(this);
    return null;
  }
  
  public final void func_73660_a() {
    if (this.loadState != 2)
      return; 
    if (this.updatableComponents != null)
      for (TileEntityComponent component : this.updatableComponents)
        component.onWorldTick();  
    if (this.enableWorldTick)
      if ((getWorld()).isRemote) {
        updateEntityClient();
      } else {
        updateEntityServer();
      }  
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {}
  
  protected void updateEntityServer() {}
  
  public List<String> getNetworkedFields() {
    List<String> ret = new ArrayList<>(3);
    ret.add("teBlk=" + (isOldVersion() ? "Old-" : "") + this.teBlock.getName());
    ret.add("active");
    ret.add("facing");
    return ret;
  }
  
  private boolean isOldVersion() {
    assert func_145830_o() && !(getWorld()).isRemote;
    return (this.teBlock.getTeClass() != getClass());
  }
  
  public void onNetworkUpdate(String field) {
    if ((field.equals("active") && hasActiveTexture()) || field.equals("facing"))
      rerender(); 
  }
  
  @SideOnly(Side.CLIENT)
  private boolean hasActiveTexture() {
    if (!this.teBlock.hasActive())
      return false; 
    IBlockState stateA = getBlockState();
    IBlockState stateB = stateA.func_177226_a(this.block.typeProperty, (Comparable)MetaTeBlockProperty.getState(this.teBlock, !((MetaTeBlock)stateA.func_177229_b(this.block.typeProperty)).active));
    return !ModelComparator.isEqual(stateA, stateB, getWorld(), getPos());
  }
  
  protected Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state) {
    return state;
  }
  
  public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
    World world = getWorld();
    if (!world.isRemote);
    facing = getPlacementFacing(placer, facing);
    if (facing != getFacing())
      setFacing(facing); 
    if (world.isRemote)
      rerender(); 
  }
  
  protected RayTraceResult collisionRayTrace(Vec3d start, Vec3d end) {
    Vec3d startNormalized = start.func_178786_a(this.field_174879_c.getX(), this.field_174879_c.getY(), this.field_174879_c.getZ());
    double lengthSq = Util.square(end.field_72450_a - start.field_72450_a) + Util.square(end.field_72448_b - start.field_72448_b) + Util.square(end.field_72449_c - start.field_72449_c);
    double lengthInv = 1.0D / Math.sqrt(lengthSq);
    Vec3d direction = new Vec3d((end.field_72450_a - start.field_72450_a) * lengthInv, (end.field_72448_b - start.field_72448_b) * lengthInv, (end.field_72449_c - start.field_72449_c) * lengthInv);
    double minDistanceSq = lengthSq;
    Vec3d minIntersection = null;
    EnumFacing minIntersectionSide = null;
    MutableObject<Vec3d> intersectionOut = new MutableObject();
    for (AxisAlignedBB aabb : getAabbs(false)) {
      EnumFacing side = AabbUtil.getIntersection(startNormalized, direction, aabb, intersectionOut);
      if (side == null)
        continue; 
      Vec3d intersection = (Vec3d)intersectionOut.getValue();
      double distanceSq = Util.square(intersection.field_72450_a - startNormalized.field_72450_a) + Util.square(intersection.field_72448_b - startNormalized.field_72448_b) + Util.square(intersection.field_72449_c - startNormalized.field_72449_c);
      if (distanceSq < minDistanceSq) {
        minDistanceSq = distanceSq;
        minIntersection = intersection;
        minIntersectionSide = side;
      } 
    } 
    if (minIntersection == null)
      return null; 
    return new RayTraceResult(minIntersection.addVector(this.field_174879_c.getX(), this.field_174879_c.getY(), this.field_174879_c.getZ()), minIntersectionSide, this.field_174879_c);
  }
  
  public AxisAlignedBB getVisualBoundingBox() {
    return getAabb(false);
  }
  
  protected AxisAlignedBB getPhysicsBoundingBox() {
    return getAabb(true);
  }
  
  protected AxisAlignedBB getOutlineBoundingBox() {
    return getVisualBoundingBox();
  }
  
  protected void addCollisionBoxesToList(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
    AxisAlignedBB maskNormalized = mask.func_72317_d(-this.field_174879_c.getX(), -this.field_174879_c.getY(), -this.field_174879_c.getZ());
    for (AxisAlignedBB aabb : getAabbs(true)) {
      if (!aabb.func_72326_a(maskNormalized))
        continue; 
      list.add(aabb.func_186670_a(this.field_174879_c));
    } 
  }
  
  private AxisAlignedBB getAabb(boolean forCollision) {
    List<AxisAlignedBB> aabbs = getAabbs(forCollision);
    if (aabbs.isEmpty())
      throw new RuntimeException("No AABBs for " + this); 
    if (aabbs.size() == 1)
      return aabbs.get(0); 
    double zS = Double.POSITIVE_INFINITY, yS = zS, xS = yS;
    double zE = Double.NEGATIVE_INFINITY, yE = zE, xE = yE;
    for (AxisAlignedBB aabb : aabbs) {
      xS = Math.min(xS, aabb.field_72340_a);
      yS = Math.min(yS, aabb.field_72338_b);
      zS = Math.min(zS, aabb.field_72339_c);
      xE = Math.max(xE, aabb.field_72336_d);
      yE = Math.max(yE, aabb.field_72337_e);
      zE = Math.max(zE, aabb.field_72334_f);
    } 
    return new AxisAlignedBB(xS, yS, zS, xE, yE, zE);
  }
  
  protected void onEntityCollision(Entity entity) {}
  
  @SideOnly(Side.CLIENT)
  protected boolean shouldSideBeRendered(EnumFacing side, BlockPos otherPos) {
    AxisAlignedBB aabb = getVisualBoundingBox();
    if (aabb != defaultAabbs)
      switch (side) {
        case Self:
          if (aabb.field_72338_b > 0.0D)
            return true; 
          break;
        case None:
          if (aabb.field_72337_e < 1.0D)
            return true; 
          break;
        case Generator:
          if (aabb.field_72339_c > 0.0D)
            return true; 
          break;
        case Machine:
          if (aabb.field_72334_f < 1.0D)
            return true; 
          break;
        case AdvMachine:
          if (aabb.field_72340_a > 0.0D)
            return true; 
          break;
        case null:
          if (aabb.field_72336_d < 1.0D)
            return true; 
          break;
      }  
    World world = getWorld();
    return !world.getBlockState(otherPos).doesSideBlockRendering((IBlockAccess)world, otherPos, side.func_176734_d());
  }
  
  protected boolean doesSideBlockRendering(EnumFacing side) {
    return checkSide(getAabbs(false), side, false);
  }
  
  private static boolean checkSide(List<AxisAlignedBB> aabbs, EnumFacing side, boolean strict) {
    if (aabbs == defaultAabbs)
      return true; 
    int dx = side.getFrontOffsetX();
    int dy = side.getFrontOffsetY();
    int dz = side.getFrontOffsetZ();
    int xS = (dx + 1) / 2;
    int yS = (dy + 1) / 2;
    int zS = (dz + 1) / 2;
    int xE = (dx + 2) / 2;
    int yE = (dy + 2) / 2;
    int zE = (dz + 2) / 2;
    if (strict)
      for (AxisAlignedBB aabb : aabbs) {
        switch (side) {
          case Self:
            if (aabb.field_72338_b < 0.0D)
              return false; 
          case None:
            if (aabb.field_72337_e > 1.0D)
              return false; 
          case Generator:
            if (aabb.field_72339_c < 0.0D)
              return false; 
          case Machine:
            if (aabb.field_72334_f > 1.0D)
              return false; 
          case AdvMachine:
            if (aabb.field_72340_a < 0.0D)
              return false; 
          case null:
            if (aabb.field_72336_d > 1.0D)
              return false; 
        } 
      }  
    for (AxisAlignedBB aabb : aabbs) {
      if (aabb.field_72340_a <= xS && aabb.field_72338_b <= yS && aabb.field_72339_c <= zS && aabb.field_72336_d >= xE && aabb.field_72337_e >= yE && aabb.field_72334_f >= zE)
        return true; 
    } 
    return false;
  }
  
  protected boolean isNormalCube() {
    List<AxisAlignedBB> aabbs = getAabbs(false);
    if (aabbs == defaultAabbs)
      return true; 
    if (aabbs.size() != 1)
      return false; 
    AxisAlignedBB aabb = aabbs.get(0);
    return (aabb.field_72340_a <= 0.0D && aabb.field_72338_b <= 0.0D && aabb.field_72339_c <= 0.0D && aabb.field_72336_d >= 1.0D && aabb.field_72337_e >= 1.0D && aabb.field_72334_f >= 1.0D);
  }
  
  protected boolean isSideSolid(EnumFacing side) {
    return checkSide(getAabbs(false), side, true);
  }
  
  protected BlockFaceShape getFaceShape(EnumFacing face) {
    return isSideSolid(face) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
  }
  
  protected int getLightOpacity() {
    return isNormalCube() ? 255 : 0;
  }
  
  protected int getLightValue() {
    return 0;
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (this instanceof IHasGui) {
      if (!(getWorld()).isRemote)
        return IC2.platform.launchGui(player, (IHasGui)this); 
      return true;
    } 
    return false;
  }
  
  protected void onClicked(EntityPlayer player) {}
  
  protected void onNeighborChange(Block neighbor, BlockPos neighborPos) {
    if (this.components != null)
      for (TileEntityComponent component : this.components.values())
        component.onNeighborChange(neighbor, neighborPos);  
  }
  
  protected int getWeakPower(EnumFacing side) {
    BasicRedstoneComponent component = (BasicRedstoneComponent)getComponent(RedstoneEmitter.class);
    return (component == null) ? 0 : component.getLevel();
  }
  
  protected boolean canConnectRedstone(EnumFacing side) {
    return (hasComponent((Class)RedstoneEmitter.class) || hasComponent((Class)Redstone.class));
  }
  
  protected int getComparatorInputOverride() {
    BasicRedstoneComponent component = (BasicRedstoneComponent)getComponent(ComparatorEmitter.class);
    return (component == null) ? 0 : component.getLevel();
  }
  
  protected boolean recolor(EnumFacing side, EnumDyeColor mcColor) {
    return false;
  }
  
  protected void onExploded(Explosion explosion) {}
  
  protected void onBlockBreak() {}
  
  protected boolean onRemovedByPlayer(EntityPlayer player, boolean willHarvest) {
    return true;
  }
  
  protected boolean isFlammable(EnumFacing face) {
    return true;
  }
  
  protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target) {
    return this.block.getItemStack(this.teBlock);
  }
  
  protected boolean canHarvest(EntityPlayer player, boolean defaultValue) {
    return defaultValue;
  }
  
  protected List<ItemStack> getSelfDrops(int fortune, boolean wrench) {
    ItemStack drop = getPickBlock((EntityPlayer)null, (RayTraceResult)null);
    drop = adjustDrop(drop, wrench);
    if (drop == null)
      return Collections.emptyList(); 
    return Arrays.asList(new ItemStack[] { drop });
  }
  
  protected List<ItemStack> getAuxDrops(int fortune) {
    return Collections.emptyList();
  }
  
  protected float getHardness() {
    return this.teBlock.getHardness();
  }
  
  protected float getExplosionResistance(Entity exploder, Explosion explosion) {
    return this.teBlock.getExplosionResistance();
  }
  
  protected boolean canEntityDestroy(Entity entity) {
    return true;
  }
  
  public EnumFacing getFacing() {
    return EnumFacing.field_82609_l[this.facing];
  }
  
  protected boolean canSetFacingWrench(EnumFacing facing, EntityPlayer player) {
    if (!this.teBlock.allowWrenchRotating())
      return false; 
    if (facing == getFacing())
      return false; 
    if (!getSupportedFacings().contains(facing))
      return false; 
    return true;
  }
  
  protected boolean setFacingWrench(EnumFacing facing, EntityPlayer player) {
    if (!canSetFacingWrench(facing, player))
      return false; 
    setFacing(facing);
    return true;
  }
  
  protected boolean wrenchCanRemove(EntityPlayer player) {
    return true;
  }
  
  protected List<ItemStack> getWrenchDrops(EntityPlayer player, int fortune) {
    List<ItemStack> ret = new ArrayList<>();
    ret.addAll(getSelfDrops(fortune, true));
    ret.addAll(getAuxDrops(fortune));
    return ret;
  }
  
  protected EnumPlantType getPlantType() {
    return noCrop;
  }
  
  protected SoundType getBlockSound(Entity entity) {
    return SoundType.field_185851_d;
  }
  
  protected EnumFacing getPlacementFacing(EntityLivingBase placer, EnumFacing facing) {
    Set<EnumFacing> supportedFacings = getSupportedFacings();
    if (supportedFacings.isEmpty())
      return EnumFacing.DOWN; 
    if (placer == null)
      return (facing != null && supportedFacings.contains(facing.func_176734_d())) ? facing.func_176734_d() : getSupportedFacings().iterator().next(); 
    Vec3d dir = placer.func_70040_Z();
    EnumFacing bestFacing = null;
    double maxMatch = Double.NEGATIVE_INFINITY;
    for (EnumFacing cFacing : supportedFacings) {
      double match = dir.func_72430_b(new Vec3d(cFacing.func_176734_d().func_176730_m()));
      if (match > maxMatch) {
        maxMatch = match;
        bestFacing = cFacing;
      } 
    } 
    return bestFacing;
  }
  
  protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
    return defaultAabbs;
  }
  
  protected ItemStack adjustDrop(ItemStack drop, boolean wrench) {
    if (!wrench)
      switch (this.teBlock.getDefaultDrop()) {
        case None:
          drop = null;
          break;
        case Generator:
          drop = BlockName.te.getItemStack((Enum)TeBlock.generator);
          break;
        case Machine:
          drop = BlockName.resource.getItemStack((Enum)ResourceBlock.machine);
          break;
        case AdvMachine:
          drop = BlockName.resource.getItemStack((Enum)ResourceBlock.advanced_machine);
          break;
      }  
    return drop;
  }
  
  protected Set<EnumFacing> getSupportedFacings() {
    return this.teBlock.getSupportedFacings();
  }
  
  protected void setFacing(EnumFacing facing) {
    if (facing == null)
      throw new NullPointerException("null facing"); 
    if (this.facing == facing.ordinal())
      throw new IllegalArgumentException("unchanged facing"); 
    if (!getSupportedFacings().contains(facing))
      throw new IllegalArgumentException("invalid facing: " + facing + ", supported: " + getSupportedFacings()); 
    this.facing = (byte)facing.ordinal();
    if (!(getWorld()).isRemote)
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField(this, "facing"); 
  }
  
  public boolean getActive() {
    return this.active;
  }
  
  public void setActive(boolean active) {
    if (this.active == active)
      return; 
    this.active = active;
    ((NetworkManager)IC2.network.get(true)).updateTileEntityField(this, "active");
  }
  
  public boolean getGuiState(String name) {
    if ("active".equals(name))
      return getActive(); 
    throw new IllegalArgumentException("Unexpected GUI value requested: " + name);
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
    if (hasComponent((Class)Energy.class)) {
      Energy energy = getComponent(Energy.class);
      if (!energy.getSourceDirs().isEmpty()) {
        tooltip.add(Localization.translate("ic2.item.tooltip.PowerTier", new Object[] { Integer.valueOf(energy.getSourceTier()) }));
      } else if (!energy.getSinkDirs().isEmpty()) {
        tooltip.add(Localization.translate("ic2.item.tooltip.PowerTier", new Object[] { Integer.valueOf(energy.getSinkTier()) }));
      } 
    } 
  }
  
  public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
    if (super.hasCapability(capability, facing))
      return true; 
    if (this.capabilityComponents == null)
      return false; 
    TileEntityComponent comp = this.capabilityComponents.get(capability);
    if (comp == null)
      return false; 
    return comp.getProvidedCapabilities(facing).contains(capability);
  }
  
  public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
    if (this.capabilityComponents == null)
      return (T)super.getCapability(capability, facing); 
    TileEntityComponent comp = this.capabilityComponents.get(capability);
    if (comp == null)
      return (T)super.getCapability(capability, facing); 
    return (T)comp.getCapability(capability, facing);
  }
  
  protected final <T extends TileEntityComponent> T addComponent(T component) {
    if (component == null)
      throw new NullPointerException("null component"); 
    if (this.components == null)
      this.components = new IdentityHashMap<>(4); 
    TileEntityComponent prev = this.components.put(component.getClass(), (TileEntityComponent)component);
    if (prev != null)
      throw new RuntimeException("conflicting component while adding " + component + ", already used by " + prev + "."); 
    for (Capability<?> cap : (Iterable<Capability<?>>)component.getProvidedCapabilities(null))
      addComponentCapability(cap, (TileEntityComponent)component); 
    return component;
  }
  
  public boolean hasComponent(Class<? extends TileEntityComponent> cls) {
    if (this.components == null)
      return false; 
    return this.components.containsKey(cls);
  }
  
  public <T extends TileEntityComponent> T getComponent(Class<T> cls) {
    if (this.components == null)
      return null; 
    return (T)this.components.get(cls);
  }
  
  public final Iterable<? extends TileEntityComponent> getComponents() {
    if (this.components == null)
      return emptyComponents; 
    return this.components.values();
  }
  
  private void addComponentCapability(Capability<?> cap, TileEntityComponent component) {
    if (this.capabilityComponents == null)
      this.capabilityComponents = new IdentityHashMap<>(); 
    TileEntityComponent prev = this.capabilityComponents.put(cap, component);
    assert prev == null;
  }
  
  protected final void rerender() {
    IBlockState state = getBlockState();
    getWorld().func_184138_a(this.field_174879_c, state, state, 2);
  }
  
  protected boolean clientNeedsExtraModelInfo() {
    return false;
  }
  
  public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
    return (oldState.getBlock() != newState.getBlock());
  }
  
  private final synchronized boolean requiresWorldTick() {
    Class<?> cls = getClass();
    TickSubscription subscription = tickSubscriptions.get(cls);
    if (subscription == null) {
      boolean hasUpdateClient = false;
      boolean hasUpdateServer = false;
      boolean isClient = FMLCommonHandler.instance().getSide().isClient();
      while (cls != TileEntityBlock.class && ((!hasUpdateClient && isClient) || !hasUpdateServer)) {
        if (!hasUpdateClient && isClient) {
          boolean found = true;
          try {
            cls.getDeclaredMethod("updateEntityClient", new Class[0]);
          } catch (NoSuchMethodException e) {
            found = false;
          } 
          if (found)
            hasUpdateClient = true; 
        } 
        if (!hasUpdateServer) {
          boolean found = true;
          try {
            cls.getDeclaredMethod("updateEntityServer", new Class[0]);
          } catch (NoSuchMethodException e) {
            found = false;
          } 
          if (found)
            hasUpdateServer = true; 
        } 
        cls = cls.getSuperclass();
      } 
      if (hasUpdateClient) {
        if (hasUpdateServer) {
          subscription = TickSubscription.Both;
        } else {
          subscription = TickSubscription.Client;
        } 
      } else if (hasUpdateServer) {
        subscription = TickSubscription.Server;
      } else {
        subscription = TickSubscription.None;
      } 
      tickSubscriptions.put(getClass(), subscription);
    } 
    if ((getWorld()).isRemote)
      return (subscription == TickSubscription.Both || subscription == TickSubscription.Client); 
    return (subscription == TickSubscription.Both || subscription == TickSubscription.Server);
  }
  
  private enum TickSubscription {
    None, Client, Server, Both;
  }
  
  protected static final EnumPlantType noCrop = EnumPlantType.getPlantType("IC2_NO_CROP");
  
  private static final NBTTagCompound emptyNbt = new NBTTagCompound();
  
  private static final List<AxisAlignedBB> defaultAabbs = Arrays.asList(new AxisAlignedBB[] { new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) });
  
  private static final List<TileEntityComponent> emptyComponents = Collections.emptyList();
  
  private static final Map<Class<?>, TickSubscription> tickSubscriptions = new HashMap<>();
  
  private static final byte loadStateInitial = 0;
  
  private static final byte loadStateQueued = 1;
  
  private static final byte loadStateLoaded = 2;
  
  private static final byte loadStateUnloaded = 3;
  
  private static final boolean debugLoad = (System.getProperty("ic2.te.debugload") != null);
  
  protected final ITeBlock teBlock;
  
  private final BlockTileEntity block;
  
  private Map<Class<? extends TileEntityComponent>, TileEntityComponent> components;
  
  private Map<Capability<?>, TileEntityComponent> capabilityComponents;
  
  private List<TileEntityComponent> updatableComponents;
  
  private boolean active;
  
  private byte facing;
  
  private byte loadState;
  
  private boolean enableWorldTick;
}

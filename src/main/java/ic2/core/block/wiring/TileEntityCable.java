package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IColoredEnergyTile;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.IC2;
import ic2.core.IWorldTickCallback;
import ic2.core.block.BlockFoam;
import ic2.core.block.BlockWall;
import ic2.core.block.ITeBlock;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityWall;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.UnlistedProperty;
import ic2.core.item.block.ItemCable;
import ic2.core.item.tool.ItemToolCutter;
import ic2.core.network.NetworkManager;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.ref.TeBlock.Delegated;
import ic2.core.util.Ic2Color;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.eventhandler.Event;

@Delegated(current = TileEntityCable.class, old = TileEntityClassicCable.class)
public class TileEntityCable extends TileEntityBlock implements IEnergyConductor, INetworkTileEntityEventListener, IColoredEnergyTile {
  public static final float insulationThickness = 0.0625F;
  
  public static Class<? extends TileEntityCable> delegate() {
    return IC2.version.isClassic() ? (Class)TileEntityClassicCable.class : TileEntityCable.class;
  }
  
  public static TileEntityCable delegate(CableType cableType, int insulation) {
    return IC2.version.isClassic() ? new TileEntityClassicCable(cableType, insulation) : new TileEntityCable(cableType, insulation);
  }
  
  public static TileEntityCable delegate(CableType cableType, int insulation, Ic2Color color) {
    return IC2.version.isClassic() ? new TileEntityClassicCable(cableType, insulation, color) : new TileEntityCable(cableType, insulation, color);
  }
  
  public TileEntityCable(CableType cableType, int insulation) {
    this();
    this.cableType = cableType;
    this.insulation = insulation;
  }
  
  public TileEntityCable(CableType cableType, int insulation, Ic2Color color) {
    this(cableType, insulation);
    if (canBeColored(color))
      this.color = color; 
  }
  
  public TileEntityCable() {
    this.cableType = CableType.copper;
    this.color = Ic2Color.black;
    this.foam = CableFoam.None;
    this.foamColor = BlockWall.defaultColor;
    this.connectivity = 0;
    this.addedToEnergyNet = false;
    this.continuousUpdate = null;
    this.obscuration = (Obscuration)addComponent((TileEntityComponent)new Obscuration(this, new Runnable() {
            public void run() {
              ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)TileEntityCable.this, "obscuration");
            }
          }));
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    this.cableType = CableType.values[nbt.func_74771_c("cableType") & 0xFF];
    this.insulation = nbt.func_74771_c("insulation") & 0xFF;
    this.color = Ic2Color.values[nbt.func_74771_c("color") & 0xFF];
    this.foam = CableFoam.values[nbt.func_74771_c("foam") & 0xFF];
    this.foamColor = Ic2Color.values[nbt.func_74771_c("foamColor") & 0xFF];
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74774_a("cableType", (byte)this.cableType.ordinal());
    nbt.func_74774_a("insulation", (byte)this.insulation);
    nbt.func_74774_a("color", (byte)this.color.ordinal());
    nbt.func_74774_a("foam", (byte)this.foam.ordinal());
    nbt.func_74774_a("foamColor", (byte)this.foamColor.ordinal());
    return nbt;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if ((func_145831_w()).field_72995_K) {
      updateRenderState();
    } else {
      if (getClass() == TileEntityCable.class && (this.cableType == CableType.detector || this.cableType == CableType.splitter)) {
        IC2.log.debug(LogCategory.Block, "Fixing incorrect cable TE %s.", new Object[] { Util.toString((TileEntity)this) });
        TileEntityCable newTe = (this.cableType == CableType.detector) ? new TileEntityCableDetector() : new TileEntityCableSplitter();
        NBTTagCompound nbt = new NBTTagCompound();
        func_189515_b(nbt);
        this.field_145850_b.func_175690_a(func_174877_v(), (TileEntity)newTe);
        newTe.func_145839_a(nbt);
        return;
      } 
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent((IEnergyTile)this));
      this.addedToEnergyNet = true;
      updateConnectivity();
      if (this.foam == CableFoam.Soft)
        changeFoam(this.foam, true); 
    } 
  }
  
  protected void onUnloaded() {
    if (IC2.platform.isSimulating() && this.addedToEnergyNet) {
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent((IEnergyTile)this));
      this.addedToEnergyNet = false;
    } 
    if (this.continuousUpdate != null) {
      IC2.tickHandler.removeContinuousWorldTick(func_145831_w(), this.continuousUpdate);
      this.continuousUpdate = null;
    } 
    super.onUnloaded();
  }
  
  protected SoundType getBlockSound(Entity entity) {
    return SoundType.field_185854_g;
  }
  
  public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
    updateRenderState();
    super.onPlaced(stack, placer, facing);
  }
  
  protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target) {
    return ItemCable.getCable(this.cableType, this.insulation);
  }
  
  protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
    if (this.foam == CableFoam.Hardened || (this.foam == CableFoam.Soft && !forCollision))
      return super.getAabbs(forCollision); 
    float th = this.cableType.thickness + (this.insulation * 2) * 0.0625F;
    float sp = (1.0F - th) / 2.0F;
    List<AxisAlignedBB> ret = new ArrayList<>(7);
    ret.add(new AxisAlignedBB(sp, sp, sp, (sp + th), (sp + th), (sp + th)));
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      boolean hasConnection = ((this.connectivity & 1 << facing.ordinal()) != 0);
      if (hasConnection) {
        float zS = sp, yS = zS, xS = yS;
        float zE = sp + th, yE = zE, xE = yE;
        switch (facing) {
          case Soft:
            yS = 0.0F;
            yE = sp;
            break;
          case Hardened:
            yS = sp + th;
            yE = 1.0F;
            break;
          case None:
            zS = 0.0F;
            zE = sp;
            break;
          case null:
            zS = sp + th;
            zE = 1.0F;
            break;
          case null:
            xS = 0.0F;
            xE = sp;
            break;
          case null:
            xS = sp + th;
            xE = 1.0F;
            break;
          default:
            throw new RuntimeException();
        } 
        ret.add(new AxisAlignedBB(xS, yS, zS, xE, yE, zE));
      } 
    } 
    return ret;
  }
  
  protected boolean isNormalCube() {
    return (this.foam == CableFoam.Hardened || this.foam == CableFoam.Soft);
  }
  
  protected boolean isSideSolid(EnumFacing side) {
    return (this.foam == CableFoam.Hardened);
  }
  
  protected boolean doesSideBlockRendering(EnumFacing side) {
    return (this.foam == CableFoam.Hardened);
  }
  
  public Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state) {
    state = super.getExtendedState(state);
    CableRenderState cableRenderState = this.renderState;
    if (cableRenderState != null)
      state = state.withProperties(new Object[] { renderStateProperty, cableRenderState }); 
    TileEntityWall.WallRenderState wallRenderState = this.wallRenderState;
    if (wallRenderState != null)
      state = state.withProperties(new Object[] { TileEntityWall.renderStateProperty, wallRenderState }); 
    return state;
  }
  
  public void onNeighborChange(Block neighbor, BlockPos neighborPos) {
    super.onNeighborChange(neighbor, neighborPos);
    if (!(func_145831_w()).field_72995_K)
      updateConnectivity(); 
  }
  
  private void updateConnectivity() {
    World world = func_145831_w();
    byte newConnectivity = 0;
    int mask = 1;
    for (EnumFacing dir : EnumFacing.field_82609_l) {
      IEnergyTile tile = EnergyNet.instance.getSubTile(world, this.field_174879_c.func_177972_a(dir));
      if (((tile instanceof IEnergyAcceptor && ((IEnergyAcceptor)tile).acceptsEnergyFrom((IEnergyEmitter)this, dir.func_176734_d())) || (tile instanceof IEnergyEmitter && ((IEnergyEmitter)tile).emitsEnergyTo((IEnergyAcceptor)this, dir.func_176734_d()))) && canInteractWith(tile, dir))
        newConnectivity = (byte)(newConnectivity | mask); 
      mask *= 2;
    } 
    if (this.connectivity != newConnectivity) {
      this.connectivity = newConnectivity;
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "connectivity");
    } 
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (this.foam == CableFoam.Soft && StackUtil.consume(player, hand, StackUtil.sameItem((Block)Blocks.field_150354_m), 1)) {
      changeFoam(CableFoam.Hardened, false);
      return true;
    } 
    if (this.foam == CableFoam.None && StackUtil.consume(player, hand, StackUtil.sameStack(BlockName.foam.getItemStack((Enum)BlockFoam.FoamType.normal)), 1)) {
      foam();
      return true;
    } 
    return super.onActivated(player, hand, side, hitX, hitY, hitZ);
  }
  
  protected void onClicked(EntityPlayer player) {
    super.onClicked(player);
    ItemToolCutter cutter = (ItemToolCutter)ItemName.cutter.getInstance();
    if (!cutter.removeInsulation(player, EnumHand.MAIN_HAND, this))
      cutter.removeInsulation(player, EnumHand.OFF_HAND, this); 
  }
  
  protected float getHardness() {
    switch (this.foam) {
      case Soft:
        return BlockName.foam.getInstance().func_176195_g(null, null, null);
      case Hardened:
        return BlockName.wall.getInstance().func_176195_g(null, null, null);
    } 
    return super.getHardness();
  }
  
  protected float getExplosionResistance(Entity exploder, Explosion explosion) {
    switch (this.foam) {
      case Hardened:
        return BlockName.wall.getInstance().getExplosionResistance(func_145831_w(), this.field_174879_c, exploder, explosion);
    } 
    return super.getHardness();
  }
  
  protected int getLightOpacity() {
    return (this.foam == CableFoam.Hardened) ? 255 : 0;
  }
  
  private boolean canBeColored(Ic2Color newColor) {
    switch (this.foam) {
      case None:
        return (this.color != newColor && this.cableType.minColoredInsulation <= this.insulation);
      default:
        return false;
      case Hardened:
        break;
    } 
    return (this.color != newColor);
  }
  
  protected boolean recolor(EnumFacing side, EnumDyeColor mcColor) {
    Ic2Color newColor = Ic2Color.get(mcColor);
    if (!canBeColored(newColor))
      return false; 
    if (!(func_145831_w()).field_72995_K) {
      if (this.foam == CableFoam.None) {
        if (this.addedToEnergyNet)
          MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent((IEnergyTile)this)); 
        this.addedToEnergyNet = false;
        this.color = newColor;
        MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent((IEnergyTile)this));
        this.addedToEnergyNet = true;
        ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "color");
        updateConnectivity();
      } else {
        this.foamColor = newColor;
        ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "foamColor");
        this.obscuration.clear();
      } 
      func_70296_d();
    } 
    return true;
  }
  
  protected boolean onRemovedByPlayer(EntityPlayer player, boolean willHarvest) {
    if (changeFoam(CableFoam.None, false))
      return false; 
    return super.onRemovedByPlayer(player, willHarvest);
  }
  
  public boolean isFoamed() {
    return (this.foam != CableFoam.None);
  }
  
  public boolean foam() {
    return changeFoam(CableFoam.Soft, false);
  }
  
  public boolean tryAddInsulation() {
    if (this.insulation >= this.cableType.maxInsulation)
      return false; 
    this.insulation++;
    if (!(func_145831_w()).field_72995_K)
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "insulation"); 
    return true;
  }
  
  public boolean tryRemoveInsulation(boolean simulate) {
    if (this.insulation <= 0)
      return false; 
    if (simulate)
      return true; 
    if (this.insulation == this.cableType.minColoredInsulation) {
      CableFoam foam = this.foam;
      this.foam = CableFoam.None;
      recolor(getFacing(), EnumDyeColor.BLACK);
      this.foam = foam;
    } 
    this.insulation--;
    if (!(func_145831_w()).field_72995_K)
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "insulation"); 
    return true;
  }
  
  public boolean wrenchCanRemove(EntityPlayer player) {
    return false;
  }
  
  public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
    return canInteractWith((IEnergyTile)emitter, direction);
  }
  
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction) {
    return canInteractWith((IEnergyTile)receiver, direction);
  }
  
  public boolean canInteractWith(IEnergyTile tile, EnumFacing side) {
    if (tile instanceof IColoredEnergyTile) {
      IColoredEnergyTile other = (IColoredEnergyTile)tile;
      EnumDyeColor thisColor = getColor(side);
      EnumDyeColor otherColor = other.getColor(side.func_176734_d());
      return (thisColor == null || otherColor == null || thisColor == otherColor);
    } 
    return true;
  }
  
  public double getConductionLoss() {
    return this.cableType.loss;
  }
  
  public double getInsulationEnergyAbsorption() {
    if (this.cableType.maxInsulation == 0)
      return 2.147483647E9D; 
    if (this.cableType == CableType.tin)
      return EnergyNet.instance.getPowerFromTier(this.insulation); 
    return EnergyNet.instance.getPowerFromTier(this.insulation + 1);
  }
  
  public double getInsulationBreakdownEnergy() {
    return 9001.0D;
  }
  
  public double getConductorBreakdownEnergy() {
    return (this.cableType.capacity + 1);
  }
  
  public void removeInsulation() {
    tryRemoveInsulation(false);
  }
  
  public void removeConductor() {
    func_145831_w().func_175698_g(this.field_174879_c);
    ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this, 0, true);
  }
  
  public EnumDyeColor getColor(EnumFacing side) {
    return (this.color == Ic2Color.black) ? null : this.color.mcColor;
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = new ArrayList<>();
    ret.add("cableType");
    ret.add("insulation");
    ret.add("color");
    ret.add("foam");
    ret.add("connectivity");
    ret.add("obscuration");
    ret.addAll(super.getNetworkedFields());
    return ret;
  }
  
  public void onNetworkUpdate(String field) {
    updateRenderState();
    if (field.equals("foam") && (this.foam == CableFoam.None || this.foam == CableFoam.Hardened))
      relight(); 
    rerender();
    super.onNetworkUpdate(field);
  }
  
  private void relight() {}
  
  public void onNetworkEvent(int event) {
    int l;
    World world = func_145831_w();
    switch (event) {
      case 0:
        world.func_184133_a(null, this.field_174879_c, SoundEvents.field_187658_bx, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()) * 0.8F);
        for (l = 0; l < 8; l++)
          world.func_175688_a(EnumParticleTypes.SMOKE_LARGE, this.field_174879_c.func_177958_n() + Math.random(), this.field_174879_c.func_177956_o() + 1.2D, this.field_174879_c.func_177952_p() + Math.random(), 0.0D, 0.0D, 0.0D, new int[0]); 
        return;
    } 
    IC2.platform.displayError("An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID " + event + ", tile entity below)\nT: " + this + " (" + this.field_174879_c + ")", new Object[0]);
  }
  
  private boolean changeFoam(CableFoam foam, boolean duringLoad) {
    if (this.foam == foam && !duringLoad)
      return false; 
    World world = func_145831_w();
    if (world.field_72995_K)
      return true; 
    this.foam = foam;
    if (this.continuousUpdate != null) {
      IC2.tickHandler.removeContinuousWorldTick(world, this.continuousUpdate);
      this.continuousUpdate = null;
    } 
    if (foam != CableFoam.Hardened) {
      this.obscuration.clear();
      if (this.foamColor != BlockWall.defaultColor) {
        this.foamColor = BlockWall.defaultColor;
        if (!duringLoad)
          ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "foamColor"); 
      } 
    } 
    if (foam == CableFoam.Soft) {
      this.continuousUpdate = new IWorldTickCallback() {
          public void onTick(World world) {
            if (world.field_73012_v.nextFloat() < BlockFoam.getHardenChance(world, TileEntityCable.this.field_174879_c, TileEntityCable.this.getBlockType().getState((ITeBlock)TeBlock.cable), BlockFoam.FoamType.normal))
              TileEntityCable.this.changeFoam(CableFoam.Hardened, false); 
          }
        };
      IC2.tickHandler.requestContinuousWorldTick(world, this.continuousUpdate);
    } 
    if (!duringLoad) {
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "foam");
      world.func_175685_c(this.field_174879_c, (Block)getBlockType(), true);
      func_70296_d();
    } 
    return true;
  }
  
  protected boolean clientNeedsExtraModelInfo() {
    return true;
  }
  
  private void updateRenderState() {
    this.renderState = new CableRenderState(this.cableType, this.insulation, this.color, this.foam, this.connectivity, getActive());
    this.wallRenderState = new TileEntityWall.WallRenderState(this.foamColor, this.obscuration.getRenderState());
  }
  
  public static class CableRenderState {
    public final CableType type;
    
    public final int insulation;
    
    public final Ic2Color color;
    
    public final CableFoam foam;
    
    public final int connectivity;
    
    public final boolean active;
    
    public CableRenderState(CableType type, int insulation, Ic2Color color, CableFoam foam, int connectivity, boolean active) {
      this.type = type;
      this.insulation = insulation;
      this.color = color;
      this.foam = foam;
      this.connectivity = connectivity;
      this.active = active;
    }
    
    public int hashCode() {
      int ret = this.type.hashCode();
      ret = ret * 31 + this.insulation;
      ret = ret * 31 + this.color.hashCode();
      ret = ret * 31 + this.foam.hashCode();
      ret = ret * 31 + this.connectivity;
      ret = ret << 1 | (this.active ? 1 : 0);
      return ret;
    }
    
    public boolean equals(Object obj) {
      if (obj == this)
        return true; 
      if (!(obj instanceof CableRenderState))
        return false; 
      CableRenderState o = (CableRenderState)obj;
      return (o.type == this.type && o.insulation == this.insulation && o.color == this.color && o.foam == this.foam && o.connectivity == this.connectivity && o.active == this.active);
    }
    
    public String toString() {
      return "CableState<" + this.type + ", " + this.insulation + ", " + this.color + ", " + this.foam + ", " + this.connectivity + ", " + this.active + '>';
    }
  }
  
  public static final IUnlistedProperty<CableRenderState> renderStateProperty = (IUnlistedProperty<CableRenderState>)new UnlistedProperty("renderstate", CableRenderState.class);
  
  protected CableType cableType;
  
  protected int insulation;
  
  private Ic2Color color;
  
  private CableFoam foam;
  
  private Ic2Color foamColor;
  
  private final Obscuration obscuration;
  
  private byte connectivity;
  
  private volatile CableRenderState renderState;
  
  private volatile TileEntityWall.WallRenderState wallRenderState;
  
  public boolean addedToEnergyNet;
  
  private IWorldTickCallback continuousUpdate;
  
  private static final int EventRemoveConductor = 0;
}

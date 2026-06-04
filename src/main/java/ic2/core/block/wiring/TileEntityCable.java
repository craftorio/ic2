// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import ic2.core.block.state.UnlistedProperty;
import ic2.core.block.ITeBlock;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import java.util.Collection;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.Explosion;
import net.minecraft.block.state.IBlockState;
import ic2.core.ref.ItemName;
import ic2.core.item.tool.ItemToolCutter;
import ic2.core.block.BlockFoam;
import ic2.core.ref.BlockName;
import ic2.core.util.StackUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.EnergyNet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import ic2.core.block.state.Ic2BlockState;
import java.util.ArrayList;
import net.minecraft.util.math.AxisAlignedBB;
import java.util.List;
import ic2.core.item.block.ItemCable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.event.EnergyTileLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import ic2.core.block.BlockWall;
import ic2.core.IC2;
import ic2.core.IWorldTickCallback;
import ic2.core.block.TileEntityWall;
import ic2.core.block.comp.Obscuration;
import ic2.core.util.Ic2Color;
import net.minecraftforge.common.property.IUnlistedProperty;
import ic2.core.ref.TeBlock;
import ic2.api.energy.tile.IColoredEnergyTile;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.core.block.TileEntityBlock;

@TeBlock.Delegated(current = TileEntityCable.class, old = TileEntityClassicCable.class)
public class TileEntityCable extends TileEntityBlock implements IEnergyConductor, INetworkTileEntityEventListener, IColoredEnergyTile
{
    public static final float insulationThickness = 0.0625f;
    public static final IUnlistedProperty<CableRenderState> renderStateProperty;
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
    
    public static Class<? extends TileEntityCable> delegate() {
        return (Class<? extends TileEntityCable>)(IC2.version.isClassic() ? TileEntityClassicCable.class : TileEntityCable.class);
    }
    
    public static TileEntityCable delegate(final CableType cableType, final int insulation) {
        return IC2.version.isClassic() ? new TileEntityClassicCable(cableType, insulation) : new TileEntityCable(cableType, insulation);
    }
    
    public static TileEntityCable delegate(final CableType cableType, final int insulation, final Ic2Color color) {
        return IC2.version.isClassic() ? new TileEntityClassicCable(cableType, insulation, color) : new TileEntityCable(cableType, insulation, color);
    }
    
    public TileEntityCable(final CableType cableType, final int insulation) {
        this();
        this.cableType = cableType;
        this.insulation = insulation;
    }
    
    public TileEntityCable(final CableType cableType, final int insulation, final Ic2Color color) {
        this(cableType, insulation);
        if (this.canBeColored(color)) {
            this.color = color;
        }
    }
    
    public TileEntityCable() {
        this.cableType = CableType.copper;
        this.color = Ic2Color.black;
        this.foam = CableFoam.None;
        this.foamColor = BlockWall.defaultColor;
        this.connectivity = 0;
        this.addedToEnergyNet = false;
        this.continuousUpdate = null;
        this.obscuration = this.addComponent(new Obscuration(this, new Runnable() {
            @Override
            public void run() {
                IC2.network.get(true).updateTileEntityField(TileEntityCable.this, "obscuration");
            }
        }));
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.cableType = CableType.values[nbt.getByte("cableType") & 0xFF];
        this.insulation = (nbt.getByte("insulation") & 0xFF);
        this.color = Ic2Color.values[nbt.getByte("color") & 0xFF];
        this.foam = CableFoam.values[nbt.getByte("foam") & 0xFF];
        this.foamColor = Ic2Color.values[nbt.getByte("foamColor") & 0xFF];
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("cableType", (byte)this.cableType.ordinal());
        nbt.setByte("insulation", (byte)this.insulation);
        nbt.setByte("color", (byte)this.color.ordinal());
        nbt.setByte("foam", (byte)this.foam.ordinal());
        nbt.setByte("foamColor", (byte)this.foamColor.ordinal());
        return nbt;
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (this.getWorld().isRemote) {
            this.updateRenderState();
        }
        else {
            if (this.getClass() == TileEntityCable.class && (this.cableType == CableType.detector || this.cableType == CableType.splitter)) {
                IC2.log.debug(LogCategory.Block, "Fixing incorrect cable TE %s.", Util.toString(this));
                final TileEntityCable newTe = (this.cableType == CableType.detector) ? new TileEntityCableDetector() : new TileEntityCableSplitter();
                final NBTTagCompound nbt = new NBTTagCompound();
                this.writeToNBT(nbt);
                this.world.setTileEntity(this.getPos(), (TileEntity)newTe);
                newTe.readFromNBT(nbt);
                return;
            }
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this));
            this.addedToEnergyNet = true;
            this.updateConnectivity();
            if (this.foam == CableFoam.Soft) {
                this.changeFoam(this.foam, true);
            }
        }
    }
    
    @Override
    protected void onUnloaded() {
        if (IC2.platform.isSimulating() && this.addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this));
            this.addedToEnergyNet = false;
        }
        if (this.continuousUpdate != null) {
            IC2.tickHandler.removeContinuousWorldTick(this.getWorld(), this.continuousUpdate);
            this.continuousUpdate = null;
        }
        super.onUnloaded();
    }
    
    @Override
    protected SoundType getBlockSound(final Entity entity) {
        return SoundType.CLOTH;
    }
    
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        this.updateRenderState();
        super.onPlaced(stack, placer, facing);
    }
    
    @Override
    protected ItemStack getPickBlock(final EntityPlayer player, final RayTraceResult target) {
        return ItemCable.getCable(this.cableType, this.insulation);
    }
    
    @Override
    protected List<AxisAlignedBB> getAabbs(final boolean forCollision) {
        if (this.foam == CableFoam.Hardened || (this.foam == CableFoam.Soft && !forCollision)) {
            return super.getAabbs(forCollision);
        }
        final float th = this.cableType.thickness + this.insulation * 2 * 0.0625f;
        final float sp = (1.0f - th) / 2.0f;
        final List<AxisAlignedBB> ret = new ArrayList<AxisAlignedBB>(7);
        ret.add(new AxisAlignedBB((double)sp, (double)sp, (double)sp, (double)(sp + th), (double)(sp + th), (double)(sp + th)));
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final boolean hasConnection = (this.connectivity & 1 << facing.ordinal()) != 0x0;
            if (hasConnection) {
                float zS;
                float xS;
                float yS = xS = (zS = sp);
                float zE;
                float xE;
                float yE = xE = (zE = sp + th);
                switch (facing) {
                    case DOWN: {
                        yS = 0.0f;
                        yE = sp;
                        break;
                    }
                    case UP: {
                        yS = sp + th;
                        yE = 1.0f;
                        break;
                    }
                    case NORTH: {
                        zS = 0.0f;
                        zE = sp;
                        break;
                    }
                    case SOUTH: {
                        zS = sp + th;
                        zE = 1.0f;
                        break;
                    }
                    case WEST: {
                        xS = 0.0f;
                        xE = sp;
                        break;
                    }
                    case EAST: {
                        xS = sp + th;
                        xE = 1.0f;
                        break;
                    }
                    default: {
                        throw new RuntimeException();
                    }
                }
                ret.add(new AxisAlignedBB((double)xS, (double)yS, (double)zS, (double)xE, (double)yE, (double)zE));
            }
        }
        return ret;
    }
    
    @Override
    protected boolean isNormalCube() {
        return this.foam == CableFoam.Hardened || this.foam == CableFoam.Soft;
    }
    
    @Override
    protected boolean isSideSolid(final EnumFacing side) {
        return this.foam == CableFoam.Hardened;
    }
    
    @Override
    protected boolean doesSideBlockRendering(final EnumFacing side) {
        return this.foam == CableFoam.Hardened;
    }
    
    public Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state) {
        state = super.getExtendedState(state);
        final CableRenderState cableRenderState = this.renderState;
        if (cableRenderState != null) {
            state = state.withProperties(TileEntityCable.renderStateProperty, cableRenderState);
        }
        final TileEntityWall.WallRenderState wallRenderState = this.wallRenderState;
        if (wallRenderState != null) {
            state = state.withProperties(TileEntityWall.renderStateProperty, wallRenderState);
        }
        return state;
    }
    
    public void onNeighborChange(final Block neighbor, final BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        if (!this.getWorld().isRemote) {
            this.updateConnectivity();
        }
    }
    
    private void updateConnectivity() {
        final World world = this.getWorld();
        byte newConnectivity = 0;
        int mask = 1;
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final IEnergyTile tile = EnergyNet.instance.getSubTile(world, this.pos.offset(dir));
            if (((tile instanceof IEnergyAcceptor && ((IEnergyAcceptor)tile).acceptsEnergyFrom(this, dir.getOpposite())) || (tile instanceof IEnergyEmitter && ((IEnergyEmitter)tile).emitsEnergyTo(this, dir.getOpposite()))) && this.canInteractWith(tile, dir)) {
                newConnectivity |= (byte)mask;
            }
            mask *= 2;
        }
        if (this.connectivity != newConnectivity) {
            this.connectivity = newConnectivity;
            IC2.network.get(true).updateTileEntityField(this, "connectivity");
        }
    }
    
    @Override
    protected boolean onActivated(final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (this.foam == CableFoam.Soft && StackUtil.consume(player, hand, StackUtil.sameItem((Block)Blocks.SAND), 1)) {
            this.changeFoam(CableFoam.Hardened, false);
            return true;
        }
        if (this.foam == CableFoam.None && StackUtil.consume(player, hand, StackUtil.sameStack(BlockName.foam.getItemStack(BlockFoam.FoamType.normal)), 1)) {
            this.foam();
            return true;
        }
        return super.onActivated(player, hand, side, hitX, hitY, hitZ);
    }
    
    @Override
    protected void onClicked(final EntityPlayer player) {
        super.onClicked(player);
        final ItemToolCutter cutter = ItemName.cutter.getInstance();
        if (!cutter.removeInsulation(player, EnumHand.MAIN_HAND, this)) {
            cutter.removeInsulation(player, EnumHand.OFF_HAND, this);
        }
    }
    
    @Override
    protected float getHardness() {
        switch (this.foam) {
            case Soft: {
                return BlockName.foam.getInstance().getBlockHardness((IBlockState)null, (World)null, (BlockPos)null);
            }
            case Hardened: {
                return BlockName.wall.getInstance().getBlockHardness((IBlockState)null, (World)null, (BlockPos)null);
            }
            default: {
                return super.getHardness();
            }
        }
    }
    
    @Override
    protected float getExplosionResistance(final Entity exploder, final Explosion explosion) {
        switch (this.foam) {
            case Hardened: {
                return BlockName.wall.getInstance().getExplosionResistance(this.getWorld(), this.pos, exploder, explosion);
            }
            default: {
                return super.getHardness();
            }
        }
    }
    
    @Override
    protected int getLightOpacity() {
        return (this.foam == CableFoam.Hardened) ? 255 : 0;
    }
    
    private boolean canBeColored(final Ic2Color newColor) {
        switch (this.foam) {
            case None: {
                return this.color != newColor && this.cableType.minColoredInsulation <= this.insulation;
            }
            default: {
                return false;
            }
            case Hardened: {
                return this.color != newColor;
            }
        }
    }
    
    @Override
    protected boolean recolor(final EnumFacing side, final EnumDyeColor mcColor) {
        final Ic2Color newColor = Ic2Color.get(mcColor);
        if (!this.canBeColored(newColor)) {
            return false;
        }
        if (!this.getWorld().isRemote) {
            if (this.foam == CableFoam.None) {
                if (this.addedToEnergyNet) {
                    MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this));
                }
                this.addedToEnergyNet = false;
                this.color = newColor;
                MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this));
                this.addedToEnergyNet = true;
                IC2.network.get(true).updateTileEntityField(this, "color");
                this.updateConnectivity();
            }
            else {
                this.foamColor = newColor;
                IC2.network.get(true).updateTileEntityField(this, "foamColor");
                this.obscuration.clear();
            }
            this.markDirty();
        }
        return true;
    }
    
    @Override
    protected boolean onRemovedByPlayer(final EntityPlayer player, final boolean willHarvest) {
        return !this.changeFoam(CableFoam.None, false) && super.onRemovedByPlayer(player, willHarvest);
    }
    
    public boolean isFoamed() {
        return this.foam != CableFoam.None;
    }
    
    public boolean foam() {
        return this.changeFoam(CableFoam.Soft, false);
    }
    
    public boolean tryAddInsulation() {
        if (this.insulation >= this.cableType.maxInsulation) {
            return false;
        }
        ++this.insulation;
        if (!this.getWorld().isRemote) {
            IC2.network.get(true).updateTileEntityField(this, "insulation");
        }
        return true;
    }
    
    public boolean tryRemoveInsulation(final boolean simulate) {
        if (this.insulation <= 0) {
            return false;
        }
        if (simulate) {
            return true;
        }
        if (this.insulation == this.cableType.minColoredInsulation) {
            final CableFoam foam = this.foam;
            this.foam = CableFoam.None;
            this.recolor(this.getFacing(), EnumDyeColor.BLACK);
            this.foam = foam;
        }
        --this.insulation;
        if (!this.getWorld().isRemote) {
            IC2.network.get(true).updateTileEntityField(this, "insulation");
        }
        return true;
    }
    
    public boolean wrenchCanRemove(final EntityPlayer player) {
        return false;
    }
    
    public boolean acceptsEnergyFrom(final IEnergyEmitter emitter, final EnumFacing direction) {
        return this.canInteractWith(emitter, direction);
    }
    
    public boolean emitsEnergyTo(final IEnergyAcceptor receiver, final EnumFacing direction) {
        return this.canInteractWith(receiver, direction);
    }
    
    public boolean canInteractWith(final IEnergyTile tile, final EnumFacing side) {
        if (tile instanceof IColoredEnergyTile) {
            final IColoredEnergyTile other = (IColoredEnergyTile)tile;
            final EnumDyeColor thisColor = this.getColor(side);
            final EnumDyeColor otherColor = other.getColor(side.getOpposite());
            return thisColor == null || otherColor == null || thisColor == otherColor;
        }
        return true;
    }
    
    @Override
    public double getConductionLoss() {
        return this.cableType.loss;
    }
    
    @Override
    public double getInsulationEnergyAbsorption() {
        if (this.cableType.maxInsulation == 0) {
            return 2.147483647E9;
        }
        if (this.cableType == CableType.tin) {
            return EnergyNet.instance.getPowerFromTier(this.insulation);
        }
        return EnergyNet.instance.getPowerFromTier(this.insulation + 1);
    }
    
    @Override
    public double getInsulationBreakdownEnergy() {
        return 9001.0;
    }
    
    @Override
    public double getConductorBreakdownEnergy() {
        return this.cableType.capacity + 1;
    }
    
    @Override
    public void removeInsulation() {
        this.tryRemoveInsulation(false);
    }
    
    @Override
    public void removeConductor() {
        this.getWorld().setBlockToAir(this.pos);
        IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
    }
    
    @Override
    public EnumDyeColor getColor(final EnumFacing side) {
        return (this.color == Ic2Color.black) ? null : this.color.mcColor;
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = new ArrayList<String>();
        ret.add("cableType");
        ret.add("insulation");
        ret.add("color");
        ret.add("foam");
        ret.add("connectivity");
        ret.add("obscuration");
        ret.addAll(super.getNetworkedFields());
        return ret;
    }
    
    @Override
    public void onNetworkUpdate(final String field) {
        this.updateRenderState();
        if (field.equals("foam") && (this.foam == CableFoam.None || this.foam == CableFoam.Hardened)) {
            this.relight();
        }
        this.rerender();
        super.onNetworkUpdate(field);
    }
    
    private void relight() {
    }
    
    @Override
    public void onNetworkEvent(final int event) {
        final World world = this.getWorld();
        switch (event) {
            case 0: {
                world.playSound((EntityPlayer)null, this.pos, SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8f);
                for (int l = 0; l < 8; ++l) {
                    world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.pos.getX() + Math.random(), this.pos.getY() + 1.2, this.pos.getZ() + Math.random(), 0.0, 0.0, 0.0, new int[0]);
                }
                break;
            }
            default: {
                IC2.platform.displayError("An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID " + event + ", tile entity below)\nT: " + this + " (" + this.pos + ")", new Object[0]);
                break;
            }
        }
    }
    
    private boolean changeFoam(final CableFoam foam, final boolean duringLoad) {
        if (this.foam == foam && !duringLoad) {
            return false;
        }
        final World world = this.getWorld();
        if (world.isRemote) {
            return true;
        }
        this.foam = foam;
        if (this.continuousUpdate != null) {
            IC2.tickHandler.removeContinuousWorldTick(world, this.continuousUpdate);
            this.continuousUpdate = null;
        }
        if (foam != CableFoam.Hardened) {
            this.obscuration.clear();
            if (this.foamColor != BlockWall.defaultColor) {
                this.foamColor = BlockWall.defaultColor;
                if (!duringLoad) {
                    IC2.network.get(true).updateTileEntityField(this, "foamColor");
                }
            }
        }
        if (foam == CableFoam.Soft) {
            this.continuousUpdate = new IWorldTickCallback() {
                @Override
                public void onTick(final World world) {
                    if (world.rand.nextFloat() < BlockFoam.getHardenChance(world, TileEntityCable.this.pos, TileEntityCable.this.getBlockType().getState(TeBlock.cable), BlockFoam.FoamType.normal)) {
                        TileEntityCable.this.changeFoam(CableFoam.Hardened, false);
                    }
                }
            };
            IC2.tickHandler.requestContinuousWorldTick(world, this.continuousUpdate);
        }
        if (!duringLoad) {
            IC2.network.get(true).updateTileEntityField(this, "foam");
            world.notifyNeighborsOfStateChange(this.pos, (Block)this.getBlockType(), true);
            this.markDirty();
        }
        return true;
    }
    
    @Override
    protected boolean clientNeedsExtraModelInfo() {
        return true;
    }
    
    private void updateRenderState() {
        this.renderState = new CableRenderState(this.cableType, this.insulation, this.color, this.foam, this.connectivity, this.getActive());
        this.wallRenderState = new TileEntityWall.WallRenderState(this.foamColor, this.obscuration.getRenderState());
    }
    
    static {
        renderStateProperty = (IUnlistedProperty)new UnlistedProperty("renderstate", (Class<Object>)CableRenderState.class);
    }
    
    public static class CableRenderState
    {
        public final CableType type;
        public final int insulation;
        public final Ic2Color color;
        public final CableFoam foam;
        public final int connectivity;
        public final boolean active;
        
        public CableRenderState(final CableType type, final int insulation, final Ic2Color color, final CableFoam foam, final int connectivity, final boolean active) {
            this.type = type;
            this.insulation = insulation;
            this.color = color;
            this.foam = foam;
            this.connectivity = connectivity;
            this.active = active;
        }
        
        @Override
        public int hashCode() {
            int ret = this.type.hashCode();
            ret = ret * 31 + this.insulation;
            ret = ret * 31 + this.color.hashCode();
            ret = ret * 31 + this.foam.hashCode();
            ret = ret * 31 + this.connectivity;
            ret = (ret << 1 | (this.active ? 1 : 0));
            return ret;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CableRenderState)) {
                return false;
            }
            final CableRenderState o = (CableRenderState)obj;
            return o.type == this.type && o.insulation == this.insulation && o.color == this.color && o.foam == this.foam && o.connectivity == this.connectivity && o.active == this.active;
        }
        
        @Override
        public String toString() {
            return "CableState<" + this.type + ", " + this.insulation + ", " + this.color + ", " + this.foam + ", " + this.connectivity + ", " + this.active + '>';
        }
    }
}

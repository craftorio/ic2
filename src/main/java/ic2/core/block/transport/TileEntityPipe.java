// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport;

import ic2.core.block.transport.items.PipeSize;
import ic2.core.block.transport.items.PipeType;
import ic2.core.block.state.UnlistedProperty;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraft.item.Item;
import ic2.core.block.transport.cover.ICoverItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Collections;
import ic2.core.block.transport.cover.CoverProperty;
import java.util.Set;
import net.minecraft.util.EnumFacing;
import net.minecraft.tileentity.TileEntity;
import ic2.core.block.transport.cover.Covers;
import net.minecraftforge.common.property.IUnlistedProperty;
import ic2.core.block.transport.cover.ICoverHolder;
import ic2.api.transport.IPipe;
import ic2.core.block.TileEntityBlock;

public abstract class TileEntityPipe extends TileEntityBlock implements IPipe, ICoverHolder
{
    public static final IUnlistedProperty<PipeRenderState> renderStateProperty;
    protected volatile PipeRenderState renderState;
    protected byte connectivity;
    protected byte covers;
    protected final Covers coversComponent;
    
    public TileEntityPipe() {
        this.connectivity = 0;
        this.covers = 0;
        this.coversComponent = this.addComponent(new Covers(this));
    }
    
    @Override
    public TileEntity getTile() {
        return this;
    }
    
    @Override
    public boolean isConnected(final EnumFacing facing) {
        return (this.connectivity & 1 << facing.ordinal()) != 0x0;
    }
    
    @Override
    public abstract void flipConnection(final EnumFacing p0);
    
    @Override
    public Set<CoverProperty> getCoverProperties() {
        return Collections.emptySet();
    }
    
    @Override
    public boolean canPlaceCover(final World world, final BlockPos pos, final EnumFacing side, final ItemStack stack) {
        final Item rawItem = stack.getItem();
        if (!(rawItem instanceof ICoverItem)) {
            return false;
        }
        final ICoverItem item = (ICoverItem)rawItem;
        return item.isSuitableFor(stack, this.getCoverProperties()) && (this.covers & 1 << side.ordinal()) == 0x0;
    }
    
    @Override
    public void placeCover(final World world, final BlockPos pos, final EnumFacing side, final ItemStack stack) {
        this.coversComponent.addCover(side, stack);
        this.covers ^= (byte)(1 << side.ordinal());
        IC2.network.get(true).updateTileEntityField(this, "covers");
    }
    
    @Override
    public boolean canRemoveCover(final World world, final BlockPos pos, final EnumFacing side) {
        return (this.covers & 1 << side.ordinal()) != 0x0;
    }
    
    @Override
    public void removeCover(final World world, final BlockPos pos, final EnumFacing side) {
        final ItemStack ret = this.coversComponent.removeCover(side);
        this.covers ^= (byte)(1 << side.ordinal());
        IC2.network.get(true).updateTileEntityField(this, "covers");
        StackUtil.dropAsEntity(this.getWorld(), this.getPos(), StackUtil.copyWithSize(ret, 1));
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (this.getWorld().isRemote) {
            this.updateRenderState();
        }
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.connectivity = nbt.getByte("connectivity");
        this.covers = nbt.getByte("covers");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("connectivity", this.connectivity);
        nbt.setByte("covers", this.covers);
        return nbt;
    }
    
    @Override
    protected void onUnloaded() {
        super.onUnloaded();
    }
    
    public void onNeighborChange(final Block neighbor, final BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
    }
    
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        if (this.world.isRemote) {
            this.updateRenderState();
        }
        super.onPlaced(stack, placer, facing);
    }
    
    protected abstract void updateConnectivity();
    
    public Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state) {
        state = super.getExtendedState(state);
        final PipeRenderState pipeRenderState = this.renderState;
        if (pipeRenderState != null) {
            state = state.withProperties(TileEntityPipe.renderStateProperty, pipeRenderState);
        }
        return state;
    }
    
    @Override
    protected SoundType getBlockSound(final Entity entity) {
        return SoundType.METAL;
    }
    
    @Override
    protected boolean isNormalCube() {
        return false;
    }
    
    @Override
    protected boolean isSideSolid(final EnumFacing side) {
        return false;
    }
    
    @Override
    protected boolean doesSideBlockRendering(final EnumFacing side) {
        return false;
    }
    
    @Override
    protected int getLightOpacity() {
        return 0;
    }
    
    @Override
    protected boolean clientNeedsExtraModelInfo() {
        return true;
    }
    
    @Override
    public void onNetworkUpdate(final String field) {
        this.updateRenderState();
        this.rerender();
        super.onNetworkUpdate(field);
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("connectivity");
        ret.add("covers");
        return ret;
    }
    
    @Override
    protected List<ItemStack> getAuxDrops(final int fortune) {
        final List<ItemStack> ret = new ArrayList<ItemStack>();
        for (final EnumFacing facing : EnumFacing.VALUES) {
            if (this.coversComponent.hasCover(facing)) {
                ret.add(this.coversComponent.removeCover(facing));
            }
        }
        return ret;
    }
    
    protected abstract void updateRenderState();
    
    static {
        renderStateProperty = (IUnlistedProperty)new UnlistedProperty("renderstate", (Class<Object>)PipeRenderState.class);
    }
    
    public static class PipeRenderState
    {
        public final PipeType type;
        public final PipeSize size;
        public final int connectivity;
        public final int covers;
        public final int facing;
        
        public PipeRenderState(final PipeType type, final PipeSize size, final int connectivity, final int covers, final int facing) {
            this.type = type;
            this.size = size;
            this.connectivity = connectivity;
            this.covers = covers;
            this.facing = facing;
        }
        
        @Override
        public int hashCode() {
            int ret = this.type.hashCode();
            ret = ret * 31 + this.size.hashCode();
            ret = ret * 31 + this.connectivity;
            ret = ret * 31 + this.covers;
            ret = ret * 31 + this.facing;
            return ret;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof PipeRenderState)) {
                return false;
            }
            final PipeRenderState o = (PipeRenderState)obj;
            return o.type == this.type && o.size == this.size && o.connectivity == this.connectivity && o.covers == this.covers && o.facing == this.facing;
        }
        
        @Override
        public String toString() {
            return "PipeState<" + this.type + ", " + this.size + ", " + this.connectivity + ", " + this.covers + ", " + this.facing + '>';
        }
    }
}

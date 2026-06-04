// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import java.util.Arrays;
import ic2.core.block.state.UnlistedProperty;
import ic2.core.ref.BlockName;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.util.Ic2Color;
import ic2.core.block.comp.Obscuration;
import net.minecraftforge.common.property.IUnlistedProperty;

public class TileEntityWall extends TileEntityBlock
{
    public static final IUnlistedProperty<WallRenderState> renderStateProperty;
    protected final Obscuration obscuration;
    private Ic2Color color;
    private volatile WallRenderState renderState;
    
    public TileEntityWall() {
        this(BlockWall.defaultColor);
    }
    
    public TileEntityWall(final Ic2Color color) {
        this.color = BlockWall.defaultColor;
        this.obscuration = this.addComponent(new Obscuration(this, new Runnable() {
            @Override
            public void run() {
                IC2.network.get(true).updateTileEntityField(TileEntityWall.this, "obscuration");
            }
        }));
        this.color = color;
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.color = Ic2Color.values[nbt.getByte("color") & 0xFF];
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("color", (byte)this.color.ordinal());
        return nbt;
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (this.getWorld().isRemote) {
            this.updateRenderState();
        }
    }
    
    @Override
    protected Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state) {
        state = super.getExtendedState(state);
        final WallRenderState value = this.renderState;
        if (value != null) {
            state = state.withProperties(TileEntityWall.renderStateProperty, value);
        }
        return state;
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = new ArrayList<String>();
        ret.add("color");
        ret.add("obscuration");
        ret.addAll(super.getNetworkedFields());
        return ret;
    }
    
    @Override
    public void onNetworkUpdate(final String field) {
        super.onNetworkUpdate(field);
        if (this.updateRenderState()) {
            this.rerender();
        }
    }
    
    @Override
    protected boolean recolor(final EnumFacing side, final EnumDyeColor mcColor) {
        final Ic2Color color = Ic2Color.get(mcColor);
        if (color == this.color) {
            return false;
        }
        this.color = color;
        if (!this.getWorld().isRemote) {
            IC2.network.get(true).updateTileEntityField(this, "obscuration");
            this.markDirty();
        }
        else if (this.updateRenderState()) {
            this.rerender();
        }
        return true;
    }
    
    @Override
    protected ItemStack getPickBlock(final EntityPlayer player, final RayTraceResult target) {
        return BlockName.wall.getItemStack(this.color);
    }
    
    @Override
    protected boolean clientNeedsExtraModelInfo() {
        return this.obscuration.hasObscuration();
    }
    
    private boolean updateRenderState() {
        final WallRenderState state = new WallRenderState(this.color, this.obscuration.getRenderState());
        if (state.equals(this.renderState)) {
            return false;
        }
        this.renderState = state;
        return true;
    }
    
    static {
        renderStateProperty = (IUnlistedProperty)new UnlistedProperty("renderstate", (Class<Object>)WallRenderState.class);
    }
    
    public static class WallRenderState
    {
        public final Ic2Color color;
        public final Obscuration.ObscurationData[] obscurations;
        
        public WallRenderState(final Ic2Color color, final Obscuration.ObscurationData[] obscurations) {
            this.color = color;
            this.obscurations = obscurations;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof WallRenderState)) {
                return false;
            }
            final WallRenderState o = (WallRenderState)obj;
            return o.color == this.color && Arrays.equals(o.obscurations, this.obscurations);
        }
        
        @Override
        public int hashCode() {
            return this.color.hashCode() * 31 + Arrays.hashCode(this.obscurations);
        }
        
        @Override
        public String toString() {
            return "WallState<" + this.color + ", " + Arrays.toString(this.obscurations) + '>';
        }
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import java.util.Collections;
import net.minecraftforge.common.capabilities.Capability;
import java.util.Collection;
import net.minecraft.util.EnumFacing;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.network.GrowingBuffer;
import java.io.IOException;
import java.io.DataInput;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.TileEntityBlock;

public abstract class TileEntityComponent
{
    protected final TileEntityBlock parent;
    
    public TileEntityComponent(final TileEntityBlock parent) {
        this.parent = parent;
    }
    
    public TileEntityBlock getParent() {
        return this.parent;
    }
    
    public void readFromNbt(final NBTTagCompound nbt) {
    }
    
    public NBTTagCompound writeToNbt() {
        return null;
    }
    
    public void onLoaded() {
    }
    
    public void onUnloaded() {
    }
    
    public void onNeighborChange(final Block srcBlock, final BlockPos srcPos) {
    }
    
    public void onContainerUpdate(final EntityPlayerMP player) {
    }
    
    public void onNetworkUpdate(final DataInput is) throws IOException {
    }
    
    public boolean enableWorldTick() {
        return false;
    }
    
    public void onWorldTick() {
    }
    
    protected void setNetworkUpdate(final EntityPlayerMP player, final GrowingBuffer data) {
        IC2.network.get(true).sendComponentUpdate(this.parent, Components.getId(this.getClass()), player, data);
    }
    
    public Collection<? extends Capability<?>> getProvidedCapabilities(final EnumFacing side) {
        return (Collection<? extends Capability<?>>)Collections.emptySet();
    }
    
    public <T> T getCapability(final Capability<T> cap, final EnumFacing side) {
        return null;
    }
}

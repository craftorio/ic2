// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiWeightedFluidDistributor;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerWeightedFluidDistributor;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import java.util.Iterator;
import ic2.core.util.LiquidUtil;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.nbt.NBTTagCompound;
import java.util.ArrayList;
import ic2.api.network.ClientModifiable;
import net.minecraft.util.EnumFacing;
import java.util.List;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityWeightedFluidDistributor extends TileEntityFluidDistributor implements IWeightedDistributor
{
    @ClientModifiable
    protected List<EnumFacing> priority;
    
    public TileEntityWeightedFluidDistributor() {
        this.priority = new ArrayList<EnumFacing>(5);
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (!this.priority.isEmpty()) {
            final int[] indexes = new int[this.priority.size()];
            for (int i = 0; i < indexes.length; ++i) {
                indexes[i] = this.priority.get(i).getIndex();
            }
            nbt.setIntArray("priority", indexes);
        }
        return nbt;
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        final int[] indexes = nbt.getIntArray("priority");
        if (indexes.length > 0) {
            for (final int index : indexes) {
                this.priority.add(EnumFacing.getFront(index));
            }
        }
    }
    
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("priority");
        return ret;
    }
    
    @Override
    protected void updateConnectivity() {
        if (!this.getWorld().isRemote && !this.priority.isEmpty() && this.priority.remove(this.getFacing())) {
            this.updatePriority(true);
        }
        this.fluids.changeConnectivity(this.fluidTank, Collections.singleton(this.getFacing()), (Collection<EnumFacing>)Collections.emptySet());
    }
    
    @Override
    protected void moveFluid() {
        if (!this.priority.isEmpty()) {
            int tankAmount = this.fluidTank.getFluidAmount();
            for (final EnumFacing dir : this.priority) {
                assert dir != this.getFacing();
                final TileEntity target = this.world.getTileEntity(this.pos.offset(dir));
                final EnumFacing side = dir.getOpposite();
                if (!LiquidUtil.isFluidTile(target, side)) {
                    continue;
                }
                final int amount = LiquidUtil.fillTile(target, side, this.fluidTank.getFluid(), false);
                if (amount <= 0) {
                    continue;
                }
                tankAmount -= amount;
                this.fluidTank.drainInternal(amount, true);
                if (tankAmount <= 0) {
                    break;
                }
            }
        }
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerWeightedFluidDistributor(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiWeightedFluidDistributor(new ContainerWeightedFluidDistributor(player, this));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public List<EnumFacing> getPriority() {
        return this.priority;
    }
    
    @Override
    public void updatePriority(final boolean server) {
        IC2.network.get(server).updateTileEntityField(this, "priority");
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        final int position = event / 10;
        final EnumFacing facing = EnumFacing.getFront(event % 10 & 0x6);
        assert position >= 0 && position <= this.priority.size() : "Position was " + position;
        assert facing != this.getFacing();
        if (position == this.priority.size()) {
            this.priority.add(facing);
        }
        else {
            this.priority.set(position, facing);
        }
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiWeightedItemDistributor;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerWeightedItemDistributor;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import java.util.Iterator;
import net.minecraft.world.World;
import ic2.core.util.StackUtil;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.IInventorySlotHolder;
import java.util.ArrayList;
import ic2.core.block.invslot.InvSlot;
import ic2.api.network.ClientModifiable;
import net.minecraft.util.EnumFacing;
import java.util.List;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityWeightedItemDistributor extends TileEntityInventory implements IHasGui, IWeightedDistributor
{
    @ClientModifiable
    protected List<EnumFacing> priority;
    public final InvSlot buffer;
    
    public TileEntityWeightedItemDistributor() {
        this.priority = new ArrayList<EnumFacing>(5);
        this.buffer = new InvSlot(this, "buffer", InvSlot.Access.I, 9);
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
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("priority");
        return ret;
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateConnectivity();
    }
    
    @Override
    protected void setFacing(final EnumFacing facing) {
        super.setFacing(facing);
        this.updateConnectivity();
    }
    
    protected void updateConnectivity() {
        if (!this.getWorld().isRemote && !this.priority.isEmpty() && this.priority.remove(this.getFacing())) {
            this.updatePriority(true);
        }
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (!this.priority.isEmpty() && !this.buffer.isEmpty()) {
            final World world = this.getWorld();
            boolean hasChanged = false;
            for (final EnumFacing facing : this.priority) {
                final TileEntity te = world.getTileEntity(this.pos.offset(facing));
                final EnumFacing side = facing.getOpposite();
                if (StackUtil.isInventoryTile(te, side)) {
                    boolean empty = true;
                    for (int index = 0; index < this.buffer.size(); ++index) {
                        if (!this.buffer.isEmpty(index)) {
                            ItemStack stack = this.buffer.get(index);
                            final ItemStack transferStack = StackUtil.copy(stack);
                            int amount = StackUtil.putInInventory(te, side, transferStack, true);
                            if (amount > 0) {
                                amount = StackUtil.putInInventory(te, side, transferStack, false);
                                stack = StackUtil.decSize(stack, amount);
                                this.buffer.put(index, stack);
                                hasChanged = true;
                                empty &= StackUtil.isEmpty(stack);
                            }
                        }
                    }
                    if (hasChanged && empty) {
                        break;
                    }
                    continue;
                }
            }
            if (hasChanged) {
                this.markDirty();
            }
        }
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerWeightedItemDistributor(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiWeightedItemDistributor(new ContainerWeightedItemDistributor(player, this));
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
    public void onGuiClosed(final EntityPlayer player) {
    }
}

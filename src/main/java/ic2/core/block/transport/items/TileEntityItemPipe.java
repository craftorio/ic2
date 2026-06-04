// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport.items;

import net.minecraft.util.math.AxisAlignedBB;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import ic2.core.item.block.ItemPipe;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemStack;
import ic2.api.transport.IItemTransportTile;
import ic2.core.block.transport.TileEntityPipe;

public class TileEntityItemPipe extends TileEntityPipe implements IItemTransportTile
{
    protected PipeType type;
    protected PipeSize size;
    protected ItemStack contents;
    
    public TileEntityItemPipe() {
        this.type = PipeType.bronze;
        this.size = PipeSize.small;
    }
    
    @Override
    public void flipConnection(final EnumFacing facing) {
    }
    
    public TileEntityItemPipe(final PipeType type, final PipeSize size) {
        this();
        this.type = type;
        this.size = size;
    }
    
    @Override
    public int putItems(final ItemStack stack, final EnumFacing facing, final boolean simulate) {
        if (StackUtil.isEmpty(stack)) {
            return 0;
        }
        if (!StackUtil.isEmpty(this.contents)) {
            return 0;
        }
        if (stack.getCount() > this.getMaxStackSizeAllowed()) {
            return 0;
        }
        if (!simulate) {
            this.contents = StackUtil.copy(stack);
        }
        return stack.getCount();
    }
    
    @Override
    public int getMaxStackSizeAllowed() {
        return this.size.maxStackSize;
    }
    
    @Override
    public int getTransferRate() {
        return this.type.transferRate;
    }
    
    @Override
    public ItemStack getContents() {
        return this.contents;
    }
    
    @Override
    public void setContents(final ItemStack stack) {
        this.contents = stack;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInventoryUpdate = false;
        if (!StackUtil.isEmpty(this.contents)) {
            final EnumFacing facing = this.getFacing();
            final TileEntity target = this.world.getTileEntity(this.pos.offset(facing));
            if (target instanceof IItemTransportTile && ((IItemTransportTile)target).putItems(this.contents, facing.getOpposite(), true) > 0) {
                final int amount = ((IItemTransportTile)target).putItems(this.contents, facing.getOpposite(), false);
                final ItemStack newStack = StackUtil.copyShrunk(this.contents, amount);
                assert newStack.isEmpty();
                this.contents = null;
                needsInventoryUpdate = true;
            }
        }
        if (needsInventoryUpdate) {
            this.markDirty();
        }
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.type = PipeType.values[nbt.getByte("type") & 0xFF];
        this.size = PipeSize.values()[nbt.getByte("size") & 0xFF];
        final NBTTagList contentsTag = nbt.getTagList("contents", 10);
        for (int i = 0; i < contentsTag.tagCount(); ++i) {
            final NBTTagCompound contentTag = contentsTag.getCompoundTagAt(i);
            final ItemStack stack = new ItemStack(contentTag);
            if (!StackUtil.isEmpty(stack)) {
                this.contents = stack;
            }
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("type", (byte)this.type.ordinal());
        nbt.setByte("size", (byte)this.size.ordinal());
        final NBTTagList contentsTag = new NBTTagList();
        if (!StackUtil.isEmpty(this.contents)) {
            final NBTTagCompound contentTag = new NBTTagCompound();
            this.contents.writeToNBT(contentTag);
            contentsTag.appendTag((NBTBase)contentTag);
            nbt.setTag("contents", (NBTBase)contentsTag);
        }
        return nbt;
    }
    
    @Override
    protected void updateConnectivity() {
    }
    
    @Override
    protected ItemStack getPickBlock(final EntityPlayer player, final RayTraceResult target) {
        return ItemPipe.getPipe(this.type, this.size);
    }
    
    @Override
    protected List<ItemStack> getAuxDrops(final int fortune) {
        final List<ItemStack> ret = new ArrayList<ItemStack>(super.getAuxDrops(fortune));
        if (!StackUtil.isEmpty(this.contents)) {
            ret.add(this.contents);
        }
        return ret;
    }
    
    @Override
    protected List<AxisAlignedBB> getAabbs(final boolean forCollision) {
        final float th = this.size.thickness;
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
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("type");
        ret.add("size");
        return ret;
    }
    
    @Override
    protected void updateRenderState() {
        this.renderState = new PipeRenderState(this.type, this.size, this.connectivity, this.covers, this.getFacing().ordinal());
    }
}

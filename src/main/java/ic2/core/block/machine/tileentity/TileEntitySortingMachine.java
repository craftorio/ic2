// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiSortingMachine;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerSortingMachine;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Iterator;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.IInventorySlotHolder;
import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemStack;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.IHasGui;

@NotClassic
public class TileEntitySortingMachine extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener, IUpgradableBlock
{
    public static final int defaultTier = 2;
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlot buffer;
    private final ItemStack[][] filters;
    public EnumFacing defaultRoute;
    
    public TileEntitySortingMachine() {
        super(15000, 2, false);
        this.defaultRoute = EnumFacing.DOWN;
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 3);
        this.buffer = new InvSlot(this, "Buffer", InvSlot.Access.IO, 11);
        this.filters = new ItemStack[6][7];
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        final NBTTagList filtersTag = nbt.getTagList("filters", 10);
        for (int i = 0; i < filtersTag.tagCount(); ++i) {
            final NBTTagCompound filterTag = filtersTag.getCompoundTagAt(i);
            final int index = filterTag.getByte("index") & 0xFF;
            final ItemStack stack = new ItemStack(filterTag);
            this.filters[index / 7][index % 7] = stack;
        }
        final int defaultRouteIdx = nbt.getByte("defaultroute");
        if (defaultRouteIdx >= 0 && defaultRouteIdx < EnumFacing.VALUES.length) {
            this.defaultRoute = EnumFacing.VALUES[defaultRouteIdx];
        }
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        final NBTTagList filtersTag = new NBTTagList();
        for (int i = 0; i < 42; ++i) {
            final ItemStack stack = this.filters[i / 7][i % 7];
            if (stack != null) {
                final NBTTagCompound contentTag = new NBTTagCompound();
                contentTag.setByte("index", (byte)i);
                stack.writeToNBT(contentTag);
                filtersTag.appendTag((NBTBase)contentTag);
            }
        }
        nbt.setTag("filters", (NBTBase)filtersTag);
        nbt.setByte("defaultroute", (byte)this.defaultRoute.ordinal());
        return nbt;
    }
    
    protected void onLoaded() {
        super.onLoaded();
        if (IC2.platform.isSimulating()) {
            this.setUpgradableBlock();
        }
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
    Label_0433:
        for (int index = 0; index < this.buffer.size(); ++index) {
            if (this.energy.getEnergy() < 20.0) {
                return;
            }
            ItemStack stack = this.buffer.get(index);
            if (!StackUtil.isEmpty(stack)) {
                for (final StackUtil.AdjacentInv inv : StackUtil.getAdjacentInventories(this)) {
                    if (inv.dir != this.defaultRoute) {
                        for (final ItemStack filterStack : this.getFilterSlots(inv.dir)) {
                            if (!StackUtil.isEmpty(filterStack)) {
                                final int filterSize = StackUtil.getSize(filterStack);
                                if (StackUtil.getSize(stack) >= filterSize && StackUtil.checkItemEquality(filterStack, stack) && this.energy.canUseEnergy(filterSize * 20)) {
                                    final ItemStack transferStack = StackUtil.copyWithSize(stack, filterSize);
                                    int amount = StackUtil.putInInventory(inv.te, inv.dir, transferStack, true);
                                    if (amount != filterSize) {
                                        break;
                                    }
                                    amount = StackUtil.putInInventory(inv.te, inv.dir, transferStack, false);
                                    stack = StackUtil.decSize(stack, amount);
                                    this.buffer.put(index, stack);
                                    this.energy.useEnergy(amount * 20);
                                    if (StackUtil.isEmpty(stack)) {
                                        continue Label_0433;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        boolean inFilter = false;
                    Label_0362:
                        for (final ItemStack[] array : this.filters) {
                            final ItemStack[] sideFilters = array;
                            for (final ItemStack filter : array) {
                                if (StackUtil.checkItemEquality(filter, stack)) {
                                    inFilter = true;
                                    break Label_0362;
                                }
                            }
                        }
                        if (inFilter) {
                            continue;
                        }
                        final int amount2 = StackUtil.putInInventory(inv.te, inv.dir, StackUtil.copyWithSize(stack, 1), false);
                        if (amount2 <= 0) {
                            break;
                        }
                        stack = StackUtil.decSize(stack, amount2);
                        this.buffer.put(index, stack);
                        this.energy.useEnergy(20.0);
                        if (StackUtil.isEmpty(stack)) {
                            break;
                        }
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        if (event >= 0 && event <= 5) {
            this.defaultRoute = EnumFacing.VALUES[event];
        }
    }
    
    @Override
    public ContainerBase<TileEntitySortingMachine> getGuiContainer(final EntityPlayer player) {
        return new ContainerSortingMachine(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiSortingMachine(new ContainerSortingMachine(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Transformer);
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            this.setUpgradableBlock();
        }
    }
    
    public void setUpgradableBlock() {
        this.energy.setSinkTier(this.upgradeSlot.getTier(2));
    }
    
    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    public ItemStack[] getFilterSlots(final EnumFacing side) {
        return this.filters[side.ordinal()];
    }
}

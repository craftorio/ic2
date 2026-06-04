// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.steam;

import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.IC2;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.network.GuiSynced;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public class TileEntityBurningBox extends TileEntityInventory implements IHasGui, IGuiValueProvider
{
    protected int heat;
    protected int delta;
    public int activityMeter;
    public int ticksSinceLastActiveUpdate;
    @GuiSynced
    public int fuel;
    @GuiSynced
    private int remainingFuel;
    public final InvSlotConsumableFuel fuelSlot;
    public final InvSlotOutput ashesSlot;
    
    public TileEntityBurningBox() {
        this.heat = 0;
        this.delta = 0;
        this.activityMeter = 0;
        this.fuel = 0;
        this.remainingFuel = 0;
        this.ticksSinceLastActiveUpdate = IC2.random.nextInt(128);
        this.fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);
        this.ashesSlot = new InvSlotOutput(this, "ashes", 1);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.delta = nbt.getInteger("delta");
        this.fuel = nbt.getInteger("fuel");
        this.remainingFuel = nbt.getInteger("remainingFuel");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("delta", this.delta);
        nbt.setInteger("fuel", this.fuel);
        nbt.setInteger("remainingFuel", this.remainingFuel);
        return nbt;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInventoryUpdate = false;
        if (this.needsFuel()) {
            needsInventoryUpdate = this.gainFuel();
        }
        final boolean newActive = this.work();
        if (needsInventoryUpdate) {
            this.markDirty();
        }
        if (!this.delayActiveUpdate()) {
            this.setActive(newActive);
        }
        else {
            if (this.ticksSinceLastActiveUpdate % 128 == 0) {
                this.setActive(this.activityMeter > 0);
                this.activityMeter = 0;
            }
            if (newActive) {
                ++this.activityMeter;
            }
            else {
                --this.activityMeter;
            }
            ++this.ticksSinceLastActiveUpdate;
        }
    }
    
    public int getProvidedHeat(final EnumFacing side) {
        return (side == EnumFacing.UP) ? this.heat : 0;
    }
    
    public boolean needsFuel() {
        return this.fuel <= 0;
    }
    
    public boolean gainFuel() {
        if (!this.ashesSlot.canAdd(ItemName.misc_resource.getItemStack(MiscResourceType.ashes))) {
            return false;
        }
        final int fuelValue = this.fuelSlot.consumeFuel() / 4;
        if (fuelValue == 0) {
            return false;
        }
        this.fuel += fuelValue;
        this.remainingFuel = fuelValue;
        return true;
    }
    
    public boolean work() {
        if (this.fuel > 0) {
            --this.fuel;
            if (this.fuel == 0 && (int)(Math.random() * 2.0) == 1) {
                this.ashesSlot.add(ItemName.misc_resource.getItemStack(MiscResourceType.ashes));
            }
            this.delta = Math.min(++this.delta, 1100);
            final int temp = 55 - this.delta / 20;
            this.heat = 1400 + (int)(0.008 * -(temp * temp * temp));
            return true;
        }
        final int delta = this.delta - 1;
        this.delta = delta;
        this.delta = Math.max(delta, 0);
        final int temp = this.delta / 20;
        this.heat = (int)(0.008 * (temp * temp * temp));
        return false;
    }
    
    public boolean delayActiveUpdate() {
        return false;
    }
    
    @Override
    public ContainerBase<TileEntityBurningBox> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("fuel".equals(name)) {
            return (this.fuel == 0) ? 0.0 : (this.fuel / (double)this.remainingFuel);
        }
        throw new IllegalArgumentException("Unexpected value requested: " + name);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final List<String> tooltip, final ITooltipFlag advanced) {
        tooltip.add("");
        tooltip.add("Maximum temperature:");
        tooltip.add(" 1400K");
        tooltip.add("");
        tooltip.add("Time to reach maximum temperature:");
        tooltip.add(" 55 seconds");
        tooltip.add("");
    }
}

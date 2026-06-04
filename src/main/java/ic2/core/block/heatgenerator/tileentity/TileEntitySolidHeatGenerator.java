// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.heatgenerator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityHeatSourceInventory;

@NotClassic
public class TileEntitySolidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui, IGuiValueProvider
{
    private int heatbuffer;
    public int activityMeter;
    public int ticksSinceLastActiveUpdate;
    @GuiSynced
    public int fuel;
    @GuiSynced
    public int itemFuelTime;
    public final InvSlotConsumableFuel fuelSlot;
    public final InvSlotOutput outputslot;
    public static final int emittedHU;
    
    public TileEntitySolidHeatGenerator() {
        this.heatbuffer = 0;
        this.activityMeter = 0;
        this.fuel = 0;
        this.itemFuelTime = 0;
        this.fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);
        this.outputslot = new InvSlotOutput(this, "output", 1);
        this.ticksSinceLastActiveUpdate = IC2.random.nextInt(256);
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.needsFuel()) {
            needsInvUpdate = this.gainFuel();
        }
        final boolean newActive = this.gainheat();
        if (needsInvUpdate) {
            this.markDirty();
        }
        if (!this.delayActiveUpdate()) {
            this.setActive(newActive);
        }
        else {
            if (this.ticksSinceLastActiveUpdate % 256 == 0) {
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
    
    public boolean gainheat() {
        if (this.isConverting()) {
            this.heatbuffer += this.getMaxHeatEmittedPerTick();
            --this.fuel;
            if (this.fuel == 0 && (int)(Math.random() * 2.0) == 1) {
                this.outputslot.add(ItemName.misc_resource.getItemStack(MiscResourceType.ashes));
            }
            return true;
        }
        return false;
    }
    
    public boolean needsFuel() {
        return this.fuel <= 0 && this.getHeatBuffer() == 0;
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.fuel = nbt.getInteger("fuel");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("fuel", this.fuel);
        return nbt;
    }
    
    public boolean delayActiveUpdate() {
        return false;
    }
    
    public boolean gainFuel() {
        if (!this.outputslot.canAdd(ItemName.misc_resource.getItemStack(MiscResourceType.ashes))) {
            return false;
        }
        final int fuelValue = this.fuelSlot.consumeFuel() / 4;
        if (fuelValue == 0) {
            return false;
        }
        this.fuel += fuelValue;
        this.itemFuelTime = fuelValue;
        return true;
    }
    
    public boolean isConverting() {
        return this.fuel > 0;
    }
    
    @Override
    protected int fillHeatBuffer(int maxAmount) {
        if (this.heatbuffer - maxAmount >= 0) {
            this.heatbuffer -= maxAmount;
            return maxAmount;
        }
        maxAmount = this.heatbuffer;
        this.heatbuffer = 0;
        return maxAmount;
    }
    
    @Override
    public int getMaxHeatEmittedPerTick() {
        return TileEntitySolidHeatGenerator.emittedHU;
    }
    
    @Override
    public ContainerBase<TileEntitySolidHeatGenerator> getGuiContainer(final EntityPlayer player) {
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
            return (this.fuel == 0) ? 0.0 : (this.fuel / (double)this.itemFuelTime);
        }
        throw new IllegalArgumentException("Unexpected value requested: " + name);
    }
    
    static {
        emittedHU = Math.round(20.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/solid"));
    }
}

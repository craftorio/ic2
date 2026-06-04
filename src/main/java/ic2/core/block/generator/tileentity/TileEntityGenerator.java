// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.generator.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.tileentity.TileEntityIronFurnace;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.gui.dynamic.IGuiValueProvider;

public class TileEntityGenerator extends TileEntityBaseGenerator implements IGuiValueProvider
{
    public final InvSlotConsumableFuel fuelSlot;
    @GuiSynced
    public int totalFuel;
    
    public TileEntityGenerator() {
        super(Math.round(10.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/generator")), 1, 4000);
        this.totalFuel = 0;
        this.fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    protected void updateEntityClient() {
        super.updateEntityClient();
        if (this.getActive()) {
            TileEntityIronFurnace.showFlames(this.getWorld(), this.pos, this.getFacing());
        }
    }
    
    public double getFuelRatio() {
        if (this.fuel <= 0) {
            return 0.0;
        }
        return this.fuel / (double)this.totalFuel;
    }
    
    @Override
    public boolean gainFuel() {
        final int fuelValue = this.fuelSlot.consumeFuel() / 4;
        if (fuelValue == 0) {
            return false;
        }
        this.fuel += fuelValue;
        this.totalFuel = fuelValue;
        return true;
    }
    
    @Override
    public boolean isConverting() {
        return this.fuel > 0;
    }
    
    @Override
    public String getOperationSoundFile() {
        return "Generators/GeneratorLoop.ogg";
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("fuel".equals(name)) {
            return this.getFuelRatio();
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.totalFuel = nbt.getInteger("totalFuel");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("totalFuel", this.totalFuel);
        return nbt;
    }
}

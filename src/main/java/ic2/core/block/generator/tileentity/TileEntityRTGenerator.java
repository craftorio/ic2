// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.generator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityRTGenerator extends TileEntityBaseGenerator
{
    public final InvSlotConsumable fuelSlot;
    private static final float efficiency;
    
    public TileEntityRTGenerator() {
        super(Math.round(16.0f * TileEntityRTGenerator.efficiency), 1, 20000);
        (this.fuelSlot = new InvSlotConsumableItemStack(this, "fuel", 6, new ItemStack[] { ItemName.nuclear.getItemStack(NuclearResourceType.rtg_pellet) })).setStackSizeLimit(1);
    }
    
    @Override
    public boolean gainEnergy() {
        int counter = 0;
        for (int i = 0; i < this.fuelSlot.size(); ++i) {
            if (!this.fuelSlot.isEmpty(i)) {
                ++counter;
            }
        }
        if (counter == 0) {
            return false;
        }
        this.energy.addEnergy(Math.pow(2.0, counter - 1) * TileEntityRTGenerator.efficiency);
        return true;
    }
    
    @Override
    public boolean gainFuel() {
        return false;
    }
    
    @Override
    public boolean needsFuel() {
        return false;
    }
    
    @Override
    protected boolean delayActiveUpdate() {
        return true;
    }
    
    static {
        efficiency = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/radioisotope");
    }
}

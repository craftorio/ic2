// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;

public class ItemArmorBatpack extends ItemArmorElectric
{
    public ItemArmorBatpack() {
        super(ItemName.batpack, "batpack", EntityEquipmentSlot.CHEST, 60000.0, 100.0, 1);
    }
    
    @Override
    public boolean canProvideEnergy(final ItemStack stack) {
        return true;
    }
    
    @Override
    public double getDamageAbsorptionRatio() {
        return 0.0;
    }
    
    @Override
    public int getEnergyPerDamage() {
        return 0;
    }
}

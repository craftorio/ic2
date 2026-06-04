// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;

public class ItemArmorClassicCFPack extends ItemArmorUtility
{
    public ItemArmorClassicCFPack() {
        super(ItemName.cf_pack, "batpack", EntityEquipmentSlot.CHEST);
        this.setMaxDamage(260);
    }
    
    public boolean getCFPellet(final EntityPlayer player, final ItemStack pack) {
        if (pack.getItemDamage() > 0) {
            pack.setItemDamage(pack.getItemDamage() - 1);
            return true;
        }
        return false;
    }
    
    public double getDurabilityForDisplay(final ItemStack stack) {
        return 1.0 - super.getDurabilityForDisplay(stack);
    }
}

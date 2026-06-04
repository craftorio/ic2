// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.entity.EntityLivingBase;
import ic2.api.item.ElectricItem;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemScannerAdv extends ItemScanner
{
    public ItemScannerAdv() {
        super(ItemName.advanced_scanner, 1000000.0, 512.0, 2);
    }
    
    @Override
    public int startLayerScan(final ItemStack stack) {
        return ElectricItem.manager.use(stack, 250.0, null) ? (this.getScanRange() / 2) : 0;
    }
    
    @Override
    public int getScanRange() {
        return 12;
    }
}

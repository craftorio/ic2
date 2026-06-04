// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import java.util.Iterator;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityChargepadBatBox extends TileEntityChargepadBlock
{
    public TileEntityChargepadBatBox() {
        super(1, 32, 40000);
    }
    
    @Override
    protected void getItems(final EntityPlayer player) {
        if (player != null) {
            for (final ItemStack current : player.inventory.armorInventory) {
                if (current == null) {
                    continue;
                }
                this.chargeItem(current, 32);
            }
            for (final ItemStack current : player.inventory.mainInventory) {
                if (current == null) {
                    continue;
                }
                this.chargeItem(current, 32);
            }
        }
    }
}

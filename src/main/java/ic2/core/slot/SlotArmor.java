// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.slot;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.Item;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;

public class SlotArmor extends Slot
{
    private final EntityEquipmentSlot armorType;
    
    public SlotArmor(final InventoryPlayer inventory, final EntityEquipmentSlot armorType, final int x, final int y) {
        super((IInventory)inventory, 36 + armorType.getIndex(), x, y);
        this.armorType = armorType;
    }
    
    public boolean isItemValid(final ItemStack stack) {
        final Item item = stack.getItem();
        return item != null && item.isValidArmor(stack, this.armorType, (Entity)((InventoryPlayer)this.inventory).player);
    }
    
    @SideOnly(Side.CLIENT)
    public String getSlotTexture() {
        return ItemArmor.EMPTY_SLOT_NAMES[this.armorType.getIndex()];
    }
}

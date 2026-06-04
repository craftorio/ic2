// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.inventory.Slot;
import net.minecraft.inventory.IInventory;
import ic2.core.slot.SlotBoxable;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.item.ContainerHandHeldInventory;

public class ContainerToolbox extends ContainerHandHeldInventory<HandHeldToolbox>
{
    protected static final int height = 166;
    protected static final int windowBorder = 8;
    protected static final int slotSize = 16;
    protected static final int slotDistance = 2;
    protected static final int slotSeparator = 4;
    protected static final int hotbarYOffset = -24;
    protected static final int inventoryYOffset = -82;
    
    public ContainerToolbox(final EntityPlayer player, final HandHeldToolbox Toolbox1) {
        super(Toolbox1);
        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer((Slot)new SlotBoxable((IInventory)Toolbox1, col, 8 + col * 18, 41));
        }
        for (int row = 0; row < 3; ++row) {
            for (int col2 = 0; col2 < 9; ++col2) {
                this.addSlotToContainer(new Slot((IInventory)player.inventory, col2 + row * 9 + 9, 8 + col2 * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot((IInventory)player.inventory, col, 8 + col * 18, 142));
        }
    }
}

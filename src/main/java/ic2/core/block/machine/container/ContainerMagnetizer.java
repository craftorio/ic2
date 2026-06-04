// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import ic2.core.slot.SlotArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;

public class ContainerMagnetizer extends ContainerElectricMachine<TileEntityMagnetizer>
{
    public final EntityPlayer player;
    
    public ContainerMagnetizer(final EntityPlayer player, final TileEntityMagnetizer base1) {
        super(player, base1, 166, 8, 44);
        this.player = player;
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(base1.upgradeSlot, i, 152, 8 + i * 18));
        }
        this.addSlotToContainer((Slot)new SlotArmor(player.inventory, EntityEquipmentSlot.FEET, 45, 26));
    }
}

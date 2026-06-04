package ic2.core.block.personal;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerEnergyOMatClosed extends ContainerFullInv<TileEntityEnergyOMat> {
  private int lastTier;
  
  public ContainerEnergyOMatClosed(EntityPlayer player, TileEntityEnergyOMat tileEntity1) {
    super(player, (IInventory)tileEntity1, 166);
    this.lastTier = -1;
    addSlotToContainer((Slot)new SlotInvSlotReadOnly(tileEntity1.demandSlot, 0, 50, 17));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity1.inputSlot, 0, 143, 17));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity1.chargeSlot, 0, 143, 53));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("paidFor");
    ret.add("euOffer");
    return ret;
  }
  
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
    for (IContainerListener listener : this.listeners) {
      if (((TileEntityEnergyOMat)this.base).chargeSlot.tier != this.lastTier)
        listener.sendWindowProperty((Container)this, 0, ((TileEntityEnergyOMat)this.base).chargeSlot.tier); 
    } 
    this.lastTier = ((TileEntityEnergyOMat)this.base).chargeSlot.tier;
  }
  
  public void updateProgressBar(int index, int value) {
    super.updateProgressBar(index, value);
    switch (index) {
      case 0:
        ((TileEntityEnergyOMat)this.base).chargeSlot.tier = value;
        break;
    } 
  }
}

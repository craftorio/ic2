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
    func_75146_a((Slot)new SlotInvSlotReadOnly(tileEntity1.demandSlot, 0, 50, 17));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.inputSlot, 0, 143, 17));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.chargeSlot, 0, 143, 53));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("paidFor");
    ret.add("euOffer");
    return ret;
  }
  
  public void func_75142_b() {
    super.func_75142_b();
    for (IContainerListener listener : this.field_75149_d) {
      if (((TileEntityEnergyOMat)this.base).chargeSlot.tier != this.lastTier)
        listener.func_71112_a((Container)this, 0, ((TileEntityEnergyOMat)this.base).chargeSlot.tier); 
    } 
    this.lastTier = ((TileEntityEnergyOMat)this.base).chargeSlot.tier;
  }
  
  public void func_75137_b(int index, int value) {
    super.func_75137_b(index, value);
    switch (index) {
      case 0:
        ((TileEntityEnergyOMat)this.base).chargeSlot.tier = value;
        break;
    } 
  }
}

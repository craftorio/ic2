package ic2.core.block.reactor.container;

import ic2.core.ContainerBase;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerNuclearReactor extends ContainerBase<TileEntityNuclearReactorElectric> {
  private final int size;
  
  public ContainerNuclearReactor(EntityPlayer player, TileEntityNuclearReactorElectric te) {
    super((IInventory)te);
    this.size = te.getReactorSize();
    int startX = 26;
    int startY = 25;
    int slotCount = te.reactorSlot.size();
    for (int i = 0; i < slotCount; i++) {
      int x = i % this.size;
      int y = i / this.size;
      func_75146_a((Slot)new SlotInvSlot((InvSlot)te.reactorSlot, i, startX + 18 * x, startY + 18 * y));
    } 
    addPlayerInventorySlots(player, 214, 243);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.coolantinputSlot, 0, 8, 25));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.hotcoolinputSlot, 0, 188, 25));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.coolantoutputSlot, 0, 8, 115));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.hotcoolantoutputSlot, 0, 188, 115));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("heat");
    ret.add("maxHeat");
    ret.add("EmitHeat");
    ret.add("inputTank");
    ret.add("outputTank");
    ret.add("fluidCooled");
    return ret;
  }
}

package ic2.core.item;

import ic2.core.ContainerBase;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketHeldItemChange;

public class ContainerHandHeldInventory<T extends HandHeldInventory> extends ContainerBase<T> {
  public ContainerHandHeldInventory(T inventory) {
    super((IInventory)inventory);
  }
  
  public ItemStack slotClick(int slot, int button, ClickType type, EntityPlayer player) {
    boolean swapOut, swapTo, closeGUI = false;
    switch (type) {
      case CLONE:
        break;
      case PICKUP:
        if (slot >= 0 && slot < this.inventorySlots.size())
          closeGUI = ((HandHeldInventory)this.base).isThisContainer(((Slot)this.inventorySlots.get(slot)).getStack()); 
        break;
      case PICKUP_ALL:
      case QUICK_CRAFT:
        break;
      case QUICK_MOVE:
        if (slot >= 0 && slot < this.inventorySlots.size() && ((HandHeldInventory)this.base).isThisContainer(((Slot)this.inventorySlots.get(slot)).getStack()))
          return StackUtil.emptyStack; 
        break;
      case SWAP:
        assert slot >= 0 && slot < this.inventorySlots.size();
        assert getSlotFromInventory((IInventory)player.inventory, button) != null;
        swapOut = ((HandHeldInventory)this.base).isThisContainer(getSlotFromInventory((IInventory)player.inventory, button).getStack());
        swapTo = ((HandHeldInventory)this.base).isThisContainer(((Slot)this.inventorySlots.get(slot)).getStack());
        if (swapOut || swapTo)
          for (int i = 0; i < 9; i++) {
            if ((swapOut && slot == (getSlotFromInventory((IInventory)player.inventory, i)).slotNumber) || (swapTo && button == i)) {
              if (player instanceof EntityPlayerMP)
                ((EntityPlayerMP)player).connection.sendPacket((Packet)new SPacketHeldItemChange(i)); 
              break;
            } 
          }  
        break;
      case THROW:
        if (slot >= 0 && slot < this.inventorySlots.size())
          closeGUI = ((HandHeldInventory)this.base).isThisContainer(((Slot)this.inventorySlots.get(slot)).getStack()); 
        break;
      default:
        throw new RuntimeException("Unexpected ClickType: " + type);
    } 
    ItemStack stack = super.slotClick(slot, button, type, player);
    if (closeGUI && !(player.getEntityWorld()).isRemote) {
      ((HandHeldInventory)this.base).saveAsThrown(stack);
      player.closeScreen();
    } else if (type == ClickType.CLONE) {
      ItemStack held = player.inventory.getItemStack();
      if (((HandHeldInventory)this.base).isThisContainer(held))
        held.getTagCompound().removeTag("uid"); 
    } 
    return stack;
  }
  
  public void onContainerClosed(EntityPlayer player) {
    ((HandHeldInventory)this.base).onGuiClosed(player);
    super.onContainerClosed(player);
  }
}

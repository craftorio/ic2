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
  
  public ItemStack func_184996_a(int slot, int button, ClickType type, EntityPlayer player) {
    boolean swapOut, swapTo, closeGUI = false;
    switch (type) {
      case CLONE:
        break;
      case PICKUP:
        if (slot >= 0 && slot < this.field_75151_b.size())
          closeGUI = ((HandHeldInventory)this.base).isThisContainer(((Slot)this.field_75151_b.get(slot)).func_75211_c()); 
        break;
      case PICKUP_ALL:
      case QUICK_CRAFT:
        break;
      case QUICK_MOVE:
        if (slot >= 0 && slot < this.field_75151_b.size() && ((HandHeldInventory)this.base).isThisContainer(((Slot)this.field_75151_b.get(slot)).func_75211_c()))
          return StackUtil.emptyStack; 
        break;
      case SWAP:
        assert slot >= 0 && slot < this.field_75151_b.size();
        assert func_75147_a((IInventory)player.inventory, button) != null;
        swapOut = ((HandHeldInventory)this.base).isThisContainer(func_75147_a((IInventory)player.inventory, button).func_75211_c());
        swapTo = ((HandHeldInventory)this.base).isThisContainer(((Slot)this.field_75151_b.get(slot)).func_75211_c());
        if (swapOut || swapTo)
          for (int i = 0; i < 9; i++) {
            if ((swapOut && slot == (func_75147_a((IInventory)player.inventory, i)).field_75222_d) || (swapTo && button == i)) {
              if (player instanceof EntityPlayerMP)
                ((EntityPlayerMP)player).field_71135_a.func_147359_a((Packet)new SPacketHeldItemChange(i)); 
              break;
            } 
          }  
        break;
      case THROW:
        if (slot >= 0 && slot < this.field_75151_b.size())
          closeGUI = ((HandHeldInventory)this.base).isThisContainer(((Slot)this.field_75151_b.get(slot)).func_75211_c()); 
        break;
      default:
        throw new RuntimeException("Unexpected ClickType: " + type);
    } 
    ItemStack stack = super.func_184996_a(slot, button, type, player);
    if (closeGUI && !(player.getEntityWorld()).isRemote) {
      ((HandHeldInventory)this.base).saveAsThrown(stack);
      player.func_71053_j();
    } else if (type == ClickType.CLONE) {
      ItemStack held = player.inventory.func_70445_o();
      if (((HandHeldInventory)this.base).isThisContainer(held))
        held.func_77978_p().func_82580_o("uid"); 
    } 
    return stack;
  }
  
  public void func_75134_a(EntityPlayer player) {
    ((HandHeldInventory)this.base).onGuiClosed(player);
    super.func_75134_a(player);
  }
}

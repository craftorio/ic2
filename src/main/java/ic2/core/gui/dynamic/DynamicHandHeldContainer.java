package ic2.core.gui.dynamic;

import ic2.core.item.tool.HandHeldInventory;
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class DynamicHandHeldContainer<T extends HandHeldInventory> extends DynamicContainer<T> {
  public static <T extends HandHeldInventory> DynamicHandHeldContainer<T> create(T base, EntityPlayer player, GuiParser.GuiNode guiNode) {
    return new DynamicHandHeldContainer<>(base, player, guiNode);
  }
  
  protected DynamicHandHeldContainer(T base, EntityPlayer player, GuiParser.GuiNode guiNode) {
    super(base, player, guiNode);
  }
  
  protected SlotHologramSlot.ChangeCallback getCallback() {
    return ((HandHeldInventory)this.base).makeSaveCallback();
  }
  
  public void onContainerEvent(String event) {
    ((HandHeldInventory)this.base).onEvent(event);
    super.onContainerEvent(event);
  }
  
  public ItemStack slotClick(int slot, int button, ClickType type, EntityPlayer player) {
    boolean thrown = false;
    Slot realSlot = null;
    if (!(player.getEntityWorld()).isRemote && slot >= 0 && slot < this.inventorySlots.size()) {
      realSlot = this.inventorySlots.get(slot);
      thrown = ((HandHeldInventory)this.base).isThisContainer(realSlot.getStack());
    } 
    ItemStack stack = super.slotClick(slot, button, type, player);
    if (thrown && !realSlot.getHasStack()) {
      ((HandHeldInventory)this.base).saveAsThrown(stack);
      player.closeScreen();
    } 
    return stack;
  }
  
  public void onContainerClosed(EntityPlayer player) {
    ((HandHeldInventory)this.base).onGuiClosed(player);
    super.onContainerClosed(player);
  }
}

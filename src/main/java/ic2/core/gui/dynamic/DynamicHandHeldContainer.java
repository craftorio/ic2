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
  
  public ItemStack func_184996_a(int slot, int button, ClickType type, EntityPlayer player) {
    boolean thrown = false;
    Slot realSlot = null;
    if (!(player.func_130014_f_()).field_72995_K && slot >= 0 && slot < this.field_75151_b.size()) {
      realSlot = this.field_75151_b.get(slot);
      thrown = ((HandHeldInventory)this.base).isThisContainer(realSlot.func_75211_c());
    } 
    ItemStack stack = super.func_184996_a(slot, button, type, player);
    if (thrown && !realSlot.func_75216_d()) {
      ((HandHeldInventory)this.base).saveAsThrown(stack);
      player.func_71053_j();
    } 
    return stack;
  }
  
  public void func_75134_a(EntityPlayer player) {
    ((HandHeldInventory)this.base).onGuiClosed(player);
    super.func_75134_a(player);
  }
}

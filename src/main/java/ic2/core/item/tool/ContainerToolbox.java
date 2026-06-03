package ic2.core.item.tool;

import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.slot.SlotBoxable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerToolbox extends ContainerHandHeldInventory<HandHeldToolbox> {
  protected static final int height = 166;
  
  protected static final int windowBorder = 8;
  
  protected static final int slotSize = 16;
  
  protected static final int slotDistance = 2;
  
  protected static final int slotSeparator = 4;
  
  protected static final int hotbarYOffset = -24;
  
  protected static final int inventoryYOffset = -82;
  
  public ContainerToolbox(EntityPlayer player, HandHeldToolbox Toolbox1) {
    super(Toolbox1);
    for (int i = 0; i < 9; i++)
      func_75146_a((Slot)new SlotBoxable((IInventory)Toolbox1, i, 8 + i * 18, 41)); 
    for (int row = 0; row < 3; row++) {
      for (int j = 0; j < 9; j++)
        func_75146_a(new Slot((IInventory)player.field_71071_by, j + row * 9 + 9, 8 + j * 18, 84 + row * 18)); 
    } 
    for (int col = 0; col < 9; col++)
      func_75146_a(new Slot((IInventory)player.field_71071_by, col, 8 + col * 18, 142)); 
  }
}

package ic2.core.gui.dynamic;

import ic2.core.ContainerBase;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.network.GuiSynced;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.slot.SlotInvSlot;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;

public class DynamicContainer<T extends IInventory> extends ContainerBase<T> {
  public static <T extends IInventory> DynamicContainer<T> create(T base, EntityPlayer player, GuiParser.GuiNode guiNode) {
    return new DynamicContainer<>(base, player, guiNode);
  }
  
  protected DynamicContainer(T base, EntityPlayer player, GuiParser.GuiNode guiNode) {
    super((IInventory)base);
    initialize(player, guiNode, guiNode);
  }
  
  private void initialize(EntityPlayer player, GuiParser.GuiNode guiNode, GuiParser.ParentNode parentNode) {
    for (GuiParser.Node rawNode : parentNode.getNodes()) {
      GuiParser.PlayerInventoryNode playerInventoryNode;
      GuiParser.SlotNode slotNode;
      GuiParser.SlotGridNode slotGridNode;
      GuiParser.SlotHologramNode node;
      int xOffset;
      InvSlot slot;
      int x;
      int yOffset;
      int i;
      int size;
      int y;
      int width;
      int j;
      int height;
      int row;
      int col;
      switch (rawNode.getType()) {
        case environment:
          if (((GuiParser.EnvironmentNode)rawNode).environment != GuiEnvironment.GAME)
            continue; 
          break;
        case playerinventory:
          playerInventoryNode = (GuiParser.PlayerInventoryNode)rawNode;
          xOffset = (playerInventoryNode.style.width - 16) / 2;
          yOffset = (playerInventoryNode.style.height - 16) / 2;
          width = playerInventoryNode.style.width + playerInventoryNode.spacing;
          height = playerInventoryNode.style.height + playerInventoryNode.spacing;
          for (row = 0; row < 3; row++) {
            for (int k = 0; k < 9; k++)
              addSlotToContainer(new Slot((IInventory)player.inventory, k + row * 9 + 9, playerInventoryNode.x + k * width + xOffset, playerInventoryNode.y + row * height + yOffset)); 
          } 
          for (col = 0; col < 9; col++)
            addSlotToContainer(new Slot((IInventory)player.inventory, col, playerInventoryNode.x + col * width + xOffset, playerInventoryNode.y + playerInventoryNode.hotbarOffset + yOffset)); 
          break;
        case slot:
          if (!(this.base instanceof IInventorySlotHolder))
            throw new RuntimeException("Invalid base " + this.base + " for slot elements"); 
          slotNode = (GuiParser.SlotNode)rawNode;
          slot = ((IInventorySlotHolder)this.base).getInventorySlot(slotNode.name);
          if (slot == null)
            throw new RuntimeException("Invalid InvSlot name " + slotNode.name + " for base " + this.base); 
          i = slotNode.x + (slotNode.style.width - 16) / 2;
          j = slotNode.y + (slotNode.style.height - 16) / 2;
          addSlotToContainer((Slot)new SlotInvSlot(slot, slotNode.index, i, j));
          break;
        case slotgrid:
          if (!(this.base instanceof IInventorySlotHolder))
            throw new RuntimeException("Invalid base " + this.base + " for slot elements"); 
          slotGridNode = (GuiParser.SlotGridNode)rawNode;
          slot = ((IInventorySlotHolder)this.base).getInventorySlot(slotGridNode.name);
          if (slot == null)
            throw new RuntimeException("Invalid InvSlot name " + slotGridNode.name + " for base " + this.base); 
          size = slot.size();
          if (size > slotGridNode.offset) {
            int x0 = slotGridNode.x + (slotGridNode.style.width - 16) / 2;
            int y0 = slotGridNode.y + (slotGridNode.style.height - 16) / 2;
            GuiParser.SlotGridNode.SlotGridDimension dim = slotGridNode.getDimension(size);
            int rows = dim.rows;
            int cols = dim.cols;
            int k = slotGridNode.style.width + slotGridNode.spacing;
            int m = slotGridNode.style.height + slotGridNode.spacing;
            int idx = slotGridNode.offset;
            if (!slotGridNode.vertical) {
              int i2 = y0;
              for (int i3 = 0; i3 < rows && idx < size; i3++) {
                int i4 = x0;
                for (int i5 = 0; i5 < cols && idx < size; i5++) {
                  addSlotToContainer((Slot)new SlotInvSlot(slot, idx, i4, i2));
                  idx++;
                  i4 += k;
                } 
                i2 += m;
              } 
              break;
            } 
            int n = x0;
            for (int i1 = 0; i1 < cols && idx < size; i1++) {
              int i2 = y0;
              for (int i3 = 0; i3 < rows && idx < size; i3++) {
                addSlotToContainer((Slot)new SlotInvSlot(slot, idx, n, i2));
                idx++;
                i2 += m;
              } 
              n += k;
            } 
          } 
          break;
        case slothologram:
          if (!(this.base instanceof IHolographicSlotProvider))
            throw new RuntimeException("Invalid base " + this.base + " for holographic slot elements"); 
          node = (GuiParser.SlotHologramNode)rawNode;
          x = node.x + (node.style.width - 16) / 2;
          y = node.y + (node.style.height - 16) / 2;
          addSlotToContainer((Slot)new SlotHologramSlot(((IHolographicSlotProvider)this.base).getStacksForName(node.name), node.index, x, y, node.stackSizeLimit, getCallback()));
          break;
      } 
      if (rawNode instanceof GuiParser.ParentNode)
        initialize(player, guiNode, (GuiParser.ParentNode)rawNode); 
    } 
  }
  
  protected SlotHologramSlot.ChangeCallback getCallback() {
    return null;
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = networkedFieldCache.get(this.base.getClass());
    if (ret != null)
      return ret; 
    ret = new ArrayList<>();
    Class<?> cls = this.base.getClass();
    do {
      for (Field field : cls.getDeclaredFields()) {
        if (field.getAnnotation(GuiSynced.class) != null)
          ret.add(field.getName()); 
      } 
      cls = cls.getSuperclass();
    } while (cls != TileEntity.class && cls != Object.class);
    if (ret.isEmpty()) {
      ret = Collections.emptyList();
    } else {
      ret = new ArrayList<>(ret);
    } 
    networkedFieldCache.put(this.base.getClass(), ret);
    return ret;
  }
  
  private static Map<Class<?>, List<String>> networkedFieldCache = new IdentityHashMap<>();
}

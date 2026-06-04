package ic2.core.item.upgrade;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.gui.GuiDefaultBackground;
import ic2.core.gui.GuiElement;
import ic2.core.gui.MouseButton;
import ic2.core.gui.ScrollableList;
import ic2.core.gui.SlotGrid;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.slot.SlotHologramSlot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldOre extends HandHeldUpgradeOption {
  public class ContainerEditOre extends ContainerHandHeldInventory<HandHeldOre> {
    static final int HEIGHT = 200;
    
    public ContainerEditOre() {
      super(HandHeldOre.this);
      addPlayerInventorySlots(HandHeldOre.this.player, 200);
      for (byte slot = 0; slot < 9; slot = (byte)(slot + 1))
        addSlotToContainer((Slot)new SlotHologramSlot(HandHeldOre.this.inventory, slot, 8 + 18 * slot, 8, 1, HandHeldOre.this.makeSaveCallback())); 
    }
    
    public void func_75134_a(EntityPlayer player) {
      super.func_75134_a(player);
    }
  }
  
  @SideOnly(Side.CLIENT)
  public class GuiEditOre extends GuiDefaultBackground<ContainerEditOre> {
    public class ListItem implements ScrollableList.IListItem {
      private final String number;
      
      public ListItem(String number) {
        this.number = number;
      }
      
      public void onClick(MouseButton button) {
        System.out.println(this.number + " clicked with " + button);
      }
      
      public String getName() {
        return "Thing " + this.number;
      }
    }
    
    public GuiEditOre() {
      super((ContainerBase)new HandHeldOre.ContainerEditOre(HandHeldOre.this), 200);
      addElement((GuiElement)HandHeldOre.this.getBackButton((GuiIC2<?>)this, 10, 96));
      List<ScrollableList.IListItem> items = new ArrayList<>();
      for (String name : new String[] { "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten" })
        items.add(new ListItem(name)); 
      addElement((GuiElement)new ScrollableList((GuiIC2)this, 10, 30, 120, 60, items));
      addElement((GuiElement)new SlotGrid((GuiIC2)this, 7, 7, 9, 1, SlotGrid.SlotStyle.Normal));
      addElement((GuiElement)new SlotGrid((GuiIC2)this, 7, 117, 9, 3, SlotGrid.SlotStyle.Normal));
      addElement((GuiElement)new SlotGrid((GuiIC2)this, 7, 175, 9, 1, SlotGrid.SlotStyle.Normal));
    }
  }
  
  public HandHeldOre(HandHeldAdvancedUpgrade upgradeGUI) {
    super(upgradeGUI, "ore");
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)new ContainerEditOre();
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiEditOre();
  }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.upgrade;

import ic2.core.gui.MouseButton;
import java.util.List;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.ScrollableList;
import java.util.ArrayList;
import ic2.core.gui.GuiElement;
import ic2.core.GuiIC2;
import ic2.core.gui.GuiDefaultBackground;
import net.minecraft.inventory.Slot;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.item.ContainerHandHeldInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;

public class HandHeldOre extends HandHeldUpgradeOption
{
    public HandHeldOre(final HandHeldAdvancedUpgrade upgradeGUI) {
        super(upgradeGUI, "ore");
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerEditOre();
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiEditOre();
    }
    
    public class ContainerEditOre extends ContainerHandHeldInventory<HandHeldOre>
    {
        static final int HEIGHT = 200;
        
        public ContainerEditOre() {
            super(HandHeldOre.this);
            this.addPlayerInventorySlots(HandHeldOre.this.player, 200);
            for (byte slot = 0; slot < 9; ++slot) {
                this.addSlotToContainer((Slot)new SlotHologramSlot(HandHeldOre.this.inventory, slot, 8 + 18 * slot, 8, 1, HandHeldOre.this.makeSaveCallback()));
            }
        }
        
        @Override
        public void onContainerClosed(final EntityPlayer player) {
            super.onContainerClosed(player);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public class GuiEditOre extends GuiDefaultBackground<ContainerEditOre>
    {
        public GuiEditOre() {
            super(new ContainerEditOre(), 200);
            this.addElement(HandHeldOre.this.getBackButton(this, 10, 96));
            final List<ScrollableList.IListItem> items = new ArrayList<ScrollableList.IListItem>();
            for (final String name : new String[] { "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten" }) {
                items.add(new ListItem(name));
            }
            this.addElement(new ScrollableList(this, 10, 30, 120, 60, items));
            this.addElement(new SlotGrid(this, 7, 7, 9, 1, SlotGrid.SlotStyle.Normal));
            this.addElement(new SlotGrid(this, 7, 117, 9, 3, SlotGrid.SlotStyle.Normal));
            this.addElement(new SlotGrid(this, 7, 175, 9, 1, SlotGrid.SlotStyle.Normal));
        }
        
        public class ListItem implements ScrollableList.IListItem
        {
            private final String number;
            
            public ListItem(final String number) {
                this.number = number;
            }
            
            public void onClick(final MouseButton button) {
                System.out.println(this.number + " clicked with " + button);
            }
            
            public String getName() {
                return "Thing " + this.number;
            }
        }
    }
}

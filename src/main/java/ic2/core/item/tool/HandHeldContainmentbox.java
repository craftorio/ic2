// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.item.reactor.ItemReactorUranium;
import ic2.core.item.reactor.ItemReactorMOX;
import ic2.core.ref.ItemName;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;

public class HandHeldContainmentbox extends HandHeldInventory
{
    public HandHeldContainmentbox(final EntityPlayer player, final ItemStack stack1, final int inventorySize) {
        super(player, stack1, inventorySize);
    }
    
    @Override
    public ContainerBase<HandHeldContainmentbox> getGuiContainer(final EntityPlayer player) {
        return new ContainerContainmentbox(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiContainmentbox(new ContainerContainmentbox(player, this));
    }
    
    public String getName() {
        return "ic2.containment_box";
    }
    
    public boolean hasCustomName() {
        return false;
    }
    
    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        return stack != null && (stack.getItem() == ItemName.nuclear.getInstance() || stack.getItem() instanceof ItemReactorMOX || stack.getItem() instanceof ItemReactorUranium);
    }
}

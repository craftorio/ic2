// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.api.item.ItemWrapper;
import ic2.core.util.StackUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;

public class HandHeldToolbox extends HandHeldInventory
{
    public HandHeldToolbox(final EntityPlayer player, final ItemStack stack, final int inventorySize) {
        super(player, stack, inventorySize);
    }
    
    @Override
    public ContainerBase<HandHeldToolbox> getGuiContainer(final EntityPlayer player) {
        return new ContainerToolbox(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiToolbox(new ContainerToolbox(player, this));
    }
    
    public String getName() {
        return "toolbox";
    }
    
    public boolean hasCustomName() {
        return false;
    }
    
    @Override
    public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
        return !StackUtil.isEmpty(itemstack) && ItemWrapper.canBeStoredInToolbox(itemstack);
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;

public class HandHeldMeter extends HandHeldInventory
{
    public HandHeldMeter(final EntityPlayer player, final ItemStack stack) {
        super(player, stack, 0);
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerMeter(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiToolMeter(new ContainerMeter(player, this));
    }
    
    public String getName() {
        return "ic2.meter";
    }
    
    public boolean hasCustomName() {
        return false;
    }
    
    void closeGUI() {
        this.player.closeScreen();
    }
}

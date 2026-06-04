// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public interface IHasGui extends IInventory
{
    ContainerBase<?> getGuiContainer(final EntityPlayer p0);
    
    @SideOnly(Side.CLIENT)
    GuiScreen getGui(final EntityPlayer p0, final boolean p1);
    
    void onGuiClosed(final EntityPlayer p0);
}

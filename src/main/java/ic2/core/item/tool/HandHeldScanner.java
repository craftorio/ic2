// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class HandHeldScanner extends HandHeldInventory
{
    ItemStack itemScanner;
    EntityPlayer player;
    
    public HandHeldScanner(final EntityPlayer player, final ItemStack itemScanner) {
        super(player, itemScanner, 0);
        this.itemScanner = itemScanner;
        this.player = player;
    }
    
    @Override
    public ContainerBase<HandHeldScanner> getGuiContainer(final EntityPlayer player) {
        return new ContainerToolScanner(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiToolScanner(new ContainerToolScanner(player, this));
    }
    
    public String getName() {
        return this.itemScanner.getUnlocalizedName();
    }
    
    public boolean hasCustomName() {
        return false;
    }
}

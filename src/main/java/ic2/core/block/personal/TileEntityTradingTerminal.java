// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import java.util.Collections;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public class TileEntityTradingTerminal extends TileEntityInventory implements IHasGui, IUpgradableBlock
{
    protected int range;
    public final InvSlotUpgrade rangeUpgrade;
    
    public TileEntityTradingTerminal() {
        (this.rangeUpgrade = new InvSlotUpgrade((T)this, "range", 1)).setStackSizeLimit(16);
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.range = this.rangeUpgrade.getRemoteRange(512);
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (!this.getWorld().isRemote) {
            this.range = this.rangeUpgrade.getRemoteRange(512);
        }
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.rangeUpgrade.tick();
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerTradingTerminal(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiTradingTerminal(new ContainerTradingTerminal(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return Collections.singleton(UpgradableProperty.RemotelyAccessible);
    }
    
    @Override
    public double getEnergy() {
        return 0.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return false;
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlot;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityBetterItemBuffer extends TileEntityInventory implements IHasGui, IUpgradableBlock
{
    public final InvSlot bufferSlot;
    public final InvSlotUpgrade upgradeSlot;
    
    public TileEntityBetterItemBuffer() {
        this.bufferSlot = new InvSlot(this, "buffer", InvSlot.Access.IO, 9, InvSlot.InvSide.ANY);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.upgradeSlot.tick();
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.ItemProducing, UpgradableProperty.ItemConsuming);
    }
    
    @Override
    public double getEnergy() {
        return 0.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return true;
    }
    
    @Override
    public ContainerBase<TileEntityBetterItemBuffer> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
}

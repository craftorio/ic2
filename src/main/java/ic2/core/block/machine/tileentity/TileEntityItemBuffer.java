// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiItemBuffer;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerItemBuffer;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ic2.api.upgrade.IUpgradeItem;
import ic2.core.util.StackUtil;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlot;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityItemBuffer extends TileEntityInventory implements IHasGui, IUpgradableBlock
{
    public final InvSlot rightcontentSlot;
    public final InvSlot leftcontentSlot;
    public final InvSlotUpgrade upgradeSlot;
    private boolean tick;
    
    public TileEntityItemBuffer() {
        this.tick = true;
        this.rightcontentSlot = new InvSlot(this, "rightcontent", InvSlot.Access.IO, 24, InvSlot.InvSide.SIDE);
        this.leftcontentSlot = new InvSlot(this, "leftcontent", InvSlot.Access.IO, 24, InvSlot.InvSide.NOTSIDE);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 2);
        this.comparator.setUpdate(() -> TileEntityInventory.calcRedstoneFromInvSlots(this.rightcontentSlot, this.leftcontentSlot));
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        final ItemStack upgradeleft = this.upgradeSlot.get(0);
        final ItemStack upgraderight = this.upgradeSlot.get(1);
        if (!StackUtil.isEmpty(upgradeleft) && !StackUtil.isEmpty(upgraderight)) {
            if (this.tick) {
                if (((IUpgradeItem)upgradeleft.getItem()).onTick(upgradeleft, this)) {
                    super.markDirty();
                }
            }
            else if (((IUpgradeItem)upgraderight.getItem()).onTick(upgraderight, this)) {
                super.markDirty();
            }
            this.tick = !this.tick;
        }
        else {
            if (!StackUtil.isEmpty(upgradeleft)) {
                this.tick = true;
                if (((IUpgradeItem)upgradeleft.getItem()).onTick(upgradeleft, this)) {
                    super.markDirty();
                }
            }
            if (!StackUtil.isEmpty(upgraderight)) {
                this.tick = false;
                if (((IUpgradeItem)upgraderight.getItem()).onTick(upgraderight, this)) {
                    super.markDirty();
                }
            }
        }
    }
    
    @Override
    public ContainerBase<TileEntityItemBuffer> getGuiContainer(final EntityPlayer player) {
        return new ContainerItemBuffer(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiItemBuffer(new ContainerItemBuffer(player, this));
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.ItemProducing);
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getEnergy() {
        return 40.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return true;
    }
}

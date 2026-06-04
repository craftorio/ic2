// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiMagnetizer;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerMagnetizer;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

public class TileEntityMagnetizer extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock
{
    public InvSlotUpgrade upgradeSlot;
    public static final int defaultMaxEnergy = 100;
    public static final int defaultTier = 1;
    private static final double boostEnergy = 2.0;
    protected final Redstone redstone;
    
    public TileEntityMagnetizer() {
        super(100, 1);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
        this.redstone = this.addComponent(new Redstone(this));
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (!this.getWorld().isRemote) {
            this.setOverclockRates();
        }
    }
    
    public void setOverclockRates() {
        this.upgradeSlot.onChanged();
        final int tier = this.upgradeSlot.getTier(1);
        this.energy.setSinkTier(tier);
        this.dischargeSlot.setTier(tier);
        this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(100, 0, 0));
    }
    
    private int distance() {
        return 20 + this.upgradeSlot.augmentation;
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerMagnetizer(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiMagnetizer(new ContainerMagnetizer(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    public boolean canBoost() {
        return this.energy.getEnergy() >= 2.0;
    }
    
    public void boost(final double multiplier) {
        this.energy.useEnergy(2.0 * multiplier);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Augmentable, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage);
    }
}

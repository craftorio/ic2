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
import ic2.core.util.Util;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityTank extends TileEntityInventory implements IUpgradableBlock, IHasGui
{
    public final InvSlotUpgrade upgradeSlot;
    @GuiSynced
    protected final FluidTank fluidTank;
    protected final Fluids fluids;
    
    public TileEntityTank() {
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTank("fluid", 24000);
        this.comparator.setUpdate(() -> (this.fluidTank.getFluidAmount() == 0) ? 0 : ((int)Util.lerp(1.0f, 15.0f, this.fluidTank.getFluidAmount() / (float)this.fluidTank.getCapacity())));
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.upgradeSlot.tick();
    }
    
    @Override
    public double getEnergy() {
        return 0.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return false;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
    }
    
    @Override
    public ContainerBase<TileEntityTank> getGuiContainer(final EntityPlayer player) {
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

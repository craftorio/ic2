// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.reactor.tileentity;

import ic2.api.reactor.IReactor;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import com.google.common.base.Supplier;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.FluidReactorLookup;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.profile.NotClassic;
import ic2.api.reactor.IReactorChamber;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityReactorFluidPort extends TileEntityInventory implements IHasGui, IUpgradableBlock, IReactorChamber
{
    public final InvSlotUpgrade upgradeSlot;
    private final FluidReactorLookup lookup;
    protected final Fluids fluids;
    
    public TileEntityReactorFluidPort() {
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 1);
        this.lookup = this.addComponent(new FluidReactorLookup(this));
        (this.fluids = this.addComponent(new Fluids(this))).addUnmanagedTankHook((Supplier<? extends Collection<Fluids.InternalFluidTank>>)new Supplier<Collection<Fluids.InternalFluidTank>>() {
            public Collection<Fluids.InternalFluidTank> get() {
                final TileEntityNuclearReactorElectric reactor = TileEntityReactorFluidPort.this.getReactorInstance();
                if (reactor == null) {
                    return (Collection<Fluids.InternalFluidTank>)Collections.emptySet();
                }
                return Arrays.asList(reactor.inputTank, reactor.outputTank);
            }
        });
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.upgradeSlot.tick();
    }
    
    @Override
    public ContainerBase<TileEntityReactorFluidPort> getGuiContainer(final EntityPlayer player) {
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
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
    }
    
    @Override
    public double getEnergy() {
        return 40.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return true;
    }
    
    @Override
    public TileEntityNuclearReactorElectric getReactorInstance() {
        return this.lookup.getReactor();
    }
    
    @Override
    public boolean isWall() {
        return true;
    }
}

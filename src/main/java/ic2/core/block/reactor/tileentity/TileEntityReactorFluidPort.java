package ic2.core.block.reactor.tileentity;

import com.google.common.base.Supplier;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.FluidReactorLookup;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.profile.NotClassic;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityReactorFluidPort extends TileEntityInventory implements IHasGui, IUpgradableBlock, IReactorChamber {
  public final InvSlotUpgrade upgradeSlot;
  
  private final FluidReactorLookup lookup;
  
  protected final Fluids fluids;
  
  public TileEntityReactorFluidPort() {
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 1);
    this.lookup = (FluidReactorLookup)addComponent((TileEntityComponent)new FluidReactorLookup((TileEntityBlock)this));
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.fluids.addUnmanagedTankHook(new Supplier<Collection<Fluids.InternalFluidTank>>() {
          public Collection<Fluids.InternalFluidTank> get() {
            TileEntityNuclearReactorElectric reactor = TileEntityReactorFluidPort.this.getReactorInstance();
            if (reactor == null)
              return Collections.emptySet(); 
            return Arrays.asList(new Fluids.InternalFluidTank[] { reactor.inputTank, reactor.outputTank });
          }
        });
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.upgradeSlot.tick();
  }
  
  public ContainerBase<TileEntityReactorFluidPort> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityReactorFluidPort>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
  }
  
  public double getEnergy() {
    return 40.0D;
  }
  
  public boolean useEnergy(double amount) {
    return true;
  }
  
  public TileEntityNuclearReactorElectric getReactorInstance() {
    return this.lookup.getReactor();
  }
  
  public boolean isWall() {
    return true;
  }
}

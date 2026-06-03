package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiSteamKineticGenerator;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LiquidUtil;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntitySteamKineticGenerator extends TileEntityInventory implements IKineticSource, IHasGui, IUpgradableBlock {
  protected final FluidTank steamTank;
  
  protected final FluidTank distilledWaterTank;
  
  public final InvSlotUpgrade upgradeSlot;
  
  public final InvSlotConsumable turbineSlot;
  
  public TileEntitySteamKineticGenerator() {
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 1);
    this.turbineSlot = (InvSlotConsumable)new InvSlotConsumableItemStack((IInventorySlotHolder)this, "Turbineslot", 1, new ItemStack[] { ItemName.crafting.getItemStack((Enum)CraftingItemType.steam_turbine) });
    this.isTurbineFilledWithWater = false;
    this.condensationProgress = 0;
    this.updateTicker = IC2.random.nextInt(getTickRate());
    this.turbineSlot.setStackSizeLimit(1);
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.steamTank = (FluidTank)this.fluids.addTankInsert("steamTank", 21000, Fluids.fluidPredicate(new Fluid[] { FluidName.steam.getInstance(), FluidName.superheated_steam.getInstance() }));
    this.distilledWaterTank = (FluidTank)this.fluids.addTank("distilledWaterTank", 1000, Fluids.fluidPredicate(new Fluid[] { FluidName.distilled_water.getInstance(), FluidRegistry.WATER }));
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (this.distilledWaterTank.getCapacity() - this.distilledWaterTank.getFluidAmount() >= 1 && this.isTurbineFilledWithWater)
      this.isTurbineFilledWithWater = false; 
    if (this.steamTank.getFluidAmount() > 0 && !this.isTurbineFilledWithWater && !this.turbineSlot.isEmpty()) {
      if (!getActive()) {
        setActive(true);
        needsInvUpdate = true;
      } 
      boolean hotSteam = (this.steamTank.getFluid().getFluid() == FluidName.superheated_steam.getInstance());
      boolean turbineDoneWork = turbineDoWork(hotSteam);
      if (this.updateTicker++ >= getTickRate()) {
        if (turbineDoneWork)
          this.turbineSlot.damage(hotSteam ? 1 : 2, false); 
        this.updateTicker = 0;
      } 
    } else if (getActive()) {
      setActive(false);
      needsInvUpdate = true;
      this.kUoutput = 0;
    } 
    needsInvUpdate |= this.upgradeSlot.tickNoMark();
    if (needsInvUpdate)
      func_70296_d(); 
  }
  
  private int handleSteam(boolean hotSteam) {
    int amount = this.steamTank.getFluidAmount();
    assert amount > 0;
    this.steamTank.drainInternal(amount, true);
    int KUWorkbuffer = amount * 2 * (hotSteam ? 2 : 1);
    if (hotSteam) {
      outputSteam(amount, true);
    } else {
      int condensation = amount / 10;
      this.condensationProgress += condensation;
      outputSteam(amount - condensation, false);
    } 
    return KUWorkbuffer;
  }
  
  private boolean turbineDoWork(boolean hotSteam) {
    float throttle;
    int rawOutput = handleSteam(hotSteam);
    int waterAmount = this.distilledWaterTank.getFluidAmount();
    if (waterAmount == 0) {
      this.throttled = false;
      throttle = 1.0F;
    } else {
      this.throttled = true;
      throttle = 1.0F - waterAmount / this.distilledWaterTank.getCapacity();
    } 
    this.kUoutput = (int)(rawOutput * throttle * outputModifier);
    if (this.condensationProgress >= 100)
      if (this.distilledWaterTank.fillInternal(new FluidStack(FluidName.distilled_water.getInstance(), 1), false) == 1) {
        this.condensationProgress -= 100;
        this.distilledWaterTank.fillInternal(new FluidStack(FluidName.distilled_water.getInstance(), 1), true);
      } else {
        this.isTurbineFilledWithWater = true;
      }  
    return (this.kUoutput > 0);
  }
  
  private void outputSteam(int amount, boolean hotSteam) {
    World world = func_145831_w();
    for (EnumFacing dir : EnumFacing.field_82609_l) {
      TileEntity te = world.func_175625_s(this.field_174879_c.func_177972_a(dir));
      if (te instanceof ic2.core.block.machine.tileentity.TileEntityCondenser || (hotSteam && te instanceof TileEntitySteamKineticGenerator)) {
        int transAmount = LiquidUtil.fillTile(te, dir.func_176734_d(), new FluidStack(FluidName.steam.getInstance(), amount), false);
        if (transAmount > 0) {
          amount -= transAmount;
          if (amount <= 0)
            break; 
        } 
      } 
    } 
    if (amount > 0) {
      this.ventingSteam = true;
      if (world.field_73012_v.nextInt(10) == 0)
        (new ExplosionIC2(world, null, this.field_174879_c, 1, 1.0F, ExplosionIC2.Type.Heat)).doExplosion(); 
    } else {
      this.ventingSteam = false;
    } 
  }
  
  public int getKUoutput() {
    return this.kUoutput;
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    this.condensationProgress = nbt.func_74762_e("condensationprogress");
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74768_a("condensationprogress", this.condensationProgress);
    return nbt;
  }
  
  public ContainerBase<TileEntitySteamKineticGenerator> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntitySteamKineticGenerator>)new ContainerSteamKineticGenerator(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiSteamKineticGenerator(new ContainerSteamKineticGenerator(player, this));
  }
  
  public int maxrequestkineticenergyTick(EnumFacing directionFrom) {
    return getConnectionBandwidth(directionFrom);
  }
  
  public int getConnectionBandwidth(EnumFacing side) {
    return (side == getFacing()) ? this.kUoutput : 0;
  }
  
  public int requestkineticenergy(EnumFacing directionFrom, int requestkineticenergy) {
    return drawKineticEnergy(directionFrom, requestkineticenergy, false);
  }
  
  public int drawKineticEnergy(EnumFacing side, int request, boolean simulate) {
    return (side == getFacing()) ? this.kUoutput : 0;
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public int gaugeLiquidScaled(int i, int tank) {
    if (tank == 0 && this.distilledWaterTank.getFluidAmount() > 0)
      return this.distilledWaterTank.getFluidAmount() * i / this.distilledWaterTank.getCapacity(); 
    return 0;
  }
  
  public double getEnergy() {
    return 0.0D;
  }
  
  public boolean useEnergy(double amount) {
    return false;
  }
  
  public int getDistilledWaterTankFill() {
    return this.distilledWaterTank.getFluidAmount();
  }
  
  public FluidTank getDistilledWaterTank() {
    return this.distilledWaterTank;
  }
  
  public boolean hasTurbine() {
    return !this.turbineSlot.isEmpty();
  }
  
  public boolean isVentingSteam() {
    return this.ventingSteam;
  }
  
  public boolean isThrottled() {
    return this.throttled;
  }
  
  public boolean isTurbineBlockedByWater() {
    return this.isTurbineFilledWithWater;
  }
  
  public int getTickRate() {
    return 20;
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
  }
  
  private static final float outputModifier = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/steam");
  
  private int kUoutput;
  
  private boolean ventingSteam;
  
  private boolean throttled;
  
  private boolean isTurbineFilledWithWater;
  
  private int condensationProgress;
  
  private int updateTicker;
  
  protected final Fluids fluids;
}

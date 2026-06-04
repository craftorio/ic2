package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.IPatternStorage;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerReplicator;
import ic2.core.block.machine.gui.GuiReplicator;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.FluidName;
import ic2.core.util.StackUtil;
import ic2.core.uu.UuIndex;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityReplicator extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, INetworkClientTileEntityEventListener {
  private static final double uuPerTickBase = 1.0E-4D;
  
  private static final double euPerTickBase = 512.0D;
  
  private static final int defaultTier = 4;
  
  private static final int defaultEnergyStorage = 2000000;
  
  private double uuPerTick;
  
  private double euPerTick;
  
  private double extraUuStored;
  
  public double uuProcessed;
  
  public ItemStack pattern;
  
  private Mode mode;
  
  public int index;
  
  public int maxIndex;
  
  public double patternUu;
  
  public double patternEu;
  
  public final InvSlotConsumableLiquid fluidSlot;
  
  public final InvSlotOutput cellSlot;
  
  public final InvSlotOutput outputSlot;
  
  public final InvSlotUpgrade upgradeSlot;
  
  @GuiSynced
  public final FluidTank fluidTank;
  
  protected final Fluids fluids;
  
  public TileEntityReplicator() {
    super(2000000, 4);
    this.uuPerTick = 1.0E-4D;
    this.euPerTick = 512.0D;
    this.extraUuStored = 0.0D;
    this.uuProcessed = 0.0D;
    this.mode = Mode.STOPPED;
    this.fluidSlot = (InvSlotConsumableLiquid)new InvSlotConsumableLiquidByList((IInventorySlotHolder)this, "fluid", 1, new Fluid[] { FluidName.uu_matter.getInstance() });
    this.cellSlot = new InvSlotOutput((IInventorySlotHolder)this, "cell", 1);
    this.outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1);
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 4);
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.fluidTank = (FluidTank)this.fluids.addTank("fluidTank", 16000, Fluids.fluidPredicate(new Fluid[] { FluidName.uu_matter.getInstance() }));
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity())
      needsInvUpdate = gainFluid(); 
    boolean newActive = false;
    if (this.mode != Mode.STOPPED && this.energy.getEnergy() >= this.euPerTick && this.pattern != null && this.outputSlot.canAdd(this.pattern)) {
      boolean finish;
      double uuRemaining = this.patternUu - this.uuProcessed;
      if (uuRemaining <= this.uuPerTick) {
        finish = true;
      } else {
        uuRemaining = this.uuPerTick;
        finish = false;
      } 
      if (consumeUu(uuRemaining)) {
        newActive = true;
        this.energy.useEnergy(this.euPerTick);
        this.uuProcessed += uuRemaining;
        if (finish) {
          this.uuProcessed = 0.0D;
          if (this.mode == Mode.SINGLE) {
            this.mode = Mode.STOPPED;
          } else {
            refreshInfo();
          } 
          if (this.pattern != null) {
            this.outputSlot.add(this.pattern);
            needsInvUpdate = true;
          } 
        } 
      } 
    } 
    setActive(newActive);
    needsInvUpdate |= this.upgradeSlot.tickNoMark();
    if (needsInvUpdate)
      func_70296_d(); 
  }
  
  private boolean consumeUu(double amount) {
    if (amount <= this.extraUuStored) {
      this.extraUuStored -= amount;
      return true;
    } 
    amount -= this.extraUuStored;
    int toDrain = (int)Math.ceil(amount * 1000.0D);
    FluidStack drained = this.fluidTank.drainInternal(toDrain, false);
    if (drained != null && drained.getFluid() == FluidName.uu_matter.getInstance() && drained.amount == toDrain) {
      this.fluidTank.drainInternal(toDrain, true);
      amount -= drained.amount / 1000.0D;
      if (amount < 0.0D) {
        this.extraUuStored = -amount;
      } else {
        this.extraUuStored = 0.0D;
      } 
      return true;
    } 
    return false;
  }
  
  public void refreshInfo() {
    IPatternStorage storage = getPatternStorage();
    ItemStack oldPattern = this.pattern;
    if (storage == null) {
      this.pattern = null;
    } else {
      List<ItemStack> patterns = storage.getPatterns();
      if (this.index < 0 || this.index >= patterns.size())
        this.index = 0; 
      this.maxIndex = patterns.size();
      if (patterns.isEmpty()) {
        this.pattern = null;
      } else {
        this.pattern = patterns.get(this.index);
        this.patternUu = UuIndex.instance.getInBuckets(this.pattern);
        if (!StackUtil.checkItemEqualityStrict(this.pattern, oldPattern)) {
          this.uuProcessed = 0.0D;
          this.mode = Mode.STOPPED;
        } 
      } 
    } 
    if (this.pattern == null) {
      this.uuProcessed = 0.0D;
      this.mode = Mode.STOPPED;
    } 
  }
  
  public IPatternStorage getPatternStorage() {
    World world = getWorld();
    for (EnumFacing dir : EnumFacing.field_82609_l) {
      TileEntity target = world.func_175625_s(this.field_174879_c.func_177972_a(dir));
      if (target instanceof IPatternStorage)
        return (IPatternStorage)target; 
    } 
    return null;
  }
  
  public void setOverclockRates() {
    this.upgradeSlot.onChanged();
    this.uuPerTick = 1.0E-4D / this.upgradeSlot.processTimeMultiplier;
    this.euPerTick = (512.0D + this.upgradeSlot.extraEnergyDemand) * this.upgradeSlot.energyDemandMultiplier;
    this.energy.setSinkTier(applyModifier(4, this.upgradeSlot.extraTier, 1.0D));
    this.energy.setCapacity(applyModifier(2000000, this.upgradeSlot.extraEnergyStorage, this.upgradeSlot.energyStorageMultiplier));
  }
  
  private static int applyModifier(int base, int extra, double multiplier) {
    double ret = Math.round((base + extra) * multiplier);
    return (ret > 2.147483647E9D) ? Integer.MAX_VALUE : (int)ret;
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiReplicator(new ContainerReplicator(player, this));
  }
  
  public ContainerBase<TileEntityReplicator> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityReplicator>)new ContainerReplicator(player, this);
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (IC2.platform.isSimulating()) {
      setOverclockRates();
      refreshInfo();
    } 
  }
  
  public void func_70296_d() {
    super.func_70296_d();
    if (IC2.platform.isSimulating())
      setOverclockRates(); 
  }
  
  public boolean gainFluid() {
    return this.fluidSlot.processIntoTank((IFluidTank)this.fluidTank, this.cellSlot);
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.extraUuStored = nbt.getDouble("extraUuStored");
    this.uuProcessed = nbt.getDouble("uuProcessed");
    this.index = nbt.func_74762_e("index");
    int modeIdx = nbt.func_74762_e("mode");
    this.mode = (modeIdx < (Mode.values()).length) ? Mode.values()[modeIdx] : Mode.STOPPED;
    NBTTagCompound contentTag = nbt.getCompoundTag("pattern");
    this.pattern = new ItemStack(contentTag);
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setDouble("extraUuStored", this.extraUuStored);
    nbt.setDouble("uuProcessed", this.uuProcessed);
    nbt.func_74768_a("index", this.index);
    nbt.func_74768_a("mode", this.mode.ordinal());
    if (this.pattern != null) {
      NBTTagCompound contentTag = new NBTTagCompound();
      this.pattern.func_77955_b(contentTag);
      nbt.setTag("pattern", (NBTBase)contentTag);
    } 
    return nbt;
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    switch (event) {
      case 0:
      case 1:
        if (this.mode == Mode.STOPPED) {
          IPatternStorage storage = getPatternStorage();
          if (storage != null) {
            List<ItemStack> patterns = storage.getPatterns();
            if (!patterns.isEmpty()) {
              if (event == 0) {
                if (this.index <= 0) {
                  this.index = patterns.size() - 1;
                } else {
                  this.index--;
                } 
              } else if (this.index >= patterns.size() - 1) {
                this.index = 0;
              } else {
                this.index++;
              } 
              refreshInfo();
            } 
          } 
        } 
        break;
      case 3:
        if (this.mode != Mode.STOPPED) {
          this.uuProcessed = 0.0D;
          this.mode = Mode.STOPPED;
        } 
        break;
      case 4:
        if (this.pattern != null) {
          this.mode = Mode.SINGLE;
          if (player != null)
            IC2.achievements.issueAchievement(player, "replicateObject"); 
        } 
        break;
      case 5:
        if (this.pattern != null) {
          this.mode = Mode.CONTINUOUS;
          if (player != null)
            IC2.achievements.issueAchievement(player, "replicateObject"); 
        } 
        break;
    } 
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getEnergy() {
    return this.energy.getEnergy();
  }
  
  public boolean useEnergy(double amount) {
    return this.energy.useEnergy(amount);
  }
  
  public Mode getMode() {
    return this.mode;
  }
  
  public enum Mode {
    STOPPED, SINGLE, CONTINUOUS;
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Processing, new UpgradableProperty[] { UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming });
  }
}

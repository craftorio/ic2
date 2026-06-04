package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.network.NetworkManager;
import ic2.core.util.StackUtil;
import java.util.Collection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityStandardMachine<RI, RO, I> extends TileEntityElectricMachine implements IHasGui, IGuiValueProvider, INetworkTileEntityEventListener, IUpgradableBlock {
  protected short progress;
  
  public final int defaultEnergyConsume;
  
  public final int defaultOperationLength;
  
  public final int defaultTier;
  
  public final int defaultEnergyStorage;
  
  public int energyConsume;
  
  public int operationLength;
  
  public int operationsPerTick;
  
  @GuiSynced
  protected float guiProgress;
  
  public AudioSource audioSource;
  
  protected static final int EventStart = 0;
  
  protected static final int EventInterrupt = 1;
  
  protected static final int EventFinish = 2;
  
  protected static final int EventStop = 3;
  
  public InvSlotProcessable<RI, RO, I> inputSlot;
  
  public final InvSlotOutput outputSlot;
  
  public final InvSlotUpgrade upgradeSlot;
  
  public TileEntityStandardMachine(int energyPerTick, int length, int outputSlots) {
    this(energyPerTick, length, outputSlots, 1);
  }
  
  public TileEntityStandardMachine(int energyPerTick, int length, int outputSlots, int aDefaultTier) {
    super(energyPerTick * length, aDefaultTier);
    this.progress = 0;
    this.defaultEnergyConsume = this.energyConsume = energyPerTick;
    this.defaultOperationLength = this.operationLength = length;
    this.defaultTier = aDefaultTier;
    this.defaultEnergyStorage = energyPerTick * length;
    this.outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", outputSlots);
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 4);
    this.comparator.setUpdate(() -> this.progress * 15 / this.operationLength);
  }
  
  public void readFromNBT(NBTTagCompound nbttagcompound) {
    super.readFromNBT(nbttagcompound);
    this.progress = nbttagcompound.func_74765_d("progress");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.func_74777_a("progress", this.progress);
    return nbt;
  }
  
  public float getProgress() {
    return this.guiProgress;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (IC2.platform.isSimulating())
      setOverclockRates(); 
  }
  
  protected void onUnloaded() {
    super.onUnloaded();
    if (IC2.platform.isRendering() && this.audioSource != null) {
      IC2.audioManager.removeSources(this);
      this.audioSource = null;
    } 
  }
  
  public void func_70296_d() {
    super.func_70296_d();
    if (IC2.platform.isSimulating())
      setOverclockRates(); 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    MachineRecipeResult<RI, RO, I> output = getOutput();
    if (output != null && this.energy.useEnergy(this.energyConsume)) {
      setActive(true);
      if (this.progress == 0)
        ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this, 0, true); 
      this.progress = (short)(this.progress + 1);
      if (this.progress >= this.operationLength) {
        operate(output);
        needsInvUpdate = true;
        this.progress = 0;
        ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this, 2, true);
      } 
    } else {
      if (getActive())
        if (this.progress != 0) {
          ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this, 1, true);
        } else {
          ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this, 3, true);
        }  
      if (output == null)
        this.progress = 0; 
      setActive(false);
    } 
    needsInvUpdate |= this.upgradeSlot.tickNoMark();
    this.guiProgress = this.progress / this.operationLength;
    if (needsInvUpdate)
      super.func_70296_d(); 
  }
  
  public void setOverclockRates() {
    this.upgradeSlot.onChanged();
    double previousProgress = this.progress / this.operationLength;
    this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
    this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
    this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
    int tier = this.upgradeSlot.getTier(this.defaultTier);
    this.energy.setSinkTier(tier);
    this.dischargeSlot.setTier(tier);
    this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(this.defaultEnergyStorage, this.defaultOperationLength, this.defaultEnergyConsume));
    this.progress = (short)(int)Math.floor(previousProgress * this.operationLength + 0.1D);
  }
  
  private void operate(MachineRecipeResult<RI, RO, I> result) {
    for (int i = 0; i < this.operationsPerTick; i++) {
      Collection<ItemStack> processResult = getOutput((RO)result.getOutput());
      for (int j = 0; j < this.upgradeSlot.size(); j++) {
        ItemStack stack = this.upgradeSlot.get(j);
        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IUpgradeItem)
          processResult = ((IUpgradeItem)stack.getItem()).onProcessEnd(stack, this, processResult); 
      } 
      operateOnce(result, processResult);
      result = getOutput();
      if (result == null)
        break; 
    } 
  }
  
  protected Collection<ItemStack> getOutput(RO output) {
    return StackUtil.copy((Collection)output);
  }
  
  protected void operateOnce(MachineRecipeResult<RI, RO, I> result, Collection<ItemStack> processResult) {
    this.inputSlot.consume(result);
    this.outputSlot.add(processResult);
  }
  
  protected MachineRecipeResult<RI, RO, I> getOutput() {
    if (this.inputSlot.isEmpty())
      return null; 
    MachineRecipeResult<RI, RO, I> result = this.inputSlot.process();
    if (result == null)
      return null; 
    if (this.outputSlot.canAdd(getOutput((RO)result.getOutput())))
      return result; 
    return null;
  }
  
  public ContainerBase<? extends TileEntityStandardMachine<RI, RO, I>> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<? extends TileEntityStandardMachine<RI, RO, I>>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public String getStartSoundFile() {
    return null;
  }
  
  public String getInterruptSoundFile() {
    return null;
  }
  
  public void onNetworkEvent(int event) {
    if (this.audioSource == null && getStartSoundFile() != null)
      this.audioSource = IC2.audioManager.createSource(this, getStartSoundFile()); 
    switch (event) {
      case 0:
        if (this.audioSource != null)
          this.audioSource.play(); 
        break;
      case 2:
        if (this.audioSource != null)
          this.audioSource.stop(); 
        break;
      case 1:
        if (this.audioSource != null) {
          this.audioSource.stop();
          if (getInterruptSoundFile() != null)
            IC2.audioManager.playOnce(this, PositionSpec.Center, getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume()); 
        } 
        break;
    } 
  }
  
  public double getEnergy() {
    return this.energy.getEnergy();
  }
  
  public boolean useEnergy(double amount) {
    return this.energy.useEnergy(amount);
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    if (name.equals("progress"))
      return this.guiProgress; 
    throw new IllegalArgumentException(getClass().getSimpleName() + " Cannot get value for " + name);
  }
}

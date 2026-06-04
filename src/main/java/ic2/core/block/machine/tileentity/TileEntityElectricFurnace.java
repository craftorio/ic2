package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.IC2;
import ic2.core.audio.FutureSound;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityElectricFurnace extends TileEntityStandardMachine<ItemStack, ItemStack, ItemStack> implements INetworkClientTileEntityEventListener {
  protected double xp;
  
  protected FutureSound startingSound;
  
  protected String finishingSound;
  
  public TileEntityElectricFurnace() {
    super(3, 100, 1);
    this.xp = 0.0D;
    this.inputSlot = (InvSlotProcessable<ItemStack, ItemStack, ItemStack>)new InvSlotProcessableSmelting((IInventorySlotHolder)this, "input", 1);
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.xp = nbt.getDouble("xp");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setDouble("xp", this.xp);
    return nbt;
  }
  
  protected void onUnloaded() {
    super.onUnloaded();
    if (IC2.platform.isRendering()) {
      if (this.startingSound != null) {
        if (!this.startingSound.isComplete())
          this.startingSound.cancel(); 
        this.startingSound = null;
      } 
      if (this.finishingSound != null) {
        IC2.audioManager.removeSource(this.finishingSound);
        this.finishingSound = null;
      } 
    } 
  }
  
  protected Collection<ItemStack> getOutput(ItemStack output) {
    return Collections.singletonList(output);
  }
  
  public void operateOnce(MachineRecipeResult<ItemStack, ItemStack, ItemStack> result, Collection<ItemStack> processResult) {
    super.operateOnce(result, processResult);
    this.xp += result.getRecipe().getMetaData().func_74760_g("experience");
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    if (event == 0) {
      assert !(getWorld()).isRemote;
      this.xp = TileEntityIronFurnace.spawnXP(player, this.xp);
    } 
  }
  
  public String getStartingSoundFile() {
    return "Machines/Electro Furnace/ElectroFurnaceStart.ogg";
  }
  
  public String getStartSoundFile() {
    return "Machines/Electro Furnace/ElectroFurnaceLoop.ogg";
  }
  
  public String getInterruptSoundFile() {
    return "Machines/Electro Furnace/ElectroFurnaceStop.ogg";
  }
  
  public void onNetworkEvent(int event) {
    if (this.audioSource == null && getStartSoundFile() != null)
      this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, getStartSoundFile(), true, false, IC2.audioManager.getDefaultVolume()); 
    switch (event) {
      case 0:
        if (this.startingSound == null) {
          if (this.finishingSound != null) {
            IC2.audioManager.removeSource(this.finishingSound);
            this.finishingSound = null;
          } 
          String source = IC2.audioManager.playOnce(this, PositionSpec.Center, getStartingSoundFile(), false, IC2.audioManager.getDefaultVolume());
          if (this.audioSource != null)
            IC2.audioManager.chainSource(source, this.startingSound = new FutureSound(this.audioSource::play)); 
        } 
        break;
      case 1:
      case 3:
        if (this.audioSource != null) {
          this.audioSource.stop();
          if (this.startingSound != null) {
            if (!this.startingSound.isComplete())
              this.startingSound.cancel(); 
            this.startingSound = null;
          } 
          this.finishingSound = IC2.audioManager.playOnce(this, PositionSpec.Center, getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume());
        } 
        break;
    } 
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
  }
}

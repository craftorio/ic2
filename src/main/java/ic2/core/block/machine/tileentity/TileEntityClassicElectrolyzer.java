package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.item.type.CellType;
import ic2.core.network.GuiSynced;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock.Delegated;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Delegated(current = TileEntityElectrolyzer.class, old = TileEntityClassicElectrolyzer.class)
public class TileEntityClassicElectrolyzer extends TileEntityInventory implements IHasGui {
  public TileEntityElectricBlock mfe;
  
  public int ticker;
  
  public final InvSlotConsumableItemStack waterSlot;
  
  public final InvSlotConsumableItemStack hydrogenSlot;
  
  protected AudioSource audio;
  
  @GuiSynced
  protected final Energy energy;
  
  public TileEntityClassicElectrolyzer() {
    this.mfe = null;
    this.ticker = IC2.random.nextInt(16);
    this.waterSlot = new InvSlotConsumableItemStack((IInventorySlotHolder)this, "water", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP, new ItemStack[] { ItemName.cell.getItemStack((Enum)CellType.water) });
    this.hydrogenSlot = new InvSlotConsumableItemStack((IInventorySlotHolder)this, "hydrogen", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, new ItemStack[] { ItemName.cell.getItemStack((Enum)CellType.electrolyzed_water) });
    this.energy = (Energy)addComponent((TileEntityComponent)new Energy((TileEntityBlock)this, 20000.0D, Util.noFacings, Util.noFacings, 1));
    this.comparator.setUpdate(this.energy::getComparatorValue);
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if ((func_145831_w()).field_72995_K)
      this.audio = IC2.audioManager.createSource(this, "Machines/ElectrolyzerLoop.ogg"); 
  }
  
  protected void onUnloaded() {
    super.onUnloaded();
    if (this.audio != null) {
      IC2.audioManager.removeSources(this);
      this.audio = null;
    } 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    boolean turnActive = false;
    if (++this.ticker % 16 == 0)
      this.mfe = lookForMFE(); 
    if (this.mfe == null)
      return; 
    if (shouldDrain() && canDrain()) {
      needsInvUpdate |= drain();
      turnActive = true;
    } 
    if (shouldPower() && (canPower() || this.energy.getEnergy() > 0.0D)) {
      needsInvUpdate |= power();
      turnActive = true;
    } 
    setActive(turnActive);
    if (needsInvUpdate)
      func_70296_d(); 
  }
  
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (this.ticker++ % 32 == 0 && this.audio != null) {
      this.audio.stop();
      if (getActive())
        this.audio.play(); 
    } 
  }
  
  public boolean shouldDrain() {
    return (this.mfe != null && this.mfe.energy.getFillRatio() >= 0.7D);
  }
  
  public boolean shouldPower() {
    return (this.mfe != null && this.mfe.energy.getFillRatio() <= 0.3D);
  }
  
  public boolean canDrain() {
    return (this.waterSlot.consume(1, true, false) != null && (this.hydrogenSlot
      .isEmpty() || StackUtil.getSize(this.hydrogenSlot.get()) < Math.min(this.hydrogenSlot.getStackSizeLimit(), this.hydrogenSlot.get().func_77976_d())));
  }
  
  public boolean canPower() {
    return (this.hydrogenSlot.consume(1, true, false) != null && (this.waterSlot
      .isEmpty() || StackUtil.getSize(this.waterSlot.get()) < Math.min(this.waterSlot.getStackSizeLimit(), this.waterSlot.get().func_77976_d())));
  }
  
  public boolean drain() {
    double amount = processRate();
    if (!this.mfe.energy.useEnergy(amount))
      return false; 
    this.energy.addEnergy(amount);
    if (this.energy.useEnergy(20000.0D)) {
      this.waterSlot.consume(1);
      if (this.hydrogenSlot.isEmpty()) {
        this.hydrogenSlot.put(ItemName.cell.getItemStack((Enum)CellType.electrolyzed_water));
      } else {
        this.hydrogenSlot.put(StackUtil.incSize(this.hydrogenSlot.get()));
      } 
      return true;
    } 
    return false;
  }
  
  public boolean power() {
    if (this.energy.getEnergy() > 0.0D) {
      double out = Math.min(this.energy.getEnergy(), processRate());
      this.energy.useEnergy(out);
      this.mfe.energy.addEnergy(out);
      return false;
    } 
    this.energy.forceAddEnergy((12000 + 2000 * this.mfe.energy.getSinkTier()));
    this.hydrogenSlot.consume(1);
    if (this.waterSlot.isEmpty()) {
      this.waterSlot.put(ItemName.cell.getItemStack((Enum)CellType.water));
    } else {
      this.waterSlot.put(StackUtil.incSize(this.waterSlot.get()));
    } 
    return true;
  }
  
  public int processRate() {
    switch (this.mfe.energy.getSinkTier()) {
      default:
        return 2;
      case 2:
        return 8;
      case 3:
        return 32;
      case 4:
        break;
    } 
    return 128;
  }
  
  public TileEntityElectricBlock lookForMFE() {
    World world = func_145831_w();
    for (EnumFacing dir : EnumFacing.field_82609_l) {
      TileEntity te = world.func_175625_s(this.field_174879_c.func_177972_a(dir));
      if (te instanceof TileEntityElectricBlock)
        return (TileEntityElectricBlock)te; 
    } 
    return null;
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
}

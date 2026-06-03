package ic2.core.block.reactor.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.item.reactor.ItemReactorCondensator;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityAbstractRCI extends TileEntityElectricMachine implements IUpgradableBlock, IHasGui {
  private TileEntityNuclearReactorElectric reactor;
  
  private final ItemStack target;
  
  private final double energyPerOperation = 1000.0D;
  
  public final InvSlotConsumableItemStack inputSlot;
  
  public final InvSlotUpgrade upgradeSlot;
  
  public TileEntityAbstractRCI(ItemStack target, ItemStack coolant) {
    super(48000, 2);
    this.energyPerOperation = 1000.0D;
    this.target = target;
    this.inputSlot = new InvSlotConsumableItemStack((IInventorySlotHolder)this, "input", InvSlot.Access.I, 9, InvSlot.InvSide.ANY, new ItemStack[] { coolant });
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 4);
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (!(func_145831_w()).field_72995_K)
      updateEnergyFacings(); 
    updateReactor();
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (!this.inputSlot.isEmpty() && this.energy.getEnergy() >= 1000.0D && this.reactor != null) {
      setActive(true);
    } else {
      setActive(false);
    } 
    if (getActive())
      for (Iterator<ItemStack> it = this.reactor.reactorSlot.iterator(); it.hasNext(); ) {
        ItemStack comp = it.next();
        if (comp == null)
          continue; 
        if (StackUtil.checkItemEquality(comp, this.target)) {
          ItemReactorCondensator cond = (ItemReactorCondensator)comp.func_77973_b();
          if (cond.getDurabilityForDisplay(comp) > 0.85D && this.inputSlot.consume(1) != null && this.energy.useEnergy(1000.0D)) {
            cond.setCustomDamage(comp, 0);
            needsInvUpdate = true;
          } 
        } 
      }  
    needsInvUpdate |= this.upgradeSlot.tickNoMark();
    if (needsInvUpdate)
      func_70296_d(); 
  }
  
  protected void onNeighborChange(Block neighbor, BlockPos neighborPos) {
    super.onNeighborChange(neighbor, neighborPos);
    updateEnergyFacings();
    updateReactor();
  }
  
  public void setFacing(EnumFacing facing) {
    super.setFacing(facing);
    updateEnergyFacings();
    updateReactor();
  }
  
  public void updateEnergyFacings() {
    World world = func_145831_w();
    Set<EnumFacing> ret = new HashSet<>();
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      TileEntity te = world.func_175625_s(this.field_174879_c.func_177972_a(facing));
      if (!(te instanceof TileEntityNuclearReactorElectric) && !(te instanceof TileEntityReactorChamberElectric))
        ret.add(facing); 
    } 
    this.energy.setDirections(ret, Collections.emptySet());
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.ItemConsuming);
  }
  
  public double getEnergy() {
    return 0.0D;
  }
  
  public boolean useEnergy(double amount) {
    return false;
  }
  
  public ContainerBase<TileEntityAbstractRCI> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityAbstractRCI>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  private void updateReactor() {
    World world = func_145831_w();
    if (!world.func_175697_a(this.field_174879_c, 2)) {
      this.reactor = null;
      return;
    } 
    TileEntity tileEntity = world.func_175625_s(this.field_174879_c.func_177972_a(getFacing().func_176734_d()));
    if (tileEntity instanceof TileEntityNuclearReactorElectric) {
      this.reactor = (TileEntityNuclearReactorElectric)tileEntity;
      return;
    } 
    if (tileEntity instanceof TileEntityReactorChamberElectric) {
      this.reactor = ((TileEntityReactorChamberElectric)tileEntity).getReactorInstance();
      return;
    } 
    this.reactor = null;
  }
}

package ic2.core.block.reactor.tileentity;

import com.google.common.base.Supplier;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.util.StackUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityReactorChamberElectric extends TileEntityBlock implements IInventory, IReactorChamber, IEnergyEmitter {
  public final Redstone redstone;
  
  protected final Fluids fluids;
  
  private TileEntityNuclearReactorElectric reactor;
  
  private long lastReactorUpdate;
  
  public TileEntityReactorChamberElectric() {
    this.redstone = (Redstone)addComponent((TileEntityComponent)new Redstone(this));
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids(this));
    this.fluids.addUnmanagedTankHook(new Supplier<Collection<Fluids.InternalFluidTank>>() {
          public Collection<Fluids.InternalFluidTank> get() {
            TileEntityNuclearReactorElectric reactor = TileEntityReactorChamberElectric.this.getReactor();
            if (reactor == null)
              return Collections.emptySet(); 
            return Arrays.asList(new Fluids.InternalFluidTank[] { reactor.inputTank, reactor.outputTank });
          }
        });
  }
  
  protected void onLoaded() {
    super.onLoaded();
    updateRedstoneLink();
  }
  
  private void updateRedstoneLink() {
    if ((getWorld()).isRemote)
      return; 
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      this.redstone.linkTo(reactor.redstone); 
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    super.updateEntityClient();
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      TileEntityNuclearReactorElectric.showHeatEffects(getWorld(), this.pos, reactor.getHeat()); 
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null) {
      World world = getWorld();
      return reactor.getBlockType().onBlockActivated(world, reactor.getPos(), world.getBlockState(reactor.getPos()), player, hand, side, hitX, hitY, hitZ);
    } 
    return false;
  }
  
  protected void onNeighborChange(Block neighbor, BlockPos neighborPos) {
    super.onNeighborChange(neighbor, neighborPos);
    this.lastReactorUpdate = 0L;
    if (getReactor() == null)
      destoryChamber(true); 
  }
  
  public void destoryChamber(boolean wrench) {
    World world = getWorld();
    world.setBlockToAir(this.pos);
    for (ItemStack drop : getSelfDrops(0, wrench))
      StackUtil.dropAsEntity(world, this.pos, drop); 
  }
  
  public String getName() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.getName() : "<null>";
  }
  
  public boolean hasCustomName() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.hasCustomName() : false;
  }
  
  public ITextComponent getDisplayName() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.getDisplayName() : (ITextComponent)new TextComponentString("<null>");
  }
  
  public int getSizeInventory() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.getSizeInventory() : 0;
  }
  
  public boolean isEmpty() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.isEmpty() : true;
  }
  
  public ItemStack getStackInSlot(int index) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.getStackInSlot(index) : null;
  }
  
  public ItemStack decrStackSize(int index, int count) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.decrStackSize(index, count) : null;
  }
  
  public ItemStack removeStackFromSlot(int index) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.removeStackFromSlot(index) : null;
  }
  
  public void setInventorySlotContents(int index, ItemStack stack) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.setInventorySlotContents(index, stack); 
  }
  
  public int getInventoryStackLimit() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.getInventoryStackLimit() : 0;
  }
  
  public boolean isUsableByPlayer(EntityPlayer player) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.isUsableByPlayer(player) : false;
  }
  
  public void openInventory(EntityPlayer player) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.openInventory(player); 
  }
  
  public void closeInventory(EntityPlayer player) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.closeInventory(player); 
  }
  
  public boolean isItemValidForSlot(int index, ItemStack stack) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.isItemValidForSlot(index, stack) : false;
  }
  
  public int getField(int id) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.getField(id) : 0;
  }
  
  public void setField(int id, int value) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.setField(id, value); 
  }
  
  public int getFieldCount() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.getFieldCount() : 0;
  }
  
  public void clear() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.clear(); 
  }
  
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
    return true;
  }
  
  public TileEntityNuclearReactorElectric getReactorInstance() {
    return this.reactor;
  }
  
  public boolean isWall() {
    return false;
  }
  
  public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
    if (super.hasCapability(capability, facing))
      return (T)super.getCapability(capability, facing); 
    if (this.reactor != null)
      return (T)this.reactor.getCapability(capability, facing); 
    return null;
  }
  
  public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
    return (super.hasCapability(capability, facing) || (this.reactor != null && this.reactor.hasCapability(capability, facing)));
  }
  
  private TileEntityNuclearReactorElectric getReactor() {
    long time = getWorld().getTotalWorldTime();
    if (time != this.lastReactorUpdate) {
      updateReactor();
      this.lastReactorUpdate = time;
    } else if (this.reactor != null && this.reactor.isInvalid()) {
      this.reactor = null;
    } 
    return this.reactor;
  }
  
  private void updateReactor() {
    World world = getWorld();
    this.reactor = null;
    for (EnumFacing facing : EnumFacing.VALUES) {
      TileEntity te = world.getTileEntity(this.pos.offset(facing));
      if (te instanceof TileEntityNuclearReactorElectric) {
        this.reactor = (TileEntityNuclearReactorElectric)te;
        break;
      } 
    } 
  }
}

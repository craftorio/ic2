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
      TileEntityNuclearReactorElectric.showHeatEffects(getWorld(), this.field_174879_c, reactor.getHeat()); 
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null) {
      World world = getWorld();
      return reactor.getBlockType().func_180639_a(world, reactor.getPos(), world.func_180495_p(reactor.getPos()), player, hand, side, hitX, hitY, hitZ);
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
    world.func_175698_g(this.field_174879_c);
    for (ItemStack drop : getSelfDrops(0, wrench))
      StackUtil.dropAsEntity(world, this.field_174879_c, drop); 
  }
  
  public String func_70005_c_() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_70005_c_() : "<null>";
  }
  
  public boolean func_145818_k_() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_145818_k_() : false;
  }
  
  public ITextComponent func_145748_c_() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_145748_c_() : (ITextComponent)new TextComponentString("<null>");
  }
  
  public int func_70302_i_() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_70302_i_() : 0;
  }
  
  public boolean func_191420_l() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_191420_l() : true;
  }
  
  public ItemStack func_70301_a(int index) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_70301_a(index) : null;
  }
  
  public ItemStack func_70298_a(int index, int count) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_70298_a(index, count) : null;
  }
  
  public ItemStack func_70304_b(int index) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_70304_b(index) : null;
  }
  
  public void func_70299_a(int index, ItemStack stack) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.func_70299_a(index, stack); 
  }
  
  public int func_70297_j_() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_70297_j_() : 0;
  }
  
  public boolean func_70300_a(EntityPlayer player) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_70300_a(player) : false;
  }
  
  public void func_174889_b(EntityPlayer player) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.func_174889_b(player); 
  }
  
  public void func_174886_c(EntityPlayer player) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.func_174886_c(player); 
  }
  
  public boolean func_94041_b(int index, ItemStack stack) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_94041_b(index, stack) : false;
  }
  
  public int func_174887_a_(int id) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_174887_a_(id) : 0;
  }
  
  public void func_174885_b(int id, int value) {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.func_174885_b(id, value); 
  }
  
  public int func_174890_g() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    return (reactor != null) ? reactor.func_174890_g() : 0;
  }
  
  public void func_174888_l() {
    TileEntityNuclearReactorElectric reactor = getReactor();
    if (reactor != null)
      reactor.func_174888_l(); 
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
    long time = getWorld().func_82737_E();
    if (time != this.lastReactorUpdate) {
      updateReactor();
      this.lastReactorUpdate = time;
    } else if (this.reactor != null && this.reactor.func_145837_r()) {
      this.reactor = null;
    } 
    return this.reactor;
  }
  
  private void updateReactor() {
    World world = getWorld();
    this.reactor = null;
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      TileEntity te = world.func_175625_s(this.field_174879_c.func_177972_a(facing));
      if (te instanceof TileEntityNuclearReactorElectric) {
        this.reactor = (TileEntityNuclearReactorElectric)te;
        break;
      } 
    } 
  }
}

package ic2.core.block.reactor.tileentity;

import ic2.core.profile.NotClassic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

@NotClassic
public class TileEntityReactorAccessHatch extends TileEntityReactorVessel implements IInventory {
  private IItemHandler itemHandler;
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    if (reactor != null) {
      World world = getWorld();
      return reactor.getBlockType().func_180639_a(world, reactor.getPos(), world.func_180495_p(reactor.getPos()), player, hand, side, hitX, hitY, hitZ);
    } 
    return false;
  }
  
  public String func_70005_c_() {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_70005_c_() : "<null>";
  }
  
  public boolean func_145818_k_() {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_145818_k_() : false;
  }
  
  public ITextComponent func_145748_c_() {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_145748_c_() : (ITextComponent)new TextComponentString("<null>");
  }
  
  public int func_70302_i_() {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_70302_i_() : 0;
  }
  
  public boolean func_191420_l() {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_191420_l() : true;
  }
  
  public ItemStack func_70301_a(int index) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_70301_a(index) : null;
  }
  
  public ItemStack func_70298_a(int index, int count) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_70298_a(index, count) : null;
  }
  
  public ItemStack func_70304_b(int index) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_70304_b(index) : null;
  }
  
  public void func_70299_a(int index, ItemStack stack) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    if (reactor != null)
      reactor.func_70299_a(index, stack); 
  }
  
  public int func_70297_j_() {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_70297_j_() : 0;
  }
  
  public boolean func_70300_a(EntityPlayer player) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_70300_a(player) : false;
  }
  
  public void func_174889_b(EntityPlayer player) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    if (reactor != null)
      reactor.func_174889_b(player); 
  }
  
  public void func_174886_c(EntityPlayer player) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    if (reactor != null)
      reactor.func_174886_c(player); 
  }
  
  public boolean func_94041_b(int index, ItemStack stack) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_94041_b(index, stack) : false;
  }
  
  public int func_174887_a_(int id) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_174887_a_(id) : 0;
  }
  
  public void func_174885_b(int id, int value) {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    if (reactor != null)
      reactor.func_174885_b(id, value); 
  }
  
  public int func_174890_g() {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    return (reactor != null) ? reactor.func_174890_g() : 0;
  }
  
  public void func_174888_l() {
    TileEntityNuclearReactorElectric reactor = getReactorInstance();
    if (reactor != null)
      reactor.func_174888_l(); 
  }
  
  public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
    return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing));
  }
  
  public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      if (this.itemHandler == null)
        this.itemHandler = (IItemHandler)new InvWrapper(this); 
      return (T)CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemHandler);
    } 
    return (T)super.getCapability(capability, facing);
  }
}

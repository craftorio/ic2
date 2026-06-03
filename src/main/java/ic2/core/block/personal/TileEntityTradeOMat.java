package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.WorldData;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLinked;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.network.NetworkManager;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTradeOMat extends TileEntityInventory implements IPersonalBlock, IHasGui, INetworkTileEntityEventListener, INetworkClientTileEntityEventListener {
  private int ticker = IC2.random.nextInt(64);
  
  public final InvSlot demandSlot = new InvSlot((IInventorySlotHolder)this, "demand", InvSlot.Access.NONE, 1);
  
  public final InvSlot offerSlot = new InvSlot((IInventorySlotHolder)this, "offer", InvSlot.Access.NONE, 1);
  
  public final InvSlotConsumableLinked inputSlot = new InvSlotConsumableLinked((IInventorySlotHolder)this, "input", 1, this.demandSlot);
  
  public final InvSlotOutput outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1);
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    if (nbt.func_74764_b("ownerGameProfile"))
      this.owner = NBTUtil.func_152459_a(nbt.func_74775_l("ownerGameProfile")); 
    this.totalTradeCount = nbt.func_74762_e("totalTradeCount");
    if (nbt.func_74764_b("infinite"))
      this.infinite = nbt.func_74767_n("infinite"); 
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    if (this.owner != null) {
      NBTTagCompound ownerNbt = new NBTTagCompound();
      NBTUtil.func_180708_a(ownerNbt, this.owner);
      nbt.func_74782_a("ownerGameProfile", (NBTBase)ownerNbt);
    } 
    nbt.func_74768_a("totalTradeCount", this.totalTradeCount);
    if (this.infinite)
      nbt.func_74757_a("infinite", this.infinite); 
    return nbt;
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("owner");
    return ret;
  }
  
  public final boolean isWireless() {
    return getActive();
  }
  
  public final boolean setWireless(boolean wireless) {
    if (isWireless() == wireless)
      return false; 
    if (wireless) {
      setActive(true);
      (WorldData.get(this.field_145850_b)).tradeMarket.registerTradeOMat(this);
    } else {
      setActive(false);
      (WorldData.get(this.field_145850_b)).tradeMarket.unregisterTradeOMat(this);
    } 
    return true;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    trade();
    if (this.infinite) {
      this.stock = -1;
    } else if (++this.ticker % 64 == 0) {
      updateStock();
    } 
  }
  
  private void trade() {
    ItemStack tradedIn = this.inputSlot.consumeLinked(true);
    if (StackUtil.isEmpty(tradedIn))
      return; 
    ItemStack offer = this.offerSlot.get();
    if (StackUtil.isEmpty(offer))
      return; 
    if (!this.outputSlot.canAdd(offer))
      return; 
    if (this.infinite) {
      this.inputSlot.consumeLinked(false);
      this.outputSlot.add(offer);
    } else {
      int amount = StackUtil.fetch((TileEntity)this, offer, true);
      if (amount != StackUtil.getSize(offer))
        return; 
      int transferredOut = StackUtil.distribute((TileEntity)this, tradedIn, true);
      if (transferredOut != StackUtil.getSize(tradedIn))
        return; 
      amount = StackUtil.fetch((TileEntity)this, offer, false);
      if (amount == 0)
        return; 
      if (amount != StackUtil.getSize(offer)) {
        IC2.log.warn(LogCategory.Block, "The Trade-O-Mat at %s received an inconsistent result from an adjacent trade supply inventory, the %s items will be lost.", new Object[] { Util.formatPosition((TileEntity)this), Integer.valueOf(amount) });
        return;
      } 
      StackUtil.distribute((TileEntity)this, this.inputSlot.consumeLinked(false), false);
      this.outputSlot.add(offer);
      this.stock--;
    } 
    this.totalTradeCount++;
    ((NetworkManager)IC2.network.get(true)).initiateTileEntityEvent((TileEntity)this, 0, true);
    func_70296_d();
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (IC2.platform.isSimulating()) {
      updateStock();
      if (isWireless())
        (WorldData.get(this.field_145850_b)).tradeMarket.registerTradeOMat(this); 
    } 
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (!isWireless() && StackUtil.consume(player, hand, StackUtil.sameStack(ItemName.upgrade.getItemStack((Enum)ItemUpgradeModule.UpgradeType.remote_interface)), 1)) {
      if (!(func_145831_w()).field_72995_K)
        setWireless(true); 
      return true;
    } 
    return super.onActivated(player, hand, side, hitX, hitY, hitZ);
  }
  
  public void updateStock() {
    ItemStack offer = this.offerSlot.get();
    if (StackUtil.isEmpty(offer)) {
      this.stock = 0;
    } else {
      this.stock = StackUtil.fetch((TileEntity)this, StackUtil.copyWithSize(offer, 2147483647), true) / StackUtil.getSize(offer);
    } 
  }
  
  protected void onUnloaded() {
    super.onUnloaded();
    if (!(func_145831_w()).field_72995_K && isWireless())
      (WorldData.get(this.field_145850_b)).tradeMarket.unregisterTradeOMat(this); 
  }
  
  public boolean wrenchCanRemove(EntityPlayer player) {
    return permitsAccess(player.func_146103_bH());
  }
  
  protected List<ItemStack> getAuxDrops(int fortune) {
    List<ItemStack> drops = super.getAuxDrops(fortune);
    if (isWireless())
      drops.add(ItemName.upgrade.getItemStack((Enum)ItemUpgradeModule.UpgradeType.remote_interface)); 
    return drops;
  }
  
  public boolean permitsAccess(GameProfile profile) {
    return TileEntityPersonalChest.checkAccess(this, profile);
  }
  
  public IInventory getPrivilegedInventory(GameProfile accessor) {
    return (IInventory)this;
  }
  
  public GameProfile getOwner() {
    return this.owner;
  }
  
  public void setOwner(GameProfile owner) {
    this.owner = owner;
  }
  
  protected boolean canEntityDestroy(Entity entity) {
    return false;
  }
  
  public ContainerBase<TileEntityTradeOMat> getGuiContainer(EntityPlayer player) {
    if (permitsAccess(player.func_146103_bH()))
      return (ContainerBase<TileEntityTradeOMat>)new ContainerTradeOMatOpen(player, this); 
    return (ContainerBase<TileEntityTradeOMat>)new ContainerTradeOMatClosed(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    if (isAdmin || permitsAccess(player.func_146103_bH()))
      return (GuiScreen)new GuiTradeOMatOpen(new ContainerTradeOMatOpen(player, this), isAdmin); 
    return (GuiScreen)new GuiTradeOMatClosed(new ContainerTradeOMatClosed(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public void onNetworkEvent(int event) {
    switch (event) {
      case 0:
        IC2.audioManager.playOnce(this, "Machines/o-mat.ogg");
        return;
    } 
    IC2.platform.displayError("An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID " + event + ", tile entity below)\nT: " + this + " (" + this.field_174879_c + ")", new Object[0]);
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    if (event == 0 && 
      func_145831_w().func_73046_m().func_184103_al().func_152596_g(player.func_146103_bH())) {
      this.infinite = !this.infinite;
      if (!this.infinite)
        updateStock(); 
    } 
  }
  
  private GameProfile owner = null;
  
  public int totalTradeCount = 0;
  
  public int stock = 0;
  
  public boolean infinite = false;
  
  private static final int stockUpdateRate = 64;
  
  private static final int EventTrade = 0;
}

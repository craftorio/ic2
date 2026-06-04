package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotConsumableLinked;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityEnergyOMat extends TileEntityInventory implements IPersonalBlock, IHasGui, IEnergySink, IEnergySource, INetworkClientTileEntityEventListener, IUpgradableBlock {
  public int euOffer = 1000;
  
  private GameProfile owner = null;
  
  private boolean addedToEnergyNet = false;
  
  public int paidFor;
  
  public double euBuffer;
  
  private int euBufferMax = 10000;
  
  private int tier = 1;
  
  public final InvSlot demandSlot;
  
  public final InvSlotConsumableLinked inputSlot;
  
  public final InvSlotCharge chargeSlot;
  
  public final InvSlotUpgrade upgradeSlot;
  
  public TileEntityEnergyOMat() {
    this.demandSlot = new InvSlot((IInventorySlotHolder)this, "demand", InvSlot.Access.NONE, 1);
    this.inputSlot = new InvSlotConsumableLinked((IInventorySlotHolder)this, "input", 1, this.demandSlot);
    this.chargeSlot = new InvSlotCharge((IInventorySlotHolder)this, 1);
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 1);
  }
  
  public void readFromNBT(NBTTagCompound nbttagcompound) {
    super.readFromNBT(nbttagcompound);
    if (nbttagcompound.func_74764_b("ownerGameProfile"))
      this.owner = NBTUtil.func_152459_a(nbttagcompound.getCompoundTag("ownerGameProfile")); 
    this.euOffer = nbttagcompound.func_74762_e("euOffer");
    this.paidFor = nbttagcompound.func_74762_e("paidFor");
    try {
      this.euBuffer = nbttagcompound.getDouble("euBuffer");
    } catch (Exception e) {
      this.euBuffer = nbttagcompound.func_74762_e("euBuffer");
    } 
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    if (this.owner != null) {
      NBTTagCompound ownerNbt = new NBTTagCompound();
      NBTUtil.func_180708_a(ownerNbt, this.owner);
      nbt.setTag("ownerGameProfile", (NBTBase)ownerNbt);
    } 
    nbt.func_74768_a("euOffer", this.euOffer);
    nbt.func_74768_a("paidFor", this.paidFor);
    nbt.setDouble("euBuffer", this.euBuffer);
    return nbt;
  }
  
  public boolean wrenchCanRemove(EntityPlayer player) {
    return permitsAccess(player.func_146103_bH());
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (!(getWorld()).isRemote) {
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent((IEnergyTile)this));
      this.addedToEnergyNet = true;
    } 
  }
  
  protected void onUnloaded() {
    if (IC2.platform.isSimulating() && this.addedToEnergyNet) {
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent((IEnergyTile)this));
      this.addedToEnergyNet = false;
    } 
    super.onUnloaded();
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean invChanged = false;
    this.euBufferMax = 10000;
    this.tier = 1;
    this.chargeSlot.setTier(1);
    if (!this.upgradeSlot.isEmpty()) {
      this.euBufferMax = this.upgradeSlot.getEnergyStorage(10000, 0, 0);
      this.tier = 1 + this.upgradeSlot.extraTier;
      this.chargeSlot.setTier(this.tier);
    } 
    ItemStack tradedIn = this.inputSlot.consumeLinked(true);
    if (tradedIn != null) {
      int transferred = StackUtil.distribute((TileEntity)this, tradedIn, true);
      if (transferred == StackUtil.getSize(tradedIn)) {
        StackUtil.distribute((TileEntity)this, this.inputSlot.consumeLinked(false), false);
        this.paidFor += this.euOffer;
        invChanged = true;
      } 
    } 
    if (this.euBuffer >= 1.0D) {
      double sent = this.chargeSlot.charge(this.euBuffer);
      if (sent > 0.0D) {
        this.euBuffer -= sent;
        invChanged = true;
      } 
    } 
    if (invChanged)
      func_70296_d(); 
  }
  
  public boolean permitsAccess(GameProfile profile) {
    return TileEntityPersonalChest.checkAccess(this, profile);
  }
  
  public IInventory getPrivilegedInventory(GameProfile accessor) {
    return (IInventory)this;
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = new ArrayList<>();
    ret.add("owner");
    ret.addAll(super.getNetworkedFields());
    return ret;
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
  
  protected boolean canSetFacingWrench(EnumFacing facing, EntityPlayer player) {
    if (player == null || !permitsAccess(player.func_146103_bH()))
      return false; 
    return super.canSetFacingWrench(facing, player);
  }
  
  public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
    return !facingMatchesDirection(direction);
  }
  
  public boolean facingMatchesDirection(EnumFacing direction) {
    return (direction == getFacing());
  }
  
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction) {
    return facingMatchesDirection(direction);
  }
  
  public double getOfferedEnergy() {
    return this.euBuffer;
  }
  
  public void drawEnergy(double amount) {
    this.euBuffer -= amount;
  }
  
  public double getDemandedEnergy() {
    return Math.min(this.paidFor, this.euBufferMax - this.euBuffer);
  }
  
  public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
    double toAdd = Math.min(Math.min(amount, this.paidFor), this.euBufferMax - this.euBuffer);
    this.paidFor = (int)(this.paidFor - toAdd);
    this.euBuffer += toAdd;
    return amount - toAdd;
  }
  
  public int getSourceTier() {
    return this.tier;
  }
  
  public int getSinkTier() {
    return Integer.MAX_VALUE;
  }
  
  public ContainerBase<TileEntityEnergyOMat> getGuiContainer(EntityPlayer player) {
    if (permitsAccess(player.func_146103_bH()))
      return (ContainerBase<TileEntityEnergyOMat>)new ContainerEnergyOMatOpen(player, this); 
    return (ContainerBase<TileEntityEnergyOMat>)new ContainerEnergyOMatClosed(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    if (isAdmin || permitsAccess(player.func_146103_bH()))
      return (GuiScreen)new GuiEnergyOMatOpen(new ContainerEnergyOMatOpen(player, this)); 
    return (GuiScreen)new GuiEnergyOMatClosed(new ContainerEnergyOMatClosed(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    if (!permitsAccess(player.func_146103_bH()))
      return; 
    switch (event) {
      case 0:
        attemptSet(-100000);
        break;
      case 1:
        attemptSet(-10000);
        break;
      case 2:
        attemptSet(-1000);
        break;
      case 3:
        attemptSet(-100);
        break;
      case 4:
        attemptSet(100000);
        break;
      case 5:
        attemptSet(10000);
        break;
      case 6:
        attemptSet(1000);
        break;
      case 7:
        attemptSet(100);
        break;
    } 
  }
  
  private void attemptSet(int amount) {
    this.euOffer += amount;
    if (this.euOffer < 100)
      this.euOffer = 100; 
  }
  
  public double getEnergy() {
    return this.euBuffer;
  }
  
  public boolean useEnergy(double amount) {
    if (amount <= this.euBuffer) {
      amount -= this.euBuffer;
      return true;
    } 
    return false;
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.EnergyStorage, UpgradableProperty.Transformer);
  }
}

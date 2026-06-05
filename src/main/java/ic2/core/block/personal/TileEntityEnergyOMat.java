package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityEnergyOMat
   extends TileEntityInventory
   implements IPersonalBlock,
   IHasGui,
   IEnergySink,
   IEnergySource,
   INetworkClientTileEntityEventListener,
   IUpgradableBlock {
   public int euOffer = 1000;
   private GameProfile owner = null;
   private boolean addedToEnergyNet = false;
   public int paidFor;
   public double euBuffer;
   private int euBufferMax = 10000;
   private int tier = 1;
   public final InvSlot demandSlot = new InvSlot(this, "demand", InvSlot.Access.NONE, 1);
   public final InvSlotConsumableLinked inputSlot = new InvSlotConsumableLinked(this, "input", 1, this.demandSlot);
   public final InvSlotCharge chargeSlot = new InvSlotCharge(this, 1);
   public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);

   @Override
   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      if (nbttagcompound.hasKey("ownerGameProfile")) {
         this.owner = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("ownerGameProfile"));
      }

      this.euOffer = nbttagcompound.getInteger("euOffer");
      this.paidFor = nbttagcompound.getInteger("paidFor");

      try {
         this.euBuffer = nbttagcompound.getDouble("euBuffer");
      } catch (Exception e) {
         this.euBuffer = nbttagcompound.getInteger("euBuffer");
      }
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      super.writeToNBT(nbt);
      if (this.owner != null) {
         NBTTagCompound ownerNbt = new NBTTagCompound();
         NBTUtil.writeGameProfile(ownerNbt, this.owner);
         nbt.setTag("ownerGameProfile", ownerNbt);
      }

      nbt.setInteger("euOffer", this.euOffer);
      nbt.setInteger("paidFor", this.paidFor);
      nbt.setDouble("euBuffer", this.euBuffer);
      return nbt;
   }

   @Override
   public boolean wrenchCanRemove(EntityPlayer player) {
      return this.permitsAccess(player.getGameProfile());
   }

   @Override
   protected void onLoaded() {
      super.onLoaded();
      if (!this.getWorld().isRemote) {
         MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
         this.addedToEnergyNet = true;
      }
   }

   @Override
   protected void onUnloaded() {
      if (IC2.platform.isSimulating() && this.addedToEnergyNet) {
         MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
         this.addedToEnergyNet = false;
      }

      super.onUnloaded();
   }

   @Override
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
         int transferred = StackUtil.distribute(this, tradedIn, true);
         if (transferred == StackUtil.getSize(tradedIn)) {
            StackUtil.distribute(this, this.inputSlot.consumeLinked(false), false);
            this.paidFor = this.paidFor + this.euOffer;
            invChanged = true;
         }
      }

      if (this.euBuffer >= 1.0) {
         double sent = this.chargeSlot.charge(this.euBuffer);
         if (sent > 0.0) {
            this.euBuffer -= sent;
            invChanged = true;
         }
      }

      if (invChanged) {
         this.markDirty();
      }
   }

   @Override
   public boolean permitsAccess(GameProfile profile) {
      return TileEntityPersonalChest.checkAccess(this, profile);
   }

   @Override
   public IInventory getPrivilegedInventory(GameProfile accessor) {
      return this;
   }

   @Override
   public List<String> getNetworkedFields() {
      List<String> ret = new ArrayList<>();
      ret.add("owner");
      ret.addAll(super.getNetworkedFields());
      return ret;
   }

   @Override
   public GameProfile getOwner() {
      return this.owner;
   }

   @Override
   public void setOwner(GameProfile owner) {
      this.owner = owner;
   }

   @Override
   protected boolean canEntityDestroy(Entity entity) {
      return false;
   }

   @Override
   protected boolean canSetFacingWrench(EnumFacing facing, EntityPlayer player) {
      return player != null && this.permitsAccess(player.getGameProfile()) ? super.canSetFacingWrench(facing, player) : false;
   }

   @Override
   public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
      return !this.facingMatchesDirection(direction);
   }

   public boolean facingMatchesDirection(EnumFacing direction) {
      return direction == this.getFacing();
   }

   @Override
   public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction) {
      return this.facingMatchesDirection(direction);
   }

   @Override
   public double getOfferedEnergy() {
      return this.euBuffer;
   }

   @Override
   public void drawEnergy(double amount) {
      this.euBuffer -= amount;
   }

   @Override
   public double getDemandedEnergy() {
      return Math.min(this.paidFor, this.euBufferMax - this.euBuffer);
   }

   @Override
   public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
      double toAdd = Math.min(Math.min(amount, this.paidFor), this.euBufferMax - this.euBuffer);
      this.paidFor = (int)(this.paidFor - toAdd);
      this.euBuffer += toAdd;
      return amount - toAdd;
   }

   @Override
   public int getSourceTier() {
      return this.tier;
   }

   @Override
   public int getSinkTier() {
      return Integer.MAX_VALUE;
   }

   @Override
   public ContainerBase<TileEntityEnergyOMat> getGuiContainer(EntityPlayer player) {
      return this.permitsAccess(player.getGameProfile()) ? new ContainerEnergyOMatOpen(player, this) : new ContainerEnergyOMatClosed(player, this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return !isAdmin && !this.permitsAccess(player.getGameProfile())
         ? new GuiEnergyOMatClosed(new ContainerEnergyOMatClosed(player, this))
         : new GuiEnergyOMatOpen(new ContainerEnergyOMatOpen(player, this));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }

   @Override
   public void onNetworkEvent(EntityPlayer player, int event) {
      if (this.permitsAccess(player.getGameProfile())) {
         switch (event) {
            case 0:
               this.attemptSet(-100000);
               break;
            case 1:
               this.attemptSet(-10000);
               break;
            case 2:
               this.attemptSet(-1000);
               break;
            case 3:
               this.attemptSet(-100);
               break;
            case 4:
               this.attemptSet(100000);
               break;
            case 5:
               this.attemptSet(10000);
               break;
            case 6:
               this.attemptSet(1000);
               break;
            case 7:
               this.attemptSet(100);
         }
      }
   }

   private void attemptSet(int amount) {
      this.euOffer += amount;
      if (this.euOffer < 100) {
         this.euOffer = 100;
      }
   }

   @Override
   public double getEnergy() {
      return this.euBuffer;
   }

   @Override
   public boolean useEnergy(double amount) {
      if (amount <= this.euBuffer) {
         amount -= this.euBuffer;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public Set<UpgradableProperty> getUpgradableProperties() {
      return EnumSet.of(UpgradableProperty.EnergyStorage, UpgradableProperty.Transformer);
   }
}

package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.WorldData;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLinked;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.item.upgrade.ItemUpgradeModule;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTradeOMat
   extends TileEntityInventory
   implements IPersonalBlock,
   IHasGui,
   INetworkTileEntityEventListener,
   INetworkClientTileEntityEventListener {
   private int ticker;
   private GameProfile owner = null;
   public int totalTradeCount = 0;
   public int stock = 0;
   public boolean infinite = false;
   private static final int stockUpdateRate = 64;
   private static final int EventTrade = 0;
   public final InvSlot demandSlot;
   public final InvSlot offerSlot;
   public final InvSlotConsumableLinked inputSlot;
   public final InvSlotOutput outputSlot;

   public TileEntityTradeOMat() {
      this.ticker = IC2.random.nextInt(64);
      this.demandSlot = new InvSlot(this, "demand", InvSlot.Access.NONE, 1);
      this.offerSlot = new InvSlot(this, "offer", InvSlot.Access.NONE, 1);
      this.inputSlot = new InvSlotConsumableLinked(this, "input", 1, this.demandSlot);
      this.outputSlot = new InvSlotOutput(this, "output", 1);
   }

   @Override
   public void readFromNBT(NBTTagCompound nbt) {
      super.readFromNBT(nbt);
      if (nbt.hasKey("ownerGameProfile")) {
         this.owner = NBTUtil.readGameProfileFromNBT(nbt.getCompoundTag("ownerGameProfile"));
      }

      this.totalTradeCount = nbt.getInteger("totalTradeCount");
      if (nbt.hasKey("infinite")) {
         this.infinite = nbt.getBoolean("infinite");
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

      nbt.setInteger("totalTradeCount", this.totalTradeCount);
      if (this.infinite) {
         nbt.setBoolean("infinite", this.infinite);
      }

      return nbt;
   }

   @Override
   public List<String> getNetworkedFields() {
      List<String> ret = super.getNetworkedFields();
      ret.add("owner");
      return ret;
   }

   public final boolean isWireless() {
      return this.getActive();
   }

   public final boolean setWireless(boolean wireless) {
      if (this.isWireless() == wireless) {
         return false;
      }

      if (wireless) {
         this.setActive(true);
         WorldData.get(this.world).tradeMarket.registerTradeOMat(this);
      } else {
         this.setActive(false);
         WorldData.get(this.world).tradeMarket.unregisterTradeOMat(this);
      }

      return true;
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      this.trade();
      if (this.infinite) {
         this.stock = -1;
      } else if (++this.ticker % 64 == 0) {
         this.updateStock();
      }
   }

   private void trade() {
      ItemStack tradedIn = this.inputSlot.consumeLinked(true);
      if (!StackUtil.isEmpty(tradedIn)) {
         ItemStack offer = this.offerSlot.get();
         if (!StackUtil.isEmpty(offer)) {
            if (this.outputSlot.canAdd(offer)) {
               if (this.infinite) {
                  this.inputSlot.consumeLinked(false);
                  this.outputSlot.add(offer);
               } else {
                  int amount = StackUtil.fetch(this, offer, true);
                  if (amount != StackUtil.getSize(offer)) {
                     return;
                  }

                  int transferredOut = StackUtil.distribute(this, tradedIn, true);
                  if (transferredOut != StackUtil.getSize(tradedIn)) {
                     return;
                  }

                  amount = StackUtil.fetch(this, offer, false);
                  if (amount == 0) {
                     return;
                  }

                  if (amount != StackUtil.getSize(offer)) {
                     IC2.log
                        .warn(
                           LogCategory.Block,
                           "The Trade-O-Mat at %s received an inconsistent result from an adjacent trade supply inventory, the %s items will be lost.",
                           Util.formatPosition(this),
                           amount
                        );
                     return;
                  }

                  StackUtil.distribute(this, this.inputSlot.consumeLinked(false), false);
                  this.outputSlot.add(offer);
                  this.stock--;
               }

               this.totalTradeCount++;
               IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
               this.markDirty();
            }
         }
      }
   }

   @Override
   protected void onLoaded() {
      super.onLoaded();
      if (IC2.platform.isSimulating()) {
         this.updateStock();
         if (this.isWireless()) {
            WorldData.get(this.world).tradeMarket.registerTradeOMat(this);
         }
      }
   }

   @Override
   protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
      if (!this.isWireless()
         && StackUtil.consume(player, hand, StackUtil.sameStack(ItemName.upgrade.getItemStack(ItemUpgradeModule.UpgradeType.remote_interface)), 1)) {
         if (!this.getWorld().isRemote) {
            this.setWireless(true);
         }

         return true;
      } else {
         return super.onActivated(player, hand, side, hitX, hitY, hitZ);
      }
   }

   public void updateStock() {
      ItemStack offer = this.offerSlot.get();
      if (StackUtil.isEmpty(offer)) {
         this.stock = 0;
      } else {
         this.stock = StackUtil.fetch(this, StackUtil.copyWithSize(offer, Integer.MAX_VALUE), true) / StackUtil.getSize(offer);
      }
   }

   @Override
   protected void onUnloaded() {
      super.onUnloaded();
      if (!this.getWorld().isRemote && this.isWireless()) {
         WorldData.get(this.world).tradeMarket.unregisterTradeOMat(this);
      }
   }

   @Override
   public boolean wrenchCanRemove(EntityPlayer player) {
      return this.permitsAccess(player.getGameProfile());
   }

   @Override
   protected List<ItemStack> getAuxDrops(int fortune) {
      List<ItemStack> drops = super.getAuxDrops(fortune);
      if (this.isWireless()) {
         drops.add(ItemName.upgrade.getItemStack(ItemUpgradeModule.UpgradeType.remote_interface));
      }

      return drops;
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
   public ContainerBase<TileEntityTradeOMat> getGuiContainer(EntityPlayer player) {
      return this.permitsAccess(player.getGameProfile()) ? new ContainerTradeOMatOpen(player, this) : new ContainerTradeOMatClosed(player, this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return !isAdmin && !this.permitsAccess(player.getGameProfile())
         ? new GuiTradeOMatClosed(new ContainerTradeOMatClosed(player, this))
         : new GuiTradeOMatOpen(new ContainerTradeOMatOpen(player, this), isAdmin);
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }

   @Override
   public void onNetworkEvent(int event) {
      switch (event) {
         case 0:
            IC2.audioManager.playOnce(this, "Machines/o-mat.ogg");
            break;
         default:
            IC2.platform
               .displayError(
                  "An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID "
                     + event
                     + ", tile entity below)\nT: "
                     + this
                     + " ("
                     + this.pos
                     + ")"
               );
      }
   }

   @Override
   public void onNetworkEvent(EntityPlayer player, int event) {
      if (event == 0 && this.getWorld().getMinecraftServer().getPlayerList().canSendCommands(player.getGameProfile())) {
         this.infinite = !this.infinite;
         if (!this.infinite) {
            this.updateStock();
         }
      }
   }
}

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

   @Override
   protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      if (reactor != null) {
         World world = this.getWorld();
         return reactor.getBlockType()
            .onBlockActivated(world, reactor.getPos(), world.getBlockState(reactor.getPos()), player, hand, side, hitX, hitY, hitZ);
      } else {
         return false;
      }
   }

   public String getName() {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.getName() : "<null>";
   }

   public boolean hasCustomName() {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.hasCustomName() : false;
   }

   public ITextComponent getDisplayName() {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return (ITextComponent)(reactor != null ? reactor.getDisplayName() : new TextComponentString("<null>"));
   }

   public int getSizeInventory() {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.getSizeInventory() : 0;
   }

   public boolean isEmpty() {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.isEmpty() : true;
   }

   public ItemStack getStackInSlot(int index) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.getStackInSlot(index) : null;
   }

   public ItemStack decrStackSize(int index, int count) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.decrStackSize(index, count) : null;
   }

   public ItemStack removeStackFromSlot(int index) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.removeStackFromSlot(index) : null;
   }

   public void setInventorySlotContents(int index, ItemStack stack) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      if (reactor != null) {
         reactor.setInventorySlotContents(index, stack);
      }
   }

   public int getInventoryStackLimit() {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.getInventoryStackLimit() : 0;
   }

   public boolean isUsableByPlayer(EntityPlayer player) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.isUsableByPlayer(player) : false;
   }

   public void openInventory(EntityPlayer player) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      if (reactor != null) {
         reactor.openInventory(player);
      }
   }

   public void closeInventory(EntityPlayer player) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      if (reactor != null) {
         reactor.closeInventory(player);
      }
   }

   public boolean isItemValidForSlot(int index, ItemStack stack) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.isItemValidForSlot(index, stack) : false;
   }

   public int getField(int id) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.getField(id) : 0;
   }

   public void setField(int id, int value) {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      if (reactor != null) {
         reactor.setField(id, value);
      }
   }

   public int getFieldCount() {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      return reactor != null ? reactor.getFieldCount() : 0;
   }

   public void clear() {
      TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
      if (reactor != null) {
         reactor.clear();
      }
   }

   @Override
   public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
      return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
   }

   @Override
   public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
      if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         if (this.itemHandler == null) {
            this.itemHandler = new InvWrapper(this);
         }

         return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.itemHandler);
      } else {
         return super.getCapability(capability, facing);
      }
   }
}

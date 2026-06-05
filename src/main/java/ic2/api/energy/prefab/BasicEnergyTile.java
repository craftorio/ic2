package ic2.api.energy.prefab;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

abstract class BasicEnergyTile implements ILocatable, IEnergyTile {
   private final Object locationProvider;
   protected World world;
   protected BlockPos pos;
   protected double capacity;
   protected double energyStored;
   protected boolean addedToEnet;

   protected BasicEnergyTile(TileEntity parent, double capacity) {
      this((Object)parent, capacity);
   }

   protected BasicEnergyTile(ILocatable parent, double capacity) {
      this((Object)parent, capacity);
   }

   private BasicEnergyTile(Object locationProvider, double capacity) {
      this.locationProvider = locationProvider;
      this.capacity = capacity;
   }

   protected BasicEnergyTile(World world, BlockPos pos, double capacity) {
      if (world == null) {
         throw new NullPointerException("null world");
      }

      if (pos == null) {
         throw new NullPointerException("null pos");
      }

      this.locationProvider = null;
      this.world = world;
      this.pos = pos;
      this.capacity = capacity;
   }

   public void update() {
      if (!this.addedToEnet) {
         this.onLoad();
      }
   }

   public void onLoad() {
      if (!this.addedToEnet && !this.getWorldObj().isRemote && Info.isIc2Available()) {
         MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
         this.addedToEnet = true;
      }
   }

   public void invalidate() {
      this.onChunkUnload();
   }

   public void onChunkUnload() {
      if (this.addedToEnet && !this.getWorldObj().isRemote && Info.isIc2Available()) {
         MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
         this.addedToEnet = false;
      }
   }

   public void readFromNBT(NBTTagCompound tag) {
      NBTTagCompound data = tag.getCompoundTag(this.getNbtTagName());
      this.setEnergyStored(data.getDouble("energy"));
   }

   public NBTTagCompound writeToNBT(NBTTagCompound tag) {
      NBTTagCompound data = new NBTTagCompound();
      data.setDouble("energy", this.getEnergyStored());
      tag.setTag(this.getNbtTagName(), data);
      return tag;
   }

   public double getCapacity() {
      return this.capacity;
   }

   public void setCapacity(double capacity) {
      this.capacity = capacity;
   }

   public double getEnergyStored() {
      return this.energyStored;
   }

   public void setEnergyStored(double amount) {
      this.energyStored = amount;
   }

   public double getFreeCapacity() {
      return this.getCapacity() - this.getEnergyStored();
   }

   public double addEnergy(double amount) {
      if (this.getWorldObj().isRemote) {
         return 0.0;
      }

      double energyStored = this.getEnergyStored();
      double capacity = this.getCapacity();
      if (amount > capacity - energyStored) {
         amount = capacity - energyStored;
      }

      this.setEnergyStored(energyStored + amount);
      return amount;
   }

   public boolean canUseEnergy(double amount) {
      return this.getEnergyStored() >= amount;
   }

   public boolean useEnergy(double amount) {
      if (this.canUseEnergy(amount) && !this.getWorldObj().isRemote) {
         this.setEnergyStored(this.getEnergyStored() - amount);
         return true;
      } else {
         return false;
      }
   }

   public boolean charge(ItemStack stack) {
      if (stack != null && Info.isIc2Available() && !this.getWorldObj().isRemote) {
         double energyStored = this.getEnergyStored();
         double amount = ElectricItem.manager.charge(stack, energyStored, Math.max(this.getSinkTier(), this.getSourceTier()), false, false);
         this.setEnergyStored(energyStored - amount);
         return amount > 0.0;
      } else {
         return false;
      }
   }

   public boolean discharge(ItemStack stack, double limit) {
      if (stack != null && Info.isIc2Available() && !this.getWorldObj().isRemote) {
         double energyStored = this.getEnergyStored();
         double amount = this.getCapacity() - energyStored;
         if (amount <= 0.0) {
            return false;
         }

         if (limit > 0.0 && limit < amount) {
            amount = limit;
         }

         amount = ElectricItem.manager.discharge(stack, amount, Math.max(this.getSinkTier(), this.getSourceTier()), limit > 0.0, true, false);
         this.setEnergyStored(energyStored + amount);
         return amount > 0.0;
      } else {
         return false;
      }
   }

   @Override
   public World getWorldObj() {
      if (this.world == null) {
         this.initLocation();
      }

      return this.world;
   }

   @Override
   public BlockPos getPosition() {
      if (this.pos == null) {
         this.initLocation();
      }

      return this.pos;
   }

   private void initLocation() {
      if (this.locationProvider instanceof ILocatable) {
         ILocatable provider = (ILocatable)this.locationProvider;
         this.world = provider.getWorldObj();
         this.pos = provider.getPosition();
      } else {
         if (!(this.locationProvider instanceof TileEntity)) {
            throw new IllegalStateException("no/incompatible location provider");
         }

         TileEntity provider = (TileEntity)this.locationProvider;
         this.world = provider.getWorld();
         this.pos = provider.getPos();
      }
   }

   protected abstract String getNbtTagName();

   protected int getSinkTier() {
      return 0;
   }

   protected int getSourceTier() {
      return 0;
   }
}

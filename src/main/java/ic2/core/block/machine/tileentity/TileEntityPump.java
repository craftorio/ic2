package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.util.LiquidUtil;
import ic2.core.util.PumpUtil;
import ic2.core.util.Util;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityPump extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider {
   public final int defaultTier;
   public int energyConsume;
   public int operationsPerTick;
   public final int defaultEnergyStorage;
   public final int defaultEnergyConsume;
   public final int defaultOperationLength;
   private AudioSource audioSource;
   private TileEntityMiner miner = null;
   public boolean redstonePowered = false;
   public final InvSlotConsumableLiquid containerSlot;
   public final InvSlotOutput outputSlot;
   public final InvSlotUpgrade upgradeSlot;
   @GuiSynced
   protected final FluidTank fluidTank;
   public short progress = 0;
   public int operationLength;
   @GuiSynced
   public float guiProgress;
   protected final Fluids fluids;

   public TileEntityPump() {
      super(20, 1);
      this.containerSlot = new InvSlotConsumableLiquid(this, "input", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill);
      this.outputSlot = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.SIDE);
      this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
      this.defaultEnergyConsume = this.energyConsume = 1;
      this.defaultOperationLength = this.operationLength = 20;
      this.defaultTier = 1;
      this.defaultEnergyStorage = 1 * this.operationLength;
      this.fluids = this.addComponent(new Fluids(this));
      this.fluidTank = this.fluids.addTankExtract("fluid", 8000);
   }

   @Override
   protected void onLoaded() {
      super.onLoaded();
      if (!this.getWorld().isRemote) {
         this.setUpgradestat();
      }
   }

   @Override
   protected void onUnloaded() {
      if (IC2.platform.isRendering() && this.audioSource != null) {
         IC2.audioManager.removeSources(this);
         this.audioSource = null;
      }

      this.miner = null;
      super.onUnloaded();
   }

   @Override
   public void readFromNBT(NBTTagCompound nbt) {
      super.readFromNBT(nbt);
      this.progress = nbt.getShort("progress");
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      super.writeToNBT(nbt);
      nbt.setShort("progress", this.progress);
      return nbt;
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      boolean needsInvUpdate = false;
      if (this.canoperate() && this.energy.getEnergy() >= this.energyConsume * this.operationLength) {
         if (this.progress < this.operationLength) {
            this.progress++;
            this.energy.useEnergy(this.energyConsume);
         } else {
            this.progress = 0;
            this.operate(false);
         }

         this.setActive(true);
      } else {
         this.setActive(false);
      }

      needsInvUpdate |= this.containerSlot.processFromTank(this.fluidTank, this.outputSlot);
      needsInvUpdate |= this.upgradeSlot.tickNoMark();
      this.guiProgress = (float)this.progress / this.operationLength;
      if (needsInvUpdate) {
         super.markDirty();
      }
   }

   public boolean canoperate() {
      return this.operate(true);
   }

   public boolean operate(boolean sim) {
      if (this.miner == null || this.miner.isInvalid()) {
         this.miner = null;
         World world = this.getWorld();

         for (EnumFacing dir : Util.downSideFacings) {
            TileEntity te = world.getTileEntity(this.pos.offset(dir));
            if (te instanceof TileEntityMiner) {
               this.miner = (TileEntityMiner)te;
               break;
            }
         }
      }

      FluidStack liquid = null;
      if (this.miner != null) {
         if (this.miner.canProvideLiquid) {
            liquid = this.pump(this.miner.liquidPos, sim, this.miner);
         }
      } else {
         EnumFacing dir = this.getFacing();
         liquid = this.pump(this.pos.offset(dir), sim, this.miner);
      }

      if (liquid != null && this.fluidTank.fillInternal(liquid, false) > 0) {
         if (!sim) {
            this.fluidTank.fillInternal(liquid, true);
         }

         return true;
      } else {
         return false;
      }
   }

   public FluidStack pump(BlockPos startPos, boolean sim, TileEntityMiner miner) {
      World world = this.getWorld();
      int freeSpace = this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
      if (miner == null && freeSpace > 0) {
         TileEntity te = world.getTileEntity(startPos);
         EnumFacing side = this.getFacing().getOpposite();
         if (te != null && LiquidUtil.isFluidTile(te, side)) {
            if (freeSpace > 1000) {
               freeSpace = 1000;
            }

            return LiquidUtil.drainTile(te, side, freeSpace, sim);
         }
      }

      if (freeSpace >= 1000) {
         BlockPos cPos;
         if (miner != null && miner.canProvideLiquid) {
            assert miner.liquidPos != null;
            cPos = miner.liquidPos;
         } else {
            cPos = PumpUtil.searchFluidSource(world, startPos);
         }

         if (cPos != null) {
            return LiquidUtil.drainBlock(world, cPos, sim);
         }
      }

      return null;
   }

   @Override
   public void markDirty() {
      super.markDirty();
      if (IC2.platform.isSimulating()) {
         this.setUpgradestat();
      }
   }

   public void setUpgradestat() {
      double previousProgress = (double)this.progress / this.operationLength;
      this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
      this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
      this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
      this.energy.setSinkTier(this.upgradeSlot.getTier(this.defaultTier));
      this.dischargeSlot.setTier(this.energy.getSinkTier());
      this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(this.defaultEnergyStorage, this.defaultOperationLength, this.defaultEnergyConsume));
      this.progress = (short)Math.floor(previousProgress * this.operationLength + 0.1);
   }

   @Override
   public double getGuiValue(String name) {
      if (name.equals("progress")) {
         return this.guiProgress;
      } else {
         throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + name);
      }
   }

   @Override
   public double getEnergy() {
      return this.energy.getEnergy();
   }

   @Override
   public boolean useEnergy(double amount) {
      return this.energy.useEnergy(amount);
   }

   @Override
   public ContainerBase<TileEntityPump> getGuiContainer(EntityPlayer player) {
      return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return DynamicGui.<TileEntityPump>create(this, player, GuiParser.parse(this.teBlock));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }

   @Override
   public void onNetworkUpdate(String field) {
      if (field.equals("active")) {
         if (this.audioSource == null) {
            this.audioSource = IC2.audioManager
               .createSource(this, PositionSpec.Center, "Machines/PumpOp.ogg", true, false, IC2.audioManager.getDefaultVolume());
         }

         if (this.getActive()) {
            if (this.audioSource != null) {
               this.audioSource.play();
            }
         } else if (this.audioSource != null) {
            this.audioSource.stop();
         }
      }

      super.onNetworkUpdate(field);
   }

   @Override
   public Set<UpgradableProperty> getUpgradableProperties() {
      return EnumSet.of(
         UpgradableProperty.Processing,
         UpgradableProperty.Transformer,
         UpgradableProperty.EnergyStorage,
         UpgradableProperty.ItemConsuming,
         UpgradableProperty.ItemProducing,
         UpgradableProperty.FluidProducing
      );
   }
}

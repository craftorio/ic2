package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.init.Localization;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityTransformer extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener {
   private static final TileEntityTransformer.Mode defaultMode = TileEntityTransformer.Mode.redstone;
   private double inputFlow = 0.0;
   private double outputFlow = 0.0;
   private final int defaultTier;
   protected final Energy energy;
   private TileEntityTransformer.Mode configuredMode = defaultMode;
   private TileEntityTransformer.Mode transformMode = null;

   public TileEntityTransformer(int tier) {
      this.defaultTier = tier;
      this.energy = this.addComponent(
         new Energy(this, EnergyNet.instance.getPowerFromTier(tier) * 8.0, Collections.emptySet(), Collections.emptySet(), tier, tier, true)
            .setMultiSource(true)
      );
   }

   public String getType() {
      switch (this.energy.getSourceTier()) {
         case 1:
            return "LV";
         case 2:
            return "MV";
         case 3:
            return "HV";
         case 4:
            return "EV";
         default:
            return "";
      }
   }

   @Override
   public void readFromNBT(NBTTagCompound nbt) {
      super.readFromNBT(nbt);
      int mode = nbt.getInteger("mode");
      if (mode >= 0 && mode < TileEntityTransformer.Mode.VALUES.length) {
         this.configuredMode = TileEntityTransformer.Mode.VALUES[mode];
      } else {
         this.configuredMode = defaultMode;
      }
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      super.writeToNBT(nbt);
      nbt.setInteger("mode", this.configuredMode.ordinal());
      return nbt;
   }

   @Override
   protected void onLoaded() {
      super.onLoaded();
      if (!this.getWorld().isRemote) {
         this.updateRedstone(true);
      }
   }

   public TileEntityTransformer.Mode getMode() {
      return this.configuredMode;
   }

   @Override
   public void onNetworkEvent(EntityPlayer player, int event) {
      if (event >= 0 && event < TileEntityTransformer.Mode.VALUES.length) {
         this.configuredMode = TileEntityTransformer.Mode.VALUES[event];
         this.updateRedstone(false);
      } else if (event == 3) {
      }
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      this.updateRedstone(false);
   }

   private void updateRedstone(boolean force) {
      assert !this.getWorld().isRemote;
      TileEntityTransformer.Mode newMode;
      switch (this.configuredMode) {
         case redstone:
            newMode = this.getWorld().isBlockPowered(this.pos) ? TileEntityTransformer.Mode.stepup : TileEntityTransformer.Mode.stepdown;
            break;
         case stepdown:
         case stepup:
            newMode = this.configuredMode;
            break;
         default:
            throw new RuntimeException("invalid mode: " + this.configuredMode);
      }

      if (newMode != TileEntityTransformer.Mode.stepup && newMode != TileEntityTransformer.Mode.stepdown) {
         throw new RuntimeException("invalid mode: " + newMode);
      }

      this.energy.setEnabled(true);
      if (force || this.transformMode != newMode) {
         this.transformMode = newMode;
         this.setActive(this.isStepUp());
         if (this.isStepUp()) {
            this.energy.setSourceTier(this.defaultTier + 1);
            this.energy.setSinkTier(this.defaultTier);
            this.energy.setPacketOutput(1);
            this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));
         } else {
            this.energy.setSourceTier(this.defaultTier);
            this.energy.setSinkTier(this.defaultTier + 1);
            this.energy.setPacketOutput(4);
            this.energy.setDirections(EnumSet.of(this.getFacing()), EnumSet.complementOf(EnumSet.of(this.getFacing())));
         }

         this.outputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier());
         this.inputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSinkTier());
      }
   }

   @Override
   public void setFacing(EnumFacing facing) {
      super.setFacing(facing);
      if (!this.getWorld().isRemote) {
         this.updateRedstone(true);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
      super.addInformation(stack, tooltip, advanced);
      tooltip.add(
         String.format(
            "%s %.0f %s %s %.0f %s",
            Localization.translate("ic2.item.tooltip.Low"),
            EnergyNet.instance.getPowerFromTier(this.energy.getSinkTier()),
            Localization.translate("ic2.generic.text.EUt"),
            Localization.translate("ic2.item.tooltip.High"),
            EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier() + 1),
            Localization.translate("ic2.generic.text.EUt")
         )
      );
   }

   @Override
   public ContainerBase<TileEntityTransformer> getGuiContainer(EntityPlayer player) {
      return new ContainerTransformer(player, this, 219);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return new GuiTransformer(new ContainerTransformer(player, this, 219));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }

   public double getinputflow() {
      return !this.isStepUp() ? this.inputFlow : this.outputFlow;
   }

   public double getoutputflow() {
      return this.isStepUp() ? this.inputFlow : this.outputFlow;
   }

   private boolean isStepUp() {
      return this.transformMode == TileEntityTransformer.Mode.stepup;
   }

   public enum Mode {
      redstone,
      stepdown,
      stepup;

      static final TileEntityTransformer.Mode[] VALUES = values();
   }
}

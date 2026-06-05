package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.container.ContainerFluidDistributor;
import ic2.core.block.machine.gui.GuiFluidDistributor;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.util.LiquidUtil;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityFluidDistributor extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener {
   public final InvSlotConsumableLiquidByTank inputSlot;
   public final InvSlotOutput OutputSlot;
   @GuiSynced
   public final Fluids.InternalFluidTank fluidTank;
   protected final Fluids fluids = this.addComponent(new Fluids(this));

   public TileEntityFluidDistributor() {
      this.fluidTank = this.fluids.addTank("fluidTank", 1000);
      this.inputSlot = new InvSlotConsumableLiquidByTank(
         this, "inputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.fluidTank
      );
      this.OutputSlot = new InvSlotOutput(this, "OutputSlot", 1);
   }

   @Override
   protected void onLoaded() {
      super.onLoaded();
      this.updateConnectivity();
   }

   @Override
   public void setActive(boolean val) {
      super.setActive(val);
      this.updateConnectivity();
   }

   @Override
   public void setFacing(EnumFacing facing) {
      super.setFacing(facing);
      this.updateConnectivity();
   }

   protected void updateConnectivity() {
      EnumSet<EnumFacing> acceptingSides = EnumSet.of(this.getFacing());
      if (this.getActive()) {
         acceptingSides = EnumSet.complementOf(acceptingSides);
      }

      this.fluids.changeConnectivity(this.fluidTank, acceptingSides, Collections.emptySet());
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      this.inputSlot.processFromTank(this.fluidTank, this.OutputSlot);
      if (this.fluidTank.getFluidAmount() > 0) {
         this.moveFluid();
      }
   }

   protected void moveFluid() {
      World world = this.getWorld();
      if (this.getActive()) {
         TileEntity target = world.getTileEntity(this.pos.offset(this.getFacing()));
         EnumFacing side = this.getFacing().getOpposite();
         if (LiquidUtil.isFluidTile(target, side)) {
            int amount = LiquidUtil.fillTile(target, side, this.fluidTank.getFluid(), false);
            if (amount > 0) {
               this.fluidTank.drainInternal(amount, true);
            }
         }
      } else {
         Map<EnumFacing, TileEntity> acceptingNeighbors = new EnumMap<>(EnumFacing.class);
         int acceptedVolume = 0;

         for (EnumFacing dir : EnumFacing.VALUES) {
            if (dir != this.getFacing()) {
               TileEntity target = world.getTileEntity(this.pos.offset(dir));
               EnumFacing side = dir.getOpposite();
               if (LiquidUtil.isFluidTile(target, side)) {
                  int amount = LiquidUtil.fillTile(target, side, this.fluidTank.getFluid(), true);
                  if (amount > 0) {
                     acceptingNeighbors.put(dir, target);
                     acceptedVolume += amount;
                  }
               }
            }
         }

         while (!acceptingNeighbors.isEmpty()) {
            int amount = Math.min(acceptedVolume, this.fluidTank.getFluidAmount());
            if (amount <= 0) {
               break;
            }

            amount /= acceptingNeighbors.size();
            if (amount <= 0) {
               for (Entry<EnumFacing, TileEntity> entry : acceptingNeighbors.entrySet()) {
                  TileEntity target = entry.getValue();
                  EnumFacing side = entry.getKey().getOpposite();
                  FluidStack fs = this.fluidTank.getFluid();
                  if (fs == null) {
                     return;
                  }

                  fs = fs.copy();
                  fs.amount = Math.min(acceptedVolume, fs.amount);
                  if (fs.amount <= 0) {
                     return;
                  }

                  int cAmount = LiquidUtil.fillTile(target, side, fs, false);
                  this.fluidTank.drainInternal(cAmount, true);
                  acceptedVolume -= cAmount;
               }
               break;
            }

            Iterator<Entry<EnumFacing, TileEntity>> it = acceptingNeighbors.entrySet().iterator();

            while (it.hasNext()) {
               Entry<EnumFacing, TileEntity> entry = it.next();
               TileEntity target = entry.getValue();
               EnumFacing side = entry.getKey().getOpposite();
               FluidStack fs = this.fluidTank.getFluid();
               if (fs == null) {
                  break;
               }

               fs = fs.copy();
               if (fs.amount <= 0) {
                  break;
               }

               fs.amount = Math.min(amount, fs.amount);
               int cAmount = LiquidUtil.fillTile(target, side, fs, false);
               this.fluidTank.drainInternal(cAmount, true);
               acceptedVolume -= cAmount;
               if (cAmount < fs.amount) {
                  it.remove();
               }
            }
         }
      }
   }

   @Override
   public void onNetworkEvent(EntityPlayer player, int event) {
      this.setActive(!this.getActive());
   }

   @Override
   public ContainerBase<?> getGuiContainer(EntityPlayer player) {
      return new ContainerFluidDistributor(player, this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return new GuiFluidDistributor(new ContainerFluidDistributor(player, this));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }
}

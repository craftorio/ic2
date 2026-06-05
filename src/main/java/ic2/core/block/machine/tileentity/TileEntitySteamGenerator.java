package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.ExplosionIC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.block.machine.gui.GuiSteamGenerator;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.profile.NotClassic;
import ic2.core.ref.FluidName;
import ic2.core.util.BiomeUtil;
import ic2.core.util.LiquidUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntitySteamGenerator extends TileEntityInventory implements IHasGui, IGuiValueProvider, INetworkClientTileEntityEventListener {
   private static final float maxHeat = 500.0F;
   private static final float heatPerHu = 5.0E-4F;
   private static final float coolingPerMb = 0.1F;
   private static final float maxCooling = 2.0F;
   private static final int maxHuInput = 1200;
   private static final int maxCalcification = 100000;
   private static final int steamExpansion = 100;
   private static final float epsilon = 1.0E-4F;
   private int heatInput = 0;
   private int inputMB = 0;
   public final FluidTank waterTank;
   private int calcification = 0;
   private int outputMB = 0;
   private TileEntitySteamGenerator.outputType outputFluid = TileEntitySteamGenerator.outputType.NONE;
   private float systemHeat;
   private int pressure = 0;
   private boolean newActive = false;
   protected final Fluids fluids = this.addComponent(new Fluids(this));

   public TileEntitySteamGenerator() {
      this.waterTank = this.fluids.addTankInsert("waterTank", 10000, Fluids.fluidPredicate(FluidRegistry.WATER, FluidName.distilled_water.getInstance()));
   }

   @Override
   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.inputMB = nbttagcompound.getInteger("inputmb");
      this.pressure = nbttagcompound.getInteger("pressurevalve");
      this.systemHeat = nbttagcompound.getFloat("systemheat");
      this.calcification = nbttagcompound.getInteger("calcification");
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      super.writeToNBT(nbt);
      nbt.setInteger("inputmb", this.inputMB);
      nbt.setInteger("pressurevalve", this.pressure);
      nbt.setFloat("systemheat", this.systemHeat);
      nbt.setInteger("calcification", this.calcification);
      return nbt;
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      this.systemHeat = Math.max(this.systemHeat, BiomeUtil.getBiomeTemperature(this.getWorld(), this.pos));
      if (this.isCalcified()) {
         if (this.getActive()) {
            this.setActive(false);
         }
      } else {
         this.newActive = this.work();
         if (this.getActive() != this.newActive) {
            this.setActive(this.newActive);
         }
      }

      if (!this.getActive()) {
         this.cooldown(0.01F);
      }
   }

   private boolean work() {
      this.heatInput = this.requestHeat(1200);
      if (this.heatInput <= 0) {
         return false;
      }

      assert this.heatInput <= 1200;
      this.outputMB = 0;
      this.outputFluid = TileEntitySteamGenerator.outputType.NONE;
      if (this.waterTank.getFluid() != null && this.waterTank.getFluidAmount() > 0 && this.inputMB > 0) {
         Fluid inputFluid = this.waterTank.getFluid().getFluid();
         boolean hasDistilledWater = inputFluid == FluidName.distilled_water.getInstance();
         int maxAmount = Math.min(this.inputMB, this.waterTank.getFluidAmount());
         float hUneeded = 100.0F + this.pressure / 220.0F * 100.0F;
         float targetTemp = 100.0F + this.pressure / 220.0F * 100.0F * 2.74F;
         float reqHeat = targetTemp - this.systemHeat;
         float remainingHuInput = this.heatInput;
         if (reqHeat > 1.0E-4F) {
            int heatReq = (int)Math.ceil(reqHeat / 5.0E-4F);
            if (this.heatInput <= heatReq) {
               this.heatup(this.heatInput);
               if (this.pressure == 0 && this.systemHeat < 99.9999F) {
                  this.outputMB = maxAmount;
                  this.outputFluid = hasDistilledWater ? TileEntitySteamGenerator.outputType.DISTILLEDWATER : TileEntitySteamGenerator.outputType.WATER;
                  int transferred = LiquidUtil.distribute(this, new FluidStack(inputFluid, maxAmount), false);
                  if (transferred > 0) {
                     this.waterTank.drainInternal(transferred, true);
                  }
               }

               return true;
            }

            this.heatup(heatReq);
            remainingHuInput -= heatReq;
            reqHeat = targetTemp - this.systemHeat;
         }

         assert this.systemHeat >= targetTemp - 1.0E-4F;
         assert this.systemHeat >= 99.9999F;
         float availableSystemHu = Math.min(-reqHeat / 5.0E-4F, 1200 - this.heatInput);
         int activeAmount = Math.min(maxAmount, (int)((remainingHuInput + availableSystemHu) / hUneeded));
         int totalAmount = activeAmount;
         remainingHuInput -= activeAmount * hUneeded;
         if (remainingHuInput < 0.0F) {
            this.cooldown(-remainingHuInput * 5.0E-4F);
            reqHeat = targetTemp - this.systemHeat;
         }

         if (reqHeat <= -0.1001F) {
            int coolingAmount = Math.min(maxAmount, (int)(-reqHeat / 0.1F));
            coolingAmount = Math.min(coolingAmount, (int)Math.ceil(20.0));
            assert coolingAmount >= 0;
            this.cooldown(coolingAmount * 0.1F);
            totalAmount = Math.max(activeAmount, coolingAmount);
         }

         if (remainingHuInput > 0.0F) {
            this.heatup(remainingHuInput);
         }

         if (totalAmount <= 0) {
            return true;
         }

         if (!hasDistilledWater) {
            this.calcification += totalAmount;
         }

         this.waterTank.drainInternal(totalAmount, true);
         if (activeAmount <= 0) {
            return true;
         }

         this.outputMB = activeAmount * 100;
         Fluid output;
         if (this.systemHeat >= 373.9999F) {
            output = FluidName.superheated_steam.getInstance();
            this.outputFluid = TileEntitySteamGenerator.outputType.SUPERHEATEDSTEAM;
         } else {
            output = FluidName.steam.getInstance();
            this.outputFluid = TileEntitySteamGenerator.outputType.STEAM;
         }

         int transferred = LiquidUtil.distribute(this, new FluidStack(output, this.outputMB), false);
         int remaining = this.outputMB - transferred;
         if (remaining > 0) {
            World world = this.getWorld();
            if (world.rand.nextInt(10) == 0) {
               new ExplosionIC2(world, null, this.pos, 1, 1.0F, ExplosionIC2.Type.Heat).doExplosion();
            } else if (remaining >= 100) {
               this.waterTank.fillInternal(new FluidStack(inputFluid, remaining / 100), true);
            }
         }

         return true;
      } else {
         this.heatup(this.heatInput);
         return true;
      }
   }

   private void heatup(float heatinput) {
      assert heatinput >= -1.0E-4F;
      this.systemHeat += heatinput * 5.0E-4F;
      if (this.systemHeat > 500.0F) {
         World world = this.getWorld();
         world.setBlockToAir(this.pos);
         new ExplosionIC2(world, null, this.pos, 10, 0.01F, ExplosionIC2.Type.Heat).doExplosion();
      }
   }

   private void cooldown(float cool) {
      assert cool >= -1.0E-4F;
      this.systemHeat = Math.max(this.systemHeat - cool, BiomeUtil.getBiomeTemperature(this.getWorld(), this.pos));
   }

   private int requestHeat(int requestHeat) {
      World world = this.getWorld();
      int targetHeat = requestHeat;

      for (EnumFacing dir : EnumFacing.VALUES) {
         TileEntity target = world.getTileEntity(this.pos.offset(dir));
         if (target instanceof IHeatSource) {
            IHeatSource hs = (IHeatSource)target;
            int request = hs.drawHeat(dir.getOpposite(), targetHeat, true);
            if (request > 0) {
               targetHeat -= hs.drawHeat(dir.getOpposite(), request, false);
               if (targetHeat == 0) {
                  return requestHeat;
               }
            }
         }
      }

      return requestHeat - targetHeat;
   }

   @Override
   public void onNetworkEvent(EntityPlayer player, int event) {
      if (event <= 2000 && event >= -2000) {
         this.inputMB = Math.max(Math.min(this.inputMB + event, 1000), 0);
      } else {
         if (event > 2000) {
            this.pressure = Math.min(this.pressure + (event - 2000), 300);
         }

         if (event < -2000) {
            this.pressure = Math.max(this.pressure + event + 2000, 0);
         }
      }
   }

   public int gaugeLiquidScaled(int i, int tank) {
      if (tank == 0) {
         return this.waterTank.getFluidAmount() <= 0 ? 0 : this.waterTank.getFluidAmount() * i / this.waterTank.getCapacity();
      } else {
         return 0;
      }
   }

   @Override
   public ContainerBase<TileEntitySteamGenerator> getGuiContainer(EntityPlayer player) {
      return new ContainerSteamGenerator(player, this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return new GuiSteamGenerator(new ContainerSteamGenerator(player, this));
   }

   @Override
   public void onGuiClosed(EntityPlayer player) {
   }

   @Override
   public double getGuiValue(String name) {
      if ("heat".equals(name)) {
         return this.systemHeat == 0.0F ? 0.0 : this.systemHeat / 500.0;
      } else if ("calcification".equals(name)) {
         return this.calcification == 0 ? 0.0 : this.calcification / 100000.0;
      } else {
         throw new IllegalArgumentException();
      }
   }

   public int getOutputMB() {
      return this.outputMB;
   }

   public int getInputMB() {
      return this.inputMB;
   }

   public int getHeatInput() {
      return this.heatInput;
   }

   public int getPressure() {
      return this.pressure;
   }

   public float getSystemHeat() {
      return Math.round(this.systemHeat * 10.0F) / 10.0F;
   }

   public float getCalcification() {
      return Math.round(this.calcification / 100000.0F * 100.0F * 100.0F) / 100.0F;
   }

   public boolean isCalcified() {
      return this.calcification >= 100000;
   }

   public String getOutputFluidName() {
      return this.outputFluid.getName();
   }

   private enum outputType {
      NONE(""),
      WATER("ic2.SteamGenerator.output.water"),
      DISTILLEDWATER("ic2.SteamGenerator.output.destiwater"),
      STEAM("ic2.SteamGenerator.output.steam"),
      SUPERHEATEDSTEAM("ic2.SteamGenerator.output.hotsteam");

      private final String name;

      outputType(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }
   }
}

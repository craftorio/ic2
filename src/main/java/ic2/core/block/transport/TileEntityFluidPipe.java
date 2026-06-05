package ic2.core.block.transport;

import ic2.api.transport.IFluidPipe;
import ic2.core.IC2;
import ic2.core.block.transport.cover.CoverProperty;
import ic2.core.block.transport.cover.ICoverItem;
import ic2.core.block.transport.items.PipeSize;
import ic2.core.block.transport.items.PipeType;
import ic2.core.item.block.ItemPipe;
import ic2.core.util.LiquidUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = "ic2", value = Side.CLIENT)
public class TileEntityFluidPipe extends TileEntityPipe implements IFluidPipe {
   protected PipeType type = PipeType.bronze;
   protected PipeSize size = PipeSize.small;
   protected TileEntityFluidPipe.PipeFluidTank tank;
   private boolean debug = false;

   public TileEntityFluidPipe() {
   }

   public TileEntityFluidPipe(PipeType type, PipeSize size) {
      this();
      this.type = type;
      this.size = size;
      this.tank = new TileEntityFluidPipe.PipeFluidTank(Util.allFacings, Util.allFacings, fluid -> true, (int)(type.transferRate * size.multiplier));
   }

   @Override
   public Set<CoverProperty> getCoverProperties() {
      return EnumSet.of(CoverProperty.FluidConsuming);
   }

   @Override
   public int getTransferRate() {
      return this.type.transferRate;
   }

   @Override
   public FluidTank getTank() {
      return this.tank;
   }

   @Override
   public int getCurrentInnerCapacity() {
      return this.tank.getFluidAmount();
   }

   @Override
   public int getMaxInnerCapacity() {
      return this.tank.getCapacity();
   }

   @Override
   public void flipConnection(EnumFacing facing) {
      World world = this.getWorld();
      BlockPos pos = this.getPos();
      if (!world.isRemote) {
         this.connectivity = (byte)(this.connectivity ^ 1 << facing.ordinal());
         IC2.network.get(true).updateTileEntityField(this, "connectivity");
         world.notifyNeighborsOfStateChange(pos, this.getBlockType(), true);
         this.markDirty();
      }
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      if (this.tank.getFluid() != null && this.tank.getFluidAmount() > 0) {
         int availableFluidAmount = this.tank.getFluidAmount();
         int cPipes = 1;
         List<LiquidUtil.AdjacentFluidHandler> adjacentFluidHandlers = new ArrayList<>();

         for (EnumFacing facing : EnumFacing.VALUES) {
            if (LiquidUtil.drainTile(this, facing, Integer.MAX_VALUE, true) != null) {
               LiquidUtil.AdjacentFluidHandler target = LiquidUtil.getAdjacentHandler(this, facing);
               if (target != null) {
                  if (target.handler instanceof IFluidPipe) {
                     int targetAmount = ((IFluidPipe)target.handler).getTank().getFluidAmount();
                     if (this.tank.getFluidAmount() >= targetAmount) {
                        availableFluidAmount += targetAmount;
                        cPipes++;
                        adjacentFluidHandlers.add(target);
                     }
                  } else if (LiquidUtil.fillTile(target.handler, facing.getOpposite(), this.tank.getFluid(), true) > 0) {
                     adjacentFluidHandlers.add(target);
                  }
               }
            }
         }

         if (this.debug) {
            IC2.log.warn(LogCategory.Transport, "Number of valid adjacentFluidHandlers: %s", adjacentFluidHandlers.size());
         }

         int extraFluid = availableFluidAmount % cPipes;
         availableFluidAmount /= cPipes;
         List<LiquidUtil.AdjacentFluidHandler> adjacentPipes = new ArrayList<>();
         if (!adjacentFluidHandlers.isEmpty()) {
            Iterator<LiquidUtil.AdjacentFluidHandler> it = adjacentFluidHandlers.iterator();

            while (it.hasNext()) {
               LiquidUtil.AdjacentFluidHandler target = it.next();
               if (target.handler instanceof IFluidPipe) {
                  FluidStack ret = LiquidUtil.transfer(
                     this, target.dir, target.handler, availableFluidAmount - ((IFluidPipe)target.handler).getTank().getFluidAmount()
                  );
                  if (this.debug) {
                     IC2.log.warn(LogCategory.Transport, "Split with pipe: %s to facing %s", ret != null ? ret.amount : 0, target.dir);
                  }

                  adjacentPipes.add(target);
                  it.remove();
               }
            }
         }

         if (adjacentFluidHandlers.isEmpty()) {
            while (extraFluid > 0) {
               int bullet = IC2.random.nextInt(adjacentPipes.size());
               LiquidUtil.AdjacentFluidHandler target = adjacentPipes.get(bullet);
               LiquidUtil.transfer(this, target.dir, target.handler, 1);
               if (this.debug) {
                  IC2.log.warn(LogCategory.Transport, "Split with pipe: 1 to facing %s", target.dir);
               }

               extraFluid--;
               adjacentPipes.remove(bullet);
            }
         } else {
            availableFluidAmount += extraFluid;
         }

         if (!adjacentFluidHandlers.isEmpty()) {
            int maxTransfer = Math.min(availableFluidAmount, (int)(this.type.transferRate * this.size.multiplier / 20.0F));
            int maxAmountPerOutput = (int)Math.floor((float)maxTransfer / adjacentFluidHandlers.size());
            if (maxAmountPerOutput <= 0) {
               while (maxTransfer > 0) {
                  int bullet = IC2.random.nextInt(adjacentFluidHandlers.size());
                  LiquidUtil.AdjacentFluidHandler target = adjacentFluidHandlers.get(bullet);
                  LiquidUtil.transfer(this, target.dir, target.handler, 1);
                  if (this.debug) {
                     IC2.log.warn(LogCategory.Transport, "Transferred: 1 to facing %s", target.dir);
                  }

                  maxTransfer--;
                  adjacentFluidHandlers.remove(bullet);
               }
            } else {
               for (LiquidUtil.AdjacentFluidHandler target : adjacentFluidHandlers) {
                  FluidStack ret = LiquidUtil.transfer(this, target.dir, target.handler, maxAmountPerOutput);
                  if (this.debug) {
                     IC2.log.warn(LogCategory.Transport, "Transferred: %s to facing %s", ret != null ? ret.amount : 0, target.dir);
                  }
               }
            }
         }
      }
   }

   @Override
   public void readFromNBT(NBTTagCompound nbt) {
      super.readFromNBT(nbt);
      this.type = PipeType.values[nbt.getByte("type") & 0xFF];
      this.size = PipeSize.values()[nbt.getByte("size") & 0xFF];
      this.tank = new TileEntityFluidPipe.PipeFluidTank(Util.allFacings, Util.allFacings, fluid -> true, (int)(this.type.transferRate * this.size.multiplier));
      this.tank.readFromNBT(nbt);
   }

   @Override
   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      super.writeToNBT(nbt);
      nbt.setByte("type", (byte)this.type.ordinal());
      nbt.setByte("size", (byte)this.size.ordinal());
      this.tank.writeToNBT(nbt);
      return nbt;
   }

   @Override
   protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target) {
      return ItemPipe.getPipe(this.type, this.size);
   }

   @Override
   public List<String> getNetworkedFields() {
      List<String> ret = super.getNetworkedFields();
      ret.add("type");
      ret.add("size");
      return ret;
   }

   @Override
   protected void updateRenderState() {
      this.renderState = new TileEntityPipe.PipeRenderState(this.type, this.size, this.connectivity, this.covers, this.getFacing().ordinal());
   }

   @Override
   protected void updateConnectivity() {
      byte newConnectivity = this.connectivity;

      for (EnumFacing direction : EnumFacing.VALUES) {
         TileEntity tile = this.world.getTileEntity(this.pos.offset(direction));
         if (tile != null) {
            if (tile instanceof IFluidPipe) {
               if (((IFluidPipe)tile).isConnected(direction.getOpposite())) {
                  newConnectivity = (byte)(newConnectivity | 1 << direction.ordinal());
               }
            } else if (LiquidUtil.isFluidTile(tile, direction.getOpposite())) {
               newConnectivity = (byte)(newConnectivity | this.connectivity & 1 << direction.ordinal());
            }
         }
      }

      if (this.connectivity != newConnectivity) {
         this.connectivity = newConnectivity;
         IC2.network.get(true).updateTileEntityField(this, "connectivity");
      }
   }

   @Override
   public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
      super.onPlaced(stack, placer, facing);
      if (!this.world.isRemote) {
         TileEntity tile = this.world.getTileEntity(this.pos.offset(facing.getOpposite()));
         if (tile != null && (tile instanceof IFluidPipe || LiquidUtil.isFluidTile(tile, facing))) {
            this.flipConnection(facing.getOpposite());
            if (tile instanceof IFluidPipe) {
               IFluidPipe other = (IFluidPipe)tile;
               if (!other.isConnected(facing)) {
                  other.flipConnection(facing);
               }
            }
         }
      }
   }

   @Override
   protected void onBlockBreak() {
      super.onBlockBreak();
      if (this.tank.getFluidAmount() > 1000 && LiquidUtil.fillBlock(this.tank.getFluid(), this.world, this.pos, true)) {
         LiquidUtil.fillBlock(this.tank.getFluid(), this.world, this.pos, false);
      }
   }

   @Override
   public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
      return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
         ? facing != null && this.isConnected(facing)
         : super.hasCapability(capability, facing);
   }

   @Override
   public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
      if (capability != CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
         return super.getCapability(capability, facing);
      } else {
         return (T)(facing != null && this.isConnected(facing) ? new TileEntityFluidPipe.PipeFluidHandler(facing) : null);
      }
   }

   @Override
   protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
      if (!forCollision) {
         return super.getAabbs(false);
      }

      float th = this.size.thickness;
      float sp = (1.0F - th) / 2.0F;
      List<AxisAlignedBB> ret = new ArrayList<>(7);
      ret.add(new AxisAlignedBB(sp, sp, sp, sp + th, sp + th, sp + th));

      for (EnumFacing facing : EnumFacing.VALUES) {
         boolean hasConnection = (this.connectivity & 1 << facing.ordinal()) != 0;
         if (hasConnection) {
            float zS = sp;
            float yS = sp;
            float xS = sp;
            float yE;
            float zE;
            float xE = yE = zE = sp + th;
            switch (facing) {
               case DOWN:
                  yS = 0.0F;
                  yE = sp;
                  break;
               case UP:
                  yS = sp + th;
                  yE = 1.0F;
                  break;
               case NORTH:
                  zS = 0.0F;
                  zE = sp;
                  break;
               case SOUTH:
                  zS = sp + th;
                  zE = 1.0F;
                  break;
               case WEST:
                  xS = 0.0F;
                  xE = sp;
                  break;
               case EAST:
                  xS = sp + th;
                  xE = 1.0F;
                  break;
               default:
                  throw new RuntimeException();
            }

            ret.add(new AxisAlignedBB(xS, yS, zS, xE, yE, zE));
            float cs = 1.0F;
            float ch = 0.1F;
            boolean hasCover = (this.covers & 1 << facing.ordinal()) != 0;
            zS = 0.0F;
            yS = 0.0F;
            xS = 0.0F;
            zE = 1.0F;
            yE = 1.0F;
            xE = 1.0F;
            if (hasCover) {
               switch (facing) {
                  case DOWN:
                     yS = 0.0F;
                     yE = ch;
                     break;
                  case UP:
                     yS = cs - ch;
                     yE = 1.0F;
                     break;
                  case NORTH:
                     zS = 0.0F;
                     zE = ch;
                     break;
                  case SOUTH:
                     zS = cs - ch;
                     zE = 1.0F;
                     break;
                  case WEST:
                     xS = 0.0F;
                     xE = ch;
                     break;
                  case EAST:
                     xS = cs - ch;
                     xE = 1.0F;
                     break;
                  default:
                     throw new RuntimeException();
               }

               ret.add(new AxisAlignedBB(xS, yS, zS, xE, yE, zE));
            }
         }
      }

      return ret;
   }

   @Override
   public AxisAlignedBB getVisualBoundingBox() {
      return this.getPhysicsBoundingBox();
   }

   @Override
   protected AxisAlignedBB getOutlineBoundingBox() {
      return super.getVisualBoundingBox();
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void drawBetter(DrawBlockHighlightEvent event) {
      if (event.getSubID() == 0) {
         RayTraceResult rayTrace = event.getTarget();
         if (rayTrace.typeOfHit == Type.BLOCK) {
            EntityPlayer player = event.getPlayer();
            World world = player.getEntityWorld();
            BlockPos pos = rayTrace.getBlockPos();
            if (world.getWorldBorder().contains(pos)) {
               TileEntity te = world.getTileEntity(pos);
               if (te instanceof TileEntityFluidPipe) {
                  GlStateManager.enableBlend();
                  GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
                  GlStateManager.glLineWidth(2.0F);
                  GlStateManager.disableTexture2D();
                  GlStateManager.depthMask(false);
                  double xOffset = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
                  double yOffset = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
                  double zOffset = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
                  RenderGlobal.drawSelectionBoundingBox(
                     ((TileEntityFluidPipe)te).getVisualBoundingBox().offset(pos).grow(0.002).offset(-xOffset, -yOffset, -zOffset),
                     0.0F,
                     0.0F,
                     0.0F,
                     0.4F
                  );
                  GlStateManager.depthMask(true);
                  GlStateManager.enableTexture2D();
                  GlStateManager.disableBlend();
                  event.setCanceled(true);
               }
            }
         }
      }
   }

   private class PipeFluidHandler implements IFluidHandler {
      private final EnumFacing side;

      public PipeFluidHandler(EnumFacing side) {
         this.side = side;
      }

      @Override
      public IFluidTankProperties[] getTankProperties() {
         return TileEntityFluidPipe.this.tank.getTankProperties();
      }

      @Override
      public int fill(FluidStack resource, boolean doFill) {
         if (TileEntityFluidPipe.this.coversComponent.hasCover(this.side)) {
            ICoverItem cover = TileEntityFluidPipe.this.coversComponent.getCoverItem(this.side);
            if (!cover.allowsInput(resource)) {
               return 0;
            }
         }

         return TileEntityFluidPipe.this.tank.fill(resource, doFill);
      }

      @Nullable
      @Override
      public FluidStack drain(FluidStack resource, boolean doDrain) {
         if (TileEntityFluidPipe.this.coversComponent.hasCover(this.side)) {
            ICoverItem cover = TileEntityFluidPipe.this.coversComponent.getCoverItem(this.side);
            if (!cover.allowsOutput(resource)) {
               return null;
            }
         }

         return TileEntityFluidPipe.this.tank.drain(resource, doDrain);
      }

      @Nullable
      @Override
      public FluidStack drain(int maxDrain, boolean doDrain) {
         if (TileEntityFluidPipe.this.coversComponent.hasCover(this.side)) {
            ICoverItem cover = TileEntityFluidPipe.this.coversComponent.getCoverItem(this.side);
            if (!cover.allowsOutput(new FluidStack(Objects.requireNonNull(TileEntityFluidPipe.this.tank.getFluid()), maxDrain))) {
               return null;
            }
         }

         return TileEntityFluidPipe.this.tank.drain(maxDrain, doDrain);
      }
   }

   private class PipeFluidTank extends FluidTank {
      private final Predicate<Fluid> acceptedFluids;
      private Collection<EnumFacing> inputSides;
      private Collection<EnumFacing> outputSides;

      protected PipeFluidTank(Collection<EnumFacing> inputSides, Collection<EnumFacing> outputSides, Predicate<Fluid> acceptedFluids, int capacity) {
         super(capacity);
         this.acceptedFluids = acceptedFluids;
         this.inputSides = inputSides;
         this.outputSides = outputSides;
      }

      @Override
      public boolean canFillFluidType(FluidStack fluid) {
         return fluid != null && this.acceptsFluid(fluid.getFluid());
      }

      @Override
      public boolean canDrainFluidType(FluidStack fluid) {
         return fluid != null && this.acceptsFluid(fluid.getFluid());
      }

      public boolean acceptsFluid(Fluid fluid) {
         return this.acceptedFluids.test(fluid);
      }

      IFluidTankProperties getTankProperties(final EnumFacing side) {
         assert side == null || this.inputSides.contains(side) || this.outputSides.contains(side);
         return new IFluidTankProperties() {
            @Override
            public FluidStack getContents() {
               return PipeFluidTank.this.getFluid();
            }

            @Override
            public int getCapacity() {
               return PipeFluidTank.this.capacity;
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
               return fluidStack != null && fluidStack.amount > 0
                  ? PipeFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || PipeFluidTank.this.canFill(side))
                  : false;
            }

            @Override
            public boolean canFill() {
               return PipeFluidTank.this.canFill(side);
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
               return fluidStack != null && fluidStack.amount > 0
                  ? PipeFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || PipeFluidTank.this.canDrain(side))
                  : false;
            }

            @Override
            public boolean canDrain() {
               return PipeFluidTank.this.canDrain(side);
            }
         };
      }

      public boolean canFill(EnumFacing side) {
         return this.inputSides.contains(side);
      }

      public boolean canDrain(EnumFacing side) {
         return this.outputSides.contains(side);
      }
   }
}

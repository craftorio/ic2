package ic2.core.block.transport;

import ic2.api.transport.IFluidPipe;
import ic2.core.IC2;
import ic2.core.block.transport.cover.CoverProperty;
import ic2.core.block.transport.cover.ICoverItem;
import ic2.core.block.transport.items.PipeSize;
import ic2.core.block.transport.items.PipeType;
import ic2.core.item.block.ItemPipe;
import ic2.core.network.NetworkManager;
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
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = "ic2", value = {Side.CLIENT})
public class TileEntityFluidPipe extends TileEntityPipe implements IFluidPipe {
  public TileEntityFluidPipe(PipeType type, PipeSize size) {
    this();
    this.type = type;
    this.size = size;
    this.tank = new PipeFluidTank(Util.allFacings, Util.allFacings, fluid -> true, (int)(type.transferRate * size.multiplier));
  }
  
  public Set<CoverProperty> getCoverProperties() {
    return EnumSet.of(CoverProperty.FluidConsuming);
  }
  
  public int getTransferRate() {
    return this.type.transferRate;
  }
  
  public FluidTank getTank() {
    return this.tank;
  }
  
  public int getCurrentInnerCapacity() {
    return this.tank.getFluidAmount();
  }
  
  public int getMaxInnerCapacity() {
    return this.tank.getCapacity();
  }
  
  public void flipConnection(EnumFacing facing) {
    World world = getWorld();
    BlockPos pos = getPos();
    if (!world.isRemote) {
      this.connectivity = (byte)(this.connectivity ^ 1 << facing.ordinal());
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "connectivity");
      world.func_175685_c(pos, (Block)getBlockType(), true);
      func_70296_d();
    } 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (this.tank.getFluid() != null && this.tank.getFluidAmount() > 0) {
      int availableFluidAmount = this.tank.getFluidAmount();
      int cPipes = 1;
      List<LiquidUtil.AdjacentFluidHandler> adjacentFluidHandlers = new ArrayList<>();
      for (EnumFacing facing : EnumFacing.field_82609_l) {
        if (LiquidUtil.drainTile((TileEntity)this, facing, 2147483647, true) != null) {
          LiquidUtil.AdjacentFluidHandler target = LiquidUtil.getAdjacentHandler((TileEntity)this, facing);
          if (target != null)
            if (target.handler instanceof IFluidPipe) {
              int targetAmount = ((IFluidPipe)target.handler).getTank().getFluidAmount();
              if (this.tank.getFluidAmount() >= targetAmount) {
                availableFluidAmount += targetAmount;
                cPipes++;
                adjacentFluidHandlers.add(target);
              } 
            } else if (LiquidUtil.fillTile(target.handler, facing.func_176734_d(), this.tank.getFluid(), true) > 0) {
              adjacentFluidHandlers.add(target);
            }  
        } 
      } 
      if (this.debug)
        IC2.log.warn(LogCategory.Transport, "Number of valid adjacentFluidHandlers: %s", new Object[] { Integer.valueOf(adjacentFluidHandlers.size()) }); 
      int extraFluid = availableFluidAmount % cPipes;
      availableFluidAmount /= cPipes;
      List<LiquidUtil.AdjacentFluidHandler> adjacentPipes = new ArrayList<>();
      if (!adjacentFluidHandlers.isEmpty()) {
        Iterator<LiquidUtil.AdjacentFluidHandler> it = adjacentFluidHandlers.iterator();
        while (it.hasNext()) {
          LiquidUtil.AdjacentFluidHandler target = it.next();
          if (target.handler instanceof IFluidPipe) {
            FluidStack ret = LiquidUtil.transfer((TileEntity)this, target.dir, target.handler, availableFluidAmount - ((IFluidPipe)target.handler).getTank().getFluidAmount());
            if (this.debug)
              IC2.log.warn(LogCategory.Transport, "Split with pipe: %s to facing %s", new Object[] { Integer.valueOf((ret != null) ? ret.amount : 0), target.dir }); 
            adjacentPipes.add(target);
            it.remove();
          } 
        } 
      } 
      if (adjacentFluidHandlers.isEmpty()) {
        while (extraFluid > 0) {
          int bullet = IC2.random.nextInt(adjacentPipes.size());
          LiquidUtil.AdjacentFluidHandler target = adjacentPipes.get(bullet);
          LiquidUtil.transfer((TileEntity)this, target.dir, target.handler, 1);
          if (this.debug)
            IC2.log.warn(LogCategory.Transport, "Split with pipe: 1 to facing %s", new Object[] { target.dir }); 
          extraFluid--;
          adjacentPipes.remove(bullet);
        } 
      } else {
        availableFluidAmount += extraFluid;
      } 
      if (!adjacentFluidHandlers.isEmpty()) {
        int maxTransfer = Math.min(availableFluidAmount, (int)(this.type.transferRate * this.size.multiplier / 20.0F));
        int maxAmountPerOutput = (int)Math.floor((maxTransfer / adjacentFluidHandlers.size()));
        if (maxAmountPerOutput <= 0) {
          while (maxTransfer > 0) {
            int bullet = IC2.random.nextInt(adjacentFluidHandlers.size());
            LiquidUtil.AdjacentFluidHandler target = adjacentFluidHandlers.get(bullet);
            LiquidUtil.transfer((TileEntity)this, target.dir, target.handler, 1);
            if (this.debug)
              IC2.log.warn(LogCategory.Transport, "Transferred: 1 to facing %s", new Object[] { target.dir }); 
            maxTransfer--;
            adjacentFluidHandlers.remove(bullet);
          } 
        } else {
          for (LiquidUtil.AdjacentFluidHandler target : adjacentFluidHandlers) {
            FluidStack ret = LiquidUtil.transfer((TileEntity)this, target.dir, target.handler, maxAmountPerOutput);
            if (this.debug)
              IC2.log.warn(LogCategory.Transport, "Transferred: %s to facing %s", new Object[] { Integer.valueOf((ret != null) ? ret.amount : 0), target.dir }); 
          } 
        } 
      } 
    } 
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.type = PipeType.values[nbt.func_74771_c("type") & 0xFF];
    this.size = PipeSize.values()[nbt.func_74771_c("size") & 0xFF];
    this.tank = new PipeFluidTank(Util.allFacings, Util.allFacings, fluid -> true, (int)(this.type.transferRate * this.size.multiplier));
    this.tank.readFromNBT(nbt);
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.func_74774_a("type", (byte)this.type.ordinal());
    nbt.func_74774_a("size", (byte)this.size.ordinal());
    this.tank.writeToNBT(nbt);
    return nbt;
  }
  
  protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target) {
    return ItemPipe.getPipe(this.type, this.size);
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("type");
    ret.add("size");
    return ret;
  }
  
  protected void updateRenderState() {
    this.renderState = new TileEntityPipe.PipeRenderState(this.type, this.size, this.connectivity, this.covers, getFacing().ordinal());
  }
  
  protected void updateConnectivity() {
    byte newConnectivity = this.connectivity;
    for (EnumFacing direction : EnumFacing.field_82609_l) {
      TileEntity tile = this.field_145850_b.func_175625_s(this.field_174879_c.func_177972_a(direction));
      if (tile != null)
        if (tile instanceof IFluidPipe) {
          if (((IFluidPipe)tile).isConnected(direction.func_176734_d()))
            newConnectivity = (byte)(newConnectivity | 1 << direction.ordinal()); 
        } else if (LiquidUtil.isFluidTile(tile, direction.func_176734_d())) {
          newConnectivity = (byte)(newConnectivity | this.connectivity & 1 << direction.ordinal());
        }  
    } 
    if (this.connectivity != newConnectivity) {
      this.connectivity = newConnectivity;
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "connectivity");
    } 
  }
  
  public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
    super.onPlaced(stack, placer, facing);
    if (!this.field_145850_b.isRemote) {
      TileEntity tile = this.field_145850_b.func_175625_s(this.field_174879_c.func_177972_a(facing.func_176734_d()));
      if (tile != null && (
        tile instanceof IFluidPipe || LiquidUtil.isFluidTile(tile, facing))) {
        flipConnection(facing.func_176734_d());
        if (tile instanceof IFluidPipe) {
          IFluidPipe other = (IFluidPipe)tile;
          if (!other.isConnected(facing))
            other.flipConnection(facing); 
        } 
      } 
    } 
  }
  
  protected void onBlockBreak() {
    super.onBlockBreak();
    if (this.tank.getFluidAmount() > 1000 && 
      LiquidUtil.fillBlock(this.tank.getFluid(), this.field_145850_b, this.field_174879_c, true))
      LiquidUtil.fillBlock(this.tank.getFluid(), this.field_145850_b, this.field_174879_c, false); 
  }
  
  public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
    return (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) ? ((facing != null && 
      isConnected(facing))) : super.hasCapability(capability, facing);
  }
  
  public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
      return (facing != null && isConnected(facing)) ? (T)new PipeFluidHandler(facing) : null; 
    return (T)super.getCapability(capability, facing);
  }
  
  private class PipeFluidHandler implements IFluidHandler {
    private final EnumFacing side;
    
    public PipeFluidHandler(EnumFacing side) {
      this.side = side;
    }
    
    public IFluidTankProperties[] getTankProperties() {
      return TileEntityFluidPipe.this.tank.getTankProperties();
    }
    
    public int fill(FluidStack resource, boolean doFill) {
      if (TileEntityFluidPipe.this.coversComponent.hasCover(this.side)) {
        ICoverItem cover = TileEntityFluidPipe.this.coversComponent.getCoverItem(this.side);
        if (!cover.allowsInput(resource))
          return 0; 
      } 
      return TileEntityFluidPipe.this.tank.fill(resource, doFill);
    }
    
    @Nullable
    public FluidStack drain(FluidStack resource, boolean doDrain) {
      if (TileEntityFluidPipe.this.coversComponent.hasCover(this.side)) {
        ICoverItem cover = TileEntityFluidPipe.this.coversComponent.getCoverItem(this.side);
        if (!cover.allowsOutput(resource))
          return null; 
      } 
      return TileEntityFluidPipe.this.tank.drain(resource, doDrain);
    }
    
    @Nullable
    public FluidStack drain(int maxDrain, boolean doDrain) {
      ICoverItem cover = TileEntityFluidPipe.this.coversComponent.getCoverItem(this.side);
      if (TileEntityFluidPipe.this.coversComponent.hasCover(this.side) && !cover.allowsOutput(new FluidStack(Objects.<FluidStack>requireNonNull(TileEntityFluidPipe.this.tank.getFluid()), maxDrain)))
        return null; 
      return TileEntityFluidPipe.this.tank.drain(maxDrain, doDrain);
    }
  }
  
  protected PipeType type = PipeType.bronze;
  
  protected PipeSize size = PipeSize.small;
  
  protected PipeFluidTank tank;
  
  private boolean debug = false;
  
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
    
    public boolean canFillFluidType(FluidStack fluid) {
      return (fluid != null && acceptsFluid(fluid.getFluid()));
    }
    
    public boolean canDrainFluidType(FluidStack fluid) {
      return (fluid != null && acceptsFluid(fluid.getFluid()));
    }
    
    public boolean acceptsFluid(Fluid fluid) {
      return this.acceptedFluids.test(fluid);
    }
    
    IFluidTankProperties getTankProperties(final EnumFacing side) {
      assert side == null || this.inputSides.contains(side) || this.outputSides.contains(side);
      return new IFluidTankProperties() {
          public FluidStack getContents() {
            return TileEntityFluidPipe.PipeFluidTank.this.getFluid();
          }
          
          public int getCapacity() {
            return TileEntityFluidPipe.PipeFluidTank.this.capacity;
          }
          
          public boolean canFillFluidType(FluidStack fluidStack) {
            if (fluidStack == null || fluidStack.amount <= 0)
              return false; 
            return (TileEntityFluidPipe.PipeFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || TileEntityFluidPipe.PipeFluidTank.this.canFill(side)));
          }
          
          public boolean canFill() {
            return TileEntityFluidPipe.PipeFluidTank.this.canFill(side);
          }
          
          public boolean canDrainFluidType(FluidStack fluidStack) {
            if (fluidStack == null || fluidStack.amount <= 0)
              return false; 
            return (TileEntityFluidPipe.PipeFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || TileEntityFluidPipe.PipeFluidTank.this.canDrain(side)));
          }
          
          public boolean canDrain() {
            return TileEntityFluidPipe.PipeFluidTank.this.canDrain(side);
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
  
  protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
    if (!forCollision)
      return super.getAabbs(false); 
    float th = this.size.thickness;
    float sp = (1.0F - th) / 2.0F;
    List<AxisAlignedBB> ret = new ArrayList<>(7);
    ret.add(new AxisAlignedBB(sp, sp, sp, (sp + th), (sp + th), (sp + th)));
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      boolean hasConnection = ((this.connectivity & 1 << facing.ordinal()) != 0);
      if (hasConnection) {
        float zS = sp, yS = zS, xS = yS;
        float zE = sp + th, yE = zE, xE = yE;
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
        boolean hasCover = ((this.covers & 1 << facing.ordinal()) != 0);
        xS = yS = zS = 0.0F;
        xE = yE = zE = 1.0F;
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
  
  public AxisAlignedBB getVisualBoundingBox() {
    return getPhysicsBoundingBox();
  }
  
  protected AxisAlignedBB getOutlineBoundingBox() {
    return super.getVisualBoundingBox();
  }
  
  @SideOnly(Side.CLIENT)
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public static void drawBetter(DrawBlockHighlightEvent event) {
    if (event.getSubID() != 0)
      return; 
    RayTraceResult rayTrace = event.getTarget();
    if (rayTrace.typeOfHit != RayTraceResult.Type.BLOCK)
      return; 
    EntityPlayer player = event.getPlayer();
    World world = player.getEntityWorld();
    BlockPos pos = rayTrace.getBlockPos();
    if (!world.func_175723_af().func_177746_a(pos))
      return; 
    TileEntity te = world.func_175625_s(pos);
    if (!(te instanceof TileEntityFluidPipe))
      return; 
    GlStateManager.func_179147_l();
    GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    GlStateManager.func_187441_d(2.0F);
    GlStateManager.func_179090_x();
    GlStateManager.func_179132_a(false);
    double xOffset = player.field_70142_S + (player.posX - player.field_70142_S) * event.getPartialTicks();
    double yOffset = player.field_70137_T + (player.posY - player.field_70137_T) * event.getPartialTicks();
    double zOffset = player.field_70136_U + (player.posZ - player.field_70136_U) * event.getPartialTicks();
    RenderGlobal.func_189697_a(((TileEntityFluidPipe)te).getVisualBoundingBox().func_186670_a(pos).func_186662_g(0.002D).func_72317_d(-xOffset, -yOffset, -zOffset), 0.0F, 0.0F, 0.0F, 0.4F);
    GlStateManager.func_179132_a(true);
    GlStateManager.func_179098_w();
    GlStateManager.func_179084_k();
    event.setCanceled(true);
  }
  
  public TileEntityFluidPipe() {}
}

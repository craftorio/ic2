// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport;

import java.util.function.Predicate;
import java.util.Objects;
import javax.annotation.Nullable;
import ic2.core.block.transport.cover.ICoverItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.item.block.ItemPipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import java.util.Iterator;
import java.util.List;
import ic2.core.util.LogCategory;
import ic2.core.util.LiquidUtil;
import java.util.ArrayList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraftforge.fluids.FluidTank;
import java.util.EnumSet;
import ic2.core.block.transport.cover.CoverProperty;
import java.util.Set;
import net.minecraft.util.EnumFacing;
import java.util.Collection;
import ic2.core.util.Util;
import ic2.core.block.transport.items.PipeSize;
import ic2.core.block.transport.items.PipeType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.Mod;
import ic2.api.transport.IFluidPipe;

@Mod.EventBusSubscriber(modid = "ic2", value = { Side.CLIENT })
public class TileEntityFluidPipe extends TileEntityPipe implements IFluidPipe
{
    protected PipeType type;
    protected PipeSize size;
    protected PipeFluidTank tank;
    private boolean debug;
    
    public TileEntityFluidPipe() {
        this.type = PipeType.bronze;
        this.size = PipeSize.small;
        this.debug = false;
    }
    
    public TileEntityFluidPipe(final PipeType type, final PipeSize size) {
        this();
        this.type = type;
        this.size = size;
        this.tank = new PipeFluidTank(Util.allFacings, Util.allFacings, fluid -> true, (int)(type.transferRate * size.multiplier));
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
    public void flipConnection(final EnumFacing facing) {
        final World world = this.getWorld();
        final BlockPos pos = this.getPos();
        if (!world.isRemote) {
            this.connectivity ^= (byte)(1 << facing.ordinal());
            IC2.network.get(true).updateTileEntityField(this, "connectivity");
            world.notifyNeighborsOfStateChange(pos, (Block)this.getBlockType(), true);
            this.markDirty();
        }
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.tank.getFluid() != null && this.tank.getFluidAmount() > 0) {
            int availableFluidAmount = this.tank.getFluidAmount();
            int cPipes = 1;
            final List<LiquidUtil.AdjacentFluidHandler> adjacentFluidHandlers = new ArrayList<LiquidUtil.AdjacentFluidHandler>();
            for (final EnumFacing facing : EnumFacing.VALUES) {
                if (LiquidUtil.drainTile(this, facing, Integer.MAX_VALUE, true) != null) {
                    final LiquidUtil.AdjacentFluidHandler target = LiquidUtil.getAdjacentHandler(this, facing);
                    if (target != null) {
                        if (target.handler instanceof IFluidPipe) {
                            final int targetAmount = ((IFluidPipe)target.handler).getTank().getFluidAmount();
                            if (this.tank.getFluidAmount() >= targetAmount) {
                                availableFluidAmount += targetAmount;
                                ++cPipes;
                                adjacentFluidHandlers.add(target);
                            }
                        }
                        else if (LiquidUtil.fillTile(target.handler, facing.getOpposite(), this.tank.getFluid(), true) > 0) {
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
            final List<LiquidUtil.AdjacentFluidHandler> adjacentPipes = new ArrayList<LiquidUtil.AdjacentFluidHandler>();
            if (!adjacentFluidHandlers.isEmpty()) {
                final Iterator<LiquidUtil.AdjacentFluidHandler> it = adjacentFluidHandlers.iterator();
                while (it.hasNext()) {
                    final LiquidUtil.AdjacentFluidHandler target2 = it.next();
                    if (target2.handler instanceof IFluidPipe) {
                        final FluidStack ret = LiquidUtil.transfer(this, target2.dir, target2.handler, availableFluidAmount - ((IFluidPipe)target2.handler).getTank().getFluidAmount());
                        if (this.debug) {
                            IC2.log.warn(LogCategory.Transport, "Split with pipe: %s to facing %s", (ret != null) ? ret.amount : 0, target2.dir);
                        }
                        adjacentPipes.add(target2);
                        it.remove();
                    }
                }
            }
            if (adjacentFluidHandlers.isEmpty()) {
                while (extraFluid > 0) {
                    final int bullet = IC2.random.nextInt(adjacentPipes.size());
                    final LiquidUtil.AdjacentFluidHandler target2 = adjacentPipes.get(bullet);
                    LiquidUtil.transfer(this, target2.dir, target2.handler, 1);
                    if (this.debug) {
                        IC2.log.warn(LogCategory.Transport, "Split with pipe: 1 to facing %s", target2.dir);
                    }
                    --extraFluid;
                    adjacentPipes.remove(bullet);
                }
            }
            else {
                availableFluidAmount += extraFluid;
            }
            if (!adjacentFluidHandlers.isEmpty()) {
                int maxTransfer = Math.min(availableFluidAmount, (int)(this.type.transferRate * this.size.multiplier / 20.0f));
                final int maxAmountPerOutput = (int)Math.floor(maxTransfer / (float)adjacentFluidHandlers.size());
                if (maxAmountPerOutput <= 0) {
                    while (maxTransfer > 0) {
                        final int bullet2 = IC2.random.nextInt(adjacentFluidHandlers.size());
                        final LiquidUtil.AdjacentFluidHandler target3 = adjacentFluidHandlers.get(bullet2);
                        LiquidUtil.transfer(this, target3.dir, target3.handler, 1);
                        if (this.debug) {
                            IC2.log.warn(LogCategory.Transport, "Transferred: 1 to facing %s", target3.dir);
                        }
                        --maxTransfer;
                        adjacentFluidHandlers.remove(bullet2);
                    }
                }
                else {
                    for (final LiquidUtil.AdjacentFluidHandler target3 : adjacentFluidHandlers) {
                        final FluidStack ret2 = LiquidUtil.transfer(this, target3.dir, target3.handler, maxAmountPerOutput);
                        if (this.debug) {
                            IC2.log.warn(LogCategory.Transport, "Transferred: %s to facing %s", (ret2 != null) ? ret2.amount : 0, target3.dir);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.type = PipeType.values[nbt.getByte("type") & 0xFF];
        this.size = PipeSize.values()[nbt.getByte("size") & 0xFF];
        (this.tank = new PipeFluidTank(Util.allFacings, Util.allFacings, fluid -> true, (int)(this.type.transferRate * this.size.multiplier))).readFromNBT(nbt);
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("type", (byte)this.type.ordinal());
        nbt.setByte("size", (byte)this.size.ordinal());
        this.tank.writeToNBT(nbt);
        return nbt;
    }
    
    @Override
    protected ItemStack getPickBlock(final EntityPlayer player, final RayTraceResult target) {
        return ItemPipe.getPipe(this.type, this.size);
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("type");
        ret.add("size");
        return ret;
    }
    
    @Override
    protected void updateRenderState() {
        this.renderState = new PipeRenderState(this.type, this.size, this.connectivity, this.covers, this.getFacing().ordinal());
    }
    
    @Override
    protected void updateConnectivity() {
        byte newConnectivity = this.connectivity;
        for (final EnumFacing direction : EnumFacing.VALUES) {
            final TileEntity tile = this.world.getTileEntity(this.pos.offset(direction));
            if (tile != null) {
                if (tile instanceof IFluidPipe) {
                    if (((IFluidPipe)tile).isConnected(direction.getOpposite())) {
                        newConnectivity |= (byte)(1 << direction.ordinal());
                    }
                }
                else if (LiquidUtil.isFluidTile(tile, direction.getOpposite())) {
                    newConnectivity |= (byte)(this.connectivity & 1 << direction.ordinal());
                }
            }
        }
        if (this.connectivity != newConnectivity) {
            this.connectivity = newConnectivity;
            IC2.network.get(true).updateTileEntityField(this, "connectivity");
        }
    }
    
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        if (!this.world.isRemote) {
            final TileEntity tile = this.world.getTileEntity(this.pos.offset(facing.getOpposite()));
            if (tile != null && (tile instanceof IFluidPipe || LiquidUtil.isFluidTile(tile, facing))) {
                this.flipConnection(facing.getOpposite());
                if (tile instanceof IFluidPipe) {
                    final IFluidPipe other = (IFluidPipe)tile;
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
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        return (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) ? (facing != null && this.isConnected(facing)) : super.hasCapability(capability, facing);
    }
    
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T)((facing != null && this.isConnected(facing)) ? new PipeFluidHandler(facing) : null);
        }
        return super.getCapability(capability, facing);
    }
    
    @Override
    protected List<AxisAlignedBB> getAabbs(final boolean forCollision) {
        if (!forCollision) {
            return super.getAabbs(false);
        }
        final float th = this.size.thickness;
        final float sp = (1.0f - th) / 2.0f;
        final List<AxisAlignedBB> ret = new ArrayList<AxisAlignedBB>(7);
        ret.add(new AxisAlignedBB((double)sp, (double)sp, (double)sp, (double)(sp + th), (double)(sp + th), (double)(sp + th)));
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final boolean hasConnection = (this.connectivity & 1 << facing.ordinal()) != 0x0;
            if (hasConnection) {
                float zS;
                float xS;
                float yS = xS = (zS = sp);
                float zE;
                float xE;
                float yE = xE = (zE = sp + th);
                switch (facing) {
                    case DOWN: {
                        yS = 0.0f;
                        yE = sp;
                        break;
                    }
                    case UP: {
                        yS = sp + th;
                        yE = 1.0f;
                        break;
                    }
                    case NORTH: {
                        zS = 0.0f;
                        zE = sp;
                        break;
                    }
                    case SOUTH: {
                        zS = sp + th;
                        zE = 1.0f;
                        break;
                    }
                    case WEST: {
                        xS = 0.0f;
                        xE = sp;
                        break;
                    }
                    case EAST: {
                        xS = sp + th;
                        xE = 1.0f;
                        break;
                    }
                    default: {
                        throw new RuntimeException();
                    }
                }
                ret.add(new AxisAlignedBB((double)xS, (double)yS, (double)zS, (double)xE, (double)yE, (double)zE));
                final float cs = 1.0f;
                final float ch = 0.1f;
                final boolean hasCover = (this.covers & 1 << facing.ordinal()) != 0x0;
                yS = (xS = (zS = 0.0f));
                yE = (xE = (zE = 1.0f));
                if (hasCover) {
                    switch (facing) {
                        case DOWN: {
                            yS = 0.0f;
                            yE = ch;
                            break;
                        }
                        case UP: {
                            yS = cs - ch;
                            yE = 1.0f;
                            break;
                        }
                        case NORTH: {
                            zS = 0.0f;
                            zE = ch;
                            break;
                        }
                        case SOUTH: {
                            zS = cs - ch;
                            zE = 1.0f;
                            break;
                        }
                        case WEST: {
                            xS = 0.0f;
                            xE = ch;
                            break;
                        }
                        case EAST: {
                            xS = cs - ch;
                            xE = 1.0f;
                            break;
                        }
                        default: {
                            throw new RuntimeException();
                        }
                    }
                    ret.add(new AxisAlignedBB((double)xS, (double)yS, (double)zS, (double)xE, (double)yE, (double)zE));
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
    public static void drawBetter(final DrawBlockHighlightEvent event) {
        if (event.getSubID() != 0) {
            return;
        }
        final RayTraceResult rayTrace = event.getTarget();
        if (rayTrace.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        final EntityPlayer player = event.getPlayer();
        final World world = player.getEntityWorld();
        final BlockPos pos = rayTrace.getBlockPos();
        if (!world.getWorldBorder().contains(pos)) {
            return;
        }
        final TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityFluidPipe)) {
            return;
        }
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        final double xOffset = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
        final double yOffset = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
        final double zOffset = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
        RenderGlobal.drawSelectionBoundingBox(((TileEntityFluidPipe)te).getVisualBoundingBox().offset(pos).grow(0.002).offset(-xOffset, -yOffset, -zOffset), 0.0f, 0.0f, 0.0f, 0.4f);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        event.setCanceled(true);
    }
    
    private class PipeFluidHandler implements IFluidHandler
    {
        private final EnumFacing side;
        
        public PipeFluidHandler(final EnumFacing side) {
            this.side = side;
        }
        
        public IFluidTankProperties[] getTankProperties() {
            return TileEntityFluidPipe.this.tank.getTankProperties();
        }
        
        public int fill(final FluidStack resource, final boolean doFill) {
            if (TileEntityFluidPipe.this.coversComponent.hasCover(this.side)) {
                final ICoverItem cover = TileEntityFluidPipe.this.coversComponent.getCoverItem(this.side);
                if (!cover.allowsInput(resource)) {
                    return 0;
                }
            }
            return TileEntityFluidPipe.this.tank.fill(resource, doFill);
        }
        
        @Nullable
        public FluidStack drain(final FluidStack resource, final boolean doDrain) {
            if (TileEntityFluidPipe.this.coversComponent.hasCover(this.side)) {
                final ICoverItem cover = TileEntityFluidPipe.this.coversComponent.getCoverItem(this.side);
                if (!cover.allowsOutput(resource)) {
                    return null;
                }
            }
            return TileEntityFluidPipe.this.tank.drain(resource, doDrain);
        }
        
        @Nullable
        public FluidStack drain(final int maxDrain, final boolean doDrain) {
            if (TileEntityFluidPipe.this.coversComponent.hasCover(this.side)) {
                final ICoverItem cover = TileEntityFluidPipe.this.coversComponent.getCoverItem(this.side);
                if (!cover.allowsOutput(new FluidStack((FluidStack)Objects.requireNonNull(TileEntityFluidPipe.this.tank.getFluid()), maxDrain))) {
                    return null;
                }
            }
            return TileEntityFluidPipe.this.tank.drain(maxDrain, doDrain);
        }
    }
    
    private class PipeFluidTank extends FluidTank
    {
        private final Predicate<Fluid> acceptedFluids;
        private Collection<EnumFacing> inputSides;
        private Collection<EnumFacing> outputSides;
        
        protected PipeFluidTank(final Collection<EnumFacing> inputSides, final Collection<EnumFacing> outputSides, final Predicate<Fluid> acceptedFluids, final int capacity) {
            super(capacity);
            this.acceptedFluids = acceptedFluids;
            this.inputSides = inputSides;
            this.outputSides = outputSides;
        }
        
        public boolean canFillFluidType(final FluidStack fluid) {
            return fluid != null && this.acceptsFluid(fluid.getFluid());
        }
        
        public boolean canDrainFluidType(final FluidStack fluid) {
            return fluid != null && this.acceptsFluid(fluid.getFluid());
        }
        
        public boolean acceptsFluid(final Fluid fluid) {
            return this.acceptedFluids.test(fluid);
        }
        
        IFluidTankProperties getTankProperties(final EnumFacing side) {
            assert !(!this.outputSides.contains(side));
            return (IFluidTankProperties)new IFluidTankProperties() {
                public FluidStack getContents() {
                    return PipeFluidTank.this.getFluid();
                }
                
                public int getCapacity() {
                    return PipeFluidTank.this.capacity;
                }
                
                public boolean canFillFluidType(final FluidStack fluidStack) {
                    return fluidStack != null && fluidStack.amount > 0 && PipeFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || PipeFluidTank.this.canFill(side));
                }
                
                public boolean canFill() {
                    return PipeFluidTank.this.canFill(side);
                }
                
                public boolean canDrainFluidType(final FluidStack fluidStack) {
                    return fluidStack != null && fluidStack.amount > 0 && PipeFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || PipeFluidTank.this.canDrain(side));
                }
                
                public boolean canDrain() {
                    return PipeFluidTank.this.canDrain(side);
                }
            };
        }
        
        public boolean canFill(final EnumFacing side) {
            return this.inputSides.contains(side);
        }
        
        public boolean canDrain(final EnumFacing side) {
            return this.outputSides.contains(side);
        }
    }
}

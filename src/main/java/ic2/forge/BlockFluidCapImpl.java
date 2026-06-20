package ic2.forge;

import ic2.core.fluid.FluidTankInfo;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

final class BlockFluidCapImpl implements IBlockCapabilityProvider<IFluidHandler, @Nullable Direction> {

    private final Ic2FluidBlock parent;

    private final BlockEntity be;

    private final SideHandler[] sides;

    public BlockFluidCapImpl(Ic2FluidBlock fluidBlock, BlockEntity be) {
        this.parent = fluidBlock;
        this.be = be;
        this.sides = new SideHandler[Util.ALL_DIRS.length];
        for (Direction dir : Util.ALL_DIRS) {
            this.sides[dir.ordinal()] = new SideHandler(dir);
        }
    }

    @Override
    @Nullable
    public IFluidHandler getCapability(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction context) {
        if (context == null) {
            return this.sides[0];
        }
        return this.sides[context.ordinal()];
    }

    private class SideHandler implements IFluidHandler {

        private final Direction side;

        SideHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getTanks() {
            FluidTankInfo[] infos = BlockFluidCapImpl.this.parent.getTankInfos(null, null, null, BlockFluidCapImpl.this.be);
            if (infos == null) {
                return 0;
            }
            int sideMaskReq = 1 << this.side.ordinal();
            int ret = 0;
            for (FluidTankInfo info : infos) {
                if (((info.getDrainSideMask() | info.getFillSideMask()) & sideMaskReq) != 0) {
                    ret++;
                }
            }
            return ret;
        }

        private FluidTankInfo getTankInfo(int tank) {
            FluidTankInfo[] infos = BlockFluidCapImpl.this.parent.getTankInfos(null, null, null, BlockFluidCapImpl.this.be);
            if (infos == null) {
                return null;
            }
            int sideMaskReq = 1 << this.side.ordinal();
            for (FluidTankInfo info : infos) {
                if (((info.getDrainSideMask() | info.getFillSideMask()) & sideMaskReq) != 0 && tank-- == 0) {
                    return info;
                }
            }
            return null;
        }

        @Override
        public int getTankCapacity(int tank) {
            FluidTankInfo info = this.getTankInfo(tank);
            return info != null ? info.getCapacity() : 0;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            FluidTankInfo info = this.getTankInfo(tank);
            return info != null ? EnvFluidHandlerForge.getForgeFs(info.getContent()) : FluidStack.EMPTY;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack fs) {
            FluidTankInfo info = this.getTankInfo(tank);
            return info != null && info.getCapacity() > 0;
        }

        @Override
        public FluidStack drain(int amount, IFluidHandler.FluidAction action) {
            return amount <= 0 ? FluidStack.EMPTY : EnvFluidHandlerForge.getForgeFs(BlockFluidCapImpl.this.parent.drainMb(null, null, null, BlockFluidCapImpl.this.be, this.side, amount, action.simulate()));
        }

        @Override
        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            if (resource != null && !resource.isEmpty()) {
                int amount = BlockFluidCapImpl.this.parent.drainMb(null, null, null, BlockFluidCapImpl.this.be, this.side, new Ic2FluidStackImpl(resource), action.simulate());
                if (amount <= 0) {
                    return FluidStack.EMPTY;
                }
                resource = resource.copy();
                resource.setAmount(amount);
                return resource;
            } else {
                return FluidStack.EMPTY;
            }
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            return resource != null && !resource.isEmpty() ? BlockFluidCapImpl.this.parent.fillMb(null, null, null, BlockFluidCapImpl.this.be, this.side, new Ic2FluidStackImpl(resource), action.simulate()) : 0;
        }
    }
}

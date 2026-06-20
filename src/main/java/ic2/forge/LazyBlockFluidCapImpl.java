package ic2.forge;

import ic2.core.block.comp.Fluids;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.fluid.Ic2FluidBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.LazyOptional;

/**
 * Lazily wraps BlockFluidCapImpl, resolving the Fluids component on first
 * capability query. This works around the Forge issue where
 * AttachCapabilitiesEvent fires BEFORE the BE constructor completes,
 * so hasComponent(Fluids.class) always returns false during the event.
 */
final class LazyBlockFluidCapImpl implements ICapabilityProvider {

    private final BlockEntity be;

    private BlockFluidCapImpl delegate;

    LazyBlockFluidCapImpl(BlockEntity be) {
        this.be = be;
    }

    private BlockFluidCapImpl resolve() {
        if (this.delegate == null && this.be instanceof Ic2TileEntity ic2te) {
            Fluids fluids = ic2te.getComponent(Fluids.class);
            if (fluids != null) {
                this.delegate = new BlockFluidCapImpl(fluids, this.be);
            }
        }
        return this.delegate;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        BlockFluidCapImpl d = this.resolve();
        return d != null ? d.getCapability(capability, facing) : LazyOptional.empty();
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.FluidStack;
import ic2.api.recipe.ILiquidAcceptManager;
import java.util.HashSet;
import java.util.Arrays;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Iterator;
import net.minecraftforge.fluids.FluidTank;
import com.google.common.base.Suppliers;
import net.minecraft.util.EnumFacing;
import java.util.Collections;
import ic2.core.block.invslot.InvSlot;
import net.minecraftforge.fluids.Fluid;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.ArrayList;
import ic2.core.block.TileEntityBlock;
import java.util.Collection;
import com.google.common.base.Supplier;
import java.util.List;

public class Fluids extends TileEntityComponent
{
    protected final List<InternalFluidTank> managedTanks;
    protected final List<Supplier<? extends Collection<InternalFluidTank>>> unmanagedTanks;
    
    public Fluids(final TileEntityBlock parent) {
        super(parent);
        this.managedTanks = new ArrayList<InternalFluidTank>();
        this.unmanagedTanks = new ArrayList<Supplier<? extends Collection<InternalFluidTank>>>();
    }
    
    public InternalFluidTank addTankInsert(final String name, final int capacity) {
        return this.addTankInsert(name, capacity, (Predicate<Fluid>)Predicates.alwaysTrue());
    }
    
    public InternalFluidTank addTankInsert(final String name, final int capacity, final Predicate<Fluid> acceptedFluids) {
        return this.addTankInsert(name, capacity, InvSlot.InvSide.ANY, acceptedFluids);
    }
    
    public InternalFluidTank addTankInsert(final String name, final int capacity, final InvSlot.InvSide side) {
        return this.addTankInsert(name, capacity, side, (Predicate<Fluid>)Predicates.alwaysTrue());
    }
    
    public InternalFluidTank addTankInsert(final String name, final int capacity, final InvSlot.InvSide side, final Predicate<Fluid> acceptedFluids) {
        return this.addTank(name, capacity, InvSlot.Access.I, side, acceptedFluids);
    }
    
    public InternalFluidTank addTankExtract(final String name, final int capacity) {
        return this.addTankExtract(name, capacity, InvSlot.InvSide.ANY);
    }
    
    public InternalFluidTank addTankExtract(final String name, final int capacity, final InvSlot.InvSide side) {
        return this.addTank(name, capacity, InvSlot.Access.O, side);
    }
    
    public InternalFluidTank addTank(final String name, final int capacity) {
        return this.addTank(name, capacity, InvSlot.Access.IO);
    }
    
    public InternalFluidTank addTank(final String name, final int capacity, final InvSlot.Access access) {
        return this.addTank(name, capacity, access, InvSlot.InvSide.ANY);
    }
    
    public InternalFluidTank addTank(final String name, final int capacity, final Predicate<Fluid> acceptedFluids) {
        return this.addTank(name, capacity, InvSlot.Access.IO, InvSlot.InvSide.ANY, acceptedFluids);
    }
    
    public InternalFluidTank addTank(final String name, final int capacity, final InvSlot.Access access, final InvSlot.InvSide side) {
        return this.addTank(name, capacity, access, side, (Predicate<Fluid>)Predicates.alwaysTrue());
    }
    
    public InternalFluidTank addTank(final String name, final int capacity, final InvSlot.Access access, final InvSlot.InvSide side, final Predicate<Fluid> acceptedFluids) {
        return this.addTank(name, capacity, access.isInput() ? side.getAcceptedSides() : Collections.emptySet(), access.isOutput() ? side.getAcceptedSides() : Collections.emptySet(), acceptedFluids);
    }
    
    public InternalFluidTank addTank(final String name, final int capacity, final Collection<EnumFacing> inputSides, final Collection<EnumFacing> outputSides, final Predicate<Fluid> acceptedFluids) {
        return this.addTank(new InternalFluidTank(name, inputSides, outputSides, acceptedFluids, capacity));
    }
    
    public InternalFluidTank addTank(final InternalFluidTank tank) {
        this.managedTanks.add(tank);
        return tank;
    }
    
    public void addUnmanagedTanks(final InternalFluidTank tank) {
        this.unmanagedTanks.add((Supplier<? extends Collection<InternalFluidTank>>)Suppliers.ofInstance((Object)Collections.singleton(tank)));
    }
    
    public void addUnmanagedTanks(final Collection<InternalFluidTank> tanks) {
        this.addUnmanagedTankHook((Supplier<? extends Collection<InternalFluidTank>>)Suppliers.ofInstance((Object)tanks));
    }
    
    public void addUnmanagedTankHook(final Supplier<? extends Collection<InternalFluidTank>> suppl) {
        this.unmanagedTanks.add(suppl);
    }
    
    public void changeConnectivity(final InternalFluidTank tank, final InvSlot.Access access, final InvSlot.InvSide side) {
        this.changeConnectivity(tank, access.isInput() ? side.getAcceptedSides() : Collections.emptySet(), access.isOutput() ? side.getAcceptedSides() : Collections.emptySet());
    }
    
    public void changeConnectivity(final InternalFluidTank tank, final Collection<EnumFacing> inputSides, final Collection<EnumFacing> outputSides) {
        assert this.managedTanks.contains(tank);
        tank.inputSides = inputSides;
        tank.outputSides = outputSides;
    }
    
    public FluidTank getFluidTank(final String name) {
        for (final InternalFluidTank tank : this.getAllTanks()) {
            if (tank.identifier.equals(name)) {
                return tank;
            }
        }
        throw new IllegalArgumentException("Unable to find tank: " + name);
    }
    
    @Override
    public void readFromNbt(final NBTTagCompound nbt) {
        for (final InternalFluidTank tank : this.managedTanks) {
            if (nbt.hasKey(tank.identifier, 10)) {
                tank.readFromNBT(nbt.getCompoundTag(tank.identifier));
            }
        }
    }
    
    @Override
    public NBTTagCompound writeToNbt() {
        final NBTTagCompound nbt = new NBTTagCompound();
        for (final InternalFluidTank tank : this.managedTanks) {
            NBTTagCompound subTag = new NBTTagCompound();
            subTag = tank.writeToNBT(subTag);
            nbt.setTag(tank.identifier, (NBTBase)subTag);
        }
        return nbt;
    }
    
    @Override
    public Collection<? extends Capability<?>> getProvidedCapabilities(final EnumFacing side) {
        return (Collection<? extends Capability<?>>)Collections.singleton(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
    }
    
    @Override
    public <T> T getCapability(final Capability<T> cap, final EnumFacing side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T)new FluidHandler(side);
        }
        return super.getCapability(cap, side);
    }
    
    public static Predicate<Fluid> fluidPredicate(final Fluid... fluids) {
        Collection<Fluid> acceptedFluids;
        if (fluids.length > 10) {
            acceptedFluids = new HashSet<Fluid>(Arrays.asList(fluids));
        }
        else {
            acceptedFluids = Arrays.asList(fluids);
        }
        return (Predicate<Fluid>)new Predicate<Fluid>() {
            public boolean apply(final Fluid fluid) {
                return acceptedFluids.contains(fluid);
            }
        };
    }
    
    public static Predicate<Fluid> fluidPredicate(final ILiquidAcceptManager manager) {
        return (Predicate<Fluid>)new Predicate<Fluid>() {
            public boolean apply(final Fluid fluid) {
                return manager.acceptsFluid(fluid);
            }
        };
    }
    
    public Iterable<InternalFluidTank> getAllTanks() {
        if (this.unmanagedTanks.isEmpty()) {
            return this.managedTanks;
        }
        final List<InternalFluidTank> tanks = new ArrayList<InternalFluidTank>();
        tanks.addAll(this.managedTanks);
        for (final Supplier<? extends Collection<InternalFluidTank>> suppl : this.unmanagedTanks) {
            tanks.addAll((Collection<? extends InternalFluidTank>)suppl.get());
        }
        return tanks;
    }
    
    public static class InternalFluidTank extends FluidTank
    {
        protected final String identifier;
        private final Predicate<Fluid> acceptedFluids;
        private Collection<EnumFacing> inputSides;
        private Collection<EnumFacing> outputSides;
        
        protected InternalFluidTank(final String identifier, final Collection<EnumFacing> inputSides, final Collection<EnumFacing> outputSides, final Predicate<Fluid> acceptedFluids, final int capacity) {
            super(capacity);
            this.identifier = identifier;
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
            return this.acceptedFluids.apply((Object)fluid);
        }
        
        IFluidTankProperties getTankProperties(final EnumFacing side) {
            assert !(!this.outputSides.contains(side));
            return (IFluidTankProperties)new IFluidTankProperties() {
                public FluidStack getContents() {
                    return InternalFluidTank.this.getFluid();
                }
                
                public int getCapacity() {
                    return InternalFluidTank.this.capacity;
                }
                
                public boolean canFillFluidType(final FluidStack fluidStack) {
                    return fluidStack != null && fluidStack.amount > 0 && InternalFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || InternalFluidTank.this.canFill(side));
                }
                
                public boolean canFill() {
                    return InternalFluidTank.this.canFill(side);
                }
                
                public boolean canDrainFluidType(final FluidStack fluidStack) {
                    return fluidStack != null && fluidStack.amount > 0 && InternalFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || InternalFluidTank.this.canDrain(side));
                }
                
                public boolean canDrain() {
                    return InternalFluidTank.this.canDrain(side);
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
    
    private class FluidHandler implements IFluidHandler
    {
        private final EnumFacing side;
        
        FluidHandler(final EnumFacing side) {
            this.side = side;
        }
        
        public IFluidTankProperties[] getTankProperties() {
            final List<IFluidTankProperties> props = new ArrayList<IFluidTankProperties>(Fluids.this.managedTanks.size());
            for (final InternalFluidTank tank : Fluids.this.getAllTanks()) {
                if (tank.canFill(this.side) || tank.canDrain(this.side)) {
                    props.add(tank.getTankProperties(this.side));
                }
            }
            return props.toArray(new IFluidTankProperties[0]);
        }
        
        public int fill(final FluidStack resource, final boolean doFill) {
            if (resource == null || resource.amount <= 0) {
                return 0;
            }
            int total = 0;
            final FluidStack missing = resource.copy();
            for (final InternalFluidTank tank : Fluids.this.getAllTanks()) {
                if (!tank.canFill(this.side)) {
                    continue;
                }
                total += tank.fill(missing, doFill);
                missing.amount = resource.amount - total;
                if (missing.amount <= 0) {
                    break;
                }
            }
            return total;
        }
        
        public FluidStack drain(final FluidStack resource, final boolean doDrain) {
            if (resource == null || resource.amount <= 0) {
                return null;
            }
            final FluidStack ret = new FluidStack(resource.getFluid(), 0);
            for (final InternalFluidTank tank : Fluids.this.getAllTanks()) {
                if (!tank.canDrain(this.side)) {
                    continue;
                }
                final FluidStack inTank = tank.getFluid();
                if (inTank == null) {
                    continue;
                }
                if (inTank.getFluid() != resource.getFluid()) {
                    continue;
                }
                final FluidStack add = tank.drain(resource.amount - ret.amount, doDrain);
                if (add == null) {
                    continue;
                }
                assert add.getFluid() == resource.getFluid();
                final FluidStack fluidStack = ret;
                fluidStack.amount += add.amount;
                if (ret.amount >= resource.amount) {
                    break;
                }
            }
            if (ret.amount == 0) {
                return null;
            }
            return ret;
        }
        
        public FluidStack drain(final int maxDrain, final boolean doDrain) {
            for (final InternalFluidTank tank : Fluids.this.getAllTanks()) {
                if (!tank.canDrain(this.side)) {
                    continue;
                }
                final FluidStack stack = tank.drain(maxDrain, false);
                if (stack != null) {
                    stack.amount = maxDrain;
                    return this.drain(stack, doDrain);
                }
            }
            return null;
        }
    }
}

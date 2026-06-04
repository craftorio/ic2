// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.type;

import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import java.util.IdentityHashMap;
import java.util.stream.Collectors;
import java.util.Arrays;
import ic2.core.ref.ItemName;
import net.minecraftforge.fluids.FluidStack;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.Map;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraft.util.EnumActionResult;
import ic2.core.crop.TileEntityCrop;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import ic2.core.profile.NotExperimental;
import ic2.core.block.state.IIdProvider;

@NotExperimental
public enum CellType implements IIdProvider
{
    empty(0), 
    water(1, FluidRegistry.WATER), 
    lava(2, FluidRegistry.LAVA), 
    air(3, FluidName.air.getInstance()), 
    electrolyzed_water(4), 
    biofuel(5), 
    coalfuel(6), 
    bio(7), 
    hydrated_coal(8), 
    weed_ex(9), 
    hydration(10);
    
    private final int id;
    final Fluid fluid;
    
    private CellType(final int id) {
        this(id, null);
    }
    
    private CellType(final int id, final Fluid fluid) {
        this.id = id;
        this.fluid = fluid;
    }
    
    @Override
    public String getName() {
        return this.name();
    }
    
    @Override
    public int getId() {
        return this.id;
    }
    
    public int getStackSize() {
        return (this == CellType.weed_ex || this == CellType.hydration) ? 1 : 64;
    }
    
    public boolean isFluidContainer() {
        return this.fluid != null || this == CellType.empty;
    }
    
    public boolean hasCropAction() {
        return this == CellType.water || this == CellType.weed_ex || this == CellType.hydration;
    }
    
    public int getUsage(final ItemStack stack) {
        switch (this) {
            case weed_ex: {
                return stack.hasTagCompound() ? stack.getTagCompound().getInteger("weedEX") : 0;
            }
            case hydration: {
                return stack.hasTagCompound() ? stack.getTagCompound().getInteger("hydration") : 0;
            }
            default: {
                return 0;
            }
        }
    }
    
    public int getMaximum(final ItemStack stack) {
        switch (this) {
            case weed_ex: {
                return 64;
            }
            case hydration: {
                return 10000;
            }
            default: {
                return 0;
            }
        }
    }
    
    public EnumActionResult doCropAction(final ItemStack stack, final Consumer<ItemStack> result, final TileEntityCrop crop, final boolean manual) {
        assert this.hasCropAction();
        switch (this) {
            case water: {
                if (crop.getStorageWater() < 10) {
                    crop.setStorageWater(10);
                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.FAIL;
            }
            case weed_ex: {
                final IFluidHandlerItem handler = (IFluidHandlerItem)new WeedExHandler(stack);
                if (crop.applyWeedEx((IFluidHandler)handler, manual)) {
                    result.accept(handler.getContainer());
                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.FAIL;
            }
            case hydration: {
                final IFluidHandlerItem handler = (IFluidHandlerItem)new HydrationHandler(stack, manual);
                if (crop.applyHydration((IFluidHandler)handler)) {
                    result.accept(handler.getContainer());
                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.FAIL;
            }
            default: {
                throw new IllegalStateException("Type was " + this);
            }
        }
    }
    
    public static class CellFluidHandler extends FluidBucketWrapper
    {
        private static final Map<Fluid, CellType> VALID_FLUIDS;
        protected final Supplier<CellType> typeGetter;
        
        public CellFluidHandler(final ItemStack container, final Function<ItemStack, CellType> typeGetter) {
            super(container);
            this.typeGetter = (() -> typeGetter.apply(this.container));
        }
        
        public FluidStack getFluid() {
            final CellType type = this.typeGetter.get();
            assert type.isFluidContainer();
            return (type != null && type.fluid != null) ? new FluidStack(type.fluid, 1000) : null;
        }
        
        public boolean canFillFluidType(final FluidStack fluid) {
            assert fluid != null;
            assert fluid.getFluid() != null;
            return this.typeGetter.get() == CellType.empty && CellFluidHandler.VALID_FLUIDS.containsKey(fluid.getFluid());
        }
        
        protected void setFluid(final FluidStack stack) {
            if (stack == null) {
                assert this.typeGetter.get() != CellType.empty;
                this.container = ItemName.cell.getItemStack(CellType.empty);
            }
            else {
                assert this.typeGetter.get() == CellType.empty;
                assert CellFluidHandler.VALID_FLUIDS.containsKey(stack.getFluid());
                this.container = ItemName.cell.getItemStack((Enum)CellFluidHandler.VALID_FLUIDS.get(stack.getFluid()));
            }
        }
        
        static {
            VALID_FLUIDS = new IdentityHashMap<Fluid, CellType>((Map<? extends Fluid, ? extends CellType>)Arrays.stream(CellType.values()).filter(type -> type.fluid != null).collect(Collectors.toMap(type -> type.fluid, (Function<? super CellType, ?>)Function.identity())));
        }
    }
    
    private static class WeedExHandler implements IFluidHandlerItem
    {
        public static final String NBT = "weedEX";
        public static final int CHARGES = 64;
        private static final int DRAIN = 50;
        protected ItemStack container;
        
        public WeedExHandler(final ItemStack stack) {
            this.container = stack;
        }
        
        public ItemStack getContainer() {
            return this.container;
        }
        
        public int fill(final FluidStack resource, final boolean doFill) {
            return 0;
        }
        
        public FluidStack drain(final FluidStack resource, final boolean doDrain) {
            if (resource == null || resource.getFluid() != FluidName.weed_ex.getInstance()) {
                return null;
            }
            return this.drain(resource.amount, doDrain);
        }
        
        public FluidStack drain(final int maxDrain, final boolean doDrain) {
            if (maxDrain < 50) {
                return null;
            }
            if (doDrain) {
                final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(this.container);
                final int amount = nbt.getInteger("weedEX") + 1;
                if (amount >= 64) {
                    this.container = StackUtil.decSize(this.container);
                }
                else {
                    nbt.setInteger("weedEX", amount);
                }
            }
            return new FluidStack(FluidName.weed_ex.getInstance(), 50);
        }
        
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[] { (IFluidTankProperties)new FluidTankProperties(new FluidStack(FluidName.weed_ex.getInstance(), (64 - this.container.getItemDamage()) * 50), 3200, false, true) };
        }
    }
    
    private static class HydrationHandler implements IFluidHandlerItem
    {
        public static final String NBT = "hydration";
        public static final int CHARGES = 10000;
        protected ItemStack container;
        protected final boolean manual;
        
        public HydrationHandler(final ItemStack stack, final boolean manual) {
            this.container = stack;
            this.manual = manual;
        }
        
        public ItemStack getContainer() {
            return this.container;
        }
        
        public int fill(final FluidStack resource, final boolean doFill) {
            return 0;
        }
        
        public FluidStack drain(final FluidStack resource, final boolean doDrain) {
            if (resource == null || resource.getFluid() != FluidRegistry.WATER) {
                return null;
            }
            return this.drain(resource.amount, doDrain);
        }
        
        public FluidStack drain(final int maxDrain, final boolean doDrain) {
            int remaining;
            if (this.container.hasTagCompound()) {
                remaining = 10000 - this.container.getTagCompound().getInteger("hydration");
            }
            else {
                remaining = 10000;
            }
            int target = Math.min(maxDrain, remaining);
            if (!this.manual && target > 180) {
                target = 180;
            }
            if (doDrain) {
                final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(this.container);
                final int amount = nbt.getInteger("hydration") + target;
                if (amount >= 10000) {
                    this.container = StackUtil.decSize(this.container);
                }
                else {
                    nbt.setInteger("hydration", amount);
                }
            }
            return new FluidStack(FluidRegistry.WATER, target);
        }
        
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[] { (IFluidTankProperties)new FluidTankProperties(new FluidStack(FluidRegistry.WATER, (10000 - this.container.getItemDamage()) / 200), 50, false, true) };
        }
    }
}

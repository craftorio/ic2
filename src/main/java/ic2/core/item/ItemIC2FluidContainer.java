// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.block.state.IIdProvider;
import java.util.LinkedList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import ic2.core.util.StackUtil;
import java.util.Iterator;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Set;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.item.capability.CapabilityFluidHandlerItem;
import javax.annotation.Nullable;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.item.ItemStack;
import com.google.common.base.Function;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import ic2.core.ref.ItemName;
import ic2.api.item.IItemHudInfo;
import ic2.core.ref.FluidName;
import ic2.core.ref.IMultiItem;

public abstract class ItemIC2FluidContainer extends ItemIC2 implements IMultiItem<FluidName>, IItemHudInfo
{
    protected final int capacity;
    
    public ItemIC2FluidContainer(final ItemName name, final int capacity) {
        super(name);
        this.capacity = capacity;
        this.setHasSubtypes(true);
        this.addCapability((net.minecraftforge.common.capabilities.Capability<Object>)CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (com.google.common.base.Function<ItemStack, Object>)new Function<ItemStack, IFluidHandlerItem>() {
            public IFluidHandlerItem apply(@Nullable final ItemStack stack) {
                return (IFluidHandlerItem)new CapabilityFluidHandlerItem(stack, ItemIC2FluidContainer.this.capacity) {
                    public boolean canFillFluidType(final FluidStack fluid) {
                        return fluid != null && ItemIC2FluidContainer.this.canfill(fluid.getFluid());
                    }
                    
                    public boolean canDrainFluidType(final FluidStack fluid) {
                        return fluid != null && ItemIC2FluidContainer.this.canfill(fluid.getFluid());
                    }
                };
            }
        });
    }
    
    @Override
    public ItemStack getItemStack(final FluidName type) {
        return this.getItemStack(type.getInstance());
    }
    
    public ItemStack getItemStack(final Fluid fluid) {
        final ItemStack ret = new ItemStack((Item)this);
        if (fluid == null) {
            return ret;
        }
        final IFluidHandlerItem handler = FluidUtil.getFluidHandler(ret);
        if (handler == null) {
            return null;
        }
        if (handler.fill(new FluidStack(fluid, Integer.MAX_VALUE), true) > 0) {
            return handler.getContainer();
        }
        return null;
    }
    
    @Override
    public ItemStack getItemStack(final String variant) {
        if (variant == null || variant.isEmpty()) {
            return new ItemStack((Item)this);
        }
        final Fluid fluid = FluidRegistry.getFluid(variant);
        if (fluid == null) {
            return null;
        }
        return this.getItemStack(fluid);
    }
    
    @Override
    public String getVariant(final ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null stack");
        }
        if (stack.getItem() != this) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this);
        }
        final FluidStack fs = FluidUtil.getFluidContained(stack);
        if (fs == null || fs.getFluid() == null) {
            return null;
        }
        return fs.getFluid().getName();
    }
    
    @Override
    public Set<FluidName> getAllTypes() {
        return EnumSet.allOf(FluidName.class);
    }
    
    @Override
    public Set<ItemStack> getAllStacks() {
        final Set<ItemStack> ret = new HashSet<ItemStack>();
        ret.add(new ItemStack((Item)this));
        for (final Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            final ItemStack add = this.getItemStack(fluid);
            if (add != null) {
                ret.add(add);
            }
        }
        return ret;
    }
    
    public boolean hasContainerItem(final ItemStack stack) {
        return FluidUtil.getFluidContained(stack) != null;
    }
    
    public ItemStack getContainerItem(final ItemStack stack) {
        if (!this.hasContainerItem(stack)) {
            return super.getContainerItem(stack);
        }
        final ItemStack ret = StackUtil.copyWithSize(stack, 1);
        final IFluidHandlerItem handler = FluidUtil.getFluidHandler(ret);
        handler.drain(Integer.MAX_VALUE, true);
        return handler.getContainer();
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        super.addInformation(stack, world, (List)tooltip, advanced);
        final FluidStack fs = FluidUtil.getFluidContained(stack);
        if (fs != null) {
            tooltip.add("< " + fs.getLocalizedName() + ", " + fs.amount + " mB >");
        }
        else {
            tooltip.add(Localization.translate("ic2.item.FluidContainer.Empty"));
        }
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        final FluidStack fs = FluidUtil.getFluidContained(stack);
        if (fs != null) {
            info.add("< " + FluidRegistry.getFluidName(fs) + ", " + fs.amount + " mB >");
        }
        else {
            info.add(Localization.translate("ic2.item.FluidContainer.Empty"));
        }
        return info;
    }
    
    public abstract boolean canfill(final Fluid p0);
}

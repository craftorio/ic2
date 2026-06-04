// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import java.util.LinkedList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import ic2.core.util.Util;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTBase;
import ic2.core.util.StackUtil;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.item.capability.CapabilityFluidHandlerItem;
import javax.annotation.Nullable;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.item.ItemStack;
import com.google.common.base.Function;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;
import net.minecraftforge.fluids.Fluid;
import ic2.api.item.IItemHudProvider;
import ic2.api.item.IItemHudInfo;

public abstract class ItemArmorFluidTank extends ItemArmorUtility implements IItemHudInfo, IItemHudProvider.IItemHudBarProvider
{
    protected final int capacity;
    protected final Fluid allowfluid;
    
    public ItemArmorFluidTank(final ItemName name, final String armorName, final Fluid allowfluid, final int capacity) {
        super(name, armorName, EntityEquipmentSlot.CHEST);
        this.setMaxDamage(27);
        this.setMaxStackSize(1);
        this.capacity = capacity;
        this.allowfluid = allowfluid;
        this.addCapability((net.minecraftforge.common.capabilities.Capability<Object>)CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (com.google.common.base.Function<ItemStack, Object>)new Function<ItemStack, IFluidHandlerItem>() {
            public IFluidHandlerItem apply(@Nullable final ItemStack stack) {
                return (IFluidHandlerItem)new CapabilityFluidHandlerItem(stack, ItemArmorFluidTank.this.capacity) {
                    public boolean canFillFluidType(final FluidStack fluid) {
                        return fluid != null && fluid.getFluid() == ItemArmorFluidTank.this.allowfluid;
                    }
                    
                    public boolean canDrainFluidType(final FluidStack fluid) {
                        return fluid != null && fluid.getFluid() == ItemArmorFluidTank.this.allowfluid;
                    }
                    
                    public ItemStack getContainer() {
                        final ItemStack ret = super.getContainer();
                        ItemArmorFluidTank.this.Updatedamage(ret);
                        return ret;
                    }
                };
            }
        });
    }
    
    public void filltank(final ItemStack stack) {
        final NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(stack);
        final NBTTagCompound fluidTag = nbtTagCompound.getCompoundTag("Fluid");
        final FluidStack fs = new FluidStack(this.allowfluid, this.capacity);
        fs.writeToNBT(fluidTag);
        nbtTagCompound.setTag("Fluid", (NBTBase)fluidTag);
    }
    
    public double getCharge(final ItemStack stack) {
        final FluidStack fs = FluidUtil.getFluidContained(stack);
        if (fs == null) {
            return 0.0;
        }
        final double ret = fs.amount;
        return (ret > 0.0) ? ret : 0.0;
    }
    
    public double getMaxCharge(final ItemStack stack) {
        return this.capacity;
    }
    
    protected void Updatedamage(final ItemStack stack) {
        stack.setItemDamage(stack.getMaxDamage() - 1 - (int)Util.map(this.getCharge(stack), this.getMaxCharge(stack), stack.getMaxDamage() - 2));
    }
    
    public boolean isEmpty(final ItemStack stack) {
        return FluidUtil.getFluidContained(stack) == null;
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
    public int getBarPercent(final ItemStack stack) {
        return this.getMaxDamage(stack) - this.getDamage(stack) * 100 / this.getMaxDamage(stack);
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        final FluidStack fs = FluidUtil.getFluidContained(stack);
        if (fs != null) {
            info.add("< " + fs.getLocalizedName() + ", " + fs.amount + " mB >");
        }
        else {
            info.add(Localization.translate("ic2.item.FluidContainer.Empty"));
        }
        return info;
    }
    
    @Override
    public boolean getIsRepairable(final ItemStack par1ItemStack, final ItemStack par2ItemStack) {
        return false;
    }
}

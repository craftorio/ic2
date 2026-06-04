// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import ic2.core.util.Util;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.item.armor.jetpack.IJetpack;

public class ItemArmorJetpack extends ItemArmorFluidTank implements IJetpack
{
    public ItemArmorJetpack() {
        super(ItemName.jetpack, "jetpack", FluidName.biogas.getInstance(), 30000);
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        ItemStack stack = new ItemStack((Item)this, 1);
        this.filltank(stack);
        stack.setItemDamage(1);
        subItems.add((Object)stack);
        stack = new ItemStack((Item)this, 1);
        stack.setItemDamage(this.getMaxDamage(stack));
        subItems.add((Object)stack);
    }
    
    @Override
    public boolean drainEnergy(final ItemStack pack, final int amount) {
        if (this.isEmpty(pack)) {
            return false;
        }
        final IFluidHandlerItem handler = FluidUtil.getFluidHandler(pack);
        assert handler != null;
        final FluidStack drained = handler.drain(amount, false);
        if (drained == null || drained.amount < amount) {
            return false;
        }
        handler.drain(amount, true);
        this.Updatedamage(pack);
        return true;
    }
    
    @Override
    public float getPower(final ItemStack stack) {
        return 1.0f;
    }
    
    @Override
    public float getDropPercentage(final ItemStack stack) {
        return 0.2f;
    }
    
    @Override
    public boolean isJetpackActive(final ItemStack stack) {
        return true;
    }
    
    @Override
    public double getChargeLevel(final ItemStack stack) {
        return this.getCharge(stack) / this.getMaxCharge(stack);
    }
    
    @Override
    public float getHoverMultiplier(final ItemStack stack, final boolean upwards) {
        return 0.2f;
    }
    
    @Override
    public float getWorldHeightDivisor(final ItemStack stack) {
        return 1.0f;
    }
    
    @Override
    public int getBarPercent(final ItemStack stack) {
        return (int)Util.map(this.getCharge(stack), this.getMaxCharge(stack), 100.0);
    }
}

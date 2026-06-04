// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.item.Item;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import ic2.api.item.ElectricItem;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IElectricItem;

public abstract class BaseElectricItem extends ItemIC2 implements IPseudoDamageItem, IElectricItem, IItemHudInfo
{
    public static final boolean logIncorrectItemDamaging;
    protected final double maxCharge;
    protected final double transferLimit;
    protected final int tier;
    
    public BaseElectricItem(final ItemName name, final double maxCharge, final double transferLimit, final int tier) {
        super(name);
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.tier = tier;
        this.setMaxDamage(27);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }
    
    @Override
    public boolean canProvideEnergy(final ItemStack stack) {
        return false;
    }
    
    @Override
    public double getMaxCharge(final ItemStack stack) {
        return this.maxCharge;
    }
    
    @Override
    public int getTier(final ItemStack stack) {
        return this.tier;
    }
    
    @Override
    public double getTransferLimit(final ItemStack stack) {
        return this.transferLimit;
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        info.add(ElectricItem.manager.getToolTip(stack));
        return info;
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        ElectricItemManager.addChargeVariants(this, (List<ItemStack>)subItems);
    }
    
    public void setDamage(final ItemStack stack, final int damage) {
        final int prev = this.getDamage(stack);
        if (damage != prev && BaseElectricItem.logIncorrectItemDamaging) {
            IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", damage - prev);
        }
    }
    
    @Override
    public void setStackDamage(final ItemStack stack, final int damage) {
        super.setDamage(stack, damage);
    }
    
    static {
        logIncorrectItemDamaging = ConfigUtil.getBool(MainConfig.get(), "debug/logIncorrectItemDamaging");
    }
}

// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.item.type.DustResourceType;
import net.minecraft.init.Items;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.api.info.IInfoProvider;

public class ItemInfo implements IInfoProvider
{
    @Override
    public double getEnergyValue(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return 0.0;
        }
        if (StackUtil.checkItemEquality(stack, ItemName.single_use_battery.getItemStack())) {
            return 1200.0;
        }
        if (StackUtil.checkItemEquality(stack, Items.REDSTONE)) {
            return 800.0;
        }
        if (StackUtil.checkItemEquality(stack, ItemName.dust.getItemStack(DustResourceType.energium))) {
            return 16000.0;
        }
        return 0.0;
    }
    
    @Override
    public int getFuelValue(final ItemStack stack, final boolean allowLava) {
        if (StackUtil.isEmpty(stack)) {
            return 0;
        }
        if ((StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap)) || StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) && !ConfigUtil.getBool(MainConfig.get(), "misc/allowBurningScrap")) {
            return 0;
        }
        final FluidStack liquid = FluidUtil.getFluidContained(stack);
        final boolean isLava = liquid != null && liquid.amount > 0 && liquid.getFluid() == FluidRegistry.LAVA;
        if (isLava && !allowLava) {
            return 0;
        }
        final int ret = TileEntityFurnace.getItemBurnTime(stack);
        return isLava ? (ret / 10) : ret;
    }
}

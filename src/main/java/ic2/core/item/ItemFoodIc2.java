// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.init.Localization;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import ic2.core.init.BlocksItems;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import ic2.core.ref.ItemName;
import ic2.core.ref.IItemModelProvider;
import net.minecraft.item.ItemFood;

public class ItemFoodIc2 extends ItemFood implements IItemModelProvider
{
    protected ItemFoodIc2(final ItemName name, final int amount, final float saturation, final boolean isWolfFood) {
        super(amount, saturation, isWolfFood);
        this.setUnlocalizedName(name.name());
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
        BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
        name.setInstance(this);
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final ItemName name) {
        ItemIC2.registerModel((Item)this, 0, name, null);
    }
    
    public String getUnlocalizedName() {
        return "ic2." + super.getUnlocalizedName().substring(5);
    }
    
    public String getUnlocalizedName(final ItemStack stack) {
        return this.getUnlocalizedName();
    }
    
    public String getUnlocalizedNameInefficiently(final ItemStack stack) {
        return this.getUnlocalizedName(stack);
    }
    
    public String getItemStackDisplayName(final ItemStack stack) {
        return Localization.translate(this.getUnlocalizedName(stack));
    }
}

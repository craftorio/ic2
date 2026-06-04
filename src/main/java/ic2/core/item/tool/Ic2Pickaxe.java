// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.util.Util;
import ic2.core.init.Localization;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.item.ItemIC2;
import ic2.core.init.BlocksItems;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import ic2.core.ref.ItemName;
import net.minecraft.item.Item;
import ic2.core.ref.IItemModelProvider;
import net.minecraft.item.ItemPickaxe;

public class Ic2Pickaxe extends ItemPickaxe implements IItemModelProvider
{
    private final Object repairMaterial;
    
    public Ic2Pickaxe(final Item.ToolMaterial material) {
        super(material);
        this.efficiency = 5.0f;
        this.repairMaterial = "ingotBronze";
        this.setUnlocalizedName(ItemName.bronze_pickaxe.name());
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
        BlocksItems.registerItem(this, IC2.getIdentifier(ItemName.bronze_pickaxe.name()));
        ItemName.bronze_pickaxe.setInstance(this);
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
    
    public boolean getIsRepairable(final ItemStack stack1, final ItemStack stack2) {
        return stack2 != null && Util.matchesOD(stack2, this.repairMaterial);
    }
}

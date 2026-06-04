package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.Util;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Ic2Axe extends ItemAxe implements IItemModelProvider {
  private final Object repairMaterial;
  
  public Ic2Axe(Item.ToolMaterial material) {
    super(material, 8.0F, -3.1F);
    this.efficiency = 5.0F;
    this.repairMaterial = "ingotBronze";
    setUnlocalizedName(ItemName.bronze_axe.name());
    setCreativeTab((CreativeTabs)IC2.tabIC2);
    BlocksItems.registerItem((Item)this, IC2.getIdentifier(ItemName.bronze_axe.name()));
    ItemName.bronze_axe.setInstance((Item)this);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(ItemName name) {
    ItemIC2.registerModel((Item)this, 0, name, null);
  }
  
  public String getUnlocalizedName() {
    return "ic2." + super.getUnlocalizedName().substring(5);
  }
  
  public String getUnlocalizedName(ItemStack stack) {
    return getUnlocalizedName();
  }
  
  public String getUnlocalizedNameInefficiently(ItemStack stack) {
    return getUnlocalizedName(stack);
  }
  
  public String getItemStackDisplayName(ItemStack stack) {
    return Localization.translate(getUnlocalizedName(stack));
  }
  
  public boolean getIsRepairable(ItemStack stack1, ItemStack stack2) {
    return (stack2 != null && Util.matchesOD(stack2, this.repairMaterial));
  }
}

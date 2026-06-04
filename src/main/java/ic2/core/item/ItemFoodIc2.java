package ic2.core.item;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFoodIc2 extends ItemFood implements IItemModelProvider {
  protected ItemFoodIc2(ItemName name, int amount, float saturation, boolean isWolfFood) {
    super(amount, saturation, isWolfFood);
    setUnlocalizedName(name.name());
    setCreativeTab((CreativeTabs)IC2.tabIC2);
    BlocksItems.registerItem((Item)this, IC2.getIdentifier(name.name()));
    name.setInstance((Item)this);
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
}

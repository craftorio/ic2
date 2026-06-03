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
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Ic2Shovel extends ItemSpade implements IItemModelProvider {
  private final Object repairMaterial;
  
  public Ic2Shovel(Item.ToolMaterial material) {
    super(material);
    this.field_77864_a = 5.0F;
    this.repairMaterial = "ingotBronze";
    func_77655_b(ItemName.bronze_shovel.name());
    func_77637_a((CreativeTabs)IC2.tabIC2);
    BlocksItems.registerItem((Item)this, IC2.getIdentifier(ItemName.bronze_shovel.name()));
    ItemName.bronze_shovel.setInstance((Item)this);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(ItemName name) {
    ItemIC2.registerModel((Item)this, 0, name, null);
  }
  
  public String func_77658_a() {
    return "ic2." + super.func_77658_a().substring(5);
  }
  
  public String func_77667_c(ItemStack stack) {
    return func_77658_a();
  }
  
  public String func_77657_g(ItemStack stack) {
    return func_77667_c(stack);
  }
  
  public String func_77653_i(ItemStack stack) {
    return Localization.translate(func_77667_c(stack));
  }
  
  public boolean func_82789_a(ItemStack stack1, ItemStack stack2) {
    return (stack2 != null && Util.matchesOD(stack2, this.repairMaterial));
  }
}

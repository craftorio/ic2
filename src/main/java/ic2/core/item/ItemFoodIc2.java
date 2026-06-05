package ic2.core.item;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFoodIc2 extends ItemFood implements IItemModelProvider {
   protected ItemFoodIc2(ItemName name, int amount, float saturation, boolean isWolfFood) {
      super(amount, saturation, isWolfFood);
      this.setUnlocalizedName(name.name());
      this.setCreativeTab(IC2.tabIC2);
      BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
      name.setInstance(this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerModels(ItemName name) {
      ItemIC2.registerModel(this, 0, name, null);
   }

   public String getUnlocalizedName() {
      return "ic2." + super.getUnlocalizedName().substring(5);
   }

   public String getUnlocalizedName(ItemStack stack) {
      return this.getUnlocalizedName();
   }

   public String getUnlocalizedNameInefficiently(ItemStack stack) {
      return this.getUnlocalizedName(stack);
   }

   public String getItemStackDisplayName(ItemStack stack) {
      return Localization.translate(this.getUnlocalizedName(stack));
   }
}

package ic2.core.item.armor;

import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.Util;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemArmorJetpack extends ItemArmorFluidTank implements IJetpack {
   public ItemArmorJetpack() {
      super(ItemName.jetpack, "jetpack", FluidName.biogas.getInstance(), 30000);
   }

   public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
      if (this.isInCreativeTab(tab)) {
         ItemStack stack = new ItemStack(this, 1);
         this.filltank(stack);
         stack.setItemDamage(1);
         subItems.add(stack);
         stack = new ItemStack(this, 1);
         stack.setItemDamage(this.getMaxDamage(stack));
         subItems.add(stack);
      }
   }

   @Override
   public boolean drainEnergy(ItemStack pack, int amount) {
      if (this.isEmpty(pack)) {
         return false;
      } else {
         IFluidHandlerItem handler = FluidUtil.getFluidHandler(pack);
         assert handler != null;
         FluidStack drained = handler.drain(amount, false);
         if (drained != null && drained.amount >= amount) {
            handler.drain(amount, true);
            this.Updatedamage(pack);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public float getPower(ItemStack stack) {
      return 1.0F;
   }

   @Override
   public float getDropPercentage(ItemStack stack) {
      return 0.2F;
   }

   @Override
   public boolean isJetpackActive(ItemStack stack) {
      return true;
   }

   @Override
   public double getChargeLevel(ItemStack stack) {
      return this.getCharge(stack) / this.getMaxCharge(stack);
   }

   @Override
   public float getHoverMultiplier(ItemStack stack, boolean upwards) {
      return 0.2F;
   }

   @Override
   public float getWorldHeightDivisor(ItemStack stack) {
      return 1.0F;
   }

   @Override
   public int getBarPercent(ItemStack stack) {
      return (int)Util.map(this.getCharge(stack), this.getMaxCharge(stack), 100.0);
   }
}

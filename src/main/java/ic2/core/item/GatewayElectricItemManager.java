package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GatewayElectricItemManager implements IElectricItemManager {
   @Override
   public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
      if (StackUtil.isEmpty(stack)) {
         return 0.0;
      }

      IElectricItemManager manager = this.getManager(stack);
      return manager == null ? 0.0 : manager.charge(stack, amount, tier, ignoreTransferLimit, simulate);
   }

   @Override
   public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
      if (StackUtil.isEmpty(stack)) {
         return 0.0;
      }

      IElectricItemManager manager = this.getManager(stack);
      return manager == null ? 0.0 : manager.discharge(stack, amount, tier, ignoreTransferLimit, externally, simulate);
   }

   @Override
   public double getCharge(ItemStack stack) {
      if (StackUtil.isEmpty(stack)) {
         return 0.0;
      }

      IElectricItemManager manager = this.getManager(stack);
      return manager == null ? 0.0 : manager.getCharge(stack);
   }

   @Override
   public double getMaxCharge(ItemStack stack) {
      if (StackUtil.isEmpty(stack)) {
         return 0.0;
      }

      IElectricItemManager manager = this.getManager(stack);
      return manager == null ? 0.0 : manager.getMaxCharge(stack);
   }

   @Override
   public boolean canUse(ItemStack stack, double amount) {
      if (StackUtil.isEmpty(stack)) {
         return false;
      }

      IElectricItemManager manager = this.getManager(stack);
      return manager == null ? false : manager.canUse(stack, amount);
   }

   @Override
   public boolean use(ItemStack stack, double amount, EntityLivingBase entity) {
      if (StackUtil.isEmpty(stack)) {
         return false;
      }

      if (entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode) {
         return this.canUse(stack, amount);
      }

      IElectricItemManager manager = this.getManager(stack);
      return manager == null ? false : manager.use(stack, amount, entity);
   }

   @Override
   public void chargeFromArmor(ItemStack stack, EntityLivingBase entity) {
      if (!StackUtil.isEmpty(stack)) {
         if (entity != null) {
            IElectricItemManager manager = this.getManager(stack);
            if (manager != null) {
               manager.chargeFromArmor(stack, entity);
            }
         }
      }
   }

   @Override
   public String getToolTip(ItemStack stack) {
      if (StackUtil.isEmpty(stack)) {
         return null;
      }

      IElectricItemManager manager = this.getManager(stack);
      return manager == null ? null : manager.getToolTip(stack);
   }

   private IElectricItemManager getManager(ItemStack stack) {
      Item item = stack.getItem();
      if (item == null) {
         return null;
      } else if (item instanceof ISpecialElectricItem) {
         return ((ISpecialElectricItem)item).getManager(stack);
      } else {
         return item instanceof IElectricItem ? ElectricItem.rawManager : ElectricItem.getBackupManager(stack);
      }
   }

   @Override
   public int getTier(ItemStack stack) {
      if (StackUtil.isEmpty(stack)) {
         return 0;
      }

      IElectricItemManager manager = this.getManager(stack);
      return manager == null ? 0 : manager.getTier(stack);
   }
}

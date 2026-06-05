package ic2.core.item;

import ic2.core.IC2;
import ic2.core.IC2Potion;
import ic2.core.ref.ItemName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTerraWart extends ItemFoodIc2 {
   public ItemTerraWart() {
      super(ItemName.terra_wart, 0, 1.0F, false);
      this.setAlwaysEdible();
   }

   public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase player) {
      IC2.platform.removePotion(player, MobEffects.NAUSEA);
      IC2.platform.removePotion(player, MobEffects.MINING_FATIGUE);
      IC2.platform.removePotion(player, MobEffects.HUNGER);
      IC2.platform.removePotion(player, MobEffects.SLOWNESS);
      IC2.platform.removePotion(player, MobEffects.WEAKNESS);
      IC2.platform.removePotion(player, MobEffects.BLINDNESS);
      IC2.platform.removePotion(player, MobEffects.POISON);
      IC2.platform.removePotion(player, MobEffects.WITHER);
      PotionEffect effect = player.getActivePotionEffect(IC2Potion.radiation);
      if (effect != null) {
         if (effect.getDuration() <= 600) {
            IC2.platform.removePotion(player, IC2Potion.radiation);
         } else {
            IC2.platform.removePotion(player, IC2Potion.radiation);
            IC2Potion.radiation.applyTo(player, effect.getDuration() - 600, effect.getAmplifier());
         }
      }

      return super.onItemUseFinish(stack, world, player);
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity getRarity(ItemStack stack) {
      return EnumRarity.RARE;
   }
}

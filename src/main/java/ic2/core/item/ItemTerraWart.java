package ic2.core.item;

import ic2.core.IC2;
import ic2.core.IC2Potion;
import ic2.core.ref.ItemName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemTerraWart extends ItemFoodIc2 {
  public ItemTerraWart() {
    super(ItemName.terra_wart, 0, 1.0F, false);
    func_77848_i();
  }
  
  public ItemStack func_77654_b(ItemStack stack, World world, EntityLivingBase player) {
    IC2.platform.removePotion(player, MobEffects.field_76431_k);
    IC2.platform.removePotion(player, MobEffects.field_76419_f);
    IC2.platform.removePotion(player, MobEffects.field_76438_s);
    IC2.platform.removePotion(player, MobEffects.field_76421_d);
    IC2.platform.removePotion(player, MobEffects.field_76437_t);
    IC2.platform.removePotion(player, MobEffects.field_76440_q);
    IC2.platform.removePotion(player, MobEffects.field_76436_u);
    IC2.platform.removePotion(player, MobEffects.field_82731_v);
    PotionEffect effect = player.func_70660_b((Potion)IC2Potion.radiation);
    if (effect != null)
      if (effect.func_76459_b() <= 600) {
        IC2.platform.removePotion(player, (Potion)IC2Potion.radiation);
      } else {
        IC2.platform.removePotion(player, (Potion)IC2Potion.radiation);
        IC2Potion.radiation.applyTo(player, effect.func_76459_b() - 600, effect.func_76458_c());
      }  
    return super.func_77654_b(stack, world, player);
  }
  
  @SideOnly(Side.CLIENT)
  public EnumRarity func_77613_e(ItemStack stack) {
    return EnumRarity.RARE;
  }
}

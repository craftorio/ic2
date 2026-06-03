package ic2.core;

import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class IC2Potion extends Potion {
  public static IC2Potion radiation;
  
  private final List<ItemStack> curativeItems;
  
  public static void init() {
    radiation.func_76390_b("ic2.potion.radiation");
    radiation.func_76399_b(6, 0);
    radiation.func_76404_a(0.25D);
  }
  
  public IC2Potion(String name, boolean badEffect, int liquidColor, ItemStack... curativeItems) {
    super(badEffect, liquidColor);
    this.curativeItems = Arrays.asList(curativeItems);
    ForgeRegistries.POTIONS.register(setRegistryName(name));
  }
  
  public void func_76394_a(EntityLivingBase entity, int amplifier) {
    if (this == radiation)
      entity.func_70097_a(IC2DamageSource.radiation, (amplifier / 100) + 0.5F); 
  }
  
  public boolean func_76397_a(int duration, int amplifier) {
    if (this == radiation) {
      int rate = 25 >> amplifier;
      return (rate > 0) ? ((duration % rate == 0)) : true;
    } 
    return false;
  }
  
  public void applyTo(EntityLivingBase entity, int duration, int amplifier) {
    PotionEffect effect = new PotionEffect(radiation, duration, amplifier);
    effect.setCurativeItems(this.curativeItems);
    entity.func_70690_d(effect);
  }
}

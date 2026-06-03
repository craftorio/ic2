package ic2.core.item.armor;

import ic2.api.item.IHazmatLike;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.slot.ArmorSlot;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemArmorHazmat extends ItemArmorUtility implements IHazmatLike {
  public ItemArmorHazmat(ItemName name, EntityEquipmentSlot type) {
    super(name, "hazmat", type);
    func_77656_e(64);
    if (this.field_77881_a == EntityEquipmentSlot.FEET)
      MinecraftForge.EVENT_BUS.register(this); 
  }
  
  public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
    if (this.field_77881_a == EntityEquipmentSlot.HEAD && hazmatAbsorbs(source) && hasCompleteHazmat(player)) {
      if (source == DamageSource.field_76372_a || source == DamageSource.field_76371_c || source == DamageSource.field_190095_e)
        player.func_70690_d(new PotionEffect(MobEffects.field_76426_n, 60, 1)); 
      return new ISpecialArmor.ArmorProperties(10, 1.0D, 2147483647);
    } 
    if (this.field_77881_a == EntityEquipmentSlot.FEET && source == DamageSource.field_76379_h)
      return new ISpecialArmor.ArmorProperties(10, (damage < 8.0D) ? 1.0D : 0.875D, (armor.func_77958_k() - armor.func_77952_i() + 2) * 2 * 25); 
    return new ISpecialArmor.ArmorProperties(0, 0.05D, (armor.func_77958_k() - armor.func_77952_i() + 2) / 2 * 25);
  }
  
  public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
    if (hazmatAbsorbs(source) && hasCompleteHazmat(entity))
      return; 
    int damageTotal = damage * 2;
    if (this.field_77881_a == EntityEquipmentSlot.FEET && source == DamageSource.field_76379_h)
      damageTotal = (damage + 1) / 2; 
    stack.func_77972_a(damageTotal, entity);
  }
  
  @SubscribeEvent(priority = EventPriority.LOW)
  public void onEntityLivingFallEvent(LivingFallEvent event) {
    if (IC2.platform.isSimulating() && event.getEntity() instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer)event.getEntity();
      ItemStack armor = (ItemStack)player.field_71071_by.field_70460_b.get(0);
      if (armor != null && armor.func_77973_b() == this) {
        int fallDamage = (int)event.getDistance() - 3;
        if (fallDamage >= 8)
          return; 
        int armorDamage = (fallDamage + 1) / 2;
        if (armorDamage <= armor.func_77958_k() - armor.func_77952_i() && armorDamage >= 0) {
          armor.func_77972_a(armorDamage, (EntityLivingBase)player);
          event.setCanceled(true);
        } 
      } 
    } 
  }
  
  public boolean isRepairable() {
    return true;
  }
  
  public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
    return 1;
  }
  
  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
    if (!world.field_72995_K && this.field_77881_a == EntityEquipmentSlot.HEAD) {
      if (player.func_70027_ad() && hasCompleteHazmat((EntityLivingBase)player)) {
        if (isInLava(player))
          player.func_70690_d(new PotionEffect(MobEffects.field_76426_n, 20, 0, true, true)); 
        player.func_70066_B();
      } 
      int maxAir = 300;
      int refillThreshold = 100;
      int airToMbMul = 1000;
      int airToMbDiv = 150;
      int minAmount = 7;
      int air = player.func_70086_ai();
      if (air <= 100) {
        int needed = (300 - air) * 1000 / 150;
        int supplied = 0;
        for (int i = 0; i < player.field_71071_by.field_70462_a.size() && needed > 0; i++) {
          ItemStack cStack = (ItemStack)player.field_71071_by.field_70462_a.get(i);
          if (cStack != null) {
            LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(cStack, FluidName.air.getInstance(), needed, FluidContainerOutputMode.InPlacePreferred);
            if (result != null && result.fluidChange.amount >= 7)
              if (result.extraOutput == null || 
                StackUtil.storeInventoryItem(result.extraOutput, player, false)) {
                player.field_71071_by.field_70462_a.set(i, result.inPlaceOutput);
                int amount = result.fluidChange.amount;
                supplied += amount;
                needed -= amount;
              }  
          } 
        } 
        player.func_70050_g(air + supplied * 150 / 1000);
      } 
    } 
  }
  
  public boolean isInLava(EntityPlayer player) {
    int x = (int)Math.floor(player.field_70165_t);
    int y = (int)Math.floor(player.field_70163_u + 0.02D);
    int z = (int)Math.floor(player.field_70161_v);
    IBlockState state = player.func_130014_f_().func_180495_p(new BlockPos(x, y, z));
    if (state.func_177230_c() instanceof BlockLiquid && (state.func_185904_a() == Material.field_151587_i || state.func_185904_a() == Material.field_151581_o)) {
      float height = (y + 1) - BlockLiquid.func_149801_b(((Integer)state.func_177229_b((IProperty)BlockLiquid.field_176367_b)).intValue());
      return (player.field_70163_u < height);
    } 
    return false;
  }
  
  public boolean addsProtection(EntityLivingBase entity, EntityEquipmentSlot slot, ItemStack stack) {
    return true;
  }
  
  public static boolean hasCompleteHazmat(EntityLivingBase living) {
    for (EntityEquipmentSlot slot : ArmorSlot.getAll()) {
      ItemStack stack = living.func_184582_a(slot);
      if (stack == null || !(stack.func_77973_b() instanceof IHazmatLike))
        return false; 
      IHazmatLike hazmat = (IHazmatLike)stack.func_77973_b();
      if (!hazmat.addsProtection(living, slot, stack))
        return false; 
      if (hazmat.fullyProtects(living, slot, stack))
        return true; 
    } 
    return true;
  }
  
  public static boolean hazmatAbsorbs(DamageSource source) {
    return (source == DamageSource.field_76372_a || source == DamageSource.field_76368_d || source == DamageSource.field_76371_c || source == DamageSource.field_190095_e || source == DamageSource.field_76370_b || source == IC2DamageSource.electricity || source == IC2DamageSource.radiation);
  }
  
  public boolean isMetalArmor(ItemStack itemstack, EntityPlayer player) {
    return false;
  }
}

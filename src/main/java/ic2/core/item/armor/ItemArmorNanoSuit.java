package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IItemHudProvider;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmorNanoSuit extends ItemArmorElectric implements IItemHudProvider {
  public ItemArmorNanoSuit(ItemName name, EntityEquipmentSlot armorType) {
    super(name, "nano", armorType, 1000000.0D, 1600.0D, 3);
    if (armorType == EntityEquipmentSlot.FEET)
      MinecraftForge.EVENT_BUS.register(this); 
  }
  
  public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
    if (source == DamageSource.field_76379_h && this.field_77881_a == EntityEquipmentSlot.FEET) {
      int energyPerDamage = getEnergyPerDamage();
      int damageLimit = Integer.MAX_VALUE;
      if (energyPerDamage > 0)
        damageLimit = (int)Math.min(damageLimit, 25.0D * ElectricItem.manager.getCharge(armor) / energyPerDamage); 
      return new ISpecialArmor.ArmorProperties(10, (damage < 8.0D) ? 1.0D : 0.875D, damageLimit);
    } 
    return super.getProperties(player, armor, source, damage, slot);
  }
  
  @SubscribeEvent(priority = EventPriority.LOW)
  public void onEntityLivingFallEvent(LivingFallEvent event) {
    if (IC2.platform.isSimulating() && event.getEntity() instanceof EntityLivingBase) {
      EntityLivingBase entity = (EntityLivingBase)event.getEntity();
      ItemStack armor = entity.func_184582_a(EntityEquipmentSlot.FEET);
      if (armor != null && armor.getItem() == this) {
        int fallDamage = (int)event.getDistance() - 3;
        if (fallDamage >= 8)
          return; 
        double energyCost = (getEnergyPerDamage() * fallDamage);
        if (energyCost <= ElectricItem.manager.getCharge(armor)) {
          ElectricItem.manager.discharge(armor, energyCost, 2147483647, true, false, false);
          event.setCanceled(true);
        } 
      } 
    } 
  }
  
  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
    byte toggleTimer = nbtData.func_74771_c("toggleTimer");
    boolean ret = false;
    if (this.field_77881_a == EntityEquipmentSlot.HEAD) {
      IC2.platform.profilerStartSection("NanoHelmet");
      boolean Nightvision = nbtData.func_74767_n("Nightvision");
      short hubmode = nbtData.func_74765_d("HudMode");
      if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
        toggleTimer = 10;
        Nightvision = !Nightvision;
        if (IC2.platform.isSimulating()) {
          nbtData.func_74757_a("Nightvision", Nightvision);
          if (Nightvision) {
            IC2.platform.messagePlayer(player, "Nightvision enabled.", new Object[0]);
          } else {
            IC2.platform.messagePlayer(player, "Nightvision disabled.", new Object[0]);
          } 
        } 
      } 
      if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isHudModeKeyDown(player) && toggleTimer == 0) {
        toggleTimer = 10;
        if (hubmode == HudMode.getMaxMode()) {
          hubmode = 0;
        } else {
          hubmode = (short)(hubmode + 1);
        } 
        if (IC2.platform.isSimulating()) {
          nbtData.func_74777_a("HudMode", hubmode);
          IC2.platform.messagePlayer(player, Localization.translate(HudMode.getFromID(hubmode).getTranslationKey()), new Object[0]);
        } 
      } 
      if (IC2.platform.isSimulating() && toggleTimer > 0) {
        toggleTimer = (byte)(toggleTimer - 1);
        nbtData.func_74774_a("toggleTimer", toggleTimer);
      } 
      if (Nightvision && IC2.platform.isSimulating() && 
        ElectricItem.manager.use(stack, 1.0D, (EntityLivingBase)player)) {
        BlockPos pos = new BlockPos((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
        int skylight = player.getEntityWorld().func_175671_l(pos);
        if (skylight > 8) {
          IC2.platform.removePotion((EntityLivingBase)player, MobEffects.field_76439_r);
          player.func_70690_d(new PotionEffect(MobEffects.field_76440_q, 100, 0, true, true));
        } else {
          IC2.platform.removePotion((EntityLivingBase)player, MobEffects.field_76440_q);
          player.func_70690_d(new PotionEffect(MobEffects.field_76439_r, 300, 0, true, true));
        } 
        ret = true;
      } 
      IC2.platform.profilerEndSection();
    } 
    if (ret)
      player.field_71069_bz.func_75142_b(); 
  }
  
  public double getDamageAbsorptionRatio() {
    return 0.9D;
  }
  
  public int getEnergyPerDamage() {
    return 5000;
  }
  
  @SideOnly(Side.CLIENT)
  public EnumRarity func_77613_e(ItemStack stack) {
    return EnumRarity.UNCOMMON;
  }
  
  public boolean doesProvideHUD(ItemStack stack) {
    return (this.field_77881_a == EntityEquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0.0D);
  }
  
  public HudMode getHudMode(ItemStack stack) {
    return HudMode.getFromID(StackUtil.getOrCreateNbtData(stack).func_74765_d("HudMode"));
  }
}

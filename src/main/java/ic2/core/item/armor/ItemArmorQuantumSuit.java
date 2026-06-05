package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.HudMode;
import ic2.api.item.IHazmatLike;
import ic2.api.item.IItemHudProvider;
import ic2.core.IC2;
import ic2.core.IC2Potion;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.item.ItemTinCan;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmorQuantumSuit extends ItemArmorElectric implements IJetpack, IHazmatLike, IItemHudProvider {
   private static final int defaultColor = -1;
   protected static final Map<Potion, Integer> potionRemovalCost = new IdentityHashMap<>();
   private float jumpCharge;

   public ItemArmorQuantumSuit(ItemName name, EntityEquipmentSlot armorType) {
      super(name, "quantum", armorType, 1.0E7, 12000.0, 4);
      if (armorType == EntityEquipmentSlot.FEET) {
         MinecraftForge.EVENT_BUS.register(this);
      }

      potionRemovalCost.put(MobEffects.POISON, 10000);
      potionRemovalCost.put(IC2Potion.radiation, 10000);
      potionRemovalCost.put(MobEffects.WITHER, 25000);
   }

   @Override
   protected boolean hasOverlayTexture() {
      return true;
   }

   public boolean hasColor(ItemStack stack) {
      return this.getColor(stack) != -1;
   }

   public void removeColor(ItemStack stack) {
      NBTTagCompound nbt = this.getDisplayNbt(stack, false);
      if (nbt != null && nbt.hasKey("color", 3)) {
         nbt.removeTag("color");
         if (nbt.hasNoTags()) {
            stack.getTagCompound().removeTag("display");
         }
      }
   }

   public int getColor(ItemStack stack) {
      NBTTagCompound nbt = this.getDisplayNbt(stack, false);
      return nbt != null && nbt.hasKey("color", 3) ? nbt.getInteger("color") : -1;
   }

   public void setColor(ItemStack stack, int color) {
      NBTTagCompound nbt = this.getDisplayNbt(stack, true);
      nbt.setInteger("color", color);
   }

   private NBTTagCompound getDisplayNbt(ItemStack stack, boolean create) {
      NBTTagCompound nbt = stack.getTagCompound();
      if (nbt == null) {
         if (!create) {
            return null;
         }

         nbt = new NBTTagCompound();
         stack.setTagCompound(nbt);
      }

      NBTTagCompound ret;
      if (!nbt.hasKey("display", 10)) {
         if (!create) {
            return null;
         }

         ret = new NBTTagCompound();
         nbt.setTag("display", ret);
      } else {
         ret = nbt.getCompoundTag("display");
      }

      return ret;
   }

   @Override
   public boolean addsProtection(EntityLivingBase entity, EntityEquipmentSlot slot, ItemStack stack) {
      return ElectricItem.manager.getCharge(stack) > 0.0;
   }

   @Override
   public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase entity, ItemStack armor, DamageSource source, double damage, int slot) {
      int energyPerDamage = this.getEnergyPerDamage();
      int damageLimit = Integer.MAX_VALUE;
      if (energyPerDamage > 0) {
         damageLimit = (int)Math.min(damageLimit, 25.0 * ElectricItem.manager.getCharge(armor) / energyPerDamage);
      }

      if (source == DamageSource.FALL) {
         if (this.armorType == EntityEquipmentSlot.FEET) {
            return new ISpecialArmor.ArmorProperties(10, 1.0, damageLimit);
         }

         if (this.armorType == EntityEquipmentSlot.LEGS) {
            return new ISpecialArmor.ArmorProperties(9, 0.8, damageLimit);
         }
      }

      double absorptionRatio = this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio();
      return new ISpecialArmor.ArmorProperties(8, absorptionRatio, damageLimit);
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public void onEntityLivingFallEvent(LivingFallEvent event) {
      if (IC2.platform.isSimulating() && event.getEntity() instanceof EntityLivingBase) {
         EntityLivingBase entity = (EntityLivingBase)event.getEntity();
         ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
         if (armor != null && armor.getItem() == this) {
            int fallDamage = Math.max((int)event.getDistance() - 10, 0);
            double energyCost = this.getEnergyPerDamage() * fallDamage;
            if (energyCost <= ElectricItem.manager.getCharge(armor)) {
               ElectricItem.manager.discharge(armor, energyCost, Integer.MAX_VALUE, true, false, false);
               event.setCanceled(true);
            }
         }
      }
   }

   @Override
   public double getDamageAbsorptionRatio() {
      return this.armorType == EntityEquipmentSlot.CHEST ? 1.2 : 1.0;
   }

   @Override
   public int getEnergyPerDamage() {
      return 20000;
   }

   @SideOnly(Side.CLIENT)
   public EnumRarity getRarity(ItemStack stack) {
      return EnumRarity.RARE;
   }

   public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
      NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
      byte toggleTimer = nbtData.getByte("toggleTimer");
      boolean ret = false;
      switch (this.armorType) {
         case HEAD:
            IC2.platform.profilerStartSection("QuantumHelmet");
            int air = player.getAir();
            if (ElectricItem.manager.canUse(stack, 1000.0) && air < 100) {
               player.setAir(air + 200);
               ElectricItem.manager.use(stack, 1000.0, null);
               ret = true;
            } else if (air <= 0) {
               IC2.achievements.issueAchievement(player, "starveWithQHelmet");
            }

            if (ElectricItem.manager.canUse(stack, 1000.0) && player.getFoodStats().needFood()) {
               int slot = -1;

               for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                  ItemStack playerStack = (ItemStack)player.inventory.mainInventory.get(i);
                  if (!StackUtil.isEmpty(playerStack) && playerStack.getItem() == ItemName.filled_tin_can.getInstance()) {
                     slot = i;
                     break;
                  }
               }

               if (slot > -1) {
                  ItemStack playerStack = (ItemStack)player.inventory.mainInventory.get(slot);
                  ItemTinCan can = (ItemTinCan)playerStack.getItem();
                  ActionResult<ItemStack> result = can.onEaten(player, playerStack);
                  playerStack = (ItemStack)result.getResult();
                  if (StackUtil.isEmpty(playerStack)) {
                     player.inventory.mainInventory.set(slot, StackUtil.emptyStack);
                  }

                  if (result.getType() == EnumActionResult.SUCCESS) {
                     ElectricItem.manager.use(stack, 1000.0, null);
                  }

                  ret = true;
               }
            } else if (player.getFoodStats().getFoodLevel() <= 0) {
               IC2.achievements.issueAchievement(player, "starveWithQHelmet");
            }

            for (PotionEffect effect : new LinkedList(player.getActivePotionEffects())) {
               Potion potion = effect.getPotion();
               Integer cost = potionRemovalCost.get(potion);
               if (cost != null) {
                  cost = cost * (effect.getAmplifier() + 1);
                  if (ElectricItem.manager.canUse(stack, cost.intValue())) {
                     ElectricItem.manager.use(stack, cost.intValue(), null);
                     IC2.platform.removePotion(player, potion);
                  }
               }
            }

            boolean Nightvision = nbtData.getBoolean("Nightvision");
            short hubmode = nbtData.getShort("HudMode");
            if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
               toggleTimer = 10;
               Nightvision = !Nightvision;
               if (IC2.platform.isSimulating()) {
                  nbtData.setBoolean("Nightvision", Nightvision);
                  if (Nightvision) {
                     IC2.platform.messagePlayer(player, "Nightvision enabled.");
                  } else {
                     IC2.platform.messagePlayer(player, "Nightvision disabled.");
                  }
               }
            }

            if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isHudModeKeyDown(player) && toggleTimer == 0) {
               toggleTimer = 10;
               if (hubmode == HudMode.getMaxMode()) {
                  hubmode = 0;
               } else {
                  hubmode++;
               }

               if (IC2.platform.isSimulating()) {
                  nbtData.setShort("HudMode", hubmode);
                  IC2.platform.messagePlayer(player, Localization.translate(HudMode.getFromID(hubmode).getTranslationKey()));
               }
            }

            if (IC2.platform.isSimulating() && toggleTimer > 0) {
               nbtData.setByte("toggleTimer", --toggleTimer);
            }

            if (Nightvision && IC2.platform.isSimulating() && ElectricItem.manager.use(stack, 1.0, player)) {
               BlockPos pos = new BlockPos((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
               int skylight = player.getEntityWorld().getLightFromNeighbors(pos);
               if (skylight > 8) {
                  IC2.platform.removePotion(player, MobEffects.NIGHT_VISION);
                  player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0, true, true));
               } else {
                  IC2.platform.removePotion(player, MobEffects.BLINDNESS);
                  player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, true));
               }

               ret = true;
            }

            IC2.platform.profilerEndSection();
            break;
         case CHEST:
            IC2.platform.profilerStartSection("QuantumBodyarmor");
            player.extinguish();
            IC2.platform.profilerEndSection();
            break;
         case LEGS:
            IC2.platform.profilerStartSection("QuantumLeggings");
            boolean enableQuantumSpeedOnSprint;
            if (IC2.platform.isRendering()) {
               enableQuantumSpeedOnSprint = ConfigUtil.getBool(MainConfig.get(), "misc/quantumSpeedOnSprint");
            } else {
               enableQuantumSpeedOnSprint = true;
            }

            if (ElectricItem.manager.canUse(stack, 1000.0)
               && (player.onGround || player.isInWater())
               && IC2.keyboard.isForwardKeyDown(player)
               && (enableQuantumSpeedOnSprint && player.isSprinting() || !enableQuantumSpeedOnSprint && IC2.keyboard.isBoostKeyDown(player))) {
               byte speedTicker = nbtData.getByte("speedTicker");
               if (++speedTicker >= 10) {
                  speedTicker = 0;
                  ElectricItem.manager.use(stack, 1000.0, null);
                  ret = true;
               }

               nbtData.setByte("speedTicker", speedTicker);
               float speed = 0.22F;
               if (player.isInWater()) {
                  speed = 0.1F;
                  if (IC2.keyboard.isJumpKeyDown(player)) {
                     player.motionY += 0.1F;
                  }
               }

               if (speed > 0.0F) {
                  player.moveRelative(0.0F, 0.0F, 1.0F, speed);
               }
            }

            IC2.platform.profilerEndSection();
            break;
         case FEET:
            IC2.platform.profilerStartSection("QuantumBoots");
            if (IC2.platform.isSimulating()) {
               boolean wasOnGround = nbtData.hasKey("wasOnGround") ? nbtData.getBoolean("wasOnGround") : true;
               if (wasOnGround && !player.onGround && IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isBoostKeyDown(player)) {
                  ElectricItem.manager.use(stack, 4000.0, null);
                  ret = true;
               }

               if (player.onGround != wasOnGround) {
                  nbtData.setBoolean("wasOnGround", player.onGround);
               }
            } else {
               if (ElectricItem.manager.canUse(stack, 4000.0) && player.onGround) {
                  this.jumpCharge = 1.0F;
               }

               if (player.motionY >= 0.0 && this.jumpCharge > 0.0F && !player.isInWater()) {
                  if (IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isBoostKeyDown(player)) {
                     if (this.jumpCharge == 1.0F) {
                        player.motionX *= 3.5;
                        player.motionZ *= 3.5;
                     }

                     player.motionY = player.motionY + this.jumpCharge * 0.3F;
                     this.jumpCharge = (float)(this.jumpCharge * 0.75);
                  } else if (this.jumpCharge < 1.0F) {
                     this.jumpCharge = 0.0F;
                  }
               }
            }

            IC2.platform.profilerEndSection();
      }

      if (ret) {
         player.inventoryContainer.detectAndSendChanges();
      }
   }

   @Override
   public int getItemEnchantability() {
      return 0;
   }

   @Override
   public boolean drainEnergy(ItemStack pack, int amount) {
      return ElectricItem.manager.discharge(pack, amount + 6, Integer.MAX_VALUE, true, false, false) > 0.0;
   }

   @Override
   public float getPower(ItemStack stack) {
      return 1.0F;
   }

   @Override
   public float getDropPercentage(ItemStack stack) {
      return 0.05F;
   }

   @Override
   public double getChargeLevel(ItemStack stack) {
      return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
   }

   @Override
   public boolean isJetpackActive(ItemStack stack) {
      return true;
   }

   @Override
   public float getHoverMultiplier(ItemStack stack, boolean upwards) {
      return 0.1F;
   }

   @Override
   public float getWorldHeightDivisor(ItemStack stack) {
      return 0.9F;
   }

   @Override
   public boolean doesProvideHUD(ItemStack stack) {
      return this.armorType == EntityEquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0.0;
   }

   @Override
   public HudMode getHudMode(ItemStack stack) {
      return HudMode.getFromID(StackUtil.getOrCreateNbtData(stack).getShort("HudMode"));
   }
}

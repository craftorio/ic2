// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.EnumRarity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import ic2.core.init.Localization;
import ic2.api.item.HudMode;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ic2.core.IC2;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import ic2.api.item.ElectricItem;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraft.util.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;
import ic2.api.item.IItemHudProvider;

public class ItemArmorNanoSuit extends ItemArmorElectric implements IItemHudProvider
{
    public ItemArmorNanoSuit(final ItemName name, final EntityEquipmentSlot armorType) {
        super(name, "nano", armorType, 1000000.0, 1600.0, 3);
        if (armorType == EntityEquipmentSlot.FEET) {
            MinecraftForge.EVENT_BUS.register((Object)this);
        }
    }
    
    @Override
    public ISpecialArmor.ArmorProperties getProperties(final EntityLivingBase player, final ItemStack armor, final DamageSource source, final double damage, final int slot) {
        if (source == DamageSource.FALL && this.armorType == EntityEquipmentSlot.FEET) {
            final int energyPerDamage = this.getEnergyPerDamage();
            int damageLimit = Integer.MAX_VALUE;
            if (energyPerDamage > 0) {
                damageLimit = (int)Math.min(damageLimit, 25.0 * ElectricItem.manager.getCharge(armor) / energyPerDamage);
            }
            return new ISpecialArmor.ArmorProperties(10, (damage < 8.0) ? 1.0 : 0.875, damageLimit);
        }
        return super.getProperties(player, armor, source, damage, slot);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityLivingFallEvent(final LivingFallEvent event) {
        if (IC2.platform.isSimulating() && event.getEntity() instanceof EntityLivingBase) {
            final EntityLivingBase entity = (EntityLivingBase)event.getEntity();
            final ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (armor != null && armor.getItem() == this) {
                final int fallDamage = (int)event.getDistance() - 3;
                if (fallDamage >= 8) {
                    return;
                }
                final double energyCost = this.getEnergyPerDamage() * fallDamage;
                if (energyCost <= ElectricItem.manager.getCharge(armor)) {
                    ElectricItem.manager.discharge(armor, energyCost, Integer.MAX_VALUE, true, false, false);
                    event.setCanceled(true);
                }
            }
        }
    }
    
    public void onArmorTick(final World world, final EntityPlayer player, final ItemStack stack) {
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
        byte toggleTimer = nbtData.getByte("toggleTimer");
        boolean ret = false;
        if (this.armorType == EntityEquipmentSlot.HEAD) {
            IC2.platform.profilerStartSection("NanoHelmet");
            boolean Nightvision = nbtData.getBoolean("Nightvision");
            short hubmode = nbtData.getShort("HudMode");
            if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
                toggleTimer = 10;
                Nightvision = !Nightvision;
                if (IC2.platform.isSimulating()) {
                    nbtData.setBoolean("Nightvision", Nightvision);
                    if (Nightvision) {
                        IC2.platform.messagePlayer(player, "Nightvision enabled.", new Object[0]);
                    }
                    else {
                        IC2.platform.messagePlayer(player, "Nightvision disabled.", new Object[0]);
                    }
                }
            }
            if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isHudModeKeyDown(player) && toggleTimer == 0) {
                toggleTimer = 10;
                if (hubmode == HudMode.getMaxMode()) {
                    hubmode = 0;
                }
                else {
                    ++hubmode;
                }
                if (IC2.platform.isSimulating()) {
                    nbtData.setShort("HudMode", hubmode);
                    IC2.platform.messagePlayer(player, Localization.translate(HudMode.getFromID(hubmode).getTranslationKey()), new Object[0]);
                }
            }
            if (IC2.platform.isSimulating() && toggleTimer > 0) {
                final NBTTagCompound nbtTagCompound = nbtData;
                final String s = "toggleTimer";
                --toggleTimer;
                nbtTagCompound.setByte(s, toggleTimer);
            }
            if (Nightvision && IC2.platform.isSimulating() && ElectricItem.manager.use(stack, 1.0, (EntityLivingBase)player)) {
                final BlockPos pos = new BlockPos((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
                final int skylight = player.getEntityWorld().getLightFromNeighbors(pos);
                if (skylight > 8) {
                    IC2.platform.removePotion((EntityLivingBase)player, MobEffects.NIGHT_VISION);
                    player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0, true, true));
                }
                else {
                    IC2.platform.removePotion((EntityLivingBase)player, MobEffects.BLINDNESS);
                    player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, true));
                }
                ret = true;
            }
            IC2.platform.profilerEndSection();
        }
        if (ret) {
            player.inventoryContainer.detectAndSendChanges();
        }
    }
    
    @Override
    public double getDamageAbsorptionRatio() {
        return 0.9;
    }
    
    @Override
    public int getEnergyPerDamage() {
        return 5000;
    }
    
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(final ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }
    
    @Override
    public boolean doesProvideHUD(final ItemStack stack) {
        return this.armorType == EntityEquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0.0;
    }
    
    @Override
    public HudMode getHudMode(final ItemStack stack) {
        return HudMode.getFromID(StackUtil.getOrCreateNbtData(stack).getShort("HudMode"));
    }
}

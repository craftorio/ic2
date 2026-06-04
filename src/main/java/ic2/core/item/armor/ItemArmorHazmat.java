// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import ic2.core.IC2DamageSource;
import java.util.Iterator;
import ic2.core.slot.ArmorSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.math.BlockPos;
import ic2.core.util.StackUtil;
import ic2.core.util.LiquidUtil;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.ref.FluidName;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.IC2;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraft.util.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;
import ic2.api.item.IHazmatLike;

public class ItemArmorHazmat extends ItemArmorUtility implements IHazmatLike
{
    public ItemArmorHazmat(final ItemName name, final EntityEquipmentSlot type) {
        super(name, "hazmat", type);
        this.setMaxDamage(64);
        if (this.armorType == EntityEquipmentSlot.FEET) {
            MinecraftForge.EVENT_BUS.register((Object)this);
        }
    }
    
    @Override
    public ISpecialArmor.ArmorProperties getProperties(final EntityLivingBase player, final ItemStack armor, final DamageSource source, final double damage, final int slot) {
        if (this.armorType == EntityEquipmentSlot.HEAD && hazmatAbsorbs(source) && hasCompleteHazmat(player)) {
            if (source == DamageSource.IN_FIRE || source == DamageSource.LAVA || source == DamageSource.HOT_FLOOR) {
                player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 60, 1));
            }
            return new ISpecialArmor.ArmorProperties(10, 1.0, Integer.MAX_VALUE);
        }
        if (this.armorType == EntityEquipmentSlot.FEET && source == DamageSource.FALL) {
            return new ISpecialArmor.ArmorProperties(10, (damage < 8.0) ? 1.0 : 0.875, (armor.getMaxDamage() - armor.getItemDamage() + 2) * 2 * 25);
        }
        return new ISpecialArmor.ArmorProperties(0, 0.05, (armor.getMaxDamage() - armor.getItemDamage() + 2) / 2 * 25);
    }
    
    @Override
    public void damageArmor(final EntityLivingBase entity, final ItemStack stack, final DamageSource source, final int damage, final int slot) {
        if (hazmatAbsorbs(source) && hasCompleteHazmat(entity)) {
            return;
        }
        int damageTotal = damage * 2;
        if (this.armorType == EntityEquipmentSlot.FEET && source == DamageSource.FALL) {
            damageTotal = (damage + 1) / 2;
        }
        stack.damageItem(damageTotal, entity);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityLivingFallEvent(final LivingFallEvent event) {
        if (IC2.platform.isSimulating() && event.getEntity() instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer)event.getEntity();
            final ItemStack armor = (ItemStack)player.inventory.armorInventory.get(0);
            if (armor != null && armor.getItem() == this) {
                final int fallDamage = (int)event.getDistance() - 3;
                if (fallDamage >= 8) {
                    return;
                }
                final int armorDamage = (fallDamage + 1) / 2;
                if (armorDamage <= armor.getMaxDamage() - armor.getItemDamage() && armorDamage >= 0) {
                    armor.damageItem(armorDamage, (EntityLivingBase)player);
                    event.setCanceled(true);
                }
            }
        }
    }
    
    public boolean isRepairable() {
        return true;
    }
    
    @Override
    public int getArmorDisplay(final EntityPlayer player, final ItemStack armor, final int slot) {
        return 1;
    }
    
    public void onArmorTick(final World world, final EntityPlayer player, final ItemStack stack) {
        if (!world.isRemote && this.armorType == EntityEquipmentSlot.HEAD) {
            if (player.isBurning() && hasCompleteHazmat((EntityLivingBase)player)) {
                if (this.isInLava(player)) {
                    player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 20, 0, true, true));
                }
                player.extinguish();
            }
            final int maxAir = 300;
            final int refillThreshold = 100;
            final int airToMbMul = 1000;
            final int airToMbDiv = 150;
            final int minAmount = 7;
            final int air = player.getAir();
            if (air <= 100) {
                int needed = (300 - air) * 1000 / 150;
                int supplied = 0;
                for (int i = 0; i < player.inventory.mainInventory.size() && needed > 0; ++i) {
                    final ItemStack cStack = (ItemStack)player.inventory.mainInventory.get(i);
                    if (cStack != null) {
                        final LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(cStack, FluidName.air.getInstance(), needed, FluidContainerOutputMode.InPlacePreferred);
                        if (result != null) {
                            if (result.fluidChange.amount >= 7) {
                                if (result.extraOutput == null || StackUtil.storeInventoryItem(result.extraOutput, player, false)) {
                                    player.inventory.mainInventory.set(i, (Object)result.inPlaceOutput);
                                    final int amount = result.fluidChange.amount;
                                    supplied += amount;
                                    needed -= amount;
                                }
                            }
                        }
                    }
                }
                player.setAir(air + supplied * 150 / 1000);
            }
        }
    }
    
    public boolean isInLava(final EntityPlayer player) {
        final int x = (int)Math.floor(player.posX);
        final int y = (int)Math.floor(player.posY + 0.02);
        final int z = (int)Math.floor(player.posZ);
        final IBlockState state = player.getEntityWorld().getBlockState(new BlockPos(x, y, z));
        if (state.getBlock() instanceof BlockLiquid && (state.getMaterial() == Material.LAVA || state.getMaterial() == Material.FIRE)) {
            final float height = y + 1 - BlockLiquid.getLiquidHeightPercent((int)state.getValue((IProperty)BlockLiquid.LEVEL));
            return player.posY < height;
        }
        return false;
    }
    
    @Override
    public boolean addsProtection(final EntityLivingBase entity, final EntityEquipmentSlot slot, final ItemStack stack) {
        return true;
    }
    
    public static boolean hasCompleteHazmat(final EntityLivingBase living) {
        for (final EntityEquipmentSlot slot : ArmorSlot.getAll()) {
            final ItemStack stack = living.getItemStackFromSlot(slot);
            if (stack == null || !(stack.getItem() instanceof IHazmatLike)) {
                return false;
            }
            final IHazmatLike hazmat = (IHazmatLike)stack.getItem();
            if (!hazmat.addsProtection(living, slot, stack)) {
                return false;
            }
            if (hazmat.fullyProtects(living, slot, stack)) {
                return true;
            }
        }
        return true;
    }
    
    public static boolean hazmatAbsorbs(final DamageSource source) {
        return source == DamageSource.IN_FIRE || source == DamageSource.IN_WALL || source == DamageSource.LAVA || source == DamageSource.HOT_FLOOR || source == DamageSource.ON_FIRE || source == IC2DamageSource.electricity || source == IC2DamageSource.radiation;
    }
    
    @Override
    public boolean isMetalArmor(final ItemStack itemstack, final EntityPlayer player) {
        return false;
    }
}

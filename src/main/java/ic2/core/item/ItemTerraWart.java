// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.EnumRarity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import ic2.core.IC2Potion;
import net.minecraft.init.MobEffects;
import ic2.core.IC2;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemTerraWart extends ItemFoodIc2
{
    public ItemTerraWart() {
        super(ItemName.terra_wart, 0, 1.0f, false);
        this.setAlwaysEdible();
    }
    
    public ItemStack onItemUseFinish(final ItemStack stack, final World world, final EntityLivingBase player) {
        IC2.platform.removePotion(player, MobEffects.NAUSEA);
        IC2.platform.removePotion(player, MobEffects.MINING_FATIGUE);
        IC2.platform.removePotion(player, MobEffects.HUNGER);
        IC2.platform.removePotion(player, MobEffects.SLOWNESS);
        IC2.platform.removePotion(player, MobEffects.WEAKNESS);
        IC2.platform.removePotion(player, MobEffects.BLINDNESS);
        IC2.platform.removePotion(player, MobEffects.POISON);
        IC2.platform.removePotion(player, MobEffects.WITHER);
        final PotionEffect effect = player.getActivePotionEffect((Potion)IC2Potion.radiation);
        if (effect != null) {
            if (effect.getDuration() <= 600) {
                IC2.platform.removePotion(player, IC2Potion.radiation);
            }
            else {
                IC2.platform.removePotion(player, IC2Potion.radiation);
                IC2Potion.radiation.applyTo(player, effect.getDuration() - 600, effect.getAmplifier());
            }
        }
        return super.onItemUseFinish(stack, world, player);
    }
    
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(final ItemStack stack) {
        return EnumRarity.RARE;
    }
}

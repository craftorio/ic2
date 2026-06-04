// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.Arrays;
import net.minecraft.item.ItemStack;
import java.util.List;
import net.minecraft.potion.Potion;

public class IC2Potion extends Potion
{
    public static IC2Potion radiation;
    private final List<ItemStack> curativeItems;
    
    public static void init() {
        IC2Potion.radiation.setPotionName("ic2.potion.radiation");
        IC2Potion.radiation.setIconIndex(6, 0);
        IC2Potion.radiation.setEffectiveness(0.25);
    }
    
    public IC2Potion(final String name, final boolean badEffect, final int liquidColor, final ItemStack... curativeItems) {
        super(badEffect, liquidColor);
        this.curativeItems = Arrays.asList(curativeItems);
        ForgeRegistries.POTIONS.register(this.setRegistryName(name));
    }
    
    public void performEffect(final EntityLivingBase entity, final int amplifier) {
        if (this == IC2Potion.radiation) {
            entity.attackEntityFrom((DamageSource)IC2DamageSource.radiation, amplifier / 100 + 0.5f);
        }
    }
    
    public boolean isReady(final int duration, final int amplifier) {
        if (this == IC2Potion.radiation) {
            final int rate = 25 >> amplifier;
            return rate <= 0 || duration % rate == 0;
        }
        return false;
    }
    
    public void applyTo(final EntityLivingBase entity, final int duration, final int amplifier) {
        final PotionEffect effect = new PotionEffect((Potion)IC2Potion.radiation, duration, amplifier);
        effect.setCurativeItems((List)this.curativeItems);
        entity.addPotionEffect(effect);
    }
}

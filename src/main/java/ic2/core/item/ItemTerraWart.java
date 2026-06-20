package ic2.core.item;

import ic2.core.Ic2Potion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemTerraWart extends Item
{
	public ItemTerraWart(Properties settings)
	{
		super(settings);
	}

	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity player)
	{
		Holder<MobEffect> radiationHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(Ic2Potion.radiation);
		player.removeEffect(MobEffects.CONFUSION);
		player.removeEffect(MobEffects.DIG_SLOWDOWN);
		player.removeEffect(MobEffects.HUNGER);
		player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
		player.removeEffect(MobEffects.WEAKNESS);
		player.removeEffect(MobEffects.BLINDNESS);
		player.removeEffect(MobEffects.POISON);
		player.removeEffect(MobEffects.WITHER);
		MobEffectInstance effect = player.getEffect(radiationHolder);
		if (effect != null)
		{
			if (effect.getDuration() <= 600)
			{
				player.removeEffect(radiationHolder);
			} else
			{
				player.removeEffect(radiationHolder);
				Ic2Potion.radiation.applyTo(player, effect.getDuration() - 600, effect.getAmplifier());
			}
		}

		return super.finishUsingItem(stack, world, player);
	}
}

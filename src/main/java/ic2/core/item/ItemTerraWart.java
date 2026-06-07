package ic2.core.item;

import ic2.core.Ic2Potion;
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

	public ItemStack m_5922_(ItemStack stack, Level world, LivingEntity player)
	{
		player.m_21195_(MobEffects.f_19604_);
		player.m_21195_(MobEffects.f_19599_);
		player.m_21195_(MobEffects.f_19612_);
		player.m_21195_(MobEffects.f_19597_);
		player.m_21195_(MobEffects.f_19613_);
		player.m_21195_(MobEffects.f_19610_);
		player.m_21195_(MobEffects.f_19614_);
		player.m_21195_(MobEffects.f_19615_);
		MobEffectInstance effect = player.m_21124_(Ic2Potion.radiation);
		if (effect != null)
		{
			if (effect.m_19557_() <= 600)
			{
				player.m_21195_(Ic2Potion.radiation);
			} else
			{
				player.m_21195_(Ic2Potion.radiation);
				Ic2Potion.radiation.applyTo(player, effect.m_19557_() - 600, effect.m_19564_());
			}
		}

		return super.m_5922_(stack, world, player);
	}
}

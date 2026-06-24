package ic2.core.item;

import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class ItemMug extends Item implements ItemLike
{
	public ItemMug.MugType mugType;

	public ItemMug(Properties settings, ItemMug.MugType mugType)
	{
		super(settings);
		this.mugType = mugType;
	}

	public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving)
	{
		if (!(entityLiving instanceof Player player))
		{
			return stack;
		} else
		{
			ItemMug.MugType type = this.getType(stack);
			if (type != null && type != ItemMug.MugType.empty)
			{
				int maxAmplifier;
				int extraDuration = switch (type)
				{
					case cold_coffee ->
					{
						maxAmplifier = 1;
						yield 600;
					}
					case dark_coffee ->
					{
						maxAmplifier = 5;
						yield 1200;
					}
					case coffee ->
					{
						maxAmplifier = 6;
						yield 1200;
					}
					default -> throw new IllegalStateException("unexpected type: " + type);
				};

				int highest = 0;
				int x = this.amplifyEffect(player, MobEffects.MOVEMENT_SPEED, maxAmplifier, extraDuration);
				if (x > highest)
				{
					highest = x;
				}

				x = this.amplifyEffect(player, MobEffects.DIG_SPEED, maxAmplifier, extraDuration);
				if (x > highest)
				{
					highest = x;
				}

				if (type == ItemMug.MugType.coffee)
				{
					highest -= 2;
				}

				if (highest >= 3)
				{
					player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, (highest - 2) * 200, 0));
					if (highest >= 4)
					{
						player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, highest - 3));
					}
				}

				return new ItemStack(Ic2Items.EMPTY_MUG, 1);
			} else
			{
				return stack;
			}
		}
	}

	private int amplifyEffect(Player player, MobEffect potion, int maxAmplifier, int extraDuration)
	{
		MobEffectInstance eff = player.getEffect(potion);
		if (eff != null)
		{
			int newAmp = eff.getAmplifier();
			int newDur = eff.getDuration();
			if (newAmp < maxAmplifier)
			{
				newAmp++;
			}

			newDur += extraDuration;
			assert potion == eff.getEffect();
			player.addEffect(new MobEffectInstance(potion, newDur, newAmp));
			return newAmp;
		} else
		{
			player.addEffect(new MobEffectInstance(potion, 300, 0));
			return 1;
		}
	}

	public int getUseDuration(ItemStack stack)
	{
		ItemMug.MugType type = this.getType(stack);
		return type != null && type != ItemMug.MugType.empty ? 32 : 0;
	}

	public UseAnim getUseAnimation(ItemStack stack)
	{
		ItemMug.MugType type = this.getType(stack);
		return type != null && type != ItemMug.MugType.empty ? UseAnim.DRINK : UseAnim.NONE;
	}

	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemMug.MugType type = this.getType(StackUtil.get(player, hand));
		if (type != null && type != ItemMug.MugType.empty)
		{
			player.startUsingItem(hand);
		}

		return super.use(world, player, hand);
	}

	private ItemMug.MugType getType(ItemStack stack)
	{
		return ((ItemMug) stack.getItem()).mugType;
	}

	public enum MugType
	{
		empty,
		cold_coffee,
		dark_coffee,
		coffee
	}
}

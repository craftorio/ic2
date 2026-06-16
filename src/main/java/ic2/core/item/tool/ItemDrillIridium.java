package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

@NotClassic
public class ItemDrillIridium extends ItemDrill
{
	private static final Tier IRIDIUM_TOOL_MATERIAL = new Tier()
	{
		public int getUses()
		{
			return 3000;
		}

		public float getSpeed()
		{
			return 15.0F;
		}

		public float getAttackDamageBonus()
		{
			return 5.0F;
		}

		public int getLevel()
		{
			return 100;
		}

		public int getEnchantmentValue()
		{
			return 20;
		}

		public Ingredient getRepairIngredient()
		{
			return Ingredient.of(new ItemLike[] { Ic2Items.IRIDIUM });
		}
	};

	public ItemDrillIridium(Properties settings)
	{
		super(settings, 800, IRIDIUM_TOOL_MATERIAL, 300000, 1000, 3, 24.0F);
	}

	@Override
	protected ItemStack getItemStack(double charge)
	{
		ItemStack ret = super.getItemStack(charge);
		Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<>();
		enchantmentMap.put(Enchantments.BLOCK_FORTUNE, 3);
		EnchantmentHelper.setEnchantments(enchantmentMap, ret);
		return ret;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		if (!world.isClientSide && IC2.keyboard.isModeSwitchKeyDown(player))
		{
			Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<>();
			ItemStack stack = StackUtil.get(player, hand);
			if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0)
			{
				enchantmentMap.put(Enchantments.SILK_TOUCH, 1);
				IC2.sideProxy.messagePlayer(player, "item.ic2.mining_laser.tooltip.mode", "item.ic2.mining_laser.tooltip.mode.silkTouch");
			} else
			{
				enchantmentMap.put(Enchantments.BLOCK_FORTUNE, 3);
				IC2.sideProxy.messagePlayer(player, "item.ic2.mining_laser.tooltip.mode", "item.ic2.mining_laser.tooltip.mode.normal");
			}

			EnchantmentHelper.setEnchantments(enchantmentMap, stack);
		}

		return super.use(world, player, hand);
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		return IC2.keyboard.isModeSwitchKeyDown(context.getPlayer()) ? InteractionResult.PASS : super.useOn(context);
	}
}

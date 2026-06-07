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
		public int m_6609_()
		{
			return 3000;
		}

		public float m_6624_()
		{
			return 15.0F;
		}

		public float m_6631_()
		{
			return 5.0F;
		}

		public int m_6604_()
		{
			return 100;
		}

		public int m_6601_()
		{
			return 20;
		}

		public Ingredient m_6282_()
		{
			return Ingredient.m_43929_(new ItemLike[] { Ic2Items.IRIDIUM });
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
		enchantmentMap.put(Enchantments.f_44987_, 3);
		EnchantmentHelper.m_44865_(enchantmentMap, ret);
		return ret;
	}

	@Override
	public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand)
	{
		if (!world.isClientSide && IC2.keyboard.isModeSwitchKeyDown(player))
		{
			Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<>();
			enchantmentMap.put(Enchantments.f_44987_, 3);
			ItemStack stack = StackUtil.get(player, hand);
			if (EnchantmentHelper.m_44843_(Enchantments.f_44985_, stack) == 0)
			{
				enchantmentMap.put(Enchantments.f_44985_, 1);
				IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.silkTouch");
			} else
			{
				IC2.sideProxy.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.normal");
			}

			EnchantmentHelper.m_44865_(enchantmentMap, stack);
		}

		return super.m_7203_(world, player, hand);
	}

	@Override
	public InteractionResult m_6225_(UseOnContext context)
	{
		return IC2.keyboard.isModeSwitchKeyDown(context.m_43723_()) ? InteractionResult.PASS : super.m_6225_(context);
	}
}

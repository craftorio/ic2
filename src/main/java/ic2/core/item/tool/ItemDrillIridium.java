package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@NotClassic
public class ItemDrillIridium extends ItemDrill
{
	public ItemDrillIridium()
	{
		super(ItemName.iridium_drill, 800, HarvestLevel.Iridium, 300000, 1000, 3, 24.0F);
	}

	@Override
	protected ItemStack getItemStack(double charge)
	{
		ItemStack ret = super.getItemStack(charge);
		Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<>();
		enchantmentMap.put(Enchantments.FORTUNE, 3);
		EnchantmentHelper.setEnchantments(enchantmentMap, ret);
		return ret;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		if (!world.isRemote && IC2.keyboard.isModeSwitchKeyDown(player))
		{
			Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<>();
			enchantmentMap.put(Enchantments.FORTUNE, 3);
			ItemStack stack = StackUtil.get(player, hand);
			if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0)
			{
				enchantmentMap.put(Enchantments.SILK_TOUCH, 1);
				IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.silkTouch");
			} else
			{
				IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.normal");
			}

			EnchantmentHelper.setEnchantments(enchantmentMap, stack);
		}

		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public EnumActionResult onItemUse(
		EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset
	)
	{
		return IC2.keyboard.isModeSwitchKeyDown(player) ? EnumActionResult.PASS : super.onItemUse(player, world, pos, hand, side, xOffset, yOffset, zOffset);
	}
}

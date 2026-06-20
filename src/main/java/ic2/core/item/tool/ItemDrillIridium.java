package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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

		public int getEnchantmentValue()
		{
			return 20;
		}

		public Ingredient getRepairIngredient()
		{
			return Ingredient.of(new ItemLike[] { Ic2Items.IRIDIUM });
		}

		public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops()
		{
			return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
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
		ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		var enchantmentRegistry = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).lookupOrThrow(Registries.ENCHANTMENT);
		enchantments.set(enchantmentRegistry.getOrThrow(Enchantments.FORTUNE), 3);
		ret.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
		return ret;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		if (!world.isClientSide && IC2.keyboard.isModeSwitchKeyDown(player))
		{
			ItemStack stack = StackUtil.get(player, hand);
			var enchantmentRegistry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
			if (stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(enchantmentRegistry.getOrThrow(Enchantments.SILK_TOUCH)) == 0)
			{
				enchantments.set(enchantmentRegistry.getOrThrow(Enchantments.SILK_TOUCH), 1);
				IC2.sideProxy.messagePlayer(player, "item.ic2.mining_laser.tooltip.mode", "item.ic2.mining_laser.tooltip.mode.silkTouch");
			} else
			{
				enchantments.set(enchantmentRegistry.getOrThrow(Enchantments.FORTUNE), 3);
				IC2.sideProxy.messagePlayer(player, "item.ic2.mining_laser.tooltip.mode", "item.ic2.mining_laser.tooltip.mode.normal");
			}

			stack.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
		}

		return super.use(world, player, hand);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context)
	{
		return IC2.keyboard.isModeSwitchKeyDown(context.getPlayer()) ? InteractionResult.PASS : super.useOn(context);
	}
}

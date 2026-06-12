package ic2.core.item.tool;

import ic2.api.crops.CropCard;
import ic2.api.item.ElectricItem;
import ic2.core.IHasGui;
import ic2.core.crop.TileEntityCrop;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.IHandHeldInventory;
import ic2.core.util.StackUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class ItemCropAnalyzer extends BaseElectricItem implements IHandHeldInventory
{
	public ItemCropAnalyzer(Properties settings)
	{
		super(settings, 100000.0, 128.0, 2);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public @NotNull Rarity getRarity(@NotNull ItemStack stack)
	{
		return Rarity.UNCOMMON;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide)
		{
			this.getInventory(player, hand, stack).openManagedItem(player, hand, null);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();

		if (!world.isClientSide && player != null && !player.isCrouching())
		{
			BlockEntity te = world.getBlockEntity(pos);
			if (te instanceof TileEntityCrop crop)
			{
				if (crop.getCrop() == null)
				{
					return InteractionResult.PASS;
				}

				if (ElectricItem.manager.discharge(StackUtil.get(player, context.getHand()), HandHeldCropAnalyzer.energyForLevel(2), 3, true, false, false) > 0.0)
				{
					CropCard plant = crop.getCrop();
					player.sendSystemMessage(Component.translatable("ic2.crop_analyzer.crop_name", plant.getUnlocalizedName()));
					player.sendSystemMessage(Component.translatable("ic2.crop.analyzer.crop_discovered_by", plant.getDiscoveredBy()));
					player.sendSystemMessage(Component.translatable("ic2.crop_analyzer.crop_age", crop.getCurrentAge()));
					player.sendSystemMessage(Component.translatable("ic2.crop_analyzer.crop_nutrients", crop.getStorageNutrients()));
					player.sendSystemMessage(Component.translatable("ic2.crop_analyzer.crop_water", crop.getStorageWater()));
					player.sendSystemMessage(Component.translatable("ic2.crop_analyzer.crop_weed_extra", crop.getStorageWeedEX()));
					player.sendSystemMessage(Component.translatable("ic2.crop_analyzer.crop.growth_points", crop.getGrowthPoints(), plant.getGrowthDuration(crop)));
					return InteractionResult.SUCCESS;
				}
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack stack, Player player)
	{
		if (!player.getCommandSenderWorld().isClientSide && !StackUtil.isEmpty(stack) && player.containerMenu instanceof ContainerAnalyzer)
		{
			HandHeldCropAnalyzer analyzer = ((ContainerAnalyzer) player.containerMenu).base;
			if (analyzer.isThisContainer(stack))
			{
				analyzer.saveAsThrown(stack);
				player.closeContainer();
			}
		}

		return true;
	}

	@Override
	public IHasGui getInventory(Player player, InteractionHand hand, ItemStack stack)
	{
		return new HandHeldCropAnalyzer(player, hand, stack);
	}
}

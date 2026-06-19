package ic2.core.item.crafting;

import ic2.api.item.IBlockCuttingBlade;
import ic2.core.item.type.BlockCuttingBladeType;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BlockCuttingBlade extends Item implements IBlockCuttingBlade
{
	private final BlockCuttingBladeType type;

	public BlockCuttingBlade(Properties settings, BlockCuttingBladeType type)
	{
		super(settings);
		this.type = type;
	}

	@Override
	public int getHardness(ItemStack stack)
	{
		return switch (this.type)
		{
			case iron -> 3;
			case steel -> 6;
			case diamond -> 9;
		};
	}

	public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced)
	{
		tooltip.add(Component.translatable(switch (type)
		{
			case iron -> "ic2.iron_cutting_blade.info";
			case steel -> "ic2.steel_cutting_blade.info";
			case diamond -> "ic2.diamond_cutting_blade.info";
		}));
		tooltip.add(Component.translatable("ic2.cutting_blade.hardness", this.getHardness(stack)));
	}
}

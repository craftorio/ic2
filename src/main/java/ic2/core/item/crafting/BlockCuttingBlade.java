package ic2.core.item.crafting;

import ic2.api.item.IBlockCuttingBlade;
import ic2.core.item.type.BlockCuttingBladeType;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

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
			default -> 0;
		};
	}

	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		switch (this.type)
		{
			case iron:
				tooltip.add(Component.translatable("ic2.IronBlockCuttingBlade.info"));
				break;
			case steel:
				tooltip.add(Component.translatable("ic2.AdvIronBlockCuttingBlade.info"));
				break;
			case diamond:
				tooltip.add(Component.translatable("ic2.DiamondBlockCuttingBlade.info"));
		}

		tooltip.add(Component.translatable("ic2.CuttingBlade.hardness", new Object[] { this.getHardness(stack) }));
	}
}

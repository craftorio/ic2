package ic2.core.item.crafting;

import ic2.api.item.IBlockCuttingBlade;
import ic2.core.init.Localization;
import ic2.core.item.ItemMulti;
import ic2.core.item.type.BlockCuttingBladeType;
import ic2.core.ref.ItemName;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCuttingBlade extends ItemMulti<BlockCuttingBladeType> implements IBlockCuttingBlade
{
	public BlockCuttingBlade()
	{
		super(ItemName.block_cutting_blade, BlockCuttingBladeType.class);
	}

	@Override
	public int getHardness(ItemStack stack)
	{
		BlockCuttingBladeType blade = this.getType(stack);
		if (blade == null)
		{
			return 0;
		}

		switch (blade)
		{
			case iron:
				return 3;
			case steel:
				return 6;
			case diamond:
				return 9;
			default:
				return 0;
		}
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced)
	{
		BlockCuttingBladeType blade = this.getType(stack);
		if (blade != null)
		{
			switch (blade)
			{
				case iron:
					tooltip.add(Localization.translate("ic2.IronBlockCuttingBlade.info"));
					break;
				case steel:
					tooltip.add(Localization.translate("ic2.AdvIronBlockCuttingBlade.info"));
					break;
				case diamond:
					tooltip.add(Localization.translate("ic2.DiamondBlockCuttingBlade.info"));
			}

			tooltip.add(Localization.translate("ic2.CuttingBlade.hardness", this.getHardness(stack)));
		}
	}
}

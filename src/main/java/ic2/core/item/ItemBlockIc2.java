package ic2.core.item;

import ic2.api.energy.EnergyNet;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockIc2 extends BlockItem
{
	public ItemBlockIc2(Block block, Properties properties)
	{
		super(block, properties);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
	{
		if (this.getBlock() instanceof Ic2TileEntityBlock block)
		{
			Ic2TileEntity dummyTe = block.getDummyTe();
			if (dummyTe.hasComponent(Energy.class))
			{
				Energy energy = dummyTe.getComponent(Energy.class);
				if (!energy.getSourceDirs().isEmpty())
				{
					tooltip.add(Component.translatable("ic2.item.tooltip.power_tier", energy.getSourceTier()).withStyle(ChatFormatting.GRAY));
				} else if (!energy.getSinkDirs().isEmpty())
				{
					tooltip.add(Component.translatable("ic2.item.tooltip.power_tier", energy.getSinkTier()).withStyle(ChatFormatting.GRAY));
				}

				if (dummyTe instanceof TileEntityElectricBlock electricBlock)
				{
					tooltip.add(Component.translatable("ic2.item.tooltip.Output",
							Math.round(EnergyNet.instance.getPowerFromTier(energy.getSourceTier()))));
					tooltip.add(Component.translatable("ic2.item.tooltip.Capacity", electricBlock.getCapacity()));
					double stored = stack.hasTag() ? stack.getTag().getDouble("energy") : 0.0;
					tooltip.add(Component.translatable("ic2.item.tooltip.Store", (long) stored));
				}
			}
		}
	}
}

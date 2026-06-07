package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemReactorHeatStorage extends AbstractDamageableReactorComponent
{
	public ItemReactorHeatStorage(Properties settings, int heatStorage)
	{
		super(settings, heatStorage);
	}

	@Override
	public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return true;
	}

	@Override
	public int getMaxHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return this.getMaxUse();
	}

	@Override
	public int getCurrentHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return this.getUse(stack);
	}

	@Override
	public int alterHeat(ItemStack stack, IReactor reactor, int x, int y, int heat)
	{
		int myHeat = this.getCurrentHeat(stack, reactor, x, y);
		myHeat += heat;
		int max = this.getMaxHeat(stack, reactor, x, y);
		if (myHeat > max)
		{
			reactor.setItemAt(x, y, null);
			heat = max - myHeat + 1;
		} else
		{
			if (myHeat < 0)
			{
				heat = myHeat;
				myHeat = 0;
			} else
			{
				heat = 0;
			}

			this.setUse(stack, myHeat);
		}

		return heat;
	}

	@Override
	public void m_7373_(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		super.m_7373_(stack, world, tooltip, advanced);
		if (this.getUse(stack) > 0)
		{
			tooltip.add(Component.m_237113_("ic2.reactoritem.heatwarning.line1").m_130940_(ChatFormatting.GRAY));
			tooltip.add(Component.m_237113_("ic2.reactoritem.heatwarning.line2").m_130940_(ChatFormatting.GRAY));
		}
	}
}

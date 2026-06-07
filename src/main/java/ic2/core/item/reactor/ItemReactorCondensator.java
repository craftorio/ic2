package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemReactorCondensator extends AbstractDamageableReactorComponent
{
	public ItemReactorCondensator(Properties settings, int maxdmg)
	{
		super(settings, maxdmg);
	}

	@Override
	public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return this.getCurrentHeat(stack) < this.getMaxHeat(stack, reactor, x, y);
	}

	@Override
	public int getMaxHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return this.getMaxUse();
	}

	private int getCurrentHeat(ItemStack stack)
	{
		return this.getUse(stack);
	}

	@Override
	public int alterHeat(ItemStack stack, IReactor reactor, int x, int y, int heat)
	{
		if (heat < 0)
		{
			return heat;
		}

		int currentHeat = this.getCurrentHeat(stack);
		int amount = Math.min(heat, this.getMaxHeat(stack, reactor, x, y) - currentHeat);
		heat -= amount;
		this.setUse(stack, currentHeat + amount);
		return heat;
	}
}

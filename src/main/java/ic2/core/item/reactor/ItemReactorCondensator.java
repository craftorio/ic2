package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

public class ItemReactorCondensator extends AbstractDamageableReactorComponent
{
	public ItemReactorCondensator(ItemName name, int maxdmg)
	{
		super(name, maxdmg);
	}

	@Override
	public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return this.getCurrentHeat(stack) < this.getMaxCustomDamage(stack);
	}

	@Override
	public int getMaxHeat(ItemStack stack, IReactor reactor, int x, int y)
	{
		return this.getMaxCustomDamage(stack);
	}

	private int getCurrentHeat(ItemStack stack)
	{
		return this.getCustomDamage(stack);
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
		this.setCustomDamage(stack, currentHeat + amount);
		return heat;
	}
}

package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemReactorReflector extends AbstractDamageableReactorComponent
{
	public ItemReactorReflector(Properties settings, int maxDamage)
	{
		super(settings, maxDamage);
	}

	@Override
	public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun)
	{
		if (!heatrun)
		{
			IReactorComponent source = (IReactorComponent) pulsingStack.getItem();
			source.acceptUraniumPulse(pulsingStack, reactor, stack, pulseX, pulseY, youX, youY, heatrun);
		} else if (this.getUse(stack) + 1 >= this.getMaxUse())
		{
			reactor.setItemAt(youX, youY, null);
		} else
		{
			this.incrementUse(stack);
		}

		return true;
	}

	@Override
	public float influenceExplosion(ItemStack stack, IReactor reactor)
	{
		return -1.0F;
	}
}

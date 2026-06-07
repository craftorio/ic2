package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.core.profile.NotExperimental;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

@NotExperimental
public class ItemReactorDepletedUranium extends AbstractDamageableReactorComponent
{
	public ItemReactorDepletedUranium(Properties settings)
	{
		super(settings, 10000);
	}

	@Override
	public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun)
	{
		if (heatrun)
		{
			int myLevel = this.getUse(stack) + 1 + reactor.getHeat() / 3000;
			if (myLevel >= this.getMaxUse())
			{
				reactor.setItemAt(youX, youY, new ItemStack(Ic2Items.RE_ENRICHED_URANIUM));
			} else
			{
				this.setUse(stack, myLevel);
			}
		}

		return true;
	}

	@Override
	public double getUseFraction(ItemStack stack)
	{
		return 1.0 - super.getUseFraction(stack);
	}
}

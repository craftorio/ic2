package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

@NotClassic
public class ItemReactorLithiumCell extends AbstractDamageableReactorComponent
{
	public ItemReactorLithiumCell()
	{
		super(ItemName.lithium_fuel_rod, 10000);
	}

	@Override
	public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun)
	{
		if (heatrun)
		{
			int myLevel = this.getCustomDamage(stack) + reactor.getHeat() / 3000;
			if (myLevel >= this.getMaxCustomDamage(stack))
			{
				reactor.setItemAt(youX, youY, ItemName.tritium_fuel_rod.getItemStack());
			} else
			{
				this.setCustomDamage(stack, myLevel);
			}
		}

		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return 1.0 - super.getDurabilityForDisplay(stack);
	}
}

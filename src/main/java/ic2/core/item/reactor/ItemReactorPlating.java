package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemReactorPlating extends AbstractReactorComponent
{
	private final int maxHeatAdd;
	private final float effectModifier;

	public ItemReactorPlating(Properties settings, int maxheatadd, float effectmodifier)
	{
		super(settings);
		this.maxHeatAdd = maxheatadd;
		this.effectModifier = effectmodifier;
	}

	@Override
	public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatrun)
	{
		if (heatrun)
		{
			reactor.setMaxHeat(reactor.getMaxHeat() + this.maxHeatAdd);
			reactor.setHeatEffectModifier(reactor.getHeatEffectModifier() * this.effectModifier);
		}
	}

	@Override
	public float influenceExplosion(ItemStack stack, IReactor reactor)
	{
		return this.effectModifier >= 1.0F ? 0.0F : this.effectModifier;
	}
}

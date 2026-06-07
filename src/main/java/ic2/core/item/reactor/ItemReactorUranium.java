package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.Ic2Potion;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.ref.Ic2Items;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemReactorUranium extends AbstractDamageableReactorComponent
{
	public final int numberOfCells;

	public ItemReactorUranium(Properties settings, int cells)
	{
		this(settings, cells, 20000);
	}

	protected ItemReactorUranium(Properties settings, int cells, int duration)
	{
		super(settings, duration);
		this.numberOfCells = cells;
	}

	@Override
	public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatRun)
	{
		if (reactor.produceEnergy())
		{
			int basePulses = 1 + this.numberOfCells / 2;

			for (int iteration = 0; iteration < this.numberOfCells; iteration++)
			{
				int pulses = basePulses;
				if (!heatRun)
				{
					for (int i = 0; i < pulses; i++)
					{
						this.acceptUraniumPulse(stack, reactor, stack, x, y, x, y, heatRun);
					}

					pulses += checkPulseable(reactor, x - 1, y, stack, x, y, heatRun)
						+ checkPulseable(reactor, x + 1, y, stack, x, y, heatRun)
						+ checkPulseable(reactor, x, y - 1, stack, x, y, heatRun)
						+ checkPulseable(reactor, x, y + 1, stack, x, y, heatRun);
				} else
				{
					pulses += checkPulseable(reactor, x - 1, y, stack, x, y, heatRun)
						+ checkPulseable(reactor, x + 1, y, stack, x, y, heatRun)
						+ checkPulseable(reactor, x, y - 1, stack, x, y, heatRun)
						+ checkPulseable(reactor, x, y + 1, stack, x, y, heatRun);
					int heat = triangularNumber(pulses) * 4;
					heat = this.getFinalHeat(stack, reactor, x, y, heat);
					Queue<ItemReactorUranium.ItemStackCoord> heatAcceptors = new ArrayDeque<>();
					this.checkHeatAcceptor(reactor, x - 1, y, heatAcceptors);
					this.checkHeatAcceptor(reactor, x + 1, y, heatAcceptors);
					this.checkHeatAcceptor(reactor, x, y - 1, heatAcceptors);
					this.checkHeatAcceptor(reactor, x, y + 1, heatAcceptors);

					while (!heatAcceptors.isEmpty() && heat > 0)
					{
						int dheat = heat / heatAcceptors.size();
						heat -= dheat;
						ItemReactorUranium.ItemStackCoord acceptor = heatAcceptors.remove();
						IReactorComponent acceptorComp = (IReactorComponent) acceptor.stack.getItem();
						dheat = acceptorComp.alterHeat(acceptor.stack, reactor, acceptor.x, acceptor.y, dheat);
						heat += dheat;
					}

					if (heat > 0)
					{
						reactor.addHeat(heat);
					}
				}
			}

			if (!heatRun && this.getUse(stack) >= this.getMaxUse() - 1)
			{
				reactor.setItemAt(x, y, this.getDepletedStack(stack, reactor));
			} else if (!heatRun)
			{
				this.incrementUse(stack);
			}
		}
	}

	protected int getFinalHeat(ItemStack stack, IReactor reactor, int x, int y, int heat)
	{
		return heat;
	}

	protected ItemStack getDepletedStack(ItemStack stack, IReactor reactor)
	{
		return new ItemStack(switch (this.numberOfCells)
		{
			case 1 -> Ic2Items.DEPLETED_URANIUM_FUEL_ROD;
			case 2 -> Ic2Items.DEPLETED_DUAL_URANIUM_FUEL_ROD;
			default -> throw new RuntimeException("invalid cell count: " + this.numberOfCells);
			case 4 -> Ic2Items.DEPLETED_QUAD_URANIUM_FUEL_ROD;
		});
	}

	protected static int checkPulseable(IReactor reactor, int x, int y, ItemStack stack, int mex, int mey, boolean heatrun)
	{
		ItemStack other = reactor.getItemAt(x, y);
		return other != null
			&& other.getItem() instanceof IReactorComponent
			&& ((IReactorComponent) other.getItem()).acceptUraniumPulse(other, reactor, stack, x, y, mex, mey, heatrun)
			? 1
			: 0;
	}

	protected static int triangularNumber(int x)
	{
		return (x * x + x) / 2;
	}

	protected void checkHeatAcceptor(IReactor reactor, int x, int y, Collection<ItemReactorUranium.ItemStackCoord> heatAcceptors)
	{
		ItemStack stack = reactor.getItemAt(x, y);
		if (stack != null && stack.getItem() instanceof IReactorComponent && ((IReactorComponent) stack.getItem()).canStoreHeat(stack, reactor, x, y))
		{
			heatAcceptors.add(new ItemReactorUranium.ItemStackCoord(stack, x, y));
		}
	}

	@Override
	public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun)
	{
		if (!heatrun)
		{
			reactor.addOutput(1.0F);
		}

		return true;
	}

	@Override
	public float influenceExplosion(ItemStack stack, IReactor reactor)
	{
		return 2 * this.numberOfCells;
	}

	public void m_6883_(ItemStack stack, Level world, Entity entity, int slotIndex, boolean isCurrentItem)
	{
		if (entity instanceof LivingEntity entityLiving && !ItemArmorHazmat.hasCompleteHazmat(entityLiving))
		{
			Ic2Potion.radiation.applyTo(entityLiving, 200, 100);
		}
	}

	private static class ItemStackCoord
	{
		public final ItemStack stack;
		public final int x;
		public final int y;

		public ItemStackCoord(ItemStack stack, int x, int y)
		{
			this.stack = stack;
			this.x = x;
			this.y = y;
		}
	}
}

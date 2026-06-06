package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.IC2Potion;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.ItemName;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemReactorUranium extends AbstractDamageableReactorComponent
{
	public final int numberOfCells;

	public ItemReactorUranium(ItemName name, int cells)
	{
		this(name, cells, 20000);
	}

	protected ItemReactorUranium(ItemName name, int cells, int duration)
	{
		super(name, duration);
		this.setMaxStackSize(64);
		this.numberOfCells = cells;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ItemName name)
	{
		this.registerModel(0, name, null);
		this.registerModel(1, name, null);
	}

	public int getMetadata(ItemStack stack)
	{
		return this.getCustomDamage(stack) > 0 ? 1 : 0;
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

			if (!heatRun && this.getCustomDamage(stack) >= this.getMaxCustomDamage(stack) - 1)
			{
				reactor.setItemAt(x, y, this.getDepletedStack(stack, reactor));
			} else if (!heatRun)
			{
				this.applyCustomDamage(stack, 1, null);
			}
		}
	}

	protected int getFinalHeat(ItemStack stack, IReactor reactor, int x, int y, int heat)
	{
		return heat;
	}

	protected ItemStack getDepletedStack(ItemStack stack, IReactor reactor)
	{
		ItemStack ret;
		switch (this.numberOfCells)
		{
			case 1:
				ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_uranium);
				break;
			case 2:
				ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_dual_uranium);
				break;
			case 3:
			default:
				throw new RuntimeException("invalid cell count: " + this.numberOfCells);
			case 4:
				ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_quad_uranium);
		}

		return ret.copy();
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

	public void onUpdate(ItemStack stack, World world, Entity entity, int slotIndex, boolean isCurrentItem)
	{
		if (entity instanceof EntityLivingBase)
		{
			EntityLivingBase entityLiving = (EntityLivingBase) entity;
			if (!ItemArmorHazmat.hasCompleteHazmat(entityLiving))
			{
				IC2Potion.radiation.applyTo(entityLiving, 200, 100);
			}
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

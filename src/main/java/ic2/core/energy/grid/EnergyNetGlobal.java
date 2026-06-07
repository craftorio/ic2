package ic2.core.energy.grid;

import ic2.api.energy.IEnergyNet;
import ic2.api.energy.IEnergyNetEventReceiver;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import ic2.core.IC2;
import ic2.core.energy.leg.EnergyCalculatorLeg;
import ic2.core.event.WorldData;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnergyNetGlobal implements IEnergyNet
{
	private static final List<IEnergyNetEventReceiver> eventReceivers = new CopyOnWriteArrayList<>();
	private static IEnergyCalculator calculator;

	public static EnergyNetGlobal create()
	{
		if (System.getProperty("IC2ExpEnet") != null)
		{
		}

		calculator = new EnergyCalculatorLeg();
		return new EnergyNetGlobal();
	}

	private EnergyNetGlobal()
	{
	}

	@Override
	public IEnergyTile getTile(Level world, BlockPos pos)
	{
		if (world == null)
		{
			throw new NullPointerException("null world");
		} else if (pos == null)
		{
			throw new NullPointerException("null pos");
		} else
		{
			return getLocal(world).getIoTile(pos);
		}
	}

	@Override
	public IEnergyTile getSubTile(Level world, BlockPos pos)
	{
		if (world == null)
		{
			throw new NullPointerException("null world");
		} else if (pos == null)
		{
			throw new NullPointerException("null pos");
		} else
		{
			return getLocal(world).getSubTile(pos);
		}
	}

	@Override
	public <T extends BlockEntity & IEnergyTile> void addBlockEntityTile(T tile)
	{
		if (tile == null)
		{
			throw new NullPointerException("null tile");
		}

		addTile(tile, tile.getLevel(), tile.getBlockPos());
	}

	@Override
	public <T extends ILocatable & IEnergyTile> void addLocatableTile(T tile)
	{
		if (tile == null)
		{
			throw new NullPointerException("null tile");
		}

		addTile(tile, tile.getWorldObj(), tile.getPosition());
	}

	private static void addTile(IEnergyTile tile, Level world, BlockPos pos)
	{
		if (EnergyNetSettings.logEnetApiAccessTraces)
		{
			IC2.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API addTile %s.", Util.toString(tile, world, pos));
		} else if (EnergyNetSettings.logEnetApiAccesses)
		{
			IC2.log.debug(LogCategory.EnergyNet, "API addTile %s.", Util.toString(tile, world, pos));
		}

		getLocal(world).addTile(tile, pos);
	}

	@Override
	public void removeTile(IEnergyTile tile)
	{
		if (tile == null)
		{
			throw new NullPointerException("null tile");
		}

		Level world = this.getWorld(tile);
		BlockPos pos = this.getPos(tile);
		if (EnergyNetSettings.logEnetApiAccessTraces)
		{
			IC2.log.debug(LogCategory.EnergyNet, new Throwable("Called from:"), "API removeTile %s.", Util.toString(tile, world, pos));
		} else if (EnergyNetSettings.logEnetApiAccesses)
		{
			IC2.log.debug(LogCategory.EnergyNet, "API removeTile %s.", Util.toString(tile, world, pos));
		}

		getLocal(world).removeTile(tile, pos);
	}

	@Override
	public Level getWorld(IEnergyTile tile)
	{
		if (tile == null)
		{
			throw new NullPointerException("null tile");
		} else if (tile instanceof ILocatable)
		{
			return ((ILocatable) tile).getWorldObj();
		} else if (tile instanceof BlockEntity)
		{
			return ((BlockEntity) tile).getLevel();
		} else
		{
			throw new UnsupportedOperationException("unlocatable tile type: " + tile.getClass().getName());
		}
	}

	@Override
	public BlockPos getPos(IEnergyTile tile)
	{
		if (tile == null)
		{
			throw new NullPointerException("null tile");
		} else if (tile instanceof ILocatable)
		{
			return ((ILocatable) tile).getPosition();
		} else if (tile instanceof BlockEntity)
		{
			return ((BlockEntity) tile).getBlockPos();
		} else
		{
			throw new UnsupportedOperationException("unlocatable tile type: " + tile.getClass().getName());
		}
	}

	@Override
	public NodeStats getNodeStats(IEnergyTile tile)
	{
		return getLocal(this.getWorld(tile)).getNodeStats(tile);
	}

	@Override
	public int getAdjacentConnections(IEnergyTile tile)
	{
		return getLocal(this.getWorld(tile)).getAdjacentConnections(tile);
	}

	public boolean dumpDebugInfo(Level world, BlockPos pos, PrintStream console, PrintStream chat)
	{
		return getLocal(world).dumpDebugInfo(pos, console, chat);
	}

	@Override
	public double getPowerFromTier(int tier)
	{
		if (tier < 14)
		{
			return 8 << tier * 2;
		} else
		{
			return tier < 30 ? 8.0 * Math.pow(4.0, tier) : 9.223372E18F;
		}
	}

	@Override
	public int getTierFromPower(double power)
	{
		return power <= 0.0 ? 0 : (int) Math.ceil(Math.log(power / 8.0) / Math.log(4.0));
	}

	@Override
	public synchronized void registerEventReceiver(IEnergyNetEventReceiver receiver)
	{
		if (!eventReceivers.contains(receiver))
		{
			eventReceivers.add(receiver);
		}
	}

	@Override
	public synchronized void unregisterEventReceiver(IEnergyNetEventReceiver receiver)
	{
		eventReceivers.remove(receiver);
	}

	static Iterable<IEnergyNetEventReceiver> getEventReceivers()
	{
		return eventReceivers;
	}

	static IEnergyCalculator getCalculator()
	{
		return calculator;
	}

	public static EnergyNetLocal getLocal(Level world)
	{
		if (world.isClientSide)
		{
			throw new IllegalStateException("not applicable clientside");
		}

		assert world.getServer().m_18695_();
		return WorldData.get(world).energyNet;
	}
}

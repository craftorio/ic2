package ic2.core.block.machine.tileentity;

import ic2.api.network.ClientModifiable;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerWeightedFluidDistributor;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.LiquidUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityWeightedFluidDistributor extends TileEntityFluidDistributor implements IWeightedDistributor
{
	@ClientModifiable
	protected List<Direction> priority = new ArrayList<>(5);

	public TileEntityWeightedFluidDistributor(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.WEIGHTED_FLUID_DISTRIBUTOR, pos, state);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		int[] indexes = nbt.m_128465_("priority");
		if (indexes.length > 0)
		{
			for (int index : indexes)
			{
				this.priority.add(Direction.m_122376_(index));
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		if (!this.priority.isEmpty())
		{
			int[] indexes = new int[this.priority.size()];

			for (int i = 0; i < indexes.length; i++)
			{
				indexes[i] = this.priority.get(i).m_122411_();
			}

			nbt.m_128385_("priority", indexes);
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("priority");
		return ret;
	}

	@Override
	protected void updateConnectivity()
	{
		if (!this.getLevel().isClientSide && !this.priority.isEmpty() && this.priority.remove(this.getFacing()))
		{
			this.updatePriority(true);
		}

		this.fluids.changeConnectivity(this.fluidTank, Collections.singleton(this.getFacing()), Collections.emptySet());
	}

	@Override
	protected void moveFluid()
	{
		if (!this.priority.isEmpty())
		{
			int tankAmount = this.fluidTank.getFluidAmount();

			for (Direction dir : this.priority)
			{
				assert dir != this.getFacing();
				BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(dir));
				Direction side = dir.m_122424_();
				if (LiquidUtil.isFluidTile(target, side))
				{
					int amount = LiquidUtil.fillTile(target, side, this.fluidTank.getFluidStack(), false);
					if (amount > 0)
					{
						tankAmount -= amount;
						this.fluidTank.drainMbUnchecked(amount, false);
						if (tankAmount <= 0)
						{
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerWeightedFluidDistributor(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerWeightedFluidDistributor(syncId, inventory, this);
	}

	@Override
	public List<Direction> getPriority()
	{
		return this.priority;
	}

	@Override
	public void updatePriority(boolean server)
	{
		IC2.network.get(server).updateTileEntityField(this, "priority");
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		int position = event / 10;
		Direction facing = Direction.m_122376_(event % 10 & 6);
		assert position >= 0 && position <= this.priority.size() : "Position was " + position;
		assert facing != this.getFacing();
		if (position == this.priority.size())
		{
			this.priority.add(facing);
		} else
		{
			this.priority.set(position, facing);
		}
	}
}

package ic2.core.block.machine.tileentity;

import ic2.api.network.ClientModifiable;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerWeightedFluidDistributor;
import ic2.core.block.machine.gui.GuiWeightedFluidDistributor;
import ic2.core.profile.NotClassic;
import ic2.core.util.LiquidUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityWeightedFluidDistributor extends TileEntityFluidDistributor implements IWeightedDistributor
{
	@ClientModifiable
	protected final List<EnumFacing> priority = new ArrayList<>(5);

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (!this.priority.isEmpty())
		{
			int[] indexes = new int[this.priority.size()];

			for (int i = 0; i < indexes.length; i++)
			{
				indexes[i] = this.priority.get(i).getIndex();
			}

			nbt.setIntArray("priority", indexes);
		}

		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		int[] indexes = nbt.getIntArray("priority");
		if (indexes.length > 0)
		{
			for (int index : indexes)
			{
				this.priority.add(EnumFacing.getFront(index));
			}
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
		if (!this.getWorld().isRemote && !this.priority.isEmpty() && this.priority.remove(this.getFacing()))
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

			for (EnumFacing dir : this.priority)
			{
				assert dir != this.getFacing();
				TileEntity target = this.world.getTileEntity(this.pos.offset(dir));
				EnumFacing side = dir.getOpposite();
				if (LiquidUtil.isFluidTile(target, side))
				{
					int amount = LiquidUtil.fillTile(target, side, this.fluidTank.getFluid(), false);
					if (amount > 0)
					{
						tankAmount -= amount;
						this.fluidTank.drainInternal(amount, true);
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
	public ContainerBase<?> getGuiContainer(EntityPlayer player)
	{
		return new ContainerWeightedFluidDistributor(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiWeightedFluidDistributor(new ContainerWeightedFluidDistributor(player, this));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public List<EnumFacing> getPriority()
	{
		return this.priority;
	}

	@Override
	public void updatePriority(boolean server)
	{
		IC2.network.get(server).updateTileEntityField(this, "priority");
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		int position = event / 10;
		EnumFacing facing = EnumFacing.getFront(event % 10 & 6);
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

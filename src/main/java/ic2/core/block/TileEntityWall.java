package ic2.core.block;

import ic2.core.IC2;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.UnlistedProperty;
import ic2.core.ref.BlockName;
import ic2.core.util.Ic2Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.property.IUnlistedProperty;

public class TileEntityWall extends TileEntityBlock
{
	public static final IUnlistedProperty<TileEntityWall.WallRenderState> renderStateProperty = new UnlistedProperty<>(
		"renderstate", TileEntityWall.WallRenderState.class
	);
	protected final Obscuration obscuration;
	private Ic2Color color = BlockWall.defaultColor;
	private volatile TileEntityWall.WallRenderState renderState;

	public TileEntityWall()
	{
		this(BlockWall.defaultColor);
	}

	public TileEntityWall(Ic2Color color)
	{
		this.obscuration = this.addComponent(new Obscuration(this, new Runnable()
		{
			@Override
			public void run()
			{
				IC2.network.get(true).updateTileEntityField(TileEntityWall.this, "obscuration");
			}
		}));
		this.color = color;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.color = Ic2Color.values[nbt.getByte("color") & 0xFF];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setByte("color", (byte) this.color.ordinal());
		return nbt;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (this.getWorld().isRemote)
		{
			this.updateRenderState();
		}
	}

	@Override
	protected Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state)
	{
		state = super.getExtendedState(state);
		TileEntityWall.WallRenderState value = this.renderState;
		if (value != null)
		{
			state = state.withProperties(renderStateProperty, value);
		}

		return state;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>();
		ret.add("color");
		ret.add("obscuration");
		ret.addAll(super.getNetworkedFields());
		return ret;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
		if (this.updateRenderState())
		{
			this.rerender();
		}
	}

	@Override
	protected boolean recolor(EnumFacing side, EnumDyeColor mcColor)
	{
		Ic2Color color = Ic2Color.get(mcColor);
		if (color == this.color)
		{
			return false;
		}

		this.color = color;
		if (!this.getWorld().isRemote)
		{
			IC2.network.get(true).updateTileEntityField(this, "obscuration");
			this.markDirty();
		} else if (this.updateRenderState())
		{
			this.rerender();
		}

		return true;
	}

	@Override
	protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target)
	{
		return BlockName.wall.getItemStack(this.color);
	}

	@Override
	protected boolean clientNeedsExtraModelInfo()
	{
		return this.obscuration.hasObscuration();
	}

	private boolean updateRenderState()
	{
		TileEntityWall.WallRenderState state = new TileEntityWall.WallRenderState(this.color, this.obscuration.getRenderState());
		if (state.equals(this.renderState))
		{
			return false;
		}

		this.renderState = state;
		return true;
	}

	public static class WallRenderState
	{
		public final Ic2Color color;
		public final Obscuration.ObscurationData[] obscurations;

		public WallRenderState(Ic2Color color, Obscuration.ObscurationData[] obscurations)
		{
			this.color = color;
			this.obscurations = obscurations;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
			{
				return true;
			}

			if (!(obj instanceof TileEntityWall.WallRenderState))
			{
				return false;
			}

			TileEntityWall.WallRenderState o = (TileEntityWall.WallRenderState) obj;
			return o.color == this.color && Arrays.equals(o.obscurations, this.obscurations);
		}

		@Override
		public int hashCode()
		{
			return this.color.hashCode() * 31 + Arrays.hashCode(this.obscurations);
		}

		@Override
		public String toString()
		{
			return "WallState<" + this.color + ", " + Arrays.toString(this.obscurations) + '>';
		}
	}
}

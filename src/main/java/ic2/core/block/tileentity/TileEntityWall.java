package ic2.core.block.tileentity;

import ic2.core.IC2;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.misc.WallBlock;
import ic2.core.ref.Ic2BlockEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TileEntityWall extends Ic2TileEntity
{
	protected final Obscuration obscuration;
	private DyeColor color = WallBlock.DEFAULT_COLOR;
	private volatile TileEntityWall.WallRenderState renderState;

	public TileEntityWall(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.WALL, pos, state);
		this.obscuration = this.addComponent(new Obscuration(this, new Runnable()
		{
			@Override
			public void run()
			{
				IC2.network.get(true).updateTileEntityField(TileEntityWall.this, "obscuration");
			}
		}));
	}

	public void setColor(DyeColor color)
	{
		this.color = color;
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.color = DyeColor.byId(nbt.getByte("color"));
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putByte("color", (byte) this.color.getId());
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (this.getLevel().isClientSide)
		{
			this.updateRenderState();
		}
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
	protected boolean recolor(Direction side, DyeColor color)
	{
		if (color == this.color)
		{
			return false;
		}

		this.color = color;
		if (!this.getLevel().isClientSide)
		{
			IC2.network.get(true).updateTileEntityField(this, "obscuration");
			this.setChanged();
		} else if (this.updateRenderState())
		{
			this.rerender();
		}

		return true;
	}

	@Override
	protected ItemStack getPickBlock(Player player, BlockHitResult target)
	{
		return new ItemStack(WallBlock.get(this.color));
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
		public final DyeColor color;
		public final Obscuration.ObscurationData[] obscurations;

		public WallRenderState(DyeColor color, Obscuration.ObscurationData[] obscurations)
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
			} else
			{
				return !(obj instanceof TileEntityWall.WallRenderState o) ? false : o.color == this.color && Arrays.equals(o.obscurations, this.obscurations);
			}
		}

		@Override
		public int hashCode()
		{
			return this.color.hashCode() * 31 + Arrays.hashCode(this.obscurations);
		}

		@Override
		public String toString()
		{
			return "WallState<" + this.color + ", " + Arrays.toString(this.obscurations) + ">";
		}
	}
}

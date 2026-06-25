package ic2.core.block.comp;

import ic2.core.block.state.BlockStateUtil;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.item.tool.ItemObscurator;
import ic2.core.util.Util;

import java.util.Arrays;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class Obscuration extends TileEntityComponent
{
	private final Runnable changeHandler;
	private Obscuration.ObscurationData[] dataMap;

	public Obscuration(Ic2TileEntity parent, Runnable changeHandler)
	{
		super(parent);
		this.changeHandler = changeHandler;
	}

	@Override
	public void readFromNbt(CompoundTag nbt)
	{
		if (!nbt.isEmpty())
		{
			for (Direction facing : Util.ALL_DIRS)
			{
				if (nbt.contains(facing.getSerializedName(), 10))
				{
					CompoundTag cNbt = nbt.getCompound(facing.getSerializedName());
					Block block = Util.getBlock(cNbt.getString("block"));
					if (block != null)
					{
						String variant = cNbt.getString("variant");
						BlockState state = BlockStateUtil.getState(block, variant);
						if (state != null)
						{
							int rawSide = cNbt.getByte("side");
							if (rawSide >= 0 && rawSide < Util.ALL_DIRS.length)
							{
								Direction side = Util.ALL_DIRS[rawSide];
								int[] colorMultipliers = ItemObscurator.internColorMultipliers(cNbt.getIntArray("colorMuls"));
								Obscuration.ObscurationData data = new Obscuration.ObscurationData(state, variant, side, colorMultipliers);
								if (this.dataMap == null)
								{
									this.dataMap = new Obscuration.ObscurationData[Util.ALL_DIRS.length];
								}

								this.dataMap[facing.ordinal()] = data.intern();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public CompoundTag writeToNbt()
	{
		if (this.dataMap == null)
		{
			return null;
		}

		CompoundTag ret = new CompoundTag();

		for (Direction facing : Util.ALL_DIRS)
		{
			Obscuration.ObscurationData data = this.dataMap[facing.ordinal()];
			if (data != null)
			{
				CompoundTag nbt = new CompoundTag();
				nbt.putString("block", Util.getName(data.state.getBlock()).toString());
				nbt.putString("variant", data.variant);
				nbt.putByte("side", (byte) data.side.ordinal());
				nbt.putIntArray("colorMuls", data.colorMultipliers);
				ret.put(facing.getSerializedName(), nbt);
			}
		}

		return ret;
	}

	public boolean applyObscuration(Direction side, Obscuration.ObscurationData data)
	{
		if (this.dataMap != null && data.equals(this.dataMap[side.ordinal()]))
		{
			return false;
		}

		if (this.dataMap == null)
		{
			this.dataMap = new Obscuration.ObscurationData[Util.ALL_DIRS.length];
		}

		this.dataMap[side.ordinal()] = data.intern();
		if (this.parent.getLevel() != null && !this.parent.getLevel().isClientSide)
		{
			this.parent.setChanged();
		}

		this.changeHandler.run();
		return true;
	}

	public void clear()
	{
		this.dataMap = null;
		this.changeHandler.run();
	}

	public boolean hasObscuration()
	{
		return this.dataMap != null;
	}

	public Obscuration.ObscurationData[] getRenderState()
	{
		return this.dataMap == null ? null : Arrays.copyOf(this.dataMap, this.dataMap.length);
	}

	public record ObscurationData(BlockState state, String variant, Direction side, int[] colorMultipliers)
		{

			@Override
			public boolean equals(Object obj)
			{
				if (obj == this)
				{
					return true;
				} else
				{
					return !(obj instanceof ObscurationData o)
						? false
						: o.state.equals(this.state)
						  && o.variant.equals(this.variant)
						  && o.side == this.side
						  && Arrays.equals(o.colorMultipliers, this.colorMultipliers);
				}
			}
	
			@Override
			public int hashCode()
			{
				return (this.state.hashCode() * 7 + this.side.ordinal()) * 23;
			}
	
			public ObscurationData intern()
			{
				return this;
			}
		}
}

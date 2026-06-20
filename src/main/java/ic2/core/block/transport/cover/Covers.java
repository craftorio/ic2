package ic2.core.block.transport.cover;

import ic2.core.IC2;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;

public class Covers extends TileEntityComponent
{
	protected ItemStack[] covers = new ItemStack[6];

	public Covers(Ic2TileEntity parent)
	{
		super(parent);
	}

	public void addCover(Direction side, ItemStack cover)
	{
		if (StackUtil.isEmpty(this.covers[side.ordinal()]))
		{
			ItemStack ret = cover.copy();
			CompoundTag nbtTagCompound = StackUtil.getOrCreateNbtData(ret);
			nbtTagCompound.putByte("side", (byte) side.ordinal());
			this.covers[side.ordinal()] = ret;
		}
	}

	public ItemStack removeCover(Direction side)
	{
		ItemStack ret = this.covers[side.ordinal()];
		ret.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(null));
		this.covers[side.ordinal()] = null;
		return ret;
	}

	public boolean hasCover(Direction side)
	{
		return !StackUtil.isEmpty(this.covers[side.ordinal()]);
	}

	public ICoverItem getCoverItem(Direction side)
	{
		ItemStack stack = this.covers[side.ordinal()];
		return StackUtil.isEmpty(stack) ? null : (ICoverItem) stack.getItem();
	}

	@Override
	public void readFromNbt(CompoundTag nbt)
	{
		ListTag coversTag = nbt.getList("covers", 10);

		for (int i = 0; i < coversTag.size(); i++)
		{
			CompoundTag coverTag = coversTag.getCompound(i);
			int index = coverTag.getByte("facing") & 255;
			if (index >= this.covers.length)
			{
				IC2.log.error(LogCategory.Block, "Can't load cover for %s, index %d is out of bounds.", Util.toString(this.parent), index);
			} else
			{
				ItemStack cover = ItemStack.of(coverTag);
				if (StackUtil.isEmpty(cover))
				{
					IC2.log
						.warn(
							LogCategory.Block,
							"Can't load cover %s for %s, index %d, no matching item for %d:%d.",
							StackUtil.toStringSafe(cover),
							Util.toString(this.parent),
							index,
							coverTag.getShort("id"),
							coverTag.getShort("Damage")
						);
				} else
				{
					if (!StackUtil.isEmpty(this.covers[index]))
					{
						IC2.log
							.error(
								LogCategory.Block,
								"Loading cover to non-empty cover for %s, index %d, replacing %s with %s.",
								Util.toString(this.parent),
								index,
								this.covers[index],
								cover
							);
					}

					this.covers[index] = cover;
				}
			}
		}
	}

	@Override
	public CompoundTag writeToNbt()
	{
		CompoundTag ret = new CompoundTag();
		ListTag coversTag = new ListTag();

		for (Direction facing : Util.ALL_DIRS)
		{
			ItemStack cover = this.covers[facing.ordinal()];
			if (!StackUtil.isEmpty(cover))
			{
				CompoundTag coverTag = new CompoundTag();
				coverTag.putByte("facing", (byte) facing.ordinal());
				cover.save(coverTag);
				coversTag.add(coverTag);
			}
		}

		ret.put("covers", coversTag);
		return ret;
	}

	@Override
	public boolean enableWorldTick()
	{
		return !this.parent.getLevel().isClientSide;
	}

	@Override
	public void onWorldTick()
	{
		for (Direction facing : Util.ALL_DIRS)
		{
			if (!StackUtil.isEmpty(this.covers[facing.ordinal()]))
			{
				((ICoverItem) this.covers[facing.ordinal()].getItem()).onTick(this.covers[facing.ordinal()], (ICoverHolder) this.parent);
			}
		}
	}
}

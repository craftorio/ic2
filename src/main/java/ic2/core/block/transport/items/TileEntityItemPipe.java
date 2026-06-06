package ic2.core.block.transport.items;

import ic2.api.transport.IItemTransportTile;
import ic2.core.block.transport.TileEntityPipe;
import ic2.core.item.block.ItemPipe;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;

public class TileEntityItemPipe extends TileEntityPipe implements IItemTransportTile
{
	protected PipeType type = PipeType.bronze;
	protected PipeSize size = PipeSize.small;
	protected ItemStack contents;

	public TileEntityItemPipe()
	{
	}

	@Override
	public void flipConnection(EnumFacing facing)
	{
	}

	public TileEntityItemPipe(PipeType type, PipeSize size)
	{
		this();
		this.type = type;
		this.size = size;
	}

	@Override
	public int putItems(ItemStack stack, EnumFacing facing, boolean simulate)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0;
		}

		if (!StackUtil.isEmpty(this.contents))
		{
			return 0;
		}

		if (stack.getCount() > this.getMaxStackSizeAllowed())
		{
			return 0;
		}

		if (!simulate)
		{
			this.contents = StackUtil.copy(stack);
		}

		return stack.getCount();
	}

	@Override
	public int getMaxStackSizeAllowed()
	{
		return this.size.maxStackSize;
	}

	@Override
	public int getTransferRate()
	{
		return this.type.transferRate;
	}

	@Override
	public ItemStack getContents()
	{
		return this.contents;
	}

	@Override
	public void setContents(ItemStack stack)
	{
		this.contents = stack;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInventoryUpdate = false;
		if (!StackUtil.isEmpty(this.contents))
		{
			EnumFacing facing = this.getFacing();
			TileEntity target = this.world.getTileEntity(this.pos.offset(facing));
			if (target instanceof IItemTransportTile && ((IItemTransportTile) target).putItems(this.contents, facing.getOpposite(), true) > 0)
			{
				int amount = ((IItemTransportTile) target).putItems(this.contents, facing.getOpposite(), false);
				ItemStack newStack = StackUtil.copyShrunk(this.contents, amount);
				assert newStack.isEmpty();
				this.contents = null;
				needsInventoryUpdate = true;
			}
		}

		if (needsInventoryUpdate)
		{
			this.markDirty();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.type = PipeType.values[nbt.getByte("type") & 0xFF];
		this.size = PipeSize.values()[nbt.getByte("size") & 0xFF];
		NBTTagList contentsTag = nbt.getTagList("contents", 10);

		for (int i = 0; i < contentsTag.tagCount(); i++)
		{
			NBTTagCompound contentTag = contentsTag.getCompoundTagAt(i);
			ItemStack stack = new ItemStack(contentTag);
			if (!StackUtil.isEmpty(stack))
			{
				this.contents = stack;
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setByte("type", (byte) this.type.ordinal());
		nbt.setByte("size", (byte) this.size.ordinal());
		NBTTagList contentsTag = new NBTTagList();
		if (!StackUtil.isEmpty(this.contents))
		{
			NBTTagCompound contentTag = new NBTTagCompound();
			this.contents.writeToNBT(contentTag);
			contentsTag.appendTag(contentTag);
			nbt.setTag("contents", contentsTag);
		}

		return nbt;
	}

	@Override
	protected void updateConnectivity()
	{
	}

	@Override
	protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target)
	{
		return ItemPipe.getPipe(this.type, this.size);
	}

	@Override
	protected List<ItemStack> getAuxDrops(int fortune)
	{
		List<ItemStack> ret = new ArrayList<>(super.getAuxDrops(fortune));
		if (!StackUtil.isEmpty(this.contents))
		{
			ret.add(this.contents);
		}

		return ret;
	}

	@Override
	protected List<AxisAlignedBB> getAabbs(boolean forCollision)
	{
		float th = this.size.thickness;
		float sp = (1.0F - th) / 2.0F;
		List<AxisAlignedBB> ret = new ArrayList<>(7);
		ret.add(new AxisAlignedBB(sp, sp, sp, sp + th, sp + th, sp + th));

		for (EnumFacing facing : EnumFacing.VALUES)
		{
			boolean hasConnection = (this.connectivity & 1 << facing.ordinal()) != 0;
			if (hasConnection)
			{
				float zS = sp;
				float yS = sp;
				float xS = sp;
				float yE;
				float zE;
				float xE = yE = zE = sp + th;
				switch (facing)
				{
					case DOWN:
						yS = 0.0F;
						yE = sp;
						break;
					case UP:
						yS = sp + th;
						yE = 1.0F;
						break;
					case NORTH:
						zS = 0.0F;
						zE = sp;
						break;
					case SOUTH:
						zS = sp + th;
						zE = 1.0F;
						break;
					case WEST:
						xS = 0.0F;
						xE = sp;
						break;
					case EAST:
						xS = sp + th;
						xE = 1.0F;
						break;
					default:
						throw new RuntimeException();
				}

				ret.add(new AxisAlignedBB(xS, yS, zS, xE, yE, zE));
			}
		}

		return ret;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("type");
		ret.add("size");
		return ret;
	}

	@Override
	protected void updateRenderState()
	{
		this.renderState = new TileEntityPipe.PipeRenderState(this.type, this.size, this.connectivity, this.covers, this.getFacing().ordinal());
	}
}

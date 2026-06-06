package ic2.core.block.transport;

import ic2.api.transport.IPipe;
import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.UnlistedProperty;
import ic2.core.block.transport.cover.CoverProperty;
import ic2.core.block.transport.cover.Covers;
import ic2.core.block.transport.cover.ICoverHolder;
import ic2.core.block.transport.cover.ICoverItem;
import ic2.core.block.transport.items.PipeSize;
import ic2.core.block.transport.items.PipeType;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IUnlistedProperty;

public abstract class TileEntityPipe extends TileEntityBlock implements IPipe, ICoverHolder
{
	public static final IUnlistedProperty<TileEntityPipe.PipeRenderState> renderStateProperty = new UnlistedProperty<>(
		"renderstate", TileEntityPipe.PipeRenderState.class
	);
	protected volatile TileEntityPipe.PipeRenderState renderState;
	protected byte connectivity = 0;
	protected byte covers = 0;
	protected final Covers coversComponent = this.addComponent(new Covers(this));

	@Override
	public TileEntity getTile()
	{
		return this;
	}

	@Override
	public boolean isConnected(EnumFacing facing)
	{
		return (this.connectivity & 1 << facing.ordinal()) != 0;
	}

	@Override
	public abstract void flipConnection(EnumFacing var1);

	@Override
	public Set<CoverProperty> getCoverProperties()
	{
		return Collections.emptySet();
	}

	@Override
	public boolean canPlaceCover(World world, BlockPos pos, EnumFacing side, ItemStack stack)
	{
		Item rawItem = stack.getItem();
		if (!(rawItem instanceof ICoverItem))
		{
			return false;
		}

		ICoverItem item = (ICoverItem) rawItem;
		return item.isSuitableFor(stack, this.getCoverProperties()) && (this.covers & 1 << side.ordinal()) == 0;
	}

	@Override
	public void placeCover(World world, BlockPos pos, EnumFacing side, ItemStack stack)
	{
		this.coversComponent.addCover(side, stack);
		this.covers = (byte) (this.covers ^ 1 << side.ordinal());
		IC2.network.get(true).updateTileEntityField(this, "covers");
	}

	@Override
	public boolean canRemoveCover(World world, BlockPos pos, EnumFacing side)
	{
		return (this.covers & 1 << side.ordinal()) != 0;
	}

	@Override
	public void removeCover(World world, BlockPos pos, EnumFacing side)
	{
		ItemStack ret = this.coversComponent.removeCover(side);
		this.covers = (byte) (this.covers ^ 1 << side.ordinal());
		IC2.network.get(true).updateTileEntityField(this, "covers");
		StackUtil.dropAsEntity(this.getWorld(), this.getPos(), StackUtil.copyWithSize(ret, 1));
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
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.connectivity = nbt.getByte("connectivity");
		this.covers = nbt.getByte("covers");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setByte("connectivity", this.connectivity);
		nbt.setByte("covers", this.covers);
		return nbt;
	}

	@Override
	protected void onUnloaded()
	{
		super.onUnloaded();
	}

	@Override
	public void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
	}

	@Override
	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		if (this.world.isRemote)
		{
			this.updateRenderState();
		}

		super.onPlaced(stack, placer, facing);
	}

	protected abstract void updateConnectivity();

	@Override
	public Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state)
	{
		state = super.getExtendedState(state);
		TileEntityPipe.PipeRenderState pipeRenderState = this.renderState;
		if (pipeRenderState != null)
		{
			state = state.withProperties(renderStateProperty, pipeRenderState);
		}

		return state;
	}

	@Override
	protected SoundType getBlockSound(Entity entity)
	{
		return SoundType.METAL;
	}

	@Override
	protected boolean isNormalCube()
	{
		return false;
	}

	@Override
	protected boolean isSideSolid(EnumFacing side)
	{
		return false;
	}

	@Override
	protected boolean doesSideBlockRendering(EnumFacing side)
	{
		return false;
	}

	@Override
	protected int getLightOpacity()
	{
		return 0;
	}

	@Override
	protected boolean clientNeedsExtraModelInfo()
	{
		return true;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		this.updateRenderState();
		this.rerender();
		super.onNetworkUpdate(field);
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("connectivity");
		ret.add("covers");
		return ret;
	}

	@Override
	protected List<ItemStack> getAuxDrops(int fortune)
	{
		List<ItemStack> ret = new ArrayList<>();

		for (EnumFacing facing : EnumFacing.VALUES)
		{
			if (this.coversComponent.hasCover(facing))
			{
				ret.add(this.coversComponent.removeCover(facing));
			}
		}

		return ret;
	}

	protected abstract void updateRenderState();

	public static class PipeRenderState
	{
		public final PipeType type;
		public final PipeSize size;
		public final int connectivity;
		public final int covers;
		public final int facing;

		public PipeRenderState(PipeType type, PipeSize size, int connectivity, int covers, int facing)
		{
			this.type = type;
			this.size = size;
			this.connectivity = connectivity;
			this.covers = covers;
			this.facing = facing;
		}

		@Override
		public int hashCode()
		{
			int ret = this.type.hashCode();
			ret = ret * 31 + this.size.hashCode();
			ret = ret * 31 + this.connectivity;
			ret = ret * 31 + this.covers;
			return ret * 31 + this.facing;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
			{
				return true;
			}

			if (!(obj instanceof TileEntityPipe.PipeRenderState))
			{
				return false;
			}

			TileEntityPipe.PipeRenderState o = (TileEntityPipe.PipeRenderState) obj;
			return o.type == this.type && o.size == this.size && o.connectivity == this.connectivity && o.covers == this.covers && o.facing == this.facing;
		}

		@Override
		public String toString()
		{
			return "PipeState<" + this.type + ", " + this.size + ", " + this.connectivity + ", " + this.covers + ", " + this.facing + '>';
		}
	}
}

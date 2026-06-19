package ic2.core.block.reactor.tileentity;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Arrays;
import java.util.Collections;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityReactorChamberElectric extends Ic2TileEntity implements Container, IReactorChamber, IEnergyEmitter
{
	public final Redstone redstone = this.addComponent(new Redstone(this));
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	private TileEntityNuclearReactorElectric reactor;
	private long lastReactorUpdate;

	public TileEntityReactorChamberElectric(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.REACTOR_CHAMBER, pos, state);
		this.fluids.addUnmanagedTankHook(() ->
		{
			TileEntityNuclearReactorElectric reactor = TileEntityReactorChamberElectric.this.getReactor();
			return reactor == null ? Collections.emptySet() : Arrays.asList(reactor.inputTank, reactor.outputTank);
		});
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateRedstoneLink();
	}

	private void updateRedstoneLink()
	{
		if (!this.getLevel().isClientSide)
		{
			TileEntityNuclearReactorElectric reactor = this.getReactor();
			if (reactor != null)
			{
				this.redstone.linkTo(reactor.redstone);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		if (reactor != null)
		{
			TileEntityNuclearReactorElectric.showHeatEffects(this.getLevel(), this.worldPosition, reactor.getHeat());
		}
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		if (reactor != null)
		{
			Level world = this.getLevel();
			return reactor.getBlockType().use(reactor.getBlockState(), world, reactor.getBlockPos(), player, hand, new BlockHitResult(hit, side, reactor.getBlockPos(), false));
		} else
		{
			return InteractionResult.PASS;
		}
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		this.lastReactorUpdate = 0L;
		if (this.getReactor() == null)
		{
			this.destroyChamber(true);
		}
	}

	public void destroyChamber(boolean wrench)
	{
		Level world = this.getLevel();
		world.removeBlock(this.worldPosition, false);

		for (ItemStack drop : this.getSelfDrops(wrench))
		{
			StackUtil.dropAsEntity(world, this.worldPosition, drop);
		}
	}

	public int getContainerSize()
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		return reactor != null ? reactor.getContainerSize() : 0;
	}

	public boolean isEmpty()
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		return reactor != null ? reactor.isEmpty() : true;
	}

	public ItemStack getItem(int index)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		return reactor != null ? reactor.getItem(index) : null;
	}

	public ItemStack removeItem(int index, int count)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		return reactor != null ? reactor.removeItem(index, count) : null;
	}

	public ItemStack removeItemNoUpdate(int index)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		return reactor != null ? reactor.removeItemNoUpdate(index) : null;
	}

	public void setItem(int index, ItemStack stack)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		if (reactor != null)
		{
			reactor.setItem(index, stack);
		}
	}

	public int getMaxStackSize()
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		return reactor != null ? reactor.getMaxStackSize() : 0;
	}

	public boolean stillValid(Player player)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		return reactor != null && reactor.stillValid(player);
	}

	public void startOpen(Player player)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		if (reactor != null)
		{
			reactor.startOpen(player);
		}
	}

	public void stopOpen(Player player)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		if (reactor != null)
		{
			reactor.stopOpen(player);
		}
	}

	public boolean canPlaceItem(int index, ItemStack stack)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		return reactor != null && reactor.canPlaceItem(index, stack);
	}

	public void clearContent()
	{
		TileEntityNuclearReactorElectric reactor = this.getReactor();
		if (reactor != null)
		{
			reactor.clearContent();
		}
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction side)
	{
		return true;
	}

	public TileEntityNuclearReactorElectric getReactorInstance()
	{
		return this.reactor;
	}

	@Override
	public boolean isWall()
	{
		return false;
	}

	private TileEntityNuclearReactorElectric getReactor()
	{
		long time = this.getLevel().getGameTime();
		if (time != this.lastReactorUpdate)
		{
			this.updateReactor();
			this.lastReactorUpdate = time;
		} else if (this.reactor != null && this.reactor.isRemoved())
		{
			this.reactor = null;
		}

		return this.reactor;
	}

	private void updateReactor()
	{
		Level world = this.getLevel();
		this.reactor = null;

		for (Direction facing : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(facing));
			if (te instanceof TileEntityNuclearReactorElectric)
			{
				this.reactor = (TileEntityNuclearReactorElectric) te;
				break;
			}
		}
	}
}

package ic2.core.block.reactor.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@NotClassic
public class TileEntityReactorAccessHatch extends TileEntityReactorVessel implements Container
{
	public TileEntityReactorAccessHatch(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.REACTOR_ACCESS_HATCH, pos, state);
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		return reactor != null ? reactor.onActivated(player, hand, side, hit) : InteractionResult.PASS;
	}

	public int getContainerSize()
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		return reactor != null ? reactor.getContainerSize() : 0;
	}

	public boolean isEmpty()
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		return reactor != null ? reactor.isEmpty() : true;
	}

	public ItemStack getItem(int index)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		return reactor != null ? reactor.getItem(index) : null;
	}

	public ItemStack removeItem(int index, int count)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		return reactor != null ? reactor.removeItem(index, count) : null;
	}

	public ItemStack removeItemNoUpdate(int index)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		return reactor != null ? reactor.removeItemNoUpdate(index) : null;
	}

	public void setItem(int index, ItemStack stack)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		if (reactor != null)
		{
			reactor.setItem(index, stack);
		}
	}

	public int getMaxStackSize()
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		return reactor != null ? reactor.getMaxStackSize() : 0;
	}

	public boolean stillValid(Player player)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		return reactor != null ? reactor.stillValid(player) : false;
	}

	public void startOpen(Player player)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		if (reactor != null)
		{
			reactor.startOpen(player);
		}
	}

	public void stopOpen(Player player)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		if (reactor != null)
		{
			reactor.stopOpen(player);
		}
	}

	public boolean canPlaceItem(int index, ItemStack stack)
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		return reactor != null ? reactor.canPlaceItem(index, stack) : false;
	}

	public void clearContent()
	{
		TileEntityNuclearReactorElectric reactor = this.getReactorInstance();
		if (reactor != null)
		{
			reactor.clearContent();
		}
	}
}

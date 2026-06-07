package ic2.core.block.reactor.tileentity;

import ic2.core.block.comp.Redstone;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityReactorRedstonePort extends TileEntityReactorVessel
{
	public final Redstone redstone = this.addComponent(new Redstone(this));

	public TileEntityReactorRedstonePort(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.REACTOR_REDSTONE_PORT, pos, state);
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
			TileEntityNuclearReactorElectric reactor = this.lookup.getReactor();
			if (reactor != null)
			{
				this.redstone.linkTo(reactor.redstone);
			}
		}
	}
}

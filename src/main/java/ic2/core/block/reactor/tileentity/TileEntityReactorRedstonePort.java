package ic2.core.block.reactor.tileentity;

import ic2.core.block.comp.Redstone;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityReactorRedstonePort extends TileEntityReactorVessel
{
	public final Redstone redstone = this.addComponent(new Redstone(this));

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateRedstoneLink();
	}

	private void updateRedstoneLink()
	{
		if (!this.getWorld().isRemote)
		{
			TileEntityNuclearReactorElectric reactor = this.lookup.getReactor();
			if (reactor != null)
			{
				this.redstone.linkTo(reactor.redstone);
			}
		}
	}
}

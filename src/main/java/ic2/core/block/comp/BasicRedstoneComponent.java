package ic2.core.block.comp;

import ic2.core.block.tileentity.Ic2TileEntity;

import java.util.function.IntSupplier;

public abstract class BasicRedstoneComponent extends TileEntityComponent
{
	private int level;
	private IntSupplier update;

	public BasicRedstoneComponent(Ic2TileEntity parent)
	{
		super(parent);
	}

	public int getLevel()
	{
		return this.level;
	}

	public void setLevel(int newLevel)
	{
		if (newLevel != this.level)
		{
			this.level = newLevel;
			this.onChange();
		}
	}

	public abstract void onChange();

	@Override
	public boolean enableWorldTick()
	{
		return this.update != null;
	}

	@Override
	public void onWorldTick()
	{
		assert this.update != null;
		this.setLevel(this.update.getAsInt());
	}

	public void setUpdate(IntSupplier update)
	{
		this.update = update;
	}
}

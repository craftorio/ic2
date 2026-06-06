package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;

@NotClassic
public enum UpdateKitType implements IIdProvider
{
	mfsu(0);

	private final int id;

	UpdateKitType(int id)
	{
		this.id = id;
	}

	@Override
	public String getName()
	{
		return this.name();
	}

	@Override
	public int getId()
	{
		return this.id;
	}
}

package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;

@NotClassic
public enum OreResourceType implements IIdProvider
{
	copper(0),
	gold(1),
	iron(2),
	lead(3),
	silver(4),
	tin(5),
	uranium(6);

	private final int id;

	OreResourceType(int id)
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

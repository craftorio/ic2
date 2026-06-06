package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;
import ic2.core.profile.NotExperimental;

public enum IngotResourceType implements IIdProvider
{
	alloy(0),
	bronze(1),
	copper(2),
	@NotClassic
	lead(3),
	@NotClassic
	silver(4),
	@NotClassic
	steel(5),
	tin(6),
	@NotExperimental
	refined_iron(7),
	@NotExperimental
	uranium(8);

	private final int id;

	IngotResourceType(int id)
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

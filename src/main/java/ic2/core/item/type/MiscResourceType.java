package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;

public enum MiscResourceType implements IIdProvider
{
	@NotClassic
	ashes(0),
	iridium_ore(1),
	@NotClassic
	iridium_shard(2),
	matter(3),
	resin(4),
	@NotClassic
	slag(5),
	@NotClassic
	iodine(6),
	water_sheet(7),
	lava_sheet(8);

	private final int id;

	MiscResourceType(int id)
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

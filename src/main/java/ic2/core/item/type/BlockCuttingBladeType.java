package ic2.core.item.type;

import ic2.api.block.IIdProvider;
import ic2.core.profile.NotClassic;

@NotClassic
public enum BlockCuttingBladeType implements IIdProvider
{
	iron(0),
	steel(1),
	diamond(2);

	private final int id;

	BlockCuttingBladeType(int id)
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

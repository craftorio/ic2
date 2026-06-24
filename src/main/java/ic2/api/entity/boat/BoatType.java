package ic2.api.entity.boat;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.world.item.Item;

public class BoatType
{
	private static final Set<BoatType> VALUES = new ObjectArraySet<>();
	private final String name;
	private final Item baseItem;

	protected BoatType(Item baseItem, String name)
	{
		this.name = name;
		this.baseItem = baseItem;
	}

	public static BoatType register(Item baseItem, String name)
	{
		BoatType type = new BoatType(baseItem, name);
		VALUES.add(type);
		return type;
	}

	public static Stream<BoatType> stream()
	{
		return VALUES.stream();
	}

	public String getName()
	{
		return this.name;
	}

	public Item getBaseItem()
	{
		return this.baseItem;
	}
}

package ic2.core.block.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Map.Entry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStateUtil
{
	public static String getVariantString(BlockState state)
	{
		ImmutableMap<Property<?>, Comparable<?>> properties = state.getValues();
		if (properties.isEmpty())
		{
			return "normal";
		}

		StringBuilder ret = new StringBuilder();
		UnmodifiableIterator var3 = properties.entrySet().iterator();

		while (var3.hasNext())
		{
			Entry<Property<?>, Comparable<?>> entry = (Entry<Property<?>, Comparable<?>>) var3.next();
			Property property = entry.getKey();
			if (ret.length() > 0)
			{
				ret.append(',');
			}

			ret.append(property.getName());
			ret.append('=');
			ret.append(property.getName(entry.getValue()));
		}

		return ret.toString();
	}

	public static BlockState getState(Block block, String variant)
	{
		BlockState ret = block.defaultBlockState();
		if (!variant.isEmpty() && !variant.equals("normal"))
		{
			int pos = 0;

			while (pos < variant.length())
			{
				int nextPos = variant.indexOf(44, pos);
				if (nextPos == -1)
				{
					nextPos = variant.length();
				}

				int sepPos = variant.indexOf(61, pos);
				if (sepPos == -1 || sepPos >= nextPos)
				{
					return null;
				}

				String name = variant.substring(pos, sepPos);
				String value = variant.substring(sepPos + 1, nextPos);
				ret = applyProperty(ret, name, value);
				pos = nextPos + 1;
			}

			return ret;
		} else
		{
			return ret;
		}
	}

	private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, String name, String value)
	{
		Property<T> property = null;

		for (Property<?> cProperty : state.getProperties())
		{
			if (cProperty.getName().equals(name))
			{
				property = (Property<T>) cProperty;
				break;
			}
		}

		if (property == null)
		{
			return state;
		}

		for (T cValue : property.getPossibleValues())
		{
			if (value.equals(property.getName(cValue)))
			{
				return (BlockState) state.setValue(property, cValue);
			}
		}

		return state;
	}
}

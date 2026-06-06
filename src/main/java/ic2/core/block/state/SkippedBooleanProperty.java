package ic2.core.block.state;

import net.minecraft.block.properties.PropertyBool;

public class SkippedBooleanProperty extends PropertyBool implements ISkippableProperty
{
	public SkippedBooleanProperty(String name)
	{
		super(name);
	}
}

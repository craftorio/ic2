package ic2.core.ref;

import ic2.core.IC2;
import net.minecraft.world.level.block.state.properties.WoodType;

public class Ic2SignType extends WoodType
{
	public static final WoodType RUBBER = IC2.envProxy.registerSignType("rubber");

	protected Ic2SignType(String name)
	{
		super(name);
	}
}

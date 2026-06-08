package ic2.core.ref;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.Material.Builder;

public class IC2Material
{
	public static final Material MACHINE = new Builder(MaterialColor.COLOR_LIGHT_GRAY).build();
	public static final Material PIPE = new Builder(MaterialColor.COLOR_LIGHT_GRAY).build();
	public static final Material CABLE = new Builder(MaterialColor.COLOR_BLACK).build();
	public static final Material STEAM = new Builder(MaterialColor.COLOR_LIGHT_GRAY).noCollider().nonSolid().replaceable().liquid().build();
}

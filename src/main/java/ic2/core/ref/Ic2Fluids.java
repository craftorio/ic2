package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.fluid.EnvFluidHandler;
import ic2.core.fluid.FluidHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Material;

public final class Ic2Fluids
{
	public static final EnvFluidHandler.FluidRefs UU_MATTER = create(
		"uu_matter", Material.WATER, 3000, 3000, 0, 300, false, "uu_matter", "uu_matter", -12909261
	);
	public static final EnvFluidHandler.FluidRefs CONSTRUCTION_FOAM = create(
		"construction_foam", Material.WATER, 10000, 50000, 0, 300, false, "fluid_3", "hot_coolant", -199089630
	);
	public static final EnvFluidHandler.FluidRefs COOLANT = create("coolant", Material.WATER, 1000, 3000, 0, 300, false, "coolant", "coolant", -15443350);
	public static final EnvFluidHandler.FluidRefs CREOSOTE = create("creosote", Material.WATER, 10000, 50000, 0, 300, false, "fluid", "fluid", -331005940);
	public static final EnvFluidHandler.FluidRefs HOT_COOLANT = create(
		"hot_coolant", Material.WATER, 1000, 3000, 0, 1200, false, "hot_coolant", "hot_coolant", -4904908
	);
	public static final EnvFluidHandler.FluidRefs PAHOEHOE_LAVA = create(
		"pahoehoe_lava", Material.WATER, 50000, 250000, 10, 1200, false, "pahoehoe_lava", null, -8686484
	);
	public static final EnvFluidHandler.FluidRefs BIOMASS = create("biomass", Material.WATER, 1000, 3000, 0, 300, false, "fluid", "fluid", -1237485016);
	public static final EnvFluidHandler.FluidRefs BIOGAS = create("biogas", Material.WATER, 1000, 3000, 0, 300, true, "fluid_3", null, -188435879);
	public static final EnvFluidHandler.FluidRefs DISTILLED_WATER = create(
		"distilled_water", Material.WATER, 1000, 1000, 0, 300, false, "fluid_water", "fluid_water", -632331785
	);
	public static final EnvFluidHandler.FluidRefs SUPERHEATED_STEAM = create(
		"superheated_steam", IC2Material.STEAM, -3000, 100, 0, 600, true, "fluid_3", null, -185797131
	);
	public static final EnvFluidHandler.FluidRefs STEAM = create("steam", IC2Material.STEAM, -800, 300, 0, 420, true, "fluid_3", null, -186852132);
	public static final EnvFluidHandler.FluidRefs HOT_WATER = create(
		"hot_water", Material.WATER, 1000, 1000, 0, 350, false, "fluid_water", "fluid_water", -632884747
	);
	public static final EnvFluidHandler.FluidRefs WEED_EX = create("weed_ex", Material.WATER, 1000, 1000, 0, 300, false, "weed_ex", null, -16298220);
	public static final EnvFluidHandler.FluidRefs AIR = create("air", IC2Material.STEAM, 0, 500, 0, 300, true, "fluid_2", null, 1610481149);
	public static final EnvFluidHandler.FluidRefs HYDROGEN = create("hydrogen", IC2Material.STEAM, 0, 500, 0, 300, true, "fluid_2", null, -2034379563);
	public static final EnvFluidHandler.FluidRefs OXYGEN = create("oxygen", IC2Material.STEAM, 0, 500, 0, 300, true, "fluid_2", null, -2034581547);
	public static final EnvFluidHandler.FluidRefs HEAVY_WATER = create("heavy_water", Material.WATER, 1000, 1000, 0, 300, false, "fluid", "fluid", -45191196);

	public static void init()
	{
	}

	private static EnvFluidHandler.FluidRefs create(
		String name,
		Material material,
		int density,
		int viscosity,
		int luminosity,
		int temperature,
		boolean isGaseous,
		String stillSprite,
		String flowingSprite,
		int color
	)
	{
		assert color >>> 24 >= 8;
		ResourceLocation stillSpriteId = IC2.getIdentifier("blocks/fluid/" + stillSprite + "_still");
		ResourceLocation flowingSpriteId = flowingSprite != null ? IC2.getIdentifier("blocks/fluid/" + flowingSprite + "_flow") : null;
		return FluidHandler.createFluid(
			IC2.getIdentifier(name), material, density, viscosity, luminosity, temperature, isGaseous, stillSpriteId, flowingSpriteId, color
		);
	}
}

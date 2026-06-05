package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;
import ic2.core.profile.NotExperimental;

public enum CraftingItemType implements IIdProvider {
   rubber(0),
   circuit(1),
   advanced_circuit(2),
   alloy(3),
   iridium(4),
   @NotClassic
   coil(5),
   @NotClassic
   electric_motor(6),
   @NotClassic
   heat_conductor(7),
   @NotClassic
   copper_boiler(8),
   fuel_rod(9),
   tin_can(10),
   @NotClassic
   small_power_unit(11),
   @NotClassic
   power_unit(12),
   carbon_fibre(13),
   carbon_mesh(14),
   carbon_plate(15),
   coal_ball(16),
   coal_block(17),
   coal_chunk(18),
   industrial_diamond(19),
   plant_ball(20),
   @NotExperimental
   compressed_plants(39),
   bio_chaff(21),
   @NotExperimental
   compressed_hydrated_coal(22),
   scrap(23),
   scrap_box(24),
   @NotClassic
   cf_powder(25),
   @NotExperimental
   pellet(26),
   @NotClassic
   raw_crystal_memory(27),
   @NotClassic
   iron_shaft(29),
   @NotClassic
   steel_shaft(30),
   @NotClassic
   wood_rotor_blade(31),
   @NotClassic
   iron_rotor_blade(32),
   @NotClassic
   steel_rotor_blade(33),
   @NotClassic
   carbon_rotor_blade(34),
   @NotClassic
   steam_turbine_blade(35),
   @NotClassic
   steam_turbine(36),
   @NotClassic
   jetpack_attachment_plate(37),
   coin(38),
   @NotExperimental
   empty_fuel_can(40),
   @NotClassic
   bronze_rotor_blade(41),
   @NotClassic
   bronze_shaft(42);

   private final int id;

   CraftingItemType(int id) {
      this.id = id;
   }

   @Override
   public String getName() {
      return this.name();
   }

   @Override
   public int getId() {
      return this.id;
   }
}

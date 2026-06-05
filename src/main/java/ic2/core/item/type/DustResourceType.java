package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.Both;
import ic2.core.profile.NotClassic;

@NotClassic
public enum DustResourceType implements IIdProvider {
   @Both
   bronze(0),
   @Both
   clay(1),
   @Both
   coal(2),
   @Both
   coal_fuel(3),
   @Both
   copper(4),
   diamond(5),
   energium(6),
   @Both
   gold(7),
   @Both
   iron(8),
   lapis(9),
   lead(10),
   lithium(11),
   obsidian(12),
   silicon_dioxide(13),
   @Both
   silver(14),
   stone(15),
   sulfur(16),
   @Both
   tin(17),
   small_bronze(18),
   small_copper(19),
   small_gold(20),
   @Both
   small_iron(21),
   small_lapis(22),
   small_lead(23),
   small_lithium(24),
   small_obsidian(25),
   small_silver(26),
   small_sulfur(27),
   small_tin(28),
   tin_hydrated(29),
   netherrack(30),
   ender_pearl(31),
   ender_eye(32),
   milk(33),
   emerald(34),
   small_emerald(35),
   small_diamond(36);

   private final int id;

   DustResourceType(int id) {
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

package ic2.core.item.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.Both;
import ic2.core.profile.NotClassic;
import ic2.core.profile.NotExperimental;

@NotClassic
public enum NuclearResourceType implements IIdProvider, IRadioactiveItemType {
   @Both
   uranium(0, 60, 100),
   uranium_235(1, 150, 100),
   uranium_238(2, 10, 90),
   plutonium(3, 150, 100),
   mox(4, 300, 100),
   small_uranium_235(5, 150, 100),
   small_uranium_238(6, 10, 90),
   small_plutonium(7, 150, 100),
   uranium_pellet(8, 60, 100),
   mox_pellet(9, 300, 100),
   rtg_pellet(10, 2, 90),
   depleted_uranium(11, 10, 100),
   depleted_dual_uranium(12, 10, 100),
   depleted_quad_uranium(13, 10, 100),
   depleted_mox(14, 10, 100),
   depleted_dual_mox(15, 10, 100),
   depleted_quad_mox(16, 10, 100),
   @NotExperimental
   near_depleted_uranium(17, 15, 100),
   @NotExperimental
   re_enriched_uranium(18, 30, 100);

   private final int id;
   private final int radLen;
   private final int radAmplifier;

   NuclearResourceType(int id, int radLen, int radAmplifier) {
      this.id = id;
      this.radLen = radLen;
      this.radAmplifier = radAmplifier;
   }

   @Override
   public String getName() {
      return this.name();
   }

   @Override
   public int getId() {
      return this.id;
   }

   @Override
   public int getRadiationDuration() {
      return this.radLen;
   }

   @Override
   public int getRadiationAmplifier() {
      return this.radAmplifier;
   }
}

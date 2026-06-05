package ic2.core.block.type;

import ic2.core.block.state.IIdProvider;
import ic2.core.profile.NotClassic;
import net.minecraft.block.SoundType;

public enum ResourceBlock implements IIdProvider, IExtBlockType, IBlockSound {
   @NotClassic
   basalt(20.0F, 45.0F, false),
   copper_ore(3.0F, 5.0F, false),
   @NotClassic
   lead_ore(2.0F, 4.0F, false),
   tin_ore(3.0F, 5.0F, false),
   uranium_ore(4.0F, 6.0F, false),
   bronze_block(5.0F, 10.0F, true),
   copper_block(4.0F, 10.0F, true),
   @NotClassic
   lead_block(4.0F, 10.0F, true),
   @NotClassic
   steel_block(8.0F, 10.0F, true),
   tin_block(4.0F, 10.0F, true),
   uranium_block(6.0F, 10.0F, true),
   reinforced_stone(80.0F, 180.0F, false),
   machine(5.0F, 10.0F, true),
   advanced_machine(8.0F, 10.0F, true),
   @NotClassic
   reactor_vessel(40.0F, 90.0F, false),
   @NotClassic
   silver_block(4.0F, 10.0F, true);

   private final float hardness;
   private final float explosionResistance;
   private final boolean metal;

   ResourceBlock(float hardness, float explosionResistance, boolean metal) {
      this.hardness = hardness;
      this.explosionResistance = explosionResistance;
      this.metal = metal;
   }

   @Override
   public String getName() {
      return this.name();
   }

   @Override
   public int getId() {
      return this.ordinal();
   }

   @Override
   public float getHardness() {
      return this.hardness;
   }

   @Override
   public float getExplosionResistance() {
      return this.explosionResistance;
   }

   @Override
   public SoundType getSound() {
      return this.metal ? SoundType.METAL : SoundType.STONE;
   }
}

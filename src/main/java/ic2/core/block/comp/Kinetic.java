package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;
import java.util.Set;
import net.minecraft.util.EnumFacing;

public class Kinetic extends TileEntityComponent {
  private Set<EnumFacing> sinkDirections;
  
  private Set<EnumFacing> sourceDirections;
  
  public Kinetic(TileEntityBlock parent, double capacity, Set<EnumFacing> sinkDirections, Set<EnumFacing> sourceDirections, int sinkTier, int sourceTier, boolean fullEnergy) {
    super(parent);
  }
}

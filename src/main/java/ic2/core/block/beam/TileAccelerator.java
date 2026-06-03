package ic2.core.block.beam;

import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public class TileAccelerator extends TileEntityElectricMachine {
  public TileAccelerator() {
    super(5000, 2);
  }
  
  protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
    return FACING_AABBs.get(getFacing().func_176740_k());
  }
  
  protected int getLightOpacity() {
    return 0;
  }
  
  private static Map<EnumFacing.Axis, List<AxisAlignedBB>> makeAABBMap() {
    Map<EnumFacing.Axis, List<AxisAlignedBB>> ret = new EnumMap<>(EnumFacing.Axis.class);
    ret.put(EnumFacing.Axis.X, Collections.singletonList(new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 0.75D)));
    ret.put(EnumFacing.Axis.Y, Collections.singletonList(new AxisAlignedBB(0.0D, 0.25D, 0.0D, 1.0D, 0.75D, 1.0D)));
    ret.put(EnumFacing.Axis.Z, Collections.singletonList(new AxisAlignedBB(0.25D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D)));
    return ret;
  }
  
  private static final Map<EnumFacing.Axis, List<AxisAlignedBB>> FACING_AABBs = makeAABBMap();
}

package ic2.core.block.beam;

import ic2.core.block.machine.tileentity.TileEntityElectricMachine;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;

public class TileAccelerator extends TileEntityElectricMachine
{
	private static final Map<Axis, List<AxisAlignedBB>> FACING_AABBs = makeAABBMap();

	public TileAccelerator()
	{
		super(5000, 2);
	}

	@Override
	protected List<AxisAlignedBB> getAabbs(boolean forCollision)
	{
		return FACING_AABBs.get(this.getFacing().getAxis());
	}

	@Override
	protected int getLightOpacity()
	{
		return 0;
	}

	private static Map<Axis, List<AxisAlignedBB>> makeAABBMap()
	{
		Map<Axis, List<AxisAlignedBB>> ret = new EnumMap<>(Axis.class);
		ret.put(Axis.X, Collections.singletonList(new AxisAlignedBB(0.0, 0.0, 0.25, 1.0, 1.0, 0.75)));
		ret.put(Axis.Y, Collections.singletonList(new AxisAlignedBB(0.0, 0.25, 0.0, 1.0, 0.75, 1.0)));
		ret.put(Axis.Z, Collections.singletonList(new AxisAlignedBB(0.25, 0.0, 0.0, 0.75, 1.0, 1.0)));
		return ret;
	}
}
